package com.dmitrypokrasov.timelineview.math

import android.graphics.Paint
import android.graphics.Path
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.math.data.TimelineLayoutStep
import com.dmitrypokrasov.timelineview.math.data.TimelineProgressIcon
import com.dmitrypokrasov.timelineview.model.TimelineStepData

/**
 * Simple [TimelineMathEngine] that arranges steps on a straight vertical or horizontal line.
 */
class LinearTimelineMath(
    private var mathConfig: TimelineMathConfig,
    val orientation: Orientation = Orientation.VERTICAL,
) : TimelineMathEngine {
    private data class SegmentInfo(
        val start: Float,
        val end: Float,
    ) {
        val length: Float get() = end - start
    }

    enum class Orientation { VERTICAL, HORIZONTAL }

    private var startPositionX = 0f
    private var measuredWidth = 0
    private var cachedSegments: List<SegmentInfo> = emptyList()
    private var segmentsValid = false

    override fun setConfig(config: TimelineMathConfig) {
        mathConfig = config
        segmentsValid = false
    }

    override fun getConfig(): TimelineMathConfig = mathConfig

    override fun replaceSteps(steps: List<TimelineStepData>) {
        mathConfig = mathConfig.copy(steps = steps)
        segmentsValid = false
    }

    override fun buildPath(
        pathEnable: Path,
        pathDisable: Path,
    ) {
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

    override fun getStartPosition(): Float = startPositionX

    override fun setMeasuredWidth(measuredWidth: Int) {
        this.measuredWidth = measuredWidth
        startPositionX =
            if (orientation == Orientation.VERTICAL) {
                when (mathConfig.startPosition) {
                    TimelineMathConfig.StartPosition.START -> mathConfig.spacing.marginHorizontalStroke
                    TimelineMathConfig.StartPosition.CENTER -> measuredWidth / 2f
                    TimelineMathConfig.StartPosition.END -> measuredWidth.toFloat() - mathConfig.spacing.marginHorizontalStroke
                }
            } else {
                val totalLength = getTotalLength()
                when (mathConfig.startPosition) {
                    TimelineMathConfig.StartPosition.START -> mathConfig.spacing.marginHorizontalStroke
                    TimelineMathConfig.StartPosition.CENTER -> (measuredWidth - totalLength) / 2f
                    TimelineMathConfig.StartPosition.END ->
                        measuredWidth.toFloat() - totalLength - mathConfig.spacing.marginHorizontalStroke
                }
            }
        segmentsValid = false
    }

    override fun getHorizontalIconOffset(i: Int): Float {
        return if (orientation == Orientation.VERTICAL) {
            -mathConfig.sizes.sizeIconProgress / 2f
        } else {
            getProgressPosition(i) - mathConfig.sizes.sizeIconProgress / 2f
        }
    }

    override fun getVerticalOffset(i: Int): Float =
        if (orientation == Orientation.VERTICAL) {
            getProgressPosition(i) + mathConfig.spacing.marginTopProgressIcon -
                mathConfig.sizes.sizeIconProgress / 2f
        } else {
            getHorizontalProgressTop()
        }

    override fun getSteps(): List<TimelineStepData> = mathConfig.steps

    override fun getLeftCoordinates(step: TimelineStepData): Float {
        return if (orientation == Orientation.VERTICAL) {
            -mathConfig.sizes.sizeIconProgress / 2f
        } else {
            val index = mathConfig.steps.indexOf(step).coerceAtLeast(0)
            getProgressPosition(index) - mathConfig.sizes.sizeIconProgress / 2f
        }
    }

    override fun getTopCoordinates(step: TimelineStepData): Float {
        return if (orientation == Orientation.VERTICAL) {
            val index = mathConfig.steps.indexOf(step).coerceAtLeast(0)
            getProgressPosition(index) + mathConfig.spacing.marginTopProgressIcon -
                mathConfig.sizes.sizeIconProgress / 2f
        } else {
            getHorizontalProgressTop()
        }
    }

    override fun getTitleXCoordinates(align: Paint.Align): Float {
        val stepX = getStepX()
        return when (align) {
            Paint.Align.LEFT -> -(startPositionX - mathConfig.spacing.marginHorizontalText)
            Paint.Align.CENTER -> startPositionX
            Paint.Align.RIGHT ->
                if (mathConfig.startPosition == TimelineMathConfig.StartPosition.CENTER) {
                    startPositionX - mathConfig.spacing.marginHorizontalText
                } else {
                    -startPositionX + stepX
                }
        }
    }

    override fun getIconXCoordinates(align: Paint.Align): Float {
        val stepX = getStepX()
        return when (align) {
            Paint.Align.LEFT -> -(startPositionX - mathConfig.spacing.marginHorizontalImage)
            Paint.Align.CENTER -> startPositionX
            Paint.Align.RIGHT ->
                if (mathConfig.startPosition == TimelineMathConfig.StartPosition.CENTER) {
                    startPositionX - mathConfig.spacing.marginHorizontalImage - mathConfig.sizes.sizeImageLvl
                } else {
                    -startPositionX + stepX + mathConfig.spacing.marginHorizontalImage
                }
        }
    }

    override fun getIconYCoordinates(i: Int): Float {
        return if (orientation == Orientation.VERTICAL) {
            getStepPosition(i) - mathConfig.sizes.sizeImageLvl / 2f
        } else {
            getHorizontalBaseline() - mathConfig.sizes.sizeImageLvl / 2f
        }
    }

    override fun getTitleYCoordinates(i: Int): Float =
        if (orientation == Orientation.VERTICAL) {
            getStepPosition(i) - getVerticalContentInset() + mathConfig.spacing.marginTopTitle
        } else {
            getHorizontalBaseline() + mathConfig.spacing.marginTopTitle
        }

    override fun getDescriptionYCoordinates(i: Int): Float =
        getTitleYCoordinates(i) + mathConfig.spacing.marginTopDescription

    private fun getStepX(): Float = measuredWidth - mathConfig.spacing.marginHorizontalStroke * 2

    private fun getVerticalContentInset(): Float =
        maxOf(mathConfig.sizes.sizeImageLvl, mathConfig.sizes.sizeIconProgress) / 2f

    private fun getBadgeCenterPosition(index: Int): Float {
        return if (orientation == Orientation.VERTICAL) {
            getVerticalContentInset() + mathConfig.spacing.stepYFirst + mathConfig.spacing.stepY * index
        } else {
            mathConfig.spacing.stepYFirst + mathConfig.spacing.stepY * index
        }
    }

    private fun getTotalLength(): Float =
        if (mathConfig.steps.isEmpty()) 0f else getBadgeCenterPosition(mathConfig.steps.lastIndex)

    private fun getHorizontalProgressTop(): Float =
        getHorizontalBaseline() - mathConfig.sizes.sizeIconProgress / 2f

    private fun getHorizontalBaseline(): Float =
        maxOf(mathConfig.sizes.sizeImageLvl, mathConfig.sizes.sizeIconProgress) / 2f

    override fun buildLayout(): TimelineLayout {
        val layoutSteps =
            if (orientation == Orientation.HORIZONTAL) {
                mathConfig.steps.mapIndexed { index, step ->
                    val positionX = getBadgeCenterPosition(index)
                    val baseline = getHorizontalBaseline()
                    val titleWidth =
                        TimelineTextWidthResolver.resolve(
                            measuredWidth = measuredWidth,
                            startPosition = startPositionX,
                            localX = positionX,
                            align = Paint.Align.CENTER,
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
        var previous = getPathStart()
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

    private fun getPathStart(): Float =
        if (orientation == Orientation.VERTICAL) 0f else -getHorizontalLeadIn()

    private fun getSegmentEndPosition(index: Int): Float {
        val badgeCenter = getBadgeCenterPosition(index)
        return if (orientation == Orientation.HORIZONTAL && index == mathConfig.steps.lastIndex) {
            badgeCenter - getHorizontalTerminalInset()
        } else {
            badgeCenter
        }
    }

    private fun getHorizontalLeadIn(): Float =
        (startPositionX + mathConfig.sizes.sizeImageLvl / 2f).coerceAtLeast(
            mathConfig.sizes.sizeImageLvl / 2f,
        )

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
