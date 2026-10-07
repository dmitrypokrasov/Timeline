package com.example.migration

import android.graphics.Paint
import android.graphics.Path
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.math.TimelineMathAdapter
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.math.data.TimelineLayoutStep

/** A minimal label-and-badge column. Paths are intentionally absent in this style. */
class CompactCustomMath(config: TimelineMathConfig) : TimelineMathAdapter(config) {
    private var offsets = emptyList<Float>()
    override fun getContentRows(): List<Int> = input.steps.indices.toList()
    override fun onGeometryChanged() { offsets = emptyList() }
    override fun setStepExtents(extents: List<Float>) {
        require(extents.all { it.isFinite() && it >= 0f })
        offsets = extents.runningFold(0f) { sum, extent -> sum + extent }
    }
    override fun buildPath(pathEnable: Path, pathDisable: Path) { pathEnable.reset(); pathDisable.reset() }
    override fun buildLayout(): TimelineLayout {
        val size = input.sizes.sizeImageLvl
        val x = size + 8f
        val available = (width - x).toInt().coerceAtLeast(1)
        return TimelineLayout(input.steps.mapIndexed { index, step ->
            val y = offsets.getOrNull(index) ?: index * input.spacing.stepY
            TimelineLayoutStep(step, x, y + 18f, available, x, y + 38f, available, 0f, y, Paint.Align.LEFT)
        }, null, null)
    }
}
