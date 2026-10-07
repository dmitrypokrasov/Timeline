package com.dmitrypokrasov.timelineview.ui

import android.graphics.Path
import android.graphics.RectF
import android.text.Spanned
import android.text.SpannedString
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer

/** A committed measured frame. Mutable Android objects are copied at its boundary. */
internal class TimelineFrame(
    layout: TimelineLayout,
    blocks: List<TimelineResolvedTextBlock>,
    config: TimelineMathConfig,
    val origin: Float,
    val width: Int,
    val rtl: Boolean,
    val topInset: Float,
    val height: Int,
    completed: Path,
    remaining: Path,
    targets: List<TimelineViewController.Target>,
) {
    private fun snapshot(text: CharSequence?): CharSequence? =
        when (text) {
            is Spanned -> SpannedString(text)
            null -> null
            else -> text.toString()
        }

    val layout = layout.copy(steps = layout.steps.map { it.copy(step = it.step.copy(title = snapshot(it.step.title), description = snapshot(it.step.description))) })
    val config = config.copy(steps = this.layout.steps.map { it.step })
    val blocks = blocks.toList()
    private val completed = Path(completed)
    private val remaining = Path(remaining)
    private val targets = targets.map { it.copy(bounds = RectF(it.bounds)) }

    fun targets(): List<TimelineViewController.Target> = targets.map { it.copy(bounds = RectF(it.bounds)) }

    fun restorePaths(renderer: TimelineUiRenderer) {
        renderer.getCompletedPath().set(completed)
        renderer.getRemainingPath().set(remaining)
    }
}
