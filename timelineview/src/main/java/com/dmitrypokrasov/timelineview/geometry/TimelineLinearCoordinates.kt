package com.dmitrypokrasov.timelineview.geometry

import com.dmitrypokrasov.timelineview.config.TimelineMathConfig

/** Чистые координаты, строки и сегменты Linear; native splitting остаётся в Android-оболочке. */
internal class TimelineLinearCoordinates(
    private val mathConfig: TimelineMathConfig,
    horizontal: Boolean,
    private val measuredWidth: Int,
    private val rowOffsets: FloatArray,
) {
    private enum class Orientation { VERTICAL, HORIZONTAL }

    private val orientation = if (horizontal) Orientation.HORIZONTAL else Orientation.VERTICAL
    val startPositionX: Float =
        if (!horizontal) {
            when (mathConfig.startPosition) {
                TimelineMathConfig.StartPosition.START -> mathConfig.spacing.marginHorizontalStroke.coerceAtMost(measuredWidth / 2f)
                TimelineMathConfig.StartPosition.CENTER -> measuredWidth / 2f
                TimelineMathConfig.StartPosition.END -> measuredWidth - mathConfig.spacing.marginHorizontalStroke.coerceAtMost(measuredWidth / 2f)
            }
        } else {
            mathConfig.spacing.marginHorizontalStroke.coerceAtMost(measuredWidth / 2f)
        }

    data class SegmentInfo(val start: Float, val end: Float) {
        val length: Float get() = end - start
    }

    private val cachedSegments by lazy { buildSegments() }

    fun getSegments(): List<SegmentInfo> = cachedSegments

    private fun stepDistance(index: Int): Float =
        rowOffsets.getOrNull(index) ?: (mathConfig.spacing.stepY * index)

    fun getTitleXCoordinates(align: TimelineTextAlignment): Float {
        val inset = maxOf(mathConfig.sizes.sizeImageLvl / 2f + 4f, mathConfig.spacing.marginHorizontalText - mathConfig.spacing.marginHorizontalStroke)
        return when (align) {
            TimelineTextAlignment.LEFT -> inset
            TimelineTextAlignment.CENTER -> 0f
            TimelineTextAlignment.RIGHT -> -inset
        }
    }

    fun getIconXCoordinates(): Float {
        return -mathConfig.sizes.sizeImageLvl / 2f
    }

    fun getIconYCoordinates(i: Int): Float {
        return if (orientation == Orientation.VERTICAL) {
            getStepPosition(i) - mathConfig.sizes.sizeImageLvl / 2f
        } else {
            getHorizontalBaseline() - mathConfig.sizes.sizeImageLvl / 2f
        }
    }

    fun getTitleYCoordinates(i: Int): Float =
        if (orientation == Orientation.VERTICAL) {
            getStepPosition(i) - getVerticalContentInset() + mathConfig.spacing.marginTopTitle
        } else {
            getHorizontalBaseline() + mathConfig.spacing.marginTopTitle
        }

    fun getDescriptionYCoordinates(i: Int): Float =
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

    fun getHorizontalProgressTop(): Float =
        getHorizontalBaseline() - mathConfig.sizes.sizeIconProgress / 2f

    fun getHorizontalBaseline(): Float =
        maxOf(mathConfig.sizes.sizeImageLvl, mathConfig.sizes.sizeIconProgress) / 2f

    /** Строки и progress marker используют одни сегменты, включая видимый маркер при 0%. */
    fun buildLayout(): TimelineGeometryLayout {
        val active = mathConfig.steps.indexOfFirst { it.progress != 100 }.takeIf { it >= 0 }
        val steps = if (orientation == Orientation.HORIZONTAL) horizontalSteps() else verticalSteps()
        return TimelineGeometryLayout(steps, active?.let(::progressIcon), active)
    }

    private fun horizontalSteps(): List<TimelineGeometryStep> =
        mathConfig.steps.mapIndexed { index, step ->
            val x = getBadgeCenterPosition(index)
            val baseline = getHorizontalBaseline()
            val width =
                minOf(
                    (horizontalCellWidth() - 8f).toInt().coerceAtLeast(1),
                    TimelineGeometryTextWidth.resolve(measuredWidth, startPositionX, x, TimelineTextAlignment.CENTER),
                )
            TimelineGeometryStep(
                step, x, baseline + mathConfig.spacing.marginTopTitle, width,
                x, baseline + mathConfig.spacing.marginTopTitle + mathConfig.spacing.marginTopDescription, width,
                x - mathConfig.sizes.sizeImageLvl / 2f, baseline - mathConfig.sizes.sizeImageLvl / 2f, TimelineTextAlignment.CENTER,
            )
        }

    private fun verticalSteps(): List<TimelineGeometryStep> {
        val align = if (mathConfig.startPosition == TimelineMathConfig.StartPosition.START) TimelineTextAlignment.LEFT else TimelineTextAlignment.RIGHT
        val x = getTitleXCoordinates(align)
        val width = TimelineGeometryTextWidth.resolve(measuredWidth, startPositionX, x, align)
        return mathConfig.steps.mapIndexed { index, step ->
            TimelineGeometryStep(
                step, x, getTitleYCoordinates(index), width, x, getDescriptionYCoordinates(index), width,
                getIconXCoordinates(), getIconYCoordinates(index), align,
            )
        }
    }

    private fun progressIcon(index: Int): TimelineGeometryProgressIcon {
        val position = getProgressPosition(index)
        return if (orientation == Orientation.VERTICAL) {
            TimelineGeometryProgressIcon(
                -mathConfig.sizes.sizeIconProgress / 2f,
                position + mathConfig.spacing.marginTopProgressIcon - mathConfig.sizes.sizeIconProgress / 2f,
            )
        } else {
            TimelineGeometryProgressIcon(position - mathConfig.sizes.sizeIconProgress / 2f, getHorizontalProgressTop())
        }
    }

    /** Последний горизонтальный сегмент заканчивается перед центром badge, сохраняя старую длину. */
    private fun buildSegments(): List<SegmentInfo> {
        var previous = 0f
        return mathConfig.steps.indices.map { index ->
            val anchor = getSegmentEndPosition(index)
            SegmentInfo(start = previous, end = anchor).also {
                previous = anchor
            }
        }
    }

    private fun getStepPosition(index: Int): Float =
        mathConfig.steps.getOrNull(index)?.let { getBadgeCenterPosition(index) }
            ?: if (orientation == Orientation.VERTICAL) {
                getVerticalContentInset()
            } else {
                0f
            }

    fun getProgressPosition(index: Int): Float {
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
}
