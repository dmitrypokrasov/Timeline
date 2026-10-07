package com.dmitrypokrasov.timelineview.ui

import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.math.TimelineTextBoundary
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

    fun resolve(
        layout: TimelineLayout?,
        mathEngine: TimelineMathEngine,
        uiRenderer: TimelineUiRenderer,
    ): List<TimelineResolvedTextBlock> {
        if (layout == null) return emptyList()

        return layout.steps.mapIndexed { index, stepLayout ->
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

            val baselineTop = stepLayout.titleY - uiRenderer.getTitleBaselineOffset()
            val boundary = (mathEngine as? TimelineTextBoundary)?.getTextTopBoundary(index)
            val requestedTop = if (boundary == null) baselineTop else maxOf(baselineTop, boundary + uiRenderer.getConfig().stroke.sizeStroke / 2f + MIN_GAP_BETWEEN_TITLE_AND_DESCRIPTION)
            val horizontal = mathEngine.textBelowBadge
            val badgeSize = mathEngine.getConfig().sizes.sizeImageLvl
            val overlayInset = badgeSize * ((stepLayout.step.badgeAnimation?.scale ?: 1f).coerceAtLeast(1f) - 1f) / 2f
            val titleTop =
                if (horizontal) {
                    maxOf(requestedTop, stepLayout.iconY + badgeSize + overlayInset + MIN_GAP_BETWEEN_TITLE_AND_DESCRIPTION)
                } else {
                    requestedTop
                }
            var descriptionTop = stepLayout.descriptionY - uiRenderer.getDescriptionBaselineOffset()
            descriptionTop =
                maxOf(
                    descriptionTop,
                    titleTop + titleHeight + MIN_GAP_BETWEEN_TITLE_AND_DESCRIPTION,
                )

            TimelineResolvedTextBlock(
                titleTop = titleTop,
                titleHeight = titleHeight,
                descriptionTop = descriptionTop,
                descriptionHeight = descriptionHeight,
            )
        }
    }
}
