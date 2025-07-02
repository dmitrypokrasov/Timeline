package com.dmitrypokrasov.timelineview

import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import kotlin.math.abs

internal class TimelineMath(var config: TimelineConfig) {

    private var startPositionX = 0f
    private var startPositionDisableStrokeX = 0f
    private var measuredWidth = 0

    companion object {
        private const val TAG = "TimelineMath"
    }

    fun replaceSteps(steps: List<TimelineStep>) {
        config = config.copy(steps = steps)
    }

    fun buildPath(pathEnable: Path, pathDisable: Path) {
        pathDisable.reset()
        pathEnable.reset()

        var enable = config.steps.isNotEmpty() && config.steps[0].count != 0
        var path: Path = if (enable) pathEnable else pathDisable

        config.steps.forEachIndexed { i, lvl ->
            val horizontalStep = getHorizontalStep(i)

            when {
                lvl.percents == 100 -> {
                    if (i == 0) {
                        path.rLineTo(0f, config.stepYFirst)
                        path.rLineTo(-getStepXFirst(), 0f)
                        path.rLineTo(0f, config.stepY)
                    } else {
                        path.rLineTo(horizontalStep, 0f)
                        path.rLineTo(0f, getStandartDYMove(i))
                    }
                }

                i == 0 -> {
                    path.rLineTo(0f, config.stepYFirst)
                    if (enable) {
                        initStartPositionDisableStrokeX(lvl, i)
                        path.rLineTo(-startPositionDisableStrokeX, 0f)
                        path = pathDisable
                        enable = false
                        path.moveTo(-startPositionDisableStrokeX, config.stepYFirst)
                    }
                    path.rLineTo(-(getStepXFirst() - startPositionDisableStrokeX), 0f)
                    path.rLineTo(0f, config.stepY)
                }

                enable -> {
                    initStartPositionDisableStrokeX(lvl, i)

                    val finishPositionLineXEnable =
                        getHorizontalStep(i) / abs(getHorizontalStep(i)) * startPositionDisableStrokeX

                    path.rLineTo(finishPositionLineXEnable, 0f)
                    path = pathDisable
                    enable = false

                    val startPositionLineYDisable = config.stepYFirst + config.stepY * i
                    val startPositionLineXDisable = getHorizontalOffset(i)

                    path.moveTo(startPositionLineXDisable, startPositionLineYDisable)

                    val finishPositionLineYDisable = getStandartDYMove(i)
                    val finishPositionLineXDisable =
                        if (i % 2 == 0) -(getStepX() - startPositionDisableStrokeX)
                        else getStepX() - startPositionDisableStrokeX

                    path.rLineTo(finishPositionLineXDisable, 0f)
                    path.rLineTo(0f, finishPositionLineYDisable)
                }

                else -> {
                    path.rLineTo(horizontalStep, 0f)
                    path.rLineTo(0f, getStandartDYMove(i))
                }
            }
        }
    }

    fun setMeasuredWidth(measuredWidth: Int) {
        this.measuredWidth = measuredWidth

        startPositionX = when (config.startPosition) {
            TimelineConfig.StartPosition.START -> config.marginHorizontalStroke
            TimelineConfig.StartPosition.CENTER -> measuredWidth / 2f
            TimelineConfig.StartPosition.END -> measuredWidth.toFloat() - config.marginHorizontalStroke
        }

        Log.d(TAG, "setMeasuredWidth startPositionX: $startPositionX")
    }

    fun getStartPositionX() = startPositionX

    fun getHorizontalIconOffset(i: Int): Float {
        val offset = getHorizontalOffset(i) - config.sizeIconProgress / 2f

        Log.d(
            TAG,
            "getHorizontalOffset i: $i, offset: $offset, startPositionDisableStrokeX: $startPositionDisableStrokeX, startPositionX: $startPositionX, marginHorizontalStroke: ${config.marginHorizontalStroke}"
        )

        return offset
    }

    fun getVerticalOffset(i: Int): Float {
        return (config.stepY * i) + config.marginTopProgressIcon
    }

