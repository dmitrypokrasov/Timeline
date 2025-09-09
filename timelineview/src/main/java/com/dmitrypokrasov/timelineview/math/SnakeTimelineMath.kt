package com.dmitrypokrasov.timelineview.math

import android.graphics.Paint
import android.graphics.Path
import com.dmitrypokrasov.timelineview.model.TimelineStep
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import kotlin.math.abs

/**
 * Публичная реализация [TimelineMathEngine], основанная на "змейке".
 * Вся вычислительная логика перемещена сюда из конфигурации.
 */
class SnakeTimelineMath(private var mathConfig: TimelineMathConfig) : TimelineMathEngine {

    private var startPositionX = 0f
    private var startPositionDisableStrokeX = 0f
    private var measuredWidth = 0

    override fun setConfig(config: TimelineMathConfig) {
        mathConfig = config
    }

    override fun getConfig(): TimelineMathConfig = mathConfig

    override fun replaceSteps(steps: List<TimelineStep>) {
        mathConfig = mathConfig.copy(steps = steps)
    }

    override fun buildPath(pathEnable: Path, pathDisable: Path) {
        pathDisable.reset()
        pathEnable.reset()

        var enable = mathConfig.steps.isNotEmpty() && mathConfig.steps[0].count != 0
        var path: Path = if (enable) pathEnable else pathDisable

        mathConfig.steps.forEachIndexed { i, step ->
            var horizontalStep = if (i % 2 == 0) -getStepX() else getStepX()

            when {
                step.percents == 100 -> {
                    if (i == 0) {
                        path.rLineTo(0f, mathConfig.stepYFirst)
                        path.rLineTo(-getStepXFirst(), 0f)
                        path.rLineTo(0f, mathConfig.stepY)
                    } else {
                        path.rLineTo(horizontalStep, 0f)
                        path.rLineTo(0f, getStandardDyMove(i))
                    }
                }

                i == 0 -> {
                    path.rLineTo(0f, mathConfig.stepYFirst)
                    val startPosDisable = calculateStartPositionDisableStrokeX(step, i)

                    if (enable) {
                        path.rLineTo(-startPosDisable, 0f)
                        path = pathDisable
                        enable = false
                        path.moveTo(-startPosDisable, mathConfig.stepYFirst)
                    }
                    path.rLineTo(-(getStepXFirst() - startPosDisable), 0f)
                    path.rLineTo(0f, mathConfig.stepY)
                }

                enable -> {
                    val startPosDisable = calculateStartPositionDisableStrokeX(step, i)

                    horizontalStep = if (i % 2 == 0) -getStepX() else getStepX()
                    val finishPositionLineXEnable =
                        horizontalStep / abs(horizontalStep) * startPosDisable

                    path.rLineTo(finishPositionLineXEnable, 0f)
                    path = pathDisable
                    enable = false

                    val startPositionLineYDisable = mathConfig.stepYFirst + mathConfig.stepY * i
                    val startPositionLineXDisable = calculateHorizontalOffset(i)

                    path.moveTo(startPositionLineXDisable, startPositionLineYDisable)

                    val finishPositionLineYDisable = getStandardDyMove(i)
                    val finishPositionLineXDisable =
                        if (i % 2 == 0) -(getStepX() - startPosDisable)
                        else getStepX() - startPosDisable

                    path.rLineTo(finishPositionLineXDisable, 0f)
                    path.rLineTo(0f, finishPositionLineYDisable)
                }

                else -> {
                    path.rLineTo(horizontalStep, 0f)
                    path.rLineTo(0f, getStandardDyMove(i))
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

    override fun getHorizontalIconOffset(i: Int): Float =
        calculateHorizontalOffset(i) - mathConfig.sizeIconProgress / 2f

    override fun getVerticalOffset(i: Int): Float =
        (mathConfig.stepY * i) + mathConfig.marginTopProgressIcon

    override fun getSteps(): List<TimelineStep> = mathConfig.steps

    override fun getMeasuredHeight(): Int =
        ((mathConfig.stepY * mathConfig.steps.size) +
            mathConfig.stepYFirst + mathConfig.sizeIconProgress / 2f).toInt()

    override fun getLeftCoordinates(step: TimelineStep): Float =
        if (step.count == 0) -mathConfig.sizeIconProgress / 2f
        else -startPositionDisableStrokeX - mathConfig.sizeIconProgress / 2f

    override fun getTopCoordinates(step: TimelineStep): Float =
        if (step.count == 0) -mathConfig.sizeIconProgress / 2f
        else mathConfig.stepYFirst - mathConfig.sizeIconProgress / 2f

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

    override fun getIconYCoordinates(i: Int): Float =
        calculateTitleYCoordinates(i) - (mathConfig.stepY - mathConfig.sizeImageLvl) / 2

    override fun getTitleYCoordinates(i: Int): Float = calculateTitleYCoordinates(i)

    override fun getDescriptionYCoordinates(i: Int): Float =
        calculateTitleYCoordinates(i) + mathConfig.marginTopDescription

    // --- Private helpers ---

    private fun getStandardDyMove(i: Int): Float =
        if (i == mathConfig.steps.size - 1) mathConfig.stepY / 2 else mathConfig.stepY

    private fun calculateTitleYCoordinates(i: Int): Float =
        (mathConfig.stepY * i) + mathConfig.marginTopTitle

    private fun calculateStartPositionDisableStrokeX(step: TimelineStep, i: Int): Float {
        val startPosition =
            if (i == 0) getStepXFirst() / 100 * step.percents else getStepX() / 100 * step.percents
        startPositionDisableStrokeX = startPosition
        return startPosition
    }

    private fun getStepX(): Float =
        (measuredWidth - mathConfig.marginHorizontalStroke * 2)

    private fun getStepXFirst(): Float = startPositionX - mathConfig.marginHorizontalStroke

    private fun calculateHorizontalOffset(i: Int): Float {
        return if (i % 2 == 0)
            when (mathConfig.startPosition) {
                TimelineMathConfig.StartPosition.START -> -startPositionDisableStrokeX + getStepX()
                TimelineMathConfig.StartPosition.CENTER -> -startPositionDisableStrokeX + startPositionX - mathConfig.marginHorizontalStroke
                TimelineMathConfig.StartPosition.END -> -startPositionDisableStrokeX
            }
        else startPositionDisableStrokeX - startPositionX + mathConfig.marginHorizontalStroke
    }
}
