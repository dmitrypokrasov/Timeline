package com.dmitrypokrasov.timelineview.config

import com.dmitrypokrasov.timelineview.model.TimelineConstants
import com.dmitrypokrasov.timelineview.model.TimelineStepData

/**
 * Positioning and sizing input for a timeline math engine.
 *
 * This object stores declarative input only. Concrete coordinates and paths are calculated by
 * [com.dmitrypokrasov.timelineview.math.TimelineMathEngine] implementations.
 *
 * @property startPosition where the timeline should start within the available width.
 * @property steps steps to render.
 * @property spacing offsets and inter-step distances in pixels.
 * @property sizes icon sizes in pixels.
 */
data class TimelineMathConfig(
    val startPosition: StartPosition = StartPosition.CENTER,
    val steps: List<TimelineStepData> = listOf(),
    var spacing: Spacing = Spacing(),
    var sizes: Sizes = Sizes(),
) {
    /** Placement of the timeline relative to the container width. */
    enum class StartPosition { START, CENTER, END }

    /** Spacing values used by math engines, expressed in pixels. */
    data class Spacing(
        var stepY: Float = TimelineConstants.DEFAULT_STEP_Y_SIZE,
        var stepYFirst: Float = TimelineConstants.DEFAULT_STEP_Y_FIRST_SIZE,
        var marginTopDescription: Float = TimelineConstants.DEFAULT_MARGIN_TOP_DESCRIPTION,
        var marginTopTitle: Float = TimelineConstants.DEFAULT_MARGIN_TOP_TITLE,
        var marginTopProgressIcon: Float = TimelineConstants.DEFAULT_MARGIN_TOP_PROGRESS_ICON,
        var marginHorizontalImage: Float = TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_IMAGE,
        var marginHorizontalText: Float = TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_TEXT,
        var marginHorizontalStroke: Float = TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_STROKE,
    ) {
        init {
            stepY = stepY.coerceAtLeast(0f)
            stepYFirst = stepYFirst.coerceAtLeast(0f)
            marginTopDescription = marginTopDescription.coerceAtLeast(0f)
            marginTopTitle = marginTopTitle.coerceAtLeast(0f)
            marginTopProgressIcon = marginTopProgressIcon.coerceAtLeast(0f)
            marginHorizontalImage = marginHorizontalImage.coerceAtLeast(0f)
            marginHorizontalText = marginHorizontalText.coerceAtLeast(0f)
            marginHorizontalStroke = marginHorizontalStroke.coerceAtLeast(0f)
        }
    }

    /** Icon sizes used by math engines, expressed in pixels. */
    data class Sizes(
        var sizeIconProgress: Float = TimelineConstants.DEFAULT_ICON_PROGRESS_SIZE,
        var sizeImageLvl: Float = TimelineConstants.DEFAULT_IMAGE_LVL_SIZE,
    ) {
        init {
            sizeIconProgress = sizeIconProgress.coerceAtLeast(0f)
            sizeImageLvl = sizeImageLvl.coerceAtLeast(0f)
        }
    }
}
