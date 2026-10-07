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
    val spacing: Spacing = Spacing(),
    val sizes: Sizes = Sizes(),
    val progressMode: ProgressMode = ProgressMode.SEQUENTIAL,
    val horizontalLayout: HorizontalLayout = HorizontalLayout.FIT,
    /** Minimum cell width in pixels for wrapping or horizontally scrolling content. */
    val minCellWidth: Float = 160f,
    /** Pixels per millisecond for time-scaled layouts; content collision avoidance may expand gaps. */
    val pixelsPerMillisecond: Double = 0.001,
) {
    init {
        val ids = steps.mapNotNull { it.id }
        require(ids.size == ids.toSet().size) { "Step IDs must be unique" }
        require(minCellWidth.isFinite() && minCellWidth > 0f) { "minCellWidth must be finite and positive" }
        require(pixelsPerMillisecond.isFinite() && pixelsPerMillisecond > 0) { "pixelsPerMillisecond must be finite and positive" }
    }

    /** Sequential stops line completion at the first unfinished step; independent colors each segment separately. */
    enum class ProgressMode { SEQUENTIAL, INDEPENDENT }

    /** SCROLL requests intrinsic width inside a HorizontalScrollView; WRAP creates serpentine rows. */
    enum class HorizontalLayout { FIT, WRAP, SCROLL }

    /** Placement of the timeline relative to the container width. */
    enum class StartPosition { START, CENTER, END }

    /** Spacing values used by math engines, expressed in pixels. */
    data class Spacing(
        val stepY: Float = TimelineConstants.DEFAULT_STEP_Y_SIZE,
        val stepYFirst: Float = TimelineConstants.DEFAULT_STEP_Y_FIRST_SIZE,
        val marginTopDescription: Float = TimelineConstants.DEFAULT_MARGIN_TOP_DESCRIPTION,
        val marginTopTitle: Float = TimelineConstants.DEFAULT_MARGIN_TOP_TITLE,
        val marginTopProgressIcon: Float = TimelineConstants.DEFAULT_MARGIN_TOP_PROGRESS_ICON,
        val marginHorizontalImage: Float = TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_IMAGE,
        val marginHorizontalText: Float = TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_TEXT,
        val marginHorizontalStroke: Float = TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_STROKE,
    ) {
        init {
            require(stepY.isFinite() && stepY >= 0f) { "stepY must be finite and non-negative" }
            require(stepYFirst.isFinite() && stepYFirst >= 0f) { "stepYFirst must be finite and non-negative" }
            require(marginTopDescription.isFinite() && marginTopDescription >= 0f) { "marginTopDescription must be finite and non-negative" }
            require(marginTopTitle.isFinite() && marginTopTitle >= 0f) { "marginTopTitle must be finite and non-negative" }
            require(marginTopProgressIcon.isFinite() && marginTopProgressIcon >= 0f) { "marginTopProgressIcon must be finite and non-negative" }
            require(marginHorizontalImage.isFinite() && marginHorizontalImage >= 0f) { "marginHorizontalImage must be finite and non-negative" }
            require(marginHorizontalText.isFinite() && marginHorizontalText >= 0f) { "marginHorizontalText must be finite and non-negative" }
            require(marginHorizontalStroke.isFinite() && marginHorizontalStroke >= 0f) { "marginHorizontalStroke must be finite and non-negative" }
        }
    }

    /** Icon sizes used by math engines, expressed in pixels. */
    data class Sizes(
        val sizeIconProgress: Float = TimelineConstants.DEFAULT_ICON_PROGRESS_SIZE,
        val sizeImageLvl: Float = TimelineConstants.DEFAULT_IMAGE_LVL_SIZE,
    ) {
        init {
            require(sizeIconProgress.isFinite() && sizeIconProgress >= 0f) { "sizeIconProgress must be finite and non-negative" }
            require(sizeImageLvl.isFinite() && sizeImageLvl >= 0f) { "sizeImageLvl must be finite and non-negative" }
        }
    }
}
