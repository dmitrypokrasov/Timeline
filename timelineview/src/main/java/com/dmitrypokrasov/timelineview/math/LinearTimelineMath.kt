package com.dmitrypokrasov.timelineview.math

import android.graphics.Paint
import android.graphics.Path
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.geometry.TimelineLinearCoordinates
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.model.indexOfStep
import com.dmitrypokrasov.timelineview.render.toAndroid
import com.dmitrypokrasov.timelineview.render.toGeometry

/**
 * Совместимая оболочка Linear: координаты рассчитывает чистый helper, пути адаптируются через Android.
 */
class LinearTimelineMath(
    private var mathConfig: TimelineMathConfig,
    val orientation: Orientation = Orientation.VERTICAL,
) : TimelineMathEngine {
    init {
        mathConfig = mathConfig.copy(steps = mathConfig.steps.toList())
    }

    private data class SegmentInfo(
        val start: Float,
        val end: Float,
    ) {
        val length: Float get() = end - start
    }

    enum class Orientation { VERTICAL, HORIZONTAL }

    private var wrapped = wrappingEngine(mathConfig)
    private var cornerRadius = 0f

    private fun wrappingEngine(config: TimelineMathConfig): AdaptiveGridTimelineMath? =
        if (orientation == Orientation.HORIZONTAL && config.horizontalLayout == TimelineMathConfig.HorizontalLayout.WRAP) AdaptiveGridTimelineMath(config) else null

    override val textBelowBadge: Boolean get() = orientation == Orientation.HORIZONTAL
    override val hasRoundedGeometry: Boolean get() = wrapped != null

    override fun getContentRows(): List<Int>? = wrapped?.getContentRows() ?: if (orientation == Orientation.VERTICAL) mathConfig.steps.indices.toList() else null

    override fun setCornerRadius(radius: Float) {
        require(radius.isFinite() && radius >= 0f)
        cornerRadius = radius
        wrapped?.setCornerRadius(radius)
    }

    override fun getDesiredWidth(): Int =
        if (orientation == Orientation.HORIZONTAL && mathConfig.horizontalLayout == TimelineMathConfig.HorizontalLayout.SCROLL) {
            (mathConfig.steps.size * maxOf(mathConfig.minCellWidth, mathConfig.sizes.sizeImageLvl + 8f) + mathConfig.spacing.marginHorizontalStroke * 2).toInt()
        } else {
            0
        }

    private var rowOffsets = floatArrayOf()
    private var startPositionX = 0f
    private var measuredWidth = 0
    private var cachedSegments: List<SegmentInfo> = emptyList()
    private var cachedPaths: List<TimelinePathGeometry>? = null
    private var segmentsValid = false
    private var cachedCoordinates: TimelineLinearCoordinates? = null
    private val coordinates get() = cachedCoordinates ?: TimelineLinearCoordinates(mathConfig, orientation == Orientation.HORIZONTAL, measuredWidth, rowOffsets).also { cachedCoordinates = it }

    override fun setStepExtents(extents: List<Float>) {
        wrapped?.setStepExtents(extents)
        require(extents.all { it.isFinite() && it >= 0f }) { "Row extents must be finite and non-negative" }
        rowOffsets = FloatArray(extents.size + 1)
        extents.forEachIndexed { index, extent -> rowOffsets[index + 1] = rowOffsets[index] + extent }
        cachedCoordinates = null
        segmentsValid = false
    }

    override fun setConfig(config: TimelineMathConfig) {
        mathConfig = config.copy(steps = config.steps.toList())
        wrapped =
            wrappingEngine(mathConfig)?.also {
                it.setCornerRadius(cornerRadius)
                it.setMeasuredWidth(measuredWidth)
            }
        rowOffsets = floatArrayOf()
        cachedCoordinates = null
        segmentsValid = false
    }

    override fun getConfig(): TimelineMathConfig = mathConfig

    override fun replaceSteps(steps: List<TimelineStepData>) {
        mathConfig = mathConfig.copy(steps = steps.toList())
        wrapped?.replaceSteps(steps)
        rowOffsets = floatArrayOf()
        cachedCoordinates = null
        segmentsValid = false
    }

    override fun buildPath(
        pathEnable: Path,
        pathDisable: Path,
    ) {
        wrapped?.let {
            it.buildPath(pathEnable, pathDisable)
            return
        }
        val segments = getSegments()
        val paths =
            cachedPaths ?: segments.map { segment ->
                val crossAxis = getHorizontalBaseline()

                fun point(value: Float) = if (orientation == Orientation.VERTICAL) TimelinePoint(0f, value) else TimelinePoint(value, crossAxis)
                TimelinePathGeometry(listOf(point(segment.start), point(segment.end)), 0f)
            }.also { cachedPaths = it }
        drawTimelineSegments(paths, mathConfig.steps, mathConfig.progressMode, pathEnable, pathDisable)
    }

    override fun getStartPosition(): Float = wrapped?.getStartPosition() ?: startPositionX

    override fun setMeasuredWidth(measuredWidth: Int) {
        wrapped?.setMeasuredWidth(measuredWidth)
        this.measuredWidth = measuredWidth.coerceAtLeast(0)
        cachedCoordinates = null
        startPositionX = coordinates.startPositionX
        cachedCoordinates = null
        segmentsValid = false
    }

    override fun getHorizontalIconOffset(i: Int): Float {
        wrapped?.let { return it.getHorizontalIconOffset(i) }
        return if (orientation == Orientation.VERTICAL) {
            -mathConfig.sizes.sizeIconProgress / 2f
        } else {
            getProgressPosition(i) - mathConfig.sizes.sizeIconProgress / 2f
        }
    }

    override fun getVerticalOffset(i: Int): Float =
        wrapped?.getVerticalOffset(i)
            ?: if (orientation == Orientation.VERTICAL) {
                getProgressPosition(i) + mathConfig.spacing.marginTopProgressIcon -
                    mathConfig.sizes.sizeIconProgress / 2f
            } else {
                getHorizontalProgressTop()
            }

    override fun getSteps(): List<TimelineStepData> = mathConfig.steps

    override fun getLeftCoordinates(step: TimelineStepData): Float {
        wrapped?.let { return it.getLeftCoordinates(step) }
        return if (orientation == Orientation.VERTICAL) {
            -mathConfig.sizes.sizeIconProgress / 2f
        } else {
            val index = mathConfig.steps.indexOfStep(step)
            getProgressPosition(index) - mathConfig.sizes.sizeIconProgress / 2f
        }
    }

    override fun getTopCoordinates(step: TimelineStepData): Float {
        wrapped?.let { return it.getTopCoordinates(step) }
        return if (orientation == Orientation.VERTICAL) {
            val index = mathConfig.steps.indexOfStep(step)
            getProgressPosition(index) + mathConfig.spacing.marginTopProgressIcon -
                mathConfig.sizes.sizeIconProgress / 2f
        } else {
            getHorizontalProgressTop()
        }
    }

    override fun getTitleXCoordinates(align: Paint.Align): Float = wrapped?.getTitleXCoordinates(align) ?: coordinates.getTitleXCoordinates(align.toGeometry())

    override fun getIconXCoordinates(align: Paint.Align): Float = wrapped?.getIconXCoordinates(align) ?: coordinates.getIconXCoordinates()

    override fun getIconYCoordinates(i: Int): Float = wrapped?.getIconYCoordinates(i) ?: coordinates.getIconYCoordinates(i)

    override fun getTitleYCoordinates(i: Int): Float = wrapped?.getTitleYCoordinates(i) ?: coordinates.getTitleYCoordinates(i)

    override fun getDescriptionYCoordinates(i: Int): Float = getTitleYCoordinates(i) + mathConfig.spacing.marginTopDescription

    private fun getHorizontalProgressTop(): Float = coordinates.getHorizontalProgressTop()

    private fun getHorizontalBaseline(): Float = coordinates.getHorizontalBaseline()

    override fun buildLayout(): TimelineLayout = wrapped?.buildLayout() ?: coordinates.buildLayout().toAndroid()

    private fun buildSegments(): List<SegmentInfo> = coordinates.getSegments().map { SegmentInfo(it.start, it.end) }

    private fun getSegments(): List<SegmentInfo> {
        if (!segmentsValid) {
            cachedSegments = buildSegments()
            cachedPaths = null
            segmentsValid = true
        }
        return cachedSegments
    }

    private fun getProgressPosition(index: Int): Float = coordinates.getProgressPosition(index)
}
