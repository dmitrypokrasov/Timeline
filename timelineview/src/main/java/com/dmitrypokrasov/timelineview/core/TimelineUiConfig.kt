package com.dmitrypokrasov.timelineview.core

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

/**
 * Base interface describing visual parameters of timeline rendering.
 * Only minimal properties required for drawing are exposed.
 */
interface TimelineUiConfig {
    @get:DrawableRes val iconDisableLvl: Int
    @get:DrawableRes val iconProgress: Int
    @get:ColorInt var colorProgress: Int
    @get:ColorInt var colorStroke: Int
    @get:ColorInt var colorTitle: Int
    @get:ColorInt var colorDescription: Int
    val sizeDescription: Float
    val sizeTitle: Float
    val radius: Float
    val sizeStroke: Float
}
