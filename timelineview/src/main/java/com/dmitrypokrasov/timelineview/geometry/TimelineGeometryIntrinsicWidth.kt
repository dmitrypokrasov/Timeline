package com.dmitrypokrasov.timelineview.geometry

import com.dmitrypokrasov.timelineview.config.TimelineMathConfig

/** Необязательная ширина содержимого в пикселях для измерения без ограничения родителя. */
fun interface TimelineGeometryIntrinsicWidth {
    /** Возвращает неотрицательную ширину; 0 оставляет выбор ширины родительскому View. */
    fun desiredWidth(config: TimelineMathConfig): Int
}
