package com.dmitrypokrasov.timelineview.ui

import android.graphics.Rect

internal data class TimelineBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    fun contains(
        x: Float,
        y: Float,
    ): Boolean = x in left..right && y in top..bottom

    fun offset(
        dx: Int,
        dy: Int,
    ): TimelineBounds =
        TimelineBounds(
            left = left + dx,
            top = top + dy,
            right = right + dx,
            bottom = bottom + dy,
        )

    fun toRect(): Rect = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
}
