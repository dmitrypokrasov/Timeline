package com.dmitrypokrasov.timelineview.geometry

import kotlin.math.min
import kotlin.math.roundToInt

/** Доступная физическая ширина текста до границ View, независимо от Android Paint. */
internal object TimelineGeometryTextWidth {
    fun resolve(
        measuredWidth: Int,
        startPosition: Float,
        localX: Float,
        align: TimelineTextAlignment,
    ): Int {
        if (measuredWidth <= 0) return 1
        val absoluteX = (startPosition + localX).coerceIn(0f, measuredWidth.toFloat())
        val available =
            when (align) {
                TimelineTextAlignment.LEFT -> measuredWidth - absoluteX
                TimelineTextAlignment.RIGHT -> absoluteX
                TimelineTextAlignment.CENTER -> min(absoluteX, measuredWidth - absoluteX) * 2f
            }
        return available.roundToInt().coerceAtLeast(1)
    }
}
