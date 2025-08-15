package com.dmitrypokrasov.timelineview.domain.data

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.dmitrypokrasov.timelineview.data.TimelineConstants

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
 * вынесена в реализации [com.dmitrypokrasov.timelineview.domain.TimelineUiRenderer].
 */
data class TimelineUiConfig(
    @DrawableRes val iconDisableLvl: Int = 0,
    @DrawableRes val iconProgress: Int = 0,
    @ColorInt var colorProgress: Int = 0,
    @ColorInt var colorStroke: Int = 0,
    @ColorInt var colorTitle: Int = 0,
    @ColorInt var colorDescription: Int = 0,
    val sizeDescription: Float = TimelineConstants.DEFAULT_DESCRIPTION_SIZE,
    val sizeTitle: Float = TimelineConstants.DEFAULT_TITLE_SIZE,
    val radius: Float = TimelineConstants.DEFAULT_RADIUS_SIZE,
    val sizeStroke: Float = TimelineConstants.DEFAULT_STROKE_SIZE
)
