package com.dmitrypokrasov.timelineview.math

import android.graphics.Paint
import android.graphics.Path
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.math.data.TimelineLayoutStep
import com.dmitrypokrasov.timelineview.math.data.TimelineProgressIcon
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.model.indexOfStep

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
    }

    private var width = 0
    private var origin = 0f
    private var rowOffsets = floatArrayOf()

    override fun setConfig(config: TimelineMathConfig) {
        mathConfig = config.copy(steps = config.steps.toList())
        geometry = null
        rowOffsets = floatArrayOf()
    }

    override fun getConfig(): TimelineMathConfig = mathConfig

    override fun replaceSteps(steps: List<TimelineStepData>) {
        mathConfig = mathConfig.copy(steps = steps.toList())
        geometry = null
        rowOffsets = floatArrayOf()
    }

    override fun setStepExtents(extents: List<Float>) {
        require(extents.all { it.isFinite() && it >= 0f }) { "Row extents must be finite and non-negative" }
        geometry = null
        rowOffsets = FloatArray(extents.size + 1)
        extents.forEachIndexed { index, extent -> rowOffsets[index + 1] = rowOffsets[index] + extent }
    }

    override fun setMeasuredWidth(measuredWidth: Int) {
        geometry = null
        width = measuredWidth.coerceAtLeast(0)
        origin =
            when (mathConfig.startPosition) {
                TimelineMathConfig.StartPosition.START -> edge()
                TimelineMathConfig.StartPosition.CENTER -> width / 2f
                TimelineMathConfig.StartPosition.END -> width - edge()
            }
    }

    override fun getStartPosition(): Float = origin

    override fun getSteps(): List<TimelineStepData> = mathConfig.steps

    private fun edge(): Float = mathConfig.spacing.marginHorizontalStroke.coerceAtMost(width / 2f)

    private fun rowTop(index: Int): Float =
        mathConfig.spacing.stepYFirst +
            (rowOffsets.getOrNull(index) ?: (mathConfig.spacing.stepY * index))

    private fun anchor(index: Int): TimelinePoint =
        TimelinePoint(
            (if (index % 2 == 0) edge() else width - edge()) - origin,
            rowTop(index) + mathConfig.spacing.stepY / 2f,
        )

    private fun segment(index: Int): List<TimelinePoint> {
        val end = anchor(index)
        val start = if (index == 0) TimelinePoint(0f, 0f) else anchor(index - 1)
        return listOf(start, TimelinePoint(start.x, rowTop(index)), TimelinePoint(end.x, rowTop(index)), end)
    }

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

    override fun getIconXCoordinates(align: Paint.Align): Float =
        when (align) {
            Paint.Align.LEFT -> mathConfig.spacing.marginHorizontalImage - origin
            Paint.Align.RIGHT -> width - mathConfig.spacing.marginHorizontalImage - mathConfig.sizes.sizeImageLvl - origin
            Paint.Align.CENTER -> width / 2f - mathConfig.sizes.sizeImageLvl / 2f - origin
        }

    override fun getTitleXCoordinates(align: Paint.Align): Float =
        when (align) {
            Paint.Align.LEFT -> mathConfig.spacing.marginHorizontalText.coerceAtMost(width.toFloat()) - origin
            Paint.Align.RIGHT -> width - mathConfig.spacing.marginHorizontalText.coerceAtMost(width.toFloat()) - origin
            Paint.Align.CENTER -> width / 2f - origin
        }

    override fun getIconYCoordinates(i: Int): Float = anchor(i).y - mathConfig.sizes.sizeImageLvl / 2f

    override fun getTitleYCoordinates(i: Int): Float = rowTop(i) + mathConfig.spacing.marginTopTitle

    override fun getTextTopBoundary(index: Int): Float = rowTop(index)

    override fun getDescriptionYCoordinates(i: Int): Float = getTitleYCoordinates(i) + mathConfig.spacing.marginTopDescription

    override fun buildLayout(): TimelineLayout {
        val steps =
            mathConfig.steps.mapIndexed { index, step ->
                val align = if (index % 2 == 0) Paint.Align.LEFT else Paint.Align.RIGHT
                val x = getTitleXCoordinates(align)
                val textWidth = TimelineTextWidthResolver.resolve(width, origin, x, align)
                TimelineLayoutStep(
                    step, x, getTitleYCoordinates(index), textWidth,
                    x, getDescriptionYCoordinates(index), textWidth,
                    getIconXCoordinates(align), getIconYCoordinates(index), align,
                )
            }
        val active = mathConfig.steps.indexOfFirst { it.progress < 100 }.takeIf { it >= 0 }
        return TimelineLayout(
            steps,
            active?.let { TimelineProgressIcon(getHorizontalIconOffset(it), getVerticalOffset(it)) },
            active,
        )
    }
}
