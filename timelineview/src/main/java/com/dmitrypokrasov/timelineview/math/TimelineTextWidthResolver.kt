package com.dmitrypokrasov.timelineview.math

import android.graphics.Paint
import kotlin.math.min
import kotlin.math.roundToInt

internal object TimelineTextWidthResolver {
    fun resolve(
        measuredWidth: Int,
        startPosition: Float,
        localX: Float,
        align: Paint.Align
    ): Int {
        if (measuredWidth <= 0) return 1

        val absoluteX = (startPosition + localX).coerceIn(0f, measuredWidth.toFloat())
        val available = when (align) {
            Paint.Align.LEFT -> measuredWidth - absoluteX
            Paint.Align.RIGHT -> absoluteX
            Paint.Align.CENTER -> min(absoluteX, measuredWidth - absoluteX) * 2f
        }

        return available.roundToInt().coerceAtLeast(1)
    }
}
