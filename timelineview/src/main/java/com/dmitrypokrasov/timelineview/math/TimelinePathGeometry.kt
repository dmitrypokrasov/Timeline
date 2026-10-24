package com.dmitrypokrasov.timelineview.math

import android.graphics.Path
import android.graphics.PathMeasure
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryPoint
import com.dmitrypokrasov.timelineview.geometry.TimelineRoundedPath
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.render.setCommands

internal data class TimelinePoint(val x: Float, val y: Float)

/** One source of truth for drawing, arc-length progress and curve splitting. */
internal class TimelinePathGeometry(points: List<TimelinePoint>, radius: Float) {
    private val end = points.last()
    private val start = points.first()
    private val path = Path()
    private val measure: PathMeasure
    val length: Float get() = measure.length

    init {
        path.setCommands(TimelineRoundedPath.commands(points.map { TimelineGeometryPoint(it.x, it.y) }, radius))
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
