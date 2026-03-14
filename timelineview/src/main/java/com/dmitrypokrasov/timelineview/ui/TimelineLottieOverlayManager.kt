package com.dmitrypokrasov.timelineview.ui

import android.content.Context
import android.graphics.Canvas
import android.util.Log
import android.view.View
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.dmitrypokrasov.timelineview.model.TimelineLottieSpec
import kotlin.math.roundToInt

internal class TimelineLottieOverlayManager(
    private val ownerView: View,
) {
    companion object {
        private const val TAG = "TimelineLottieOverlay"
    }

    private data class OverlayEntry(
        val drawable: LottieDrawable?,
        var autoPlayConsumed: Boolean = false,
    )

    private val drawableCache = mutableMapOf<TimelineLottieSpec, OverlayEntry>()

    fun draw(
        canvas: Canvas,
        context: Context,
        spec: TimelineLottieSpec?,
        left: Float,
        top: Float,
        size: Float,
    ) {
        if (spec == null) return

        val entry =
            drawableCache.getOrPut(spec) {
                OverlayEntry(createDrawable(context, spec))
            }
        val drawable = entry.drawable ?: return

        val scaledSize = size * spec.scale
        val inset = (size - scaledSize) / 2f
        val drawLeft = left + inset
        val drawTop = top + inset
        drawable.setBounds(
            drawLeft.roundToInt(),
            drawTop.roundToInt(),
            (drawLeft + scaledSize).roundToInt(),
            (drawTop + scaledSize).roundToInt(),
        )

        if (spec.autoPlay) {
            if (spec.repeat) {
                if (!drawable.isAnimating) {
                    drawable.playAnimation()
                }
            } else {
                if (!entry.autoPlayConsumed) {
                    drawable.progress = 0f
                    drawable.playAnimation()
                    entry.autoPlayConsumed = true
                } else if (!drawable.isAnimating && drawable.progress >= 1f) {
                    return
                }
            }
        } else if (drawable.isAnimating) {
            drawable.pauseAnimation()
        }

        drawable.draw(canvas)
    }

    fun clear() {
        drawableCache.values.forEach { entry ->
            entry.drawable?.cancelAnimation()
            entry.drawable?.callback = null
        }
        drawableCache.clear()
    }

    private fun createDrawable(
        context: Context,
        spec: TimelineLottieSpec,
    ): LottieDrawable? {
        val composition =
            LottieCompositionFactory.fromRawResSync(context, spec.rawRes).value
                ?: run {
                    Log.w(TAG, "Unable to load Lottie animation: ${spec.rawRes}")
                    return null
                }

        return LottieDrawable().apply {
            callback = ownerView
            this.composition = composition
            repeatCount = if (spec.repeat) LottieDrawable.INFINITE else 0
        }
    }
}
