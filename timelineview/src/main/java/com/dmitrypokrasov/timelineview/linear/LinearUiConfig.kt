package com.dmitrypokrasov.timelineview.linear

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.dmitrypokrasov.timelineview.core.TimelineConstants
import com.dmitrypokrasov.timelineview.core.TimelineUiConfig

/**
 * Concrete UI configuration for linear strategy.
 */
data class LinearUiConfig(
    @DrawableRes override val iconDisableLvl: Int = 0,
    @DrawableRes override val iconProgress: Int = 0,
    @ColorInt override var colorProgress: Int = 0,
    @ColorInt override var colorStroke: Int = 0,
    @ColorInt override var colorTitle: Int = 0,
    @ColorInt override var colorDescription: Int = 0,
    override val sizeDescription: Float = TimelineConstants.DEFAULT_DESCRIPTION_SIZE,
    override val sizeTitle: Float = TimelineConstants.DEFAULT_TITLE_SIZE,
    override val radius: Float = TimelineConstants.DEFAULT_RADIUS_SIZE,
    override val sizeStroke: Float = TimelineConstants.DEFAULT_STROKE_SIZE,
) : TimelineUiConfig
