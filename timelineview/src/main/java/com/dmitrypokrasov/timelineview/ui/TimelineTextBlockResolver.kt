package com.dmitrypokrasov.timelineview.ui

import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
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
    fun resolve(
        layout: TimelineLayout?,
        mathEngine: TimelineMathEngine,
        mathConfig: TimelineMathConfig,
        uiRenderer: TimelineUiRenderer
    ): List<TimelineResolvedTextBlock> {
        if (layout == null) return emptyList()

        val isLinearVertical = mathEngine is LinearTimelineMath &&
            mathEngine.orientation == LinearTimelineMath.Orientation.VERTICAL
        val minGapBetweenTitleAndDescription = 4f
        val minGapBetweenSteps = 4f
        val linearVerticalLift = if (isLinearVertical) {
            (mathConfig.spacing.stepYFirst / 2f + mathConfig.spacing.stepYFirst / 10f + mathConfig.spacing.stepYFirst / 20f).coerceAtLeast(6f)
        } else {
            0f
        }
        var previousBottom = Float.NEGATIVE_INFINITY

        return layout.steps.map { stepLayout ->
            val titleHeight = uiRenderer.measureTitleHeight(
                stepLayout.step.title ?: "",
                stepLayout.titleWidth,
                stepLayout.textAlign
            )
            val descriptionHeight = uiRenderer.measureDescriptionHeight(
                stepLayout.step.description ?: "",
                stepLayout.descriptionWidth,
                stepLayout.textAlign
            )

            var titleTop = stepLayout.titleY - uiRenderer.getTitleBaselineOffset() - linearVerticalLift
            var descriptionTop = stepLayout.descriptionY - uiRenderer.getDescriptionBaselineOffset() -
                linearVerticalLift
            descriptionTop = maxOf(descriptionTop, titleTop + titleHeight + minGapBetweenTitleAndDescription)

            if (isLinearVertical) {
                val shiftDown = (previousBottom + minGapBetweenSteps) - titleTop
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
                descriptionHeight = descriptionHeight
            )
        }
    }
}


