package com.dmitrypokrasov.timelineview.ui

import android.graphics.Path
import android.graphics.RectF
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer

internal data class TimelineVerticalBounds(val topInset: Float, val height: Int)

/** Measures content bounds, including overlays and optional stroked paths. */
class TimelineHeightCalculator {
    fun calculateHeight(
        layout: TimelineLayout?,
        mathEngine: TimelineMathEngine,
        uiRenderer: TimelineUiRenderer,
    ): Int =
        calculateBounds(layout, mathEngine, uiRenderer, TimelineTextBlockResolver.resolve(layout, mathEngine, uiRenderer)).height

    internal fun calculateBounds(
        layout: TimelineLayout?,
        mathEngine: TimelineMathEngine,
        uiRenderer: TimelineUiRenderer,
        textBlocks: List<TimelineResolvedTextBlock>,
        paths: List<Path> = emptyList(),
    ): TimelineVerticalBounds {
        var top = 0f
        var bottom = 0f

        fun include(
            y: Float,
            height: Float,
        ) {
            top = minOf(top, y)
            bottom = maxOf(bottom, y + height)
        }
        val stroke = uiRenderer.getConfig().stroke.sizeStroke / 2f
        paths.forEach { path ->
            if (!path.isEmpty) {
                val bounds = RectF()
                path.computeBounds(bounds, true)
                include(bounds.top - stroke, bounds.height() + 2f * stroke)
            }
        }
        val sizes = mathEngine.getConfig().sizes
        layout?.steps?.forEachIndexed { index, step ->
            val scale = (step.step.badgeAnimation?.scale ?: 1f).coerceAtLeast(1f)
            include(step.iconY - sizes.sizeImageLvl * (scale - 1f) / 2f, sizes.sizeImageLvl * scale)
            val block = textBlocks[index]
            if (block.titleHeight > 0) include(block.titleTop, block.titleHeight.toFloat())
            if (block.descriptionHeight > 0) include(block.descriptionTop, block.descriptionHeight.toFloat())
        }
        layout?.progressIcon?.let { progress ->
            val scale = layout.progressStepIndex?.let { layout.steps.getOrNull(it)?.step?.progressAnimation?.scale } ?: 1f
            val scaled = sizes.sizeIconProgress * scale.coerceAtLeast(1f)
            include(progress.top - (scaled - sizes.sizeIconProgress) / 2f, scaled)
        }
        return TimelineVerticalBounds(-top, kotlin.math.ceil(bottom - top).toInt())
    }
}
