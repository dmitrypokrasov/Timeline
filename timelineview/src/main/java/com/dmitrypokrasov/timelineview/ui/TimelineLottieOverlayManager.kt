package com.dmitrypokrasov.timelineview.ui

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.view.View
import android.view.ViewTreeObserver
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.LottieListener
import com.airbnb.lottie.LottieTask
import com.dmitrypokrasov.timelineview.model.TimelineLottieSpec
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.model.identity
import kotlin.math.roundToInt

/** Owns one drawable per overlay occurrence and never loads resources during drawing. */
internal class TimelineLottieOverlayManager(private val ownerView: View) {
    internal data class Key(val step: String, val progress: Boolean = false) {
        constructor(index: Int, progress: Boolean = false) : this("index:$index", progress)
    }

    private class Entry(val spec: TimelineLottieSpec) {
        val drawable = LottieDrawable()
        var task: LottieTask<LottieComposition>? = null
        var success: LottieListener<LottieComposition>? = null
        var failure: LottieListener<Throwable>? = null
        var started = false
        var visible = false
    }

    private val entries = mutableMapOf<Key, Entry>()
    private var active = false
    private var ownerVisible = true
    private val ownerBounds = Rect()
    private var observedTree: ViewTreeObserver? = null
    private val preDrawListener =
        ViewTreeObserver.OnPreDrawListener {
            val visible = ownerView.getGlobalVisibleRect(ownerBounds)
            if (ownerVisible != visible) {
                ownerVisible = visible
                entries.values.forEach(::updatePlayback)
            }
            true
        }
    internal val entryCount: Int get() = entries.size
    internal val runningAnimationCount: Int get() = entries.values.count { it.drawable.isAnimating }

    private val callback =
        object : Drawable.Callback {
            override fun invalidateDrawable(who: Drawable) = ownerView.invalidate()

            override fun scheduleDrawable(
                who: Drawable,
                what: Runnable,
                `when`: Long,
            ) {
                ownerView.postDelayed(what, (`when` - SystemClock.uptimeMillis()).coerceAtLeast(0))
            }

            override fun unscheduleDrawable(
                who: Drawable,
                what: Runnable,
            ) {
                ownerView.removeCallbacks(what)
            }
        }

    fun submit(steps: List<TimelineStepData>) {
        val desired = mutableMapOf<Key, TimelineLottieSpec>()
        steps.forEachIndexed { index, step -> step.badgeAnimation?.let { desired[Key(step.identity(index))] = it } }
        val progressIndex = steps.indexOfFirst { it.progress < 100 }
        steps.getOrNull(progressIndex)?.progressAnimation?.let { desired[Key(steps[progressIndex].identity(progressIndex), true)] = it }
        entries.keys.toList().forEach { key ->
            if (entries[key]?.spec != desired[key]) entries.remove(key)?.let(::dispose)
        }
        desired.forEach { (key, spec) ->
            if (key !in entries) {
                val entry = Entry(spec)
                entries[key] = entry
                load(key, entry)
            }
        }
    }

    private fun load(
        key: Key,
        entry: Entry,
    ) {
        entry.drawable.callback = callback
        entry.drawable.repeatCount = if (entry.spec.repeat) LottieDrawable.INFINITE else 0
        val success =
            LottieListener<LottieComposition> { composition ->
                if (entries[key] === entry) {
                    entry.drawable.composition = composition
                    updatePlayback(entry)
                    ownerView.invalidate()
                }
            }
        // An invalid optional overlay must not crash the host application's rendering.
        val failure = LottieListener<Throwable> { ownerView.invalidate() }
        entry.success = success
        entry.failure = failure
        entry.task =
            LottieCompositionFactory.fromRawRes(ownerView.context, entry.spec.rawRes)
                .addListener(success).addFailureListener(failure)
    }

    fun setActive(active: Boolean) {
        this.active = active
        if (active && observedTree == null) {
            observedTree = ownerView.viewTreeObserver.also { it.addOnPreDrawListener(preDrawListener) }
        } else if (!active) {
            observedTree?.let { tree ->
                (if (tree.isAlive) tree else ownerView.viewTreeObserver).removeOnPreDrawListener(preDrawListener)
            }
            observedTree = null
        }
        entries.values.forEach(::updatePlayback)
    }

    fun beginFrame() {
        entries.values.forEach { it.visible = false }
    }

    fun endFrame() {
        entries.values.forEach { if (!it.visible) updatePlayback(it) }
    }

    private fun updatePlayback(entry: Entry) {
        val drawable = entry.drawable
        val visible = ownerVisible && entry.visible
        if (!active || !visible || !entry.spec.autoPlay) {
            drawable.pauseAnimation()
        } else if (drawable.composition != null && !drawable.isAnimating) {
            if (!entry.started) {
                entry.started = true
                drawable.playAnimation()
            } else if (entry.spec.repeat || drawable.progress < 1f) {
                drawable.resumeAnimation()
            }
        }
    }

    fun draw(
        canvas: Canvas,
        key: Key,
        left: Float,
        top: Float,
        size: Float,
    ) {
        val entry = entries[key] ?: return
        if (size <= 0f) return
        val scaledSize = size * entry.spec.scale
        val inset = (size - scaledSize) / 2f
        val clip = canvas.clipBounds
        entry.visible = left + inset < clip.right && top + inset < clip.bottom &&
            left + inset + scaledSize > clip.left && top + inset + scaledSize > clip.top
        updatePlayback(entry)
        if (!entry.visible || entry.drawable.composition == null) return
        if (!entry.spec.repeat && entry.started && entry.drawable.progress >= 1f) return
        entry.drawable.setBounds(
            (left + inset).roundToInt(),
            (top + inset).roundToInt(),
            (left + inset + scaledSize).roundToInt(),
            (top + inset + scaledSize).roundToInt(),
        )
        entry.drawable.draw(canvas)
    }

    fun clear() {
        setActive(false)
        entries.values.forEach(::dispose)
        entries.clear()
    }

    private fun dispose(entry: Entry) {
        entry.success?.let { entry.task?.removeListener(it) }
        entry.failure?.let { entry.task?.removeFailureListener(it) }
        entry.drawable.cancelAnimation()
        entry.drawable.callback = null
    }
}
