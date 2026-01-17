package com.dmitrypokrasov.timelineview.config

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.dmitrypokrasov.timelineview.model.TimelineConstants

/**
 * Стандартная реализация конфигурации визуального оформления элементов таймлайна.
 */
data class DefaultTimelineUiConfig(
    @DrawableRes override val iconDisableLvl: Int = 0,
    @DrawableRes override val iconProgress: Int = 0,
    @ColorInt override val colorProgress: Int = 0,
    @ColorInt override val colorStroke: Int = 0,
    @ColorInt override val colorTitle: Int = 0,
    @ColorInt override val colorDescription: Int = 0,
    override val sizeDescription: Float = TimelineConstants.DEFAULT_DESCRIPTION_SIZE,
    override val sizeTitle: Float = TimelineConstants.DEFAULT_TITLE_SIZE,
    override val radius: Float = TimelineConstants.DEFAULT_RADIUS_SIZE,
    override val sizeStroke: Float = TimelineConstants.DEFAULT_STROKE_SIZE
) : TimelineUiConfig
