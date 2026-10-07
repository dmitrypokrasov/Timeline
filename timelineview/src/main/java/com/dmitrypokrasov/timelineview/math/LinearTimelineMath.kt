package com.dmitrypokrasov.timelineview.math

import android.graphics.Paint
import android.graphics.Path
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.math.data.TimelineLayoutStep
import com.dmitrypokrasov.timelineview.math.data.TimelineProgressIcon
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.model.indexOfStep

/**
 * Simple [TimelineMathEngine] that arranges steps on a straight vertical or horizontal line.
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

    override fun setStepExtents(extents: List<Float>) {
        wrapped?.setStepExtents(extents)
        require(extents.all { it.isFinite() && it >= 0f }) { "Row extents must be finite and non-negative" }
        rowOffsets = FloatArray(extents.size + 1)
        extents.forEachIndexed { index, extent -> rowOffsets[index + 1] = rowOffsets[index] + extent }
        segmentsValid = false
    }

    private fun stepDistance(index: Int): Float =
        rowOffsets.getOrNull(index) ?: (mathConfig.spacing.stepY * index)

    private var startPositionX = 0f
    private var measuredWidth = 0
    private var cachedSegments: List<SegmentInfo> = emptyList()
    private var segmentsValid = false

    override fun setConfig(config: TimelineMathConfig) {
        mathConfig = config.copy(steps = config.steps.toList())
        wrapped =
            wrappingEngine(mathConfig)?.also {
                it.setCornerRadius(cornerRadius)
                it.setMeasuredWidth(measuredWidth)
            }
        rowOffsets = floatArrayOf()
        segmentsValid = false
    }

    override fun getConfig(): TimelineMathConfig = mathConfig

    override fun replaceSteps(steps: List<TimelineStepData>) {
        mathConfig = mathConfig.copy(steps = steps.toList())
        wrapped?.replaceSteps(steps)
        rowOffsets = floatArrayOf()
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
        pathEnable.reset()
        pathDisable.reset()

        val segments = getSegments()
        val crossAxis = if (orientation == Orientation.VERTICAL) 0f else getHorizontalBaseline()
        val pathStart = segments.firstOrNull()?.start ?: 0f
        pathEnable.moveTo(
            if (orientation == Orientation.VERTICAL) 0f else pathStart,
            if (orientation == Orientation.VERTICAL) pathStart else crossAxis,
        )
        pathDisable.moveTo(
            if (orientation == Orientation.VERTICAL) 0f else pathStart,
            if (orientation == Orientation.VERTICAL) pathStart else crossAxis,
        )

        if (mathConfig.progressMode == TimelineMathConfig.ProgressMode.INDEPENDENT) {
            segments.forEachIndexed { index, segment ->
                val split = segment.start + segment.length * mathConfig.steps[index].progress / 100f
                if (split > segment.start) {
                    moveTo(pathEnable, segment.start, crossAxis)
                    lineTo(pathEnable, split, crossAxis)
                }
                if (split < segment.end) {
                    moveTo(pathDisable, split, crossAxis)
                    lineTo(pathDisable, segment.end, crossAxis)
                }
            }
            return
        }
        var drawEnable = true
        segments.forEachIndexed { index, segment ->
            val progressPosition = segment.start + segment.length * mathConfig.steps[index].progress / 100f
            if (drawEnable) {
                lineTo(pathEnable, progressPosition, crossAxis)
                if (progressPosition < segment.end) {
                    moveTo(pathDisable, progressPosition, crossAxis)
                    lineTo(pathDisable, segment.end, crossAxis)
                    drawEnable = false
                }
            } else {
                lineTo(pathDisable, segment.end, crossAxis)
            }
        }
    }

    override fun getStartPosition(): Float = wrapped?.getStartPosition() ?: startPositionX

    override fun setMeasuredWidth(measuredWidth: Int) {
        wrapped?.setMeasuredWidth(measuredWidth)
        this.measuredWidth = measuredWidth.coerceAtLeast(0)
        startPositionX =
            if (orientation == Orientation.VERTICAL) {
                when (mathConfig.startPosition) {
                    TimelineMathConfig.StartPosition.START -> mathConfig.spacing.marginHorizontalStroke.coerceAtMost(this.measuredWidth / 2f)
                    TimelineMathConfig.StartPosition.CENTER -> this.measuredWidth / 2f
                    TimelineMathConfig.StartPosition.END -> this.measuredWidth - mathConfig.spacing.marginHorizontalStroke.coerceAtMost(this.measuredWidth / 2f)
                }
            } else {
                mathConfig.spacing.marginHorizontalStroke.coerceAtMost(this.measuredWidth / 2f)
            }
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

    override fun getTitleXCoordinates(align: Paint.Align): Float {
        wrapped?.let { return it.getTitleXCoordinates(align) }
        val inset = maxOf(mathConfig.sizes.sizeImageLvl / 2f + 4f, mathConfig.spacing.marginHorizontalText - mathConfig.spacing.marginHorizontalStroke)
        return when (align) {
            Paint.Align.LEFT -> inset
            Paint.Align.CENTER -> 0f
            Paint.Align.RIGHT -> -inset
        }
    }

    override fun getIconXCoordinates(align: Paint.Align): Float {
        wrapped?.let { return it.getIconXCoordinates(align) }
        return -mathConfig.sizes.sizeImageLvl / 2f
    }

    override fun getIconYCoordinates(i: Int): Float {
        wrapped?.let { return it.getIconYCoordinates(i) }
        return if (orientation == Orientation.VERTICAL) {
            getStepPosition(i) - mathConfig.sizes.sizeImageLvl / 2f
        } else {
            getHorizontalBaseline() - mathConfig.sizes.sizeImageLvl / 2f
        }
    }

    override fun getTitleYCoordinates(i: Int): Float =
        wrapped?.getTitleYCoordinates(i)
            ?: if (orientation == Orientation.VERTICAL) {
                getStepPosition(i) - getVerticalContentInset() + mathConfig.spacing.marginTopTitle
            } else {
                getHorizontalBaseline() + mathConfig.spacing.marginTopTitle
            }

    override fun getDescriptionYCoordinates(i: Int): Float =
        getTitleYCoordinates(i) + mathConfig.spacing.marginTopDescription

    private fun getVerticalContentInset(): Float =
        maxOf(mathConfig.sizes.sizeImageLvl, mathConfig.sizes.sizeIconProgress) / 2f

    private fun getBadgeCenterPosition(index: Int): Float {
        return if (orientation == Orientation.VERTICAL) {
            getVerticalContentInset() + mathConfig.spacing.stepYFirst + stepDistance(index)
        } else {
            horizontalCellWidth() * (index + 0.5f)
        }
    }

    private fun horizontalCellWidth(): Float =
        ((measuredWidth - 2f * startPositionX).coerceAtLeast(1f) / mathConfig.steps.size.coerceAtLeast(1))

    private fun getHorizontalProgressTop(): Float =
        getHorizontalBaseline() - mathConfig.sizes.sizeIconProgress / 2f

    private fun getHorizontalBaseline(): Float =
        maxOf(mathConfig.sizes.sizeImageLvl, mathConfig.sizes.sizeIconProgress) / 2f

    override fun buildLayout(): TimelineLayout {
        wrapped?.let { return it.buildLayout() }
        val layoutSteps =
            if (orientation == Orientation.HORIZONTAL) {
                mathConfig.steps.mapIndexed { index, step ->
                    val positionX = getBadgeCenterPosition(index)
                    val baseline = getHorizontalBaseline()
                    val titleWidth =
                        minOf(
                            (horizontalCellWidth() - 8f).toInt().coerceAtLeast(1),
                            TimelineTextWidthResolver.resolve(
                                measuredWidth = measuredWidth,
                                startPosition = startPositionX,
                                localX = positionX,
                                align = Paint.Align.CENTER,
                            ),
                        )
                    TimelineLayoutStep(
                        step = step,
                        titleX = positionX,
                        titleY = baseline + mathConfig.spacing.marginTopTitle,
                        titleWidth = titleWidth,
                        descriptionX = positionX,
                        descriptionY =
                            baseline + mathConfig.spacing.marginTopTitle +
                                mathConfig.spacing.marginTopDescription,
                        descriptionWidth = titleWidth,
                        iconX = positionX - mathConfig.sizes.sizeImageLvl / 2f,
                        iconY = baseline - mathConfig.sizes.sizeImageLvl / 2f,
                        textAlign = Paint.Align.CENTER,
                    )
                }
            } else {
                val align =
                    when (mathConfig.startPosition) {
                        TimelineMathConfig.StartPosition.START -> Paint.Align.LEFT
                        else -> Paint.Align.RIGHT
                    }
                val titleX = getTitleXCoordinates(align)
                val descriptionX = getTitleXCoordinates(align)
                val titleWidth =
                    TimelineTextWidthResolver.resolve(
                        measuredWidth,
                        startPositionX,
                        titleX,
                        align,
                    )
                val descriptionWidth =
                    TimelineTextWidthResolver.resolve(
                        measuredWidth,
                        startPositionX,
                        descriptionX,
                        align,
                    )

                mathConfig.steps.mapIndexed { index, step ->
                    TimelineLayoutStep(
                        step = step,
                        titleX = titleX,
                        titleY = getTitleYCoordinates(index),
                        titleWidth = titleWidth,
                        descriptionX = descriptionX,
                        descriptionY = getDescriptionYCoordinates(index),
                        descriptionWidth = descriptionWidth,
                        iconX = getIconXCoordinates(align),
                        iconY = getIconYCoordinates(index),
                        textAlign = align,
                    )
                }
            }

        val progressIndex = mathConfig.steps.indexOfFirst { it.progress != 100 }.takeIf { it >= 0 }
        val progressIcon =
            progressIndex?.let { index ->
                val progressPosition = getProgressPosition(index)
                if (orientation == Orientation.VERTICAL) {
                    TimelineProgressIcon(
                        left = -mathConfig.sizes.sizeIconProgress / 2f,
                        top =
                            progressPosition + mathConfig.spacing.marginTopProgressIcon -
                                mathConfig.sizes.sizeIconProgress / 2f,
                    )
                } else {
                    TimelineProgressIcon(
                        left = progressPosition - mathConfig.sizes.sizeIconProgress / 2f,
                        top = getHorizontalProgressTop(),
                    )
                }
            }

        return TimelineLayout(
            steps = layoutSteps,
            progressIcon = progressIcon,
            progressStepIndex = progressIndex,
        )
    }

    private fun buildSegments(): List<SegmentInfo> {
        var previous = 0f
        return mathConfig.steps.indices.map { index ->
            val anchor = getSegmentEndPosition(index)
            SegmentInfo(start = previous, end = anchor).also {
                previous = anchor
            }
        }
    }

    private fun getSegments(): List<SegmentInfo> {
        if (!segmentsValid) {
            cachedSegments = buildSegments()
            segmentsValid = true
        }
        return cachedSegments
    }

    private fun getStepPosition(index: Int): Float =
        mathConfig.steps.getOrNull(index)?.let { getBadgeCenterPosition(index) }
            ?: if (orientation == Orientation.VERTICAL) {
                getVerticalContentInset()
            } else {
                0f
            }

    private fun getProgressPosition(index: Int): Float {
        val segment = getSegments().getOrNull(index) ?: return 0f
        val progress = segment.length * mathConfig.steps[index].progress / 100f
        return segment.start + progress
    }

    private fun getSegmentEndPosition(index: Int): Float {
        val badgeCenter = getBadgeCenterPosition(index)
        return if (orientation == Orientation.HORIZONTAL && index == mathConfig.steps.lastIndex) {
            (badgeCenter - getHorizontalTerminalInset()).coerceAtLeast(0f)
        } else {
            badgeCenter
        }
    }

    private fun getHorizontalTerminalInset(): Float =
        (mathConfig.sizes.sizeImageLvl / 2f - 2f).coerceAtLeast(0f)

    private fun moveTo(
        path: Path,
        value: Float,
        crossAxis: Float,
    ) {
        if (orientation == Orientation.VERTICAL) {
            path.moveTo(0f, value)
        } else {
            path.moveTo(value, crossAxis)
        }
    }

    private fun lineTo(
        path: Path,
        value: Float,
        crossAxis: Float,
    ) {
        if (orientation == Orientation.VERTICAL) {
            path.lineTo(0f, value)
        } else {
            path.lineTo(value, crossAxis)
        }
    }
}
