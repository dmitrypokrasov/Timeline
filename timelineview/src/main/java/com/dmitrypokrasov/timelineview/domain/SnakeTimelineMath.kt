package com.dmitrypokrasov.timelineview.domain

import android.graphics.Paint
import android.graphics.Path
import com.dmitrypokrasov.timelineview.data.TimelineStep
import com.dmitrypokrasov.timelineview.domain.data.TimelineMathConfig
import kotlin.math.abs

/**
 * Публичная реализация [TimelineMathEngine], основанная на "змейке".
 * Содержит всю вычислительную логику, ранее находившуюся в `TimelineMath`.
 */
class SnakeTimelineMath(var mathConfig: TimelineMathConfig) : TimelineMathEngine {

    companion object {
        private const val TAG = "SnakeTimelineMath"
    }

    override fun replaceSteps(steps: List<TimelineStep>) {
        mathConfig = mathConfig.copy(steps = steps)
    }
    override fun buildPath(pathEnable: Path, pathDisable: Path) {
        pathDisable.reset()
        pathEnable.reset()

        var enable = mathConfig.steps.isNotEmpty() && mathConfig.steps[0].count != 0
        var path: Path = if (enable) pathEnable else pathDisable

        mathConfig.steps.forEachIndexed { i, lvl ->
            var horizontalStep = if (i % 2 == 0) -mathConfig.getStepX() else mathConfig.getStepX()

            when {
                lvl.percents == 100 -> {
                    if (i == 0) {
                        path.rLineTo(0f, mathConfig.stepYFirst)
                        path.rLineTo(-mathConfig.getStepXFirst(), 0f)
                        path.rLineTo(0f, mathConfig.stepY)
                    } else {
                        path.rLineTo(horizontalStep, 0f)
                        path.rLineTo(0f, mathConfig.getStandardDyMove(i))
                    }
                }

                i == 0 -> {
                    path.rLineTo(0f, mathConfig.stepYFirst)
                    val startPositionDisableStrokeX =
                        mathConfig.getStartPositionDisableStrokeX(lvl, i)

                    if (enable) {
                        path.rLineTo(-startPositionDisableStrokeX, 0f)
                        path = pathDisable
                        enable = false
                        path.moveTo(-startPositionDisableStrokeX, mathConfig.stepYFirst)
                    }
                    path.rLineTo(-(mathConfig.getStepXFirst() - startPositionDisableStrokeX), 0f)
                    path.rLineTo(0f, mathConfig.stepY)
                }

                enable -> {
                    val startPositionDisableStrokeX =
                        mathConfig.getStartPositionDisableStrokeX(lvl, i)

                    horizontalStep =
                        if (i % 2 == 0) -mathConfig.getStepX() else mathConfig.getStepX()
                    val finishPositionLineXEnable =
                        horizontalStep / abs(horizontalStep) * startPositionDisableStrokeX

                    path.rLineTo(finishPositionLineXEnable, 0f)
                    path = pathDisable
                    enable = false

                    val startPositionLineYDisable = mathConfig.stepYFirst + mathConfig.stepY * i
                    val startPositionLineXDisable = mathConfig.getHorizontalOffset(i)

                    path.moveTo(startPositionLineXDisable, startPositionLineYDisable)

                    val finishPositionLineYDisable = mathConfig.getStandardDyMove(i)
                    val finishPositionLineXDisable =
                        if (i % 2 == 0) -(mathConfig.getStepX() - startPositionDisableStrokeX)
                        else mathConfig.getStepX() - startPositionDisableStrokeX

                    path.rLineTo(finishPositionLineXDisable, 0f)
                    path.rLineTo(0f, finishPositionLineYDisable)
                }

                else -> {
                    path.rLineTo(horizontalStep, 0f)
                    path.rLineTo(0f, mathConfig.getStandardDyMove(i))
                }
            }
        }
    }

    override fun getStartPosition(): Float {
        return mathConfig.getStartPosition()
    }

    override fun setMeasuredWidth(measuredWidth: Int) {
        mathConfig.setMeasuredWidth(measuredWidth)
    }
    override fun getHorizontalIconOffset(i: Int): Float {
        return mathConfig.getHorizontalIconOffset(i)
    }
    override fun getVerticalOffset(i: Int): Float {
        return mathConfig.getVerticalOffset(i)
    }

    override fun getMeasuredHeight(): Int {
        return mathConfig.getMeasuredHeight()
    }

    override fun getLeftCoordinates(lvl: TimelineStep): Float {
        return mathConfig.getLeftCoordinates(lvl)
    }
    override fun getTopCoordinates(lvl: TimelineStep): Float {
        return mathConfig.getTopCoordinates(lvl)
    }
    override fun getIconXCoordinates(align: Paint.Align): Float {
        return mathConfig.getIconXCoordinates(align)
    }
    override fun getTitleXCoordinates(align: Paint.Align): Float {
        return mathConfig.getTitleXCoordinates(align)
    }

    /** Возвращает Y-координату для иконки уровня. */
    fun getIconYCoordinates(i: Int): Float {
        return mathConfig.getIconYCoordinates(i)
    }

    override fun getTitleYCoordinates(i: Int): Float {
        return mathConfig.getTitleYCoordinates(i)
    }
    override fun getDescriptionYCoordinates(i: Int): Float {
        return mathConfig.getDescriptionYCoordinates(i)
    }
}