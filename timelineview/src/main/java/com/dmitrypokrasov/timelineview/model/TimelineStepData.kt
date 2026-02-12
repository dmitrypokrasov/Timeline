package com.dmitrypokrasov.timelineview.model

import androidx.annotation.DrawableRes

/**
 * Представляет данные шага таймлайна, готовые к отображению.
 *
 * @property title Заголовок шага.
 * @property description Описание шага.
 * @property iconRes Ресурс иконки активного шага (опционально).
 * @property iconDisabledRes Ресурс иконки неактивного шага (опционально).
 * @property progress Прогресс шага в процентах (0..100).
 */
data class TimelineStepData(
    val title: CharSequence? = null,
    val description: CharSequence? = null,
    @DrawableRes val iconRes: Int? = null,
    @DrawableRes val iconDisabledRes: Int? = null,
    val progress: Int
) {
    init {
        require(progress in 0..100) { "progress must be in 0..100" }
    }
}
