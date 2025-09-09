package com.dmitrypokrasov.timelineview.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * Представляет отдельный шаг в таймлайне прогресса.
 *
 * Каждый шаг включает в себя заголовок, описание, иконку, текущее значение счётчика
 * и максимальное значение для расчёта прогресса.
 *
 * @property title Ресурс строки заголовка для данного шага.
 * @property description Ресурс строки описания для данного шага.
 * @property icon Ресурс иконки, визуализирующей данный шаг.
 * @property count Текущее значение (например, количество выполненных задач). По умолчанию 0.
 * @property maxCount Максимальное значение счётчика, при достижении которого шаг считается полностью завершённым.
 */
data class TimelineStep(
    @StringRes val title: Int,
    @StringRes val description: Int,
    @DrawableRes val icon: Int,
    val count: Int = 0,
    val maxCount: Int
) {

    /**
     * Расчёт процента выполнения данного шага.
     *
     * @return Значение от 0 до 100, отражающее текущий прогресс.
     */
    val percents: Int
        get() = (count.toDouble() / (maxCount.toDouble() / 100)).toInt().coerceIn(0, 100)
}
