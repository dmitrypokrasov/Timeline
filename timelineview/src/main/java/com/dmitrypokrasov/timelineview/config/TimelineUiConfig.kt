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
}
