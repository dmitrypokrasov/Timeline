package com.dmitrypokrasov.timelineview.math

import android.graphics.Paint
import android.graphics.Path
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.math.data.TimelineLayoutStep
import com.dmitrypokrasov.timelineview.math.data.TimelineProgressIcon
import com.dmitrypokrasov.timelineview.model.TimelineStepData
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

    override fun replaceSteps(steps: List<TimelineStepData>) {
        mathConfig = mathConfig.copy(steps = steps)
    }

    override fun buildPath(pathEnable: Path, pathDisable: Path) {
        pathDisable.reset()
        pathEnable.reset()

        var enable = mathConfig.steps.isNotEmpty() && mathConfig.steps[0].progress != 0
        var path: Path = if (enable) pathEnable else pathDisable

        mathConfig.steps.forEachIndexed { i, step ->
            var horizontalStep = if (i % 2 == 0) -getStepX() else getStepX()

            when {
                step.progress == 100 -> {
                    if (i == 0) {
                        path.rLineTo(0f, mathConfig.spacing.stepYFirst)
                        path.rLineTo(-getStepXFirst(), 0f)
                        path.rLineTo(0f, mathConfig.spacing.stepY)
                    } else {
                        path.rLineTo(horizontalStep, 0f)
                        path.rLineTo(0f, getStandardDyMove(i))
                    }
                }

                i == 0 -> {
                    path.rLineTo(0f, mathConfig.spacing.stepYFirst)
                    val startPosDisable = calculateStartPositionDisableStrokeX(step, i)

                    if (enable) {
                        path.rLineTo(-startPosDisable, 0f)
                        path = pathDisable
                        enable = false
                        path.moveTo(-startPosDisable, mathConfig.spacing.stepYFirst)
                    }
                    path.rLineTo(-(getStepXFirst() - startPosDisable), 0f)
                    path.rLineTo(0f, mathConfig.spacing.stepY)
                }

                enable -> {
                    val startPosDisable = calculateStartPositionDisableStrokeX(step, i)

                    horizontalStep = if (i % 2 == 0) -getStepX() else getStepX()
                    val finishPositionLineXEnable =
                        horizontalStep / abs(horizontalStep) * startPosDisable

                    path.rLineTo(finishPositionLineXEnable, 0f)
                    path = pathDisable
                    enable = false

                    val startPositionLineYDisable = mathConfig.spacing.stepYFirst + mathConfig.spacing.stepY * i
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
            TimelineMathConfig.StartPosition.START -> mathConfig.spacing.marginHorizontalStroke
            TimelineMathConfig.StartPosition.CENTER -> measuredWidth / 2f
            TimelineMathConfig.StartPosition.END -> measuredWidth.toFloat() - mathConfig.spacing.marginHorizontalStroke
        }
    }

    override fun getHorizontalIconOffset(i: Int): Float =
        calculateHorizontalOffset(i) - mathConfig.sizes.sizeIconProgress / 2f

    override fun getVerticalOffset(i: Int): Float =
        (mathConfig.spacing.stepY * i) + mathConfig.spacing.marginTopProgressIcon

    override fun getSteps(): List<TimelineStepData> = mathConfig.steps

    override fun getLeftCoordinates(step: TimelineStepData): Float =
        if (step.progress == 0) -mathConfig.sizes.sizeIconProgress / 2f
        else -startPositionDisableStrokeX - mathConfig.sizes.sizeIconProgress / 2f

    override fun getTopCoordinates(step: TimelineStepData): Float =
        if (step.progress == 0) -mathConfig.sizes.sizeIconProgress / 2f
        else mathConfig.spacing.stepYFirst - mathConfig.sizes.sizeIconProgress / 2f

    override fun getIconXCoordinates(align: Paint.Align): Float {
        val stepX = getStepX()
        return when (align) {
            Paint.Align.LEFT -> -(startPositionX - mathConfig.spacing.marginHorizontalImage)
            Paint.Align.CENTER -> startPositionX
            Paint.Align.RIGHT -> if (mathConfig.startPosition == TimelineMathConfig.StartPosition.CENTER)
                startPositionX - mathConfig.spacing.marginHorizontalImage - mathConfig.sizes.sizeImageLvl
            else -startPositionX + stepX + mathConfig.spacing.marginHorizontalImage
        }
    }

    override fun getTitleXCoordinates(align: Paint.Align): Float {
        val stepX = getStepX()
        return when (align) {
            Paint.Align.LEFT -> -(startPositionX - mathConfig.spacing.marginHorizontalText)
            Paint.Align.CENTER -> startPositionX
            Paint.Align.RIGHT -> if (mathConfig.startPosition == TimelineMathConfig.StartPosition.CENTER)
                startPositionX - mathConfig.spacing.marginHorizontalText
            else -startPositionX + stepX
        }
    }

    override fun getIconYCoordinates(i: Int): Float =
        calculateTitleYCoordinates(i) - (mathConfig.spacing.stepY - mathConfig.sizes.sizeImageLvl) / 2

    override fun getTitleYCoordinates(i: Int): Float = calculateTitleYCoordinates(i)

    override fun getDescriptionYCoordinates(i: Int): Float =
        calculateTitleYCoordinates(i) + mathConfig.spacing.marginTopDescription

    // --- Private helpers ---

    private fun getStandardDyMove(i: Int): Float =
        if (i == mathConfig.steps.size - 1) mathConfig.spacing.stepY / 2 else mathConfig.spacing.stepY

    private fun calculateTitleYCoordinates(i: Int): Float =
        (mathConfig.spacing.stepY * i) + mathConfig.spacing.marginTopTitle

    private fun calculateStartPositionDisableStrokeX(step: TimelineStepData, i: Int): Float {
        val startPosition =
            if (i == 0) getStepXFirst() / 100 * step.progress else getStepX() / 100 * step.progress
        startPositionDisableStrokeX = startPosition
        return startPosition
    }

    private fun getStepX(): Float =
        (measuredWidth - mathConfig.spacing.marginHorizontalStroke * 2)

    private fun getStepXFirst(): Float = startPositionX - mathConfig.spacing.marginHorizontalStroke

    private fun calculateHorizontalOffset(i: Int): Float {
        return if (i % 2 == 0)
            when (mathConfig.startPosition) {
                TimelineMathConfig.StartPosition.START -> -startPositionDisableStrokeX + getStepX()
                TimelineMathConfig.StartPosition.CENTER -> -startPositionDisableStrokeX + startPositionX -
                    mathConfig.spacing.marginHorizontalStroke
                TimelineMathConfig.StartPosition.END -> -startPositionDisableStrokeX
            }
        else startPositionDisableStrokeX - startPositionX + mathConfig.spacing.marginHorizontalStroke
    }


    private fun calculateTextMaxWidth(align: Paint.Align, relativeX: Float): Int {
        val absoluteX = startPositionX + relativeX
        val width = when (align) {
            Paint.Align.LEFT -> measuredWidth - absoluteX
            Paint.Align.RIGHT -> absoluteX
            Paint.Align.CENTER -> minOf(absoluteX, measuredWidth - absoluteX) * 2f
        }
        return width.toInt().coerceAtLeast(1)
    }

    override fun buildLayout(): TimelineLayout {
        val steps = mathConfig.steps
        val layoutSteps = steps.mapIndexed { index, step ->
            val align = if (index % 2 == 0) Paint.Align.LEFT else Paint.Align.RIGHT
            TimelineLayoutStep(
                step = step,
                titleX = getTitleXCoordinates(align),
                titleY = getTitleYCoordinates(index),
                descriptionX = getTitleXCoordinates(align),
                descriptionY = getDescriptionYCoordinates(index),
                iconX = getIconXCoordinates(align),
                iconY = getIconYCoordinates(index),
                textAlign = align,
                descriptionMaxWidth = calculateTextMaxWidth(align, getTitleXCoordinates(align))
            )
        }

        val progressIndex = steps.indexOfFirst { it.progress != 100 }
        val progressIcon = if (progressIndex >= 0) {
            val step = steps[progressIndex]
            if (progressIndex == 0) {
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
}
