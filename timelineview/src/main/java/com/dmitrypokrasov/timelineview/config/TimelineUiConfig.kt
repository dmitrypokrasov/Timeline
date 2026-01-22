package com.dmitrypokrasov.timelineview.config

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.dmitrypokrasov.timelineview.model.TimelineConstants

/**
 * Конфигурация визуального оформления элементов таймлайна.
 *
 * @property iconDisableLvl иконка для заблокированных шагов
 * @property iconProgress иконка текущего прогресса
 * @property colorProgress цвет пройденной части линии
 * @property colorStroke цвет оставшейся части линии
 * @property colorTitle цвет заголовков шагов
 * @property colorDescription цвет описаний шагов
 * @property sizeDescription размер текста описания
 * @property sizeTitle размер текста заголовка
 * @property radius радиус скругления линии
 * @property sizeStroke толщина линии
 *
 * Содержит только значения, используемые рендерерами. Вся логика подготовки
 * вынесена в реализации [com.dmitrypokrasov.timelineview.render.TimelineUiRenderer].
 */
data class TimelineUiConfig(
    @DrawableRes var iconDisableLvl: Int = 0,
    @DrawableRes var iconProgress: Int = 0,
    @ColorInt var colorProgress: Int = 0,
    @ColorInt var colorStroke: Int = 0,
    @ColorInt var colorTitle: Int = 0,
    @ColorInt var colorDescription: Int = 0,
    var sizeDescription: Float = TimelineConstants.DEFAULT_DESCRIPTION_SIZE,
    var sizeTitle: Float = TimelineConstants.DEFAULT_TITLE_SIZE,
    var radius: Float = TimelineConstants.DEFAULT_RADIUS_SIZE,
    var sizeStroke: Float = TimelineConstants.DEFAULT_STROKE_SIZE
) {
    init {
        iconDisableLvl = iconDisableLvl.coerceAtLeast(0)
        iconProgress = iconProgress.coerceAtLeast(0)
        sizeDescription = sizeDescription.coerceAtLeast(0f)
        sizeTitle = sizeTitle.coerceAtLeast(0f)
        radius = radius.coerceAtLeast(0f)
        sizeStroke = sizeStroke.coerceAtLeast(0f)
    }
}
