package com.dmitrypokrasov.timelineview.ui

import com.dmitrypokrasov.timelineview.math.LinearTimelineMath
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer

internal data class TimelineResolvedTextBlock(
    val titleTop: Float,
    val titleHeight: Int,
    val descriptionTop: Float,
    val descriptionHeight: Int,
)

internal object TimelineTextBlockResolver {
    private const val MIN_GAP_BETWEEN_TITLE_AND_DESCRIPTION = 4f
    private const val MIN_GAP_BETWEEN_LINEAR_STEPS = 4f

    fun resolve(
        layout: TimelineLayout?,
        mathEngine: TimelineMathEngine,
        uiRenderer: TimelineUiRenderer,
    ): List<TimelineResolvedTextBlock> {
        if (layout == null) return emptyList()

        val isLinearVertical =
            mathEngine is LinearTimelineMath &&
                mathEngine.orientation == LinearTimelineMath.Orientation.VERTICAL
        var previousBottom = Float.NEGATIVE_INFINITY

        return layout.steps.map { stepLayout ->
            val titleHeight =
                uiRenderer.measureTitleHeight(
                    stepLayout.step.title ?: "",
                    stepLayout.titleWidth,
                    stepLayout.textAlign,
                )
            val descriptionHeight =
                uiRenderer.measureDescriptionHeight(
                    stepLayout.step.description ?: "",
                    stepLayout.descriptionWidth,
                    stepLayout.textAlign,
                )

            var titleTop = stepLayout.titleY - uiRenderer.getTitleBaselineOffset()
            var descriptionTop = stepLayout.descriptionY - uiRenderer.getDescriptionBaselineOffset()
            descriptionTop =
                maxOf(
                    descriptionTop,
                    titleTop + titleHeight + MIN_GAP_BETWEEN_TITLE_AND_DESCRIPTION,
                )

            if (isLinearVertical) {
                val shiftDown = (previousBottom + MIN_GAP_BETWEEN_LINEAR_STEPS) - titleTop
                if (shiftDown > 0f) {
                    titleTop += shiftDown
                    descriptionTop += shiftDown
                }
            }

            previousBottom = maxOf(previousBottom, descriptionTop + descriptionHeight)

            TimelineResolvedTextBlock(
                titleTop = titleTop,
                titleHeight = titleHeight,
                descriptionTop = descriptionTop,
                descriptionHeight = descriptionHeight,
            )
        }
    }
}
