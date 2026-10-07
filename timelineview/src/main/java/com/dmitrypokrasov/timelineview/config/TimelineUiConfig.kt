package com.dmitrypokrasov.timelineview.config

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.dmitrypokrasov.timelineview.model.TimelineConstants

/**
 * Visual configuration consumed by timeline renderers.
 *
 * This object stores renderer input only. Drawing behavior lives in
 * [com.dmitrypokrasov.timelineview.render.TimelineUiRenderer] implementations.
 *
 * @property icons default icons used by the renderer.
 * @property colors line and text colors.
 * @property textSizes title and description text sizes in pixels.
 * @property stroke line width and corner radius in pixels.
 */
data class TimelineUiConfig(
    val icons: Icons = Icons(),
    val colors: Colors = Colors(),
    val textSizes: TextSizes = TextSizes(),
    val stroke: Stroke = Stroke(),
) {
    /** Drawable resources used for fallback badge and active progress icons. */
    data class Icons(
        @DrawableRes val iconDisableLvl: Int = 0,
        @DrawableRes val iconProgress: Int = 0,
    )

    /** Opaque defaults for light surfaces, shared with XML. Supply colors for dark/custom themes. */
    data class Colors(
        @ColorInt val colorProgress: Int = TimelineConstants.DEFAULT_PROGRESS_COLOR,
        @ColorInt val colorStroke: Int = TimelineConstants.DEFAULT_STROKE_COLOR,
        @ColorInt val colorTitle: Int = TimelineConstants.DEFAULT_TITLE_COLOR,
        @ColorInt val colorDescription: Int = TimelineConstants.DEFAULT_DESCRIPTION_COLOR,
    )

    /** Text sizes for title and description blocks in pixels. */
    data class TextSizes(
        val sizeDescription: Float = TimelineConstants.DEFAULT_DESCRIPTION_SIZE,
        val sizeTitle: Float = TimelineConstants.DEFAULT_TITLE_SIZE,
    ) {
        init {
            require(sizeDescription.isFinite() && sizeDescription >= 0f) { "sizeDescription must be finite and non-negative" }
            require(sizeTitle.isFinite() && sizeTitle >= 0f) { "sizeTitle must be finite and non-negative" }
        }
    }

    /** Stroke width and corner radius in pixels. */
    data class Stroke(
        val radius: Float = TimelineConstants.DEFAULT_RADIUS_SIZE,
        val sizeStroke: Float = TimelineConstants.DEFAULT_STROKE_SIZE,
    ) {
        init {
            require(radius.isFinite() && radius >= 0f) { "radius must be finite and non-negative" }
            require(sizeStroke.isFinite() && sizeStroke >= 0f) { "sizeStroke must be finite and non-negative" }
        }
    }
}
