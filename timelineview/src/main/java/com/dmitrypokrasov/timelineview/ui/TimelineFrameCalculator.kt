package com.dmitrypokrasov.timelineview.ui

import android.content.Context
import android.graphics.Path
import android.graphics.RectF
import com.dmitrypokrasov.timelineview.geometry.TimelineTextMetrics
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.math.TimelineMeasuredGeometry
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.model.identity
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer

/** Рассчитывает кандидат frame без изменения установленного состояния controller. */
internal class TimelineFrameCalculator(private val context: Context) {
    private val heightCalculator = TimelineHeightCalculator()

    data class Calculation(val frame: TimelineFrame, val ids: Map<String, Int>, val nextId: Int, val extents: List<Float>)

    /** Предыдущие блоки допустимы только после проверки политики providers и равенства содержимого. */
    fun calculate(
        timelineMath: TimelineMathEngine,
        timelineUi: TimelineUiRenderer,
        width: Int,
        direction: Boolean,
        previous: TimelineFrame?,
        measuredExtents: List<Float>,
        virtualIds: Map<String, Int>,
        nextVirtualId: Int,
        stepClickable: Boolean,
        progressClickable: Boolean,
        collectExtents: Boolean,
    ): Calculation {
        val resolved =
            if (previous != null) {
                timelineMath.setMeasuredWidth(width)
                timelineMath.setStepExtents(measuredExtents)
                (timelineMath as? TimelineMeasuredGeometry)?.setTextMetrics(previous.blocks.map { TimelineTextMetrics(it.titleHeight, it.descriptionHeight) })
                timelineMath.buildLayout()
            } else {
                TimelineLayoutResolver.resolve(timelineMath, timelineUi, width, 4f * context.resources.displayMetrics.density)
            }
        val completed = Path()
        val remaining = Path()
        timelineMath.buildPath(completed, remaining)
        val identities = resolved.steps.mapIndexed { index, step -> step.step.identity(index) }.toSet()
        val ids = virtualIds.filterKeys { it in identities }.toMutableMap()
        var nextId = nextVirtualId
        identities.forEach { ids.getOrPut(it) { nextId++ } }
        val blocks = TimelineTextBlockResolver.resolve(resolved, timelineMath, timelineUi, previous?.blocks.orEmpty())
        val bounds = heightCalculator.calculateBounds(resolved, timelineMath, timelineUi, blocks, listOf(completed, remaining))
        val math = timelineMath.getConfig()
        val start = timelineMath.getStartPosition()
        val targets = buildTargets(resolved, math, start, width, direction, bounds.topInset, ids, stepClickable, progressClickable)
        val frame = TimelineFrame(resolved, blocks, math, start, width, direction, bounds.topInset, bounds.height, completed, remaining, targets)
        return Calculation(frame, ids, nextId, if (collectExtents) rowExtents(timelineMath, resolved) else emptyList())
    }

    private fun buildTargets(
        current: TimelineLayout,
        config: com.dmitrypokrasov.timelineview.config.TimelineMathConfig,
        origin: Float,
        measuredWidth: Int,
        rtl: Boolean,
        topInset: Float,
        ids: Map<String, Int>,
        stepClickable: Boolean,
        progressClickable: Boolean,
    ): List<TimelineViewController.Target> {
        val minSize = 48f * context.resources.displayMetrics.density

        fun bounds(
            x: Float,
            y: Float,
            size: Float,
        ): RectF {
            val extra = (minSize - size).coerceAtLeast(0f) / 2f
            val left = if (rtl) measuredWidth - origin - x - size else origin + x
            return RectF(
                (left - extra).coerceAtLeast(0f),
                (y + topInset - extra).coerceAtLeast(0f),
                (left + size + extra).coerceAtMost(measuredWidth.toFloat()),
                y + topInset + size + extra,
            )
        }
        val result =
            current.steps.mapIndexed { index, step ->
                val title =
                    step.step.title?.toString()?.takeIf { it.isNotBlank() }
                        ?: context.getString(com.dmitrypokrasov.timelineview.R.string.timeline_step, index + 1)
                val description =
                    listOfNotNull(
                        title,
                        step.step.description?.toString()?.takeIf { it.isNotBlank() },
                        context.getString(com.dmitrypokrasov.timelineview.R.string.timeline_percent, step.step.progress),
                    ).joinToString(". ")
                TimelineViewController.Target(ids.getValue(step.step.identity(index)), bounds(step.iconX, step.iconY, config.sizes.sizeImageLvl), description, stepClickable)
            }.toMutableList()
        current.progressIcon?.let { progress ->
            result +=
                TimelineViewController.Target(
                    TimelineViewController.PROGRESS_ID, bounds(progress.left, progress.top, config.sizes.sizeIconProgress),
                    context.getString(com.dmitrypokrasov.timelineview.R.string.timeline_progress), progressClickable,
                )
        }
        return result.filter { !it.bounds.isEmpty }
    }

    private fun rowExtents(
        timelineMath: TimelineMathEngine,
        layout: TimelineLayout,
    ): List<Float> {
        val rows = timelineMath.getContentRows() ?: return emptyList()
        val starts = rows.indices.filter { it == 0 || rows[it] != rows[it - 1] }
        return starts.zipWithNext { first, second -> layout.steps[second].iconY - layout.steps[first].iconY }
    }
}
