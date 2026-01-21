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

        var x = 0f
        var y = 0f
        pathEnable.moveTo(x, y)
        pathDisable.moveTo(x, y)

        var drawEnable = true

        mathConfig.steps.forEachIndexed { index, step ->
            val segment = if (orientation == Orientation.VERTICAL) {
                if (index == 0) mathConfig.stepYFirst
                else if (index == mathConfig.steps.size - 1) mathConfig.stepY / 2
                else mathConfig.stepY
            } else {
                if (index == 0) startPositionX - mathConfig.marginHorizontalStroke else getStepX()
            }

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
        startPositionX = when (mathConfig.startPosition) {
            TimelineMathConfig.StartPosition.START -> mathConfig.marginHorizontalStroke
            TimelineMathConfig.StartPosition.CENTER -> measuredWidth / 2f
            TimelineMathConfig.StartPosition.END -> measuredWidth.toFloat() - mathConfig.marginHorizontalStroke
        }
    }

    override fun getHorizontalIconOffset(i: Int): Float {
        return if (orientation == Orientation.VERTICAL) -mathConfig.sizeIconProgress / 2f
        else calculateVerticalOffset(i)
    }

    override fun getVerticalOffset(i: Int): Float {
        return if (orientation == Orientation.VERTICAL) calculateVerticalOffset(i)
        else -mathConfig.sizeIconProgress / 2f
    }

    override fun getSteps(): List<TimelineStep> = mathConfig.steps

    override fun getMeasuredHeight(): Int =
        ((mathConfig.stepY * mathConfig.steps.size) +
            mathConfig.stepYFirst + mathConfig.sizeIconProgress / 2f).toInt()

    override fun getLeftCoordinates(step: TimelineStep, index: Int): Float = -mathConfig.sizeIconProgress / 2f

    override fun getTopCoordinates(step: TimelineStep, index: Int): Float = -mathConfig.sizeIconProgress / 2f

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
}
