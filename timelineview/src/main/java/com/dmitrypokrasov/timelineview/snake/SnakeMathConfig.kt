package com.dmitrypokrasov.timelineview.snake

import com.dmitrypokrasov.timelineview.core.TimelineConstants
import com.dmitrypokrasov.timelineview.core.TimelineMathConfig
import com.dmitrypokrasov.timelineview.core.TimelineStep

/**
 * Concrete math configuration for snake strategy.
 */
data class SnakeMathConfig(
    override val startPosition: TimelineMathConfig.StartPosition = TimelineMathConfig.StartPosition.CENTER,
    override val steps: List<TimelineStep> = listOf(),
    override val stepY: Float = TimelineConstants.DEFAULT_STEP_Y_SIZE,
    override val stepYFirst: Float = TimelineConstants.DEFAULT_STEP_Y_FIRST_SIZE,
    override val marginTopDescription: Float = TimelineConstants.DEFAULT_MARGIN_TOP_DESCRIPTION,
    override val marginTopTitle: Float = TimelineConstants.DEFAULT_MARGIN_TOP_TITLE,
    override val marginTopProgressIcon: Float = TimelineConstants.DEFAULT_MARGIN_TOP_PROGRESS_ICON,
    override val marginHorizontalImage: Float = TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_IMAGE,
    override val marginHorizontalText: Float = TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_TEXT,
    override val marginHorizontalStroke: Float = TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_STROKE,
    override val sizeIconProgress: Float = TimelineConstants.DEFAULT_ICON_PROGRESS_SIZE,
    override val sizeImageLvl: Float = TimelineConstants.DEFAULT_IMAGE_LVL_SIZE,
) : TimelineMathConfig
