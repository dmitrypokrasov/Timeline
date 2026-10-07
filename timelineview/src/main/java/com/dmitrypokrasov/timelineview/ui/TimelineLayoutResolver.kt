package com.dmitrypokrasov.timelineview.ui

import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer

/** Measures row contents before asking the engine to rebuild all geometry together. */
internal object TimelineLayoutResolver {
    fun resolve(
        engine: TimelineMathEngine,
        renderer: TimelineUiRenderer,
        width: Int,
        gap: Float,
    ): TimelineLayout {
        engine.setStepExtents(emptyList())
        engine.setMeasuredWidth(width)
        val initial = engine.buildLayout()
        val rows = engine.getContentRows() ?: return initial
        require((rows.isEmpty() || rows.first() == 0) && rows.size == initial.steps.size && rows.zipWithNext().all { (a, b) -> b == a || b == a + 1 }) { "Content rows must be contiguous and ordered" }
        val blocks = TimelineTextBlockResolver.resolve(initial, engine, renderer)
        val size = engine.getConfig().sizes.sizeImageLvl
        val bounds =
            initial.steps.mapIndexed { index, step ->
                val block = blocks[index]
                val inset = size * ((step.step.badgeAnimation?.scale ?: 1f).coerceAtLeast(1f) - 1f) / 2f
                val tops = mutableListOf(step.iconY - inset)
                val bottoms = mutableListOf(step.iconY + size + inset)
                if (block.titleHeight > 0) {
                    tops += block.titleTop
                    bottoms += block.titleTop + block.titleHeight
                }
                if (block.descriptionHeight > 0) {
                    tops += block.descriptionTop
                    bottoms += block.descriptionTop + block.descriptionHeight
                }
                tops.min() to bottoms.max()
            }
        val groups = bounds.indices.groupBy { rows[it] }.values.toList()
        val rowBounds = groups.map { indices -> indices.minOf { bounds[it].first } to indices.maxOf { bounds[it].second } }
        val extents =
            rowBounds.zipWithNext().mapIndexed { index, (previous, next) ->
                val distance = initial.steps[groups[index + 1].first()].iconY - initial.steps[groups[index].first()].iconY
                distance + (previous.second + gap - next.first).coerceAtLeast(0f)
            }
        engine.setStepExtents(extents)
        return engine.buildLayout()
    }
}
