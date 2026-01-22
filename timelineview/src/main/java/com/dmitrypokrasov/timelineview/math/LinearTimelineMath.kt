package com.dmitrypokrasov.timelineview.math

import android.graphics.Paint
import android.graphics.Path
import com.dmitrypokrasov.timelineview.model.TimelineStep
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig

/**
 * Простая реализация [TimelineMathEngine], строящая путь без чередования сторон.
 * Линия может располагаться вертикально или горизонтально в зависимости от
 * [orientation]. Все расчёты перенесены из конфигурации в этот класс.
 */
class LinearTimelineMath(
    private var mathConfig: TimelineMathConfig,
    val orientation: Orientation = Orientation.VERTICAL,
) : TimelineMathEngine {

    private data class SegmentInfo(
        val length: Float,
        val stepPosition: Float
    )

    /** Возможные направления построения линии. */
    enum class Orientation { VERTICAL, HORIZONTAL }

    private var startPositionX = 0f
    private var measuredWidth = 0

    override fun setConfig(config: TimelineMathConfig) {
        mathConfig = config
    }

    override fun getConfig(): TimelineMathConfig = mathConfig

    override fun replaceSteps(steps: List<TimelineStep>) {
        mathConfig = mathConfig.copy(steps = steps)
    }

    override fun buildPath(pathEnable: Path, pathDisable: Path) {
        pathEnable.reset()
        pathDisable.reset()

        val segments = buildSegments()
        var x = 0f
        var y = if (orientation == Orientation.HORIZONTAL) getHorizontalBaseline() else 0f
        pathEnable.moveTo(x, y)
        pathDisable.moveTo(x, y)

        var drawEnable = true

        mathConfig.steps.forEachIndexed { index, step ->
            val segment = segments[index].length

            if (drawEnable) {
                val progress = segment * step.percents / 100f
                val progressLength = if (orientation == Orientation.VERTICAL && progress == 0f) {
                    segment
                } else {
                    progress
                }
                if (orientation == Orientation.VERTICAL) {
                    pathEnable.rLineTo(0f, progressLength)
                    if (progressLength < segment) {
                        pathDisable.moveTo(x, y + progressLength)
                        pathDisable.rLineTo(0f, segment - progressLength)
                        drawEnable = false
                    }
                    y += segment
                } else {
                    pathEnable.rLineTo(progressLength, 0f)
                    if (progressLength < segment) {
                        pathDisable.moveTo(x + progressLength, y)
                        pathDisable.rLineTo(segment - progressLength, 0f)
                        drawEnable = false
                    }
                    x += segment
                }
            } else {
                if (orientation == Orientation.VERTICAL) {
                    pathDisable.rLineTo(0f, segment)
                    y += segment
                } else {
                    pathDisable.rLineTo(segment, 0f)
                    x += segment
                }
            }
        }
    }

    override fun getStartPosition(): Float = startPositionX

    override fun setMeasuredWidth(measuredWidth: Int) {
        this.measuredWidth = measuredWidth
        startPositionX = if (orientation == Orientation.VERTICAL) {
            when (mathConfig.startPosition) {
                TimelineMathConfig.StartPosition.START -> mathConfig.marginHorizontalStroke
                TimelineMathConfig.StartPosition.CENTER -> measuredWidth / 2f
                TimelineMathConfig.StartPosition.END -> measuredWidth.toFloat() - mathConfig.marginHorizontalStroke
            }
        } else {
            val totalLength = getTotalLength()
            when (mathConfig.startPosition) {
                TimelineMathConfig.StartPosition.START -> mathConfig.marginHorizontalStroke
                TimelineMathConfig.StartPosition.CENTER -> (measuredWidth - totalLength) / 2f
                TimelineMathConfig.StartPosition.END ->
                    measuredWidth.toFloat() - totalLength - mathConfig.marginHorizontalStroke
            }
        }
    }

    override fun getHorizontalIconOffset(i: Int): Float {
        return if (orientation == Orientation.VERTICAL) {
            -mathConfig.sizeIconProgress / 2f
        } else {
            getProgressPosition(i) - mathConfig.sizeIconProgress / 2f
        }
    }

    override fun getVerticalOffset(i: Int): Float =
        if (orientation == Orientation.VERTICAL) {
            getProgressPosition(i) + mathConfig.marginTopProgressIcon - mathConfig.sizeIconProgress / 2f
        } else {
            getHorizontalProgressTop()
        }

    override fun getSteps(): List<TimelineStep> = mathConfig.steps

    override fun getMeasuredHeight(): Int =
        if (orientation == Orientation.VERTICAL) {
            ((mathConfig.stepY * mathConfig.steps.size) +
                mathConfig.stepYFirst + mathConfig.sizeIconProgress / 2f).toInt()
        } else {
            val maxIcon = maxOf(mathConfig.sizeImageLvl, mathConfig.sizeIconProgress)
            (maxIcon + mathConfig.marginTopTitle + mathConfig.marginTopDescription).toInt()
        }

    override fun getLeftCoordinates(step: TimelineStep): Float {
        return if (orientation == Orientation.VERTICAL) {
            -mathConfig.sizeIconProgress / 2f
        } else {
            val index = mathConfig.steps.indexOf(step).coerceAtLeast(0)
            getProgressPosition(index) - mathConfig.sizeIconProgress / 2f
        }
    }

    override fun getTopCoordinates(step: TimelineStep): Float {
        return if (orientation == Orientation.VERTICAL) {
            val index = mathConfig.steps.indexOf(step).coerceAtLeast(0)
            getProgressPosition(index) + mathConfig.marginTopProgressIcon - mathConfig.sizeIconProgress / 2f
        } else {
            getHorizontalProgressTop()
        }
    }

    override fun getTitleXCoordinates(align: Paint.Align): Float {
        val stepX = getStepX()
        return when (align) {
            Paint.Align.LEFT -> -(startPositionX - mathConfig.marginHorizontalText)
            Paint.Align.CENTER -> startPositionX
            Paint.Align.RIGHT -> if (mathConfig.startPosition == TimelineMathConfig.StartPosition.CENTER)
                startPositionX - mathConfig.marginHorizontalText
            else -startPositionX + stepX
        }
    }

    override fun getIconXCoordinates(align: Paint.Align): Float {
        val stepX = getStepX()
        return when (align) {
            Paint.Align.LEFT -> -(startPositionX - mathConfig.marginHorizontalImage)
            Paint.Align.CENTER -> startPositionX
            Paint.Align.RIGHT -> if (mathConfig.startPosition == TimelineMathConfig.StartPosition.CENTER)
                startPositionX - mathConfig.marginHorizontalImage - mathConfig.sizeImageLvl
            else -startPositionX + stepX + mathConfig.marginHorizontalImage
        }
    }

    override fun getIconYCoordinates(i: Int): Float {
        return if (orientation == Orientation.VERTICAL)
            calculateTitleYCoordinates(i) - (mathConfig.stepY - mathConfig.sizeImageLvl) / 2
        else -mathConfig.sizeIconProgress / 2f
    }

    override fun getTitleYCoordinates(i: Int): Float = calculateTitleYCoordinates(i)

    override fun getDescriptionYCoordinates(i: Int): Float =
        calculateTitleYCoordinates(i) + mathConfig.marginTopDescription

    // --- Private helpers ---

    private fun getStepX(): Float =
        (measuredWidth - mathConfig.marginHorizontalStroke * 2)

    private fun calculateTitleYCoordinates(i: Int): Float =
        getStepPosition(i) - mathConfig.stepYFirst + mathConfig.marginTopTitle

    private fun getSegmentLength(index: Int): Float {
        return if (index == 0) mathConfig.stepYFirst else mathConfig.stepY
    }

    private fun getHorizontalStepPosition(index: Int): Float =
        if (index == 0) mathConfig.stepYFirst else mathConfig.stepYFirst + mathConfig.stepY * index

    private fun getTotalLength(): Float =
        mathConfig.steps.indices.sumOf { getSegmentLength(it).toDouble() }.toFloat()

    private fun getHorizontalProgressLeft(index: Int): Float =
        getHorizontalStepPosition(index) - mathConfig.sizeIconProgress / 2f

    private fun getHorizontalProgressTop(): Float =
        getHorizontalBaseline() - mathConfig.sizeIconProgress / 2f

    private fun getHorizontalBaseline(): Float =
        maxOf(mathConfig.sizeImageLvl, mathConfig.sizeIconProgress) / 2f

    override fun buildLayout(): TimelineLayout {
        val segments = buildSegments()
        val layoutSteps = if (orientation == Orientation.HORIZONTAL) {
            mathConfig.steps.mapIndexed { index, step ->
                val positionX = segments[index].stepPosition
                val baseline = getHorizontalBaseline()
                TimelineLayoutStep(
                    step = step,
                    titleX = positionX,
                    titleY = baseline + mathConfig.marginTopTitle,
                    descriptionX = positionX,
                    descriptionY = baseline + mathConfig.marginTopTitle + mathConfig.marginTopDescription,
                    iconX = positionX - mathConfig.sizeImageLvl / 2f,
                    iconY = baseline - mathConfig.sizeImageLvl / 2f,
                    textAlign = Paint.Align.CENTER
                )
            }
        } else {
            val align = when (mathConfig.startPosition) {
                TimelineMathConfig.StartPosition.START -> Paint.Align.LEFT
                else -> Paint.Align.RIGHT
            }

            mathConfig.steps.mapIndexed { index, step ->
                TimelineLayoutStep(
                    step = step,
                    titleX = getTitleXCoordinates(align),
                    titleY = getTitleYCoordinates(index),
                    descriptionX = getTitleXCoordinates(align),
                    descriptionY = getDescriptionYCoordinates(index),
                    iconX = getIconXCoordinates(align),
                    iconY = getIconYCoordinates(index),
                    textAlign = align
                )
            }
        }

        val progressIndex = mathConfig.steps.indexOfFirst { it.percents != 100 }
        val progressIcon = if (progressIndex >= 0) {
            val progressPosition = getProgressPosition(progressIndex)
            if (orientation == Orientation.VERTICAL) {
                TimelineProgressIcon(
                    left = -mathConfig.sizeIconProgress / 2f,
                    top = progressPosition + mathConfig.marginTopProgressIcon - mathConfig.sizeIconProgress / 2f
                )
            } else {
                TimelineProgressIcon(
                    left = progressPosition - mathConfig.sizeIconProgress / 2f,
                    top = getHorizontalProgressTop()
                )
            }
        } else {
            null
        }

        return TimelineLayout(layoutSteps, progressIcon)
    }

    private fun buildSegments(): List<SegmentInfo> {
        var position = 0f
        return mathConfig.steps.mapIndexed { index, _ ->
            val length = getSegmentLength(index)
            position += length
            SegmentInfo(length = length, stepPosition = position)
        }
    }

    private fun getStepPosition(index: Int): Float =
        buildSegments().getOrNull(index)?.stepPosition ?: 0f

    private fun getProgressPosition(index: Int): Float {
        val segments = buildSegments()
        val segment = segments.getOrNull(index) ?: return 0f
        val startPosition = segment.stepPosition - segment.length
        val progress = segment.length * mathConfig.steps[index].percents / 100f
        return if (orientation == Orientation.VERTICAL && progress == 0f) {
            segment.stepPosition
        } else {
            startPosition + progress
        }
    }
}
