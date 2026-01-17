package com.dmitrypokrasov.timelineview.config

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

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
interface TimelineUiConfig {
    @get:DrawableRes
    val iconDisableLvl: Int

    @get:DrawableRes
    val iconProgress: Int

    @get:ColorInt
    val colorProgress: Int

    @get:ColorInt
    val colorStroke: Int

    @get:ColorInt
    val colorTitle: Int

    @get:ColorInt
    val colorDescription: Int

    val sizeDescription: Float
    val sizeTitle: Float
    val radius: Float
    val sizeStroke: Float
}