    fun getLeftCoordinates(lvl: TimelineStep): Float {
        return if (lvl.count == 0) -config.sizeIconProgress / 2f else -startPositionDisableStrokeX - config.sizeIconProgress / 2f
    }

    fun getTopCoordinates(lvl: TimelineStep): Float {
        return if (lvl.count == 0) -config.sizeIconProgress / 2f else config.top
    }

    fun getIconXCoordinates(align: Paint.Align): Float {
        val stepX = getStepX()
        return when (align) {
            Paint.Align.LEFT -> if (config.startPosition == TimelineConfig.StartPosition.CENTER) startPositionX - config.marginHorizontalImage - config.sizeImageLvl else -startPositionX + stepX + config.marginHorizontalImage
            Paint.Align.CENTER -> startPositionX
            Paint.Align.RIGHT -> -(startPositionX - config.marginHorizontalImage)
        }
    }

    fun getIconYCoordinates(i: Int): Float {
        return (config.stepY * i) + config.marginTopTitle - (config.stepY - config.sizeImageLvl) / 2
    }

    fun getTitleXCoordinates(align: Paint.Align): Float {
        val stepX = getStepX()
        return when (align) {
            Paint.Align.LEFT -> if (config.startPosition == TimelineConfig.StartPosition.CENTER) startPositionX - config.marginHorizontalText else -startPositionX + stepX
            Paint.Align.CENTER -> startPositionX
            Paint.Align.RIGHT -> -(startPositionX - config.marginHorizontalText)
        }
    }

    fun getTitleYCoordinates(i: Int): Float {
        return (config.stepY * i) + config.marginTopTitle
    }

    fun getDescriptionYCoordinates(i: Int): Float {
        return (config.stepY * i) + config.marginTopTitle + config.sizeTitle + config.marginTopDescription
    }

    private fun getHorizontalOffset(i: Int): Float {
        val offset = if (i % 2 == 0)
            when (config.startPosition) {
                TimelineConfig.StartPosition.START -> getStepX() - startPositionDisableStrokeX
                TimelineConfig.StartPosition.CENTER -> startPositionX - startPositionDisableStrokeX - config.marginHorizontalStroke
                TimelineConfig.StartPosition.END -> -startPositionDisableStrokeX
            }
        else startPositionDisableStrokeX - startPositionX + config.marginHorizontalStroke

        Log.d(
            TAG,
            "getHorizontalOffset i: $i, offset: $offset, startPositionDisableStrokeX: $startPositionDisableStrokeX, startPositionX: $startPositionX, marginHorizontalStroke: ${config.marginHorizontalStroke}"
        )

        return offset
    }

    private fun initStartPositionDisableStrokeX(lvl: TimelineStep, i: Int) {
        startPositionDisableStrokeX =
            when (config.startPosition) {
                TimelineConfig.StartPosition.START -> if (i == 0) getStepXFirst() / 100 * lvl.percents else getStepX() / 100 * lvl.percents
                TimelineConfig.StartPosition.CENTER -> if (i == 0) getStepXFirst() / 100 * lvl.percents else getStepX() / 100 * lvl.percents
                TimelineConfig.StartPosition.END -> if (i == 0) getStepX() / 100 * lvl.percents else getStepX() / 100 * lvl.percents
            }

        Log.d(
            TAG,
            "initStartPositionDisableStrokeX i: $i, startPositionDisableStrokeX: $startPositionDisableStrokeX"
        )
    }

    private fun getStandartDYMove(i: Int): Float {
        return if (i == config.steps.size - 1) config.stepY / 2 else config.stepY
    }

    private fun getStepX(): Float {
        return (measuredWidth - config.marginHorizontalStroke * 2)
    }

    private fun getStepXFirst(): Float {
        return startPositionX - config.marginHorizontalStroke
    }

    private fun getHorizontalStep(i: Int): Float {
        val stepX = getStepX()

        val step = if (i % 2 == 0) -stepX else stepX

        Log.d(TAG, "getHorizontalStep i: $i, step: $step")

        return step
    }
}