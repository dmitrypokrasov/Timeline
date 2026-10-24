package com.dmitrypokrasov.timelineview.math

import android.graphics.Paint
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryTextWidth
import com.dmitrypokrasov.timelineview.render.toGeometry

/** Совместимый Android-вход для общего чистого расчёта ширины. */
internal object TimelineTextWidthResolver {
    fun resolve(
        measuredWidth: Int,
        startPosition: Float,
        localX: Float,
        align: Paint.Align,
    ): Int =
        TimelineGeometryTextWidth.resolve(measuredWidth, startPosition, localX, align.toGeometry())
}
