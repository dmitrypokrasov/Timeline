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
    var icons: Icons = Icons(),
    var colors: Colors = Colors(),
    var textSizes: TextSizes = TextSizes(),
    var stroke: Stroke = Stroke(),
) {
    /** Drawable resources used for fallback badge and active progress icons. */
    data class Icons(
        @DrawableRes var iconDisableLvl: Int = 0,
        @DrawableRes var iconProgress: Int = 0,
    ) {
        init {
            iconDisableLvl = iconDisableLvl.coerceAtLeast(0)
            iconProgress = iconProgress.coerceAtLeast(0)
        }
    }

    /** Colors used to draw the timeline line and text. */
    data class Colors(
        @ColorInt var colorProgress: Int = 0,
        @ColorInt var colorStroke: Int = 0,
        @ColorInt var colorTitle: Int = 0,
        @ColorInt var colorDescription: Int = 0,
    )

    /** Text sizes for title and description blocks in pixels. */
    data class TextSizes(
        var sizeDescription: Float = TimelineConstants.DEFAULT_DESCRIPTION_SIZE,
        var sizeTitle: Float = TimelineConstants.DEFAULT_TITLE_SIZE,
    ) {
        init {
            sizeDescription = sizeDescription.coerceAtLeast(0f)
            sizeTitle = sizeTitle.coerceAtLeast(0f)
        }
    }

    /** Stroke width and corner radius in pixels. */
    data class Stroke(
        var radius: Float = TimelineConstants.DEFAULT_RADIUS_SIZE,
        var sizeStroke: Float = TimelineConstants.DEFAULT_STROKE_SIZE,
    ) {
        init {
            radius = radius.coerceAtLeast(0f)
            sizeStroke = sizeStroke.coerceAtLeast(0f)
        }
    }
}
