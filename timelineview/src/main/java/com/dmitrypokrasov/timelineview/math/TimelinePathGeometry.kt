package com.dmitrypokrasov.timelineview.math

import android.graphics.Path
import android.graphics.PathMeasure
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import kotlin.math.hypot

internal data class TimelinePoint(val x: Float, val y: Float)

/** One source of truth for drawing, arc-length progress and curve splitting. */
internal class TimelinePathGeometry(points: List<TimelinePoint>, radius: Float) {
    private val end = points.last()
    private val start = points.first()
    private val path = Path()
    private val measure: PathMeasure
    val length: Float get() = measure.length

    init {
        val vertices = points.filterIndexed { index, point -> index == 0 || point != points[index - 1] }
        path.moveTo(start.x, start.y)
        for (index in 1 until vertices.lastIndex) {
            val a = vertices[index - 1]
            val b = vertices[index]
            val c = vertices[index + 1]
            val incoming = hypot(b.x - a.x, b.y - a.y)
            val outgoing = hypot(c.x - b.x, c.y - b.y)
            val inset = minOf(radius, incoming / 2f, outgoing / 2f)
            val before = TimelinePoint(b.x - (b.x - a.x) * inset / incoming, b.y - (b.y - a.y) * inset / incoming)
            val after = TimelinePoint(b.x + (c.x - b.x) * inset / outgoing, b.y + (c.y - b.y) * inset / outgoing)
            path.lineTo(before.x, before.y)
            path.quadTo(b.x, b.y, after.x, after.y)
        }
        path.lineTo(end.x, end.y)
        measure = PathMeasure(path, false)
    }

    fun point(fraction: Float): TimelinePoint {
        if (fraction <= 0f) return start
        if (fraction >= 1f || length == 0f) return end
        val position = FloatArray(2)
        measure.getPosTan(length * fraction, position, null)
        return TimelinePoint(position[0], position[1])
    }

    fun append(
        completed: Path,
        pending: Path,
        fraction: Float,
        connect: Boolean,
    ) {
        val split = length * fraction.coerceIn(0f, 1f)
        measure.getSegment(0f, split, completed, !connect || completed.isEmpty)
        measure.getSegment(split, length, pending, !connect || pending.isEmpty)
    }
}

internal fun drawTimelineSegments(
    segments: List<TimelinePathGeometry>,
    steps: List<TimelineStepData>,
    mode: TimelineMathConfig.ProgressMode,
    completed: Path,
    pending: Path,
) {
    completed.reset()
    pending.reset()
    val active = steps.indexOfFirst { it.progress < 100 }
    segments.forEachIndexed { index, segment ->
        val fraction =
            when {
                mode == TimelineMathConfig.ProgressMode.INDEPENDENT -> steps[index].progress / 100f
                active < 0 || index < active -> 1f
                index == active -> steps[index].progress / 100f
                else -> 0f
            }
        segment.append(completed, pending, fraction, mode == TimelineMathConfig.ProgressMode.SEQUENTIAL)
    }
}
