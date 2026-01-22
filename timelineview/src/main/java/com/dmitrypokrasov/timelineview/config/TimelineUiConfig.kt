package com.dmitrypokrasov.timelineview.config

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.dmitrypokrasov.timelineview.model.TimelineConstants

/**
 * Конфигурация визуального оформления элементов таймлайна.
 *
 * @property icons группа иконок таймлайна
 * @property colors группа цветов таймлайна
 * @property textSizes группа размеров текста
 * @property stroke группа параметров линии
 *
 * Содержит только значения, используемые рендерерами. Вся логика подготовки
 * вынесена в реализации [com.dmitrypokrasov.timelineview.render.TimelineUiRenderer].
 */
data class TimelineUiConfig(
    var icons: Icons = Icons(),
    var colors: Colors = Colors(),
    var textSizes: TextSizes = TextSizes(),
    var stroke: Stroke = Stroke()
) {
    @Deprecated(
        message = "Use grouped configuration: icons.iconDisableLvl",
        replaceWith = ReplaceWith("icons.iconDisableLvl")
    )
    @DrawableRes
    var iconDisableLvl: Int
        get() = icons.iconDisableLvl
        set(value) {
            icons.iconDisableLvl = value
        }

    @Deprecated(
        message = "Use grouped configuration: icons.iconProgress",
        replaceWith = ReplaceWith("icons.iconProgress")
    )
    @DrawableRes
    var iconProgress: Int
        get() = icons.iconProgress
        set(value) {
            icons.iconProgress = value
        }

    @Deprecated(
        message = "Use grouped configuration: colors.colorProgress",
        replaceWith = ReplaceWith("colors.colorProgress")
    )
    @ColorInt
    var colorProgress: Int
        get() = colors.colorProgress
        set(value) {
            colors.colorProgress = value
        }

    @Deprecated(
        message = "Use grouped configuration: colors.colorStroke",
        replaceWith = ReplaceWith("colors.colorStroke")
    )
    @ColorInt
    var colorStroke: Int
        get() = colors.colorStroke
        set(value) {
            colors.colorStroke = value
        }

    @Deprecated(
        message = "Use grouped configuration: colors.colorTitle",
        replaceWith = ReplaceWith("colors.colorTitle")
    )
    @ColorInt
    var colorTitle: Int
        get() = colors.colorTitle
        set(value) {
            colors.colorTitle = value
        }

    @Deprecated(
        message = "Use grouped configuration: colors.colorDescription",
        replaceWith = ReplaceWith("colors.colorDescription")
    )
    @ColorInt
    var colorDescription: Int
        get() = colors.colorDescription
        set(value) {
            colors.colorDescription = value
        }

    @Deprecated(
        message = "Use grouped configuration: textSizes.sizeDescription",
        replaceWith = ReplaceWith("textSizes.sizeDescription")
    )
    var sizeDescription: Float
        get() = textSizes.sizeDescription
        set(value) {
            textSizes.sizeDescription = value
        }

    @Deprecated(
        message = "Use grouped configuration: textSizes.sizeTitle",
        replaceWith = ReplaceWith("textSizes.sizeTitle")
    )
    var sizeTitle: Float
        get() = textSizes.sizeTitle
        set(value) {
            textSizes.sizeTitle = value
        }

    @Deprecated(
        message = "Use grouped configuration: stroke.radius",
        replaceWith = ReplaceWith("stroke.radius")
    )
    var radius: Float
        get() = stroke.radius
        set(value) {
            stroke.radius = value
        }

    @Deprecated(
        message = "Use grouped configuration: stroke.sizeStroke",
        replaceWith = ReplaceWith("stroke.sizeStroke")
    )
    var sizeStroke: Float
        get() = stroke.sizeStroke
        set(value) {
            stroke.sizeStroke = value
        }

    data class Icons(
        @DrawableRes var iconDisableLvl: Int = 0,
        @DrawableRes var iconProgress: Int = 0
    ) {
        init {
            iconDisableLvl = iconDisableLvl.coerceAtLeast(0)
            iconProgress = iconProgress.coerceAtLeast(0)
        }
    }

    data class Colors(
        @ColorInt var colorProgress: Int = 0,
        @ColorInt var colorStroke: Int = 0,
        @ColorInt var colorTitle: Int = 0,
        @ColorInt var colorDescription: Int = 0
    )

    data class TextSizes(
        var sizeDescription: Float = TimelineConstants.DEFAULT_DESCRIPTION_SIZE,
        var sizeTitle: Float = TimelineConstants.DEFAULT_TITLE_SIZE
    ) {
        init {
            sizeDescription = sizeDescription.coerceAtLeast(0f)
            sizeTitle = sizeTitle.coerceAtLeast(0f)
        }
    }

    data class Stroke(
        var radius: Float = TimelineConstants.DEFAULT_RADIUS_SIZE,
        var sizeStroke: Float = TimelineConstants.DEFAULT_STROKE_SIZE
    ) {
        init {
            radius = radius.coerceAtLeast(0f)
            sizeStroke = sizeStroke.coerceAtLeast(0f)
        }
    }

    @Deprecated(
        message = "Use grouped configuration via TimelineUiConfig(icons, colors, textSizes, stroke).",
        replaceWith = ReplaceWith(
            "TimelineUiConfig(icons = Icons(iconDisableLvl, iconProgress), " +
                "colors = Colors(colorProgress, colorStroke, colorTitle, colorDescription), " +
                "textSizes = TextSizes(sizeDescription, sizeTitle), " +
                "stroke = Stroke(radius, sizeStroke))"
        )
    )
    constructor(
        @DrawableRes iconDisableLvl: Int = 0,
        @DrawableRes iconProgress: Int = 0,
        @ColorInt colorProgress: Int = 0,
        @ColorInt colorStroke: Int = 0,
        @ColorInt colorTitle: Int = 0,
        @ColorInt colorDescription: Int = 0,
        sizeDescription: Float = TimelineConstants.DEFAULT_DESCRIPTION_SIZE,
        sizeTitle: Float = TimelineConstants.DEFAULT_TITLE_SIZE,
        radius: Float = TimelineConstants.DEFAULT_RADIUS_SIZE,
        sizeStroke: Float = TimelineConstants.DEFAULT_STROKE_SIZE
    ) : this(
        icons = Icons(
            iconDisableLvl = iconDisableLvl,
            iconProgress = iconProgress
        ),
        colors = Colors(
            colorProgress = colorProgress,
            colorStroke = colorStroke,
            colorTitle = colorTitle,
            colorDescription = colorDescription
        ),
        textSizes = TextSizes(
            sizeDescription = sizeDescription,
            sizeTitle = sizeTitle
        ),
        stroke = Stroke(
            radius = radius,
            sizeStroke = sizeStroke
        )
    )

    class Builder {
        private var icons: Icons = Icons()
        private var colors: Colors = Colors()
        private var textSizes: TextSizes = TextSizes()
        private var stroke: Stroke = Stroke()

        fun icons(value: Icons) = apply { icons = value }
        fun colors(value: Colors) = apply { colors = value }
        fun textSizes(value: TextSizes) = apply { textSizes = value }
        fun stroke(value: Stroke) = apply { stroke = value }

        @Deprecated(
            message = "Use icons.iconDisableLvl",
            replaceWith = ReplaceWith("icons.iconDisableLvl = value")
        )
        fun iconDisableLvl(@DrawableRes value: Int) = apply { icons.iconDisableLvl = value }

        @Deprecated(
            message = "Use icons.iconProgress",
            replaceWith = ReplaceWith("icons.iconProgress = value")
        )
        fun iconProgress(@DrawableRes value: Int) = apply { icons.iconProgress = value }

        @Deprecated(
            message = "Use colors.colorProgress",
            replaceWith = ReplaceWith("colors.colorProgress = value")
        )
        fun colorProgress(@ColorInt value: Int) = apply { colors.colorProgress = value }

        @Deprecated(
            message = "Use colors.colorStroke",
            replaceWith = ReplaceWith("colors.colorStroke = value")
        )
        fun colorStroke(@ColorInt value: Int) = apply { colors.colorStroke = value }

        @Deprecated(
            message = "Use colors.colorTitle",
            replaceWith = ReplaceWith("colors.colorTitle = value")
        )
        fun colorTitle(@ColorInt value: Int) = apply { colors.colorTitle = value }

        @Deprecated(
            message = "Use colors.colorDescription",
            replaceWith = ReplaceWith("colors.colorDescription = value")
        )
        fun colorDescription(@ColorInt value: Int) = apply { colors.colorDescription = value }

        @Deprecated(
            message = "Use textSizes.sizeDescription",
            replaceWith = ReplaceWith("textSizes.sizeDescription = value")
        )
        fun sizeDescription(value: Float) = apply { textSizes.sizeDescription = value }

        @Deprecated(
            message = "Use textSizes.sizeTitle",
            replaceWith = ReplaceWith("textSizes.sizeTitle = value")
        )
        fun sizeTitle(value: Float) = apply { textSizes.sizeTitle = value }

        @Deprecated(
            message = "Use stroke.radius",
            replaceWith = ReplaceWith("stroke.radius = value")
        )
        fun radius(value: Float) = apply { stroke.radius = value }

        @Deprecated(
            message = "Use stroke.sizeStroke",
            replaceWith = ReplaceWith("stroke.sizeStroke = value")
        )
        fun sizeStroke(value: Float) = apply { stroke.sizeStroke = value }

        fun build(): TimelineUiConfig = TimelineUiConfig(
            icons = icons,
            colors = colors,
            textSizes = textSizes,
            stroke = stroke
        )
    }

    companion object {
        fun builder(): Builder = Builder()
    }
}
