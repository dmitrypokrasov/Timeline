package com.dmitrypokrasov.timelineview.math

import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryBounds
import com.dmitrypokrasov.timelineview.geometry.TimelineTextMetrics

/**
 * Дополнительный протокол измерения; существующие стратегии не обязаны его реализовывать.
 * Вместо автоматического расширения getContentRows стратегия сама размещает измеренные блоки.
 * Между предварительным и измеренным проходами порядок шагов и ширины текста неизменны.
 */
interface TimelineMeasuredGeometry {
    /** Пустой список обозначает предварительный проход до измерения UI. */
    fun setTextMetrics(metrics: List<TimelineTextMetrics>)

    /** Границы содержимого в локальных пикселях, до startPosition и padding. */
    fun getGeometryBounds(): TimelineGeometryBounds
}
