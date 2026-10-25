package com.dmitrypokrasov.timelineview.math

import android.graphics.Paint
import android.graphics.Path
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.geometry.TimelineSnakeCoordinates
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.math.data.TimelineProgressIcon
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.model.indexOfStep
import com.dmitrypokrasov.timelineview.render.toAndroid
import com.dmitrypokrasov.timelineview.render.toGeometry

/** Serpentine rows whose paths, progress and badges share the same measured geometry. */
class SnakeTimelineMath(private var mathConfig: TimelineMathConfig) : TimelineMathEngine, TimelineTextBoundary {
    init {
        mathConfig = mathConfig.copy(steps = mathConfig.steps.toList())
    }

    private var cornerRadius = 0f
    private var geometry: List<TimelinePathGeometry>? = null

    override val hasRoundedGeometry: Boolean get() = true

    override fun getContentRows(): List<Int> = mathConfig.steps.indices.toList()

    override fun setCornerRadius(radius: Float) {
        require(radius.isFinite() && radius >= 0f)
        cornerRadius = radius
        geometry = null
        cachedCoordinates = null
    }

    private var width = 0
    private var origin = 0f
    private var rowOffsets = floatArrayOf()
    private var cachedCoordinates: TimelineSnakeCoordinates? = null
    private val coordinates get() = cachedCoordinates ?: TimelineSnakeCoordinates(mathConfig, width, rowOffsets).also { cachedCoordinates = it }

    override fun setConfig(config: TimelineMathConfig) {
        mathConfig = config.copy(steps = config.steps.toList())
        geometry = null
        cachedCoordinates = null
        rowOffsets = floatArrayOf()
    }

    override fun getConfig(): TimelineMathConfig = mathConfig

    override fun replaceSteps(steps: List<TimelineStepData>) {
        mathConfig = mathConfig.copy(steps = steps.toList())
        geometry = null
        cachedCoordinates = null
        rowOffsets = floatArrayOf()
    }

    override fun setStepExtents(extents: List<Float>) {
        require(extents.all { it.isFinite() && it >= 0f }) { "Row extents must be finite and non-negative" }
        geometry = null
        cachedCoordinates = null
        rowOffsets = FloatArray(extents.size + 1)
        extents.forEachIndexed { index, extent -> rowOffsets[index + 1] = rowOffsets[index] + extent }
    }

    override fun setMeasuredWidth(measuredWidth: Int) {
        geometry = null
        cachedCoordinates = null
        width = measuredWidth.coerceAtLeast(0)
        cachedCoordinates = null
        origin = coordinates.origin
    }

    override fun getStartPosition(): Float = origin

    override fun getSteps(): List<TimelineStepData> = mathConfig.steps

    private fun rowTop(index: Int): Float = coordinates.rowTop(index)

    private fun segment(index: Int): List<TimelinePoint> = coordinates.segment(index).map { TimelinePoint(it.x, it.y) }

    private fun segments(): List<TimelinePathGeometry> =
        geometry ?: mathConfig.steps.indices.map {
            TimelinePathGeometry(segment(it), cornerRadius)
        }.also { geometry = it }

    private fun progressPoint(index: Int): TimelinePoint = segments()[index].point(mathConfig.steps[index].progress / 100f)

    override fun buildPath(
        pathEnable: Path,
        pathDisable: Path,
    ) {
        drawTimelineSegments(segments(), mathConfig.steps, mathConfig.progressMode, pathEnable, pathDisable)
    }

    override fun getHorizontalIconOffset(i: Int): Float = progressPoint(i).x - mathConfig.sizes.sizeIconProgress / 2f

    override fun getVerticalOffset(i: Int): Float = progressPoint(i).y - mathConfig.sizes.sizeIconProgress / 2f

    override fun getLeftCoordinates(step: TimelineStepData): Float =
        getHorizontalIconOffset(mathConfig.steps.indexOfStep(step))

    override fun getTopCoordinates(step: TimelineStepData): Float =
        getVerticalOffset(mathConfig.steps.indexOfStep(step))

    override fun getIconXCoordinates(align: Paint.Align): Float = coordinates.iconX(align.toGeometry())

    override fun getTitleXCoordinates(align: Paint.Align): Float = coordinates.titleX(align.toGeometry())

    override fun getIconYCoordinates(i: Int): Float = coordinates.iconY(i)

    override fun getTitleYCoordinates(i: Int): Float = coordinates.titleY(i)

    override fun getTextTopBoundary(index: Int): Float = rowTop(index)

    override fun getDescriptionYCoordinates(i: Int): Float = coordinates.descriptionY(i)

    override fun buildLayout(): TimelineLayout {
        val steps = coordinates.layout().toAndroid().steps
        val active = mathConfig.steps.indexOfFirst { it.progress < 100 }.takeIf { it >= 0 }
        return TimelineLayout(
            steps,
            active?.let { TimelineProgressIcon(getHorizontalIconOffset(it), getVerticalOffset(it)) },
            active,
        )
    }
}
