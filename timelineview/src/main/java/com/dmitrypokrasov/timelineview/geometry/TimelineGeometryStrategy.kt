package com.dmitrypokrasov.timelineview.geometry

import com.dmitrypokrasov.timelineview.config.TimelineMathConfig

/** Измеренные UI высоты текстовых блоков в порядке шагов layout. */
data class TimelineTextMetrics(val titleHeight: Int, val descriptionHeight: Int)

/** Вход геометрии в пикселях. Пустые textMetrics означают предварительный проход для определения ширин. */
data class TimelineGeometryInput(
    val config: TimelineMathConfig,
    val width: Int,
    val cornerRadius: Float = 0f,
    val textMetrics: List<TimelineTextMetrics> = emptyList(),
)

/** Границы содержимого в локальных пикселях, включая путь и иконки. */
data class TimelineGeometryBounds(val left: Float, val top: Float, val right: Float, val bottom: Float)

/** Единый снимок layout, команд пути и границ в локальных координатах до startPosition и padding. */
data class TimelineGeometryResult(
    val layout: TimelineGeometryLayout,
    val completed: List<TimelinePathCommand>,
    val remaining: List<TimelinePathCommand>,
    val bounds: TimelineGeometryBounds,
    val startPosition: Float = 0f,
)

/**
 * Рассчитывает геометрию без Android и не изменяет входные данные.
 * Повторный вызов с тем же входом должен возвращать равный результат.
 * Предварительный и текстовый проходы сохраняют порядок шагов и ширины текстовых блоков.
 */
fun interface TimelineGeometryStrategy {
    /** Рассчитывает детерминированный снимок геометрии; View вызывает стратегию в UI-потоке. */
    fun calculate(input: TimelineGeometryInput): TimelineGeometryResult
}
