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
        var y = 0f
        pathEnable.moveTo(x, y)
        pathDisable.moveTo(x, y)

        var drawEnable = true

        mathConfig.steps.forEachIndexed { index, step ->
            val segment = segments[index].length

            if (drawEnable) {
                val progress = segment * step.percents / 100f
                if (orientation == Orientation.VERTICAL) {
                    pathEnable.rLineTo(0f, progress)
                    if (progress < segment) {
                        pathDisable.moveTo(x, y + progress)
                        pathDisable.rLineTo(0f, segment - progress)
                        drawEnable = false
                    }
                    y += segment
                } else {
                    pathEnable.rLineTo(progress, 0f)
                    if (progress < segment) {
                        pathDisable.moveTo(x + progress, y)
                        pathDisable.rLineTo(segment - progress, 0f)
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
            getHorizontalProgressLeft(i)
        }
    }

    override fun getVerticalOffset(i: Int): Float =
        if (orientation == Orientation.VERTICAL) {
            calculateVerticalOffset(i)
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
            getHorizontalProgressLeft(index)
        }
    }

    override fun getTopCoordinates(step: TimelineStep): Float {
        return if (orientation == Orientation.VERTICAL) {
            -mathConfig.sizeIconProgress / 2f
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

    private fun calculateVerticalOffset(i: Int): Float =
        (mathConfig.stepY * i) + mathConfig.marginTopProgressIcon

    private fun calculateTitleYCoordinates(i: Int): Float =
        (mathConfig.stepY * i) + mathConfig.marginTopTitle

    private fun getSegmentLength(index: Int): Float {
        return if (index == 0) {
            mathConfig.stepYFirst
        } else if (index == mathConfig.steps.size - 1) {
            mathConfig.stepY / 2
        } else {
            mathConfig.stepY
        }
    }

    private fun getHorizontalStepPosition(index: Int): Float =
        if (index == 0) mathConfig.stepYFirst else mathConfig.stepYFirst + mathConfig.stepY * index

    private fun getTotalLength(): Float =
        mathConfig.steps.indices.sumOf { getSegmentLength(it).toDouble() }.toFloat()

    private fun getHorizontalProgressLeft(index: Int): Float =
        getHorizontalStepPosition(index) - mathConfig.sizeIconProgress / 2f

    private fun getHorizontalProgressTop(): Float = -mathConfig.sizeIconProgress / 2f

    override fun buildLayout(): TimelineLayout {
        val segments = buildSegments()
        val layoutSteps = if (orientation == Orientation.HORIZONTAL) {
            mathConfig.steps.mapIndexed { index, step ->
                val positionX = segments[index].stepPosition
                TimelineLayoutStep(
                    step = step,
                    titleX = positionX,
                    titleY = mathConfig.marginTopTitle,
                    descriptionX = positionX,
                    descriptionY = mathConfig.marginTopTitle + mathConfig.marginTopDescription,
                    iconX = positionX - mathConfig.sizeImageLvl / 2f,
                    iconY = -mathConfig.sizeImageLvl / 2f,
                    textAlign = Paint.Align.CENTER
                )
            }
        } else {
            val align = when (mathConfig.startPosition) {
                TimelineMathConfig.StartPosition.END -> Paint.Align.LEFT
                else -> Paint.Align.RIGHT
            }

            mathConfig.steps.mapIndexed { index, step ->
                val positionY = segments[index].stepPosition
                TimelineLayoutStep(
                    step = step,
                    titleX = getTitleXCoordinates(align),
                    titleY = positionY + mathConfig.marginTopTitle,
                    descriptionX = getTitleXCoordinates(align),
                    descriptionY = positionY + mathConfig.marginTopTitle + mathConfig.marginTopDescription,
                    iconX = getIconXCoordinates(align),
                    iconY = positionY - mathConfig.sizeImageLvl / 2f,
                    textAlign = align
                )
            }
        }

        val progressIndex = mathConfig.steps.indexOfFirst { it.percents != 100 }
        val progressIcon = if (progressIndex >= 0) {
            if (progressIndex == 0) {
                val step = mathConfig.steps[progressIndex]
                TimelineProgressIcon(
                    left = getLeftCoordinates(step),
                    top = getTopCoordinates(step)
                )
            } else {
                TimelineProgressIcon(
                    left = getHorizontalIconOffset(progressIndex),
                    top = getVerticalOffset(progressIndex)
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
}
