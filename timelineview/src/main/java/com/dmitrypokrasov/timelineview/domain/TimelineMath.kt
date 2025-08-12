package com.dmitrypokrasov.timelineview.domain

import android.graphics.Paint
import android.graphics.Path
import com.dmitrypokrasov.timelineview.data.TimelineStep
import com.dmitrypokrasov.timelineview.domain.data.TimelineMathConfig
import kotlin.math.abs

/**
 * Класс, отвечающий за расчёт координат, размеров и построение путей для отрисовки таймлайна.
 *
 * Используется при отрисовке на `Canvas` или в кастомном `View`, чтобы:
 * - построить линии прогресса (`Path`)
 * - рассчитать позиции иконок, заголовков и описаний
 * - адаптировать расположение под ширину вьюшки
 *
 * @property mathConfig Конфигурация математических параметров таймлайна.
 */
internal class TimelineMath(var mathConfig: TimelineMathConfig) {

    companion object {
        private const val TAG = "TimelineMath"
    }

    /**
     * Заменяет список шагов в текущей математической конфигурации.
     */
    fun replaceSteps(steps: List<TimelineStep>) {
        mathConfig = mathConfig.copy(steps = steps)
    }

    /**
     * Строит два пути:
     * - [pathEnable] — линия пройденных шагов.
     * - [pathDisable] — линия ещё не завершённых шагов.
     */
    fun buildPath(pathEnable: Path, pathDisable: Path) {
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
                        path.rLineTo(0f, mathConfig.getStandartDYMove(i))
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

                    val finishPositionLineYDisable = mathConfig.getStandartDYMove(i)
                    val finishPositionLineXDisable =
                        if (i % 2 == 0) -(mathConfig.getStepX() - startPositionDisableStrokeX)
                        else mathConfig.getStepX() - startPositionDisableStrokeX

                    path.rLineTo(finishPositionLineXDisable, 0f)
                    path.rLineTo(0f, finishPositionLineYDisable)
                }

                else -> {
                    path.rLineTo(horizontalStep, 0f)
                    path.rLineTo(0f, mathConfig.getStandartDYMove(i))
                }
            }
        }
    }

    fun getStartPosition(): Float {
        return mathConfig.getStartPosition()
    }

    /**
     * Устанавливает ширину View и рассчитывает X-координату начала рисования.
     */
    fun setMeasuredWidth(measuredWidth: Int) {
        mathConfig.setMeasuredWidth(measuredWidth)
    }

    /** Возвращает горизонтальное смещение иконки прогресса на шаге [i]. */
    fun getHorizontalIconOffset(i: Int): Float {
        return mathConfig.getHorizontalIconOffset(i)
    }

    /** Возвращает вертикальное смещение на шаге [i]. */
    fun getVerticalOffset(i: Int): Float {
        return mathConfig.getVerticalOffset(i)
    }

    fun getMeasuredHeight(): Int {
        return mathConfig.getMeasuredHeight()
    }

    /** Возвращает X-координату левой границы иконки прогресса. */
    fun getLeftCoordinates(lvl: TimelineStep): Float {
        return mathConfig.getLeftCoordinates(lvl)
    }

    /** Возвращает Y-координату верхней границы иконки прогресса. */
    fun getTopCoordinates(lvl: TimelineStep): Float {
        return mathConfig.getTopCoordinates(lvl)
    }

    /** Возвращает X-координату для иконки уровня. */
    fun getIconXCoordinates(align: Paint.Align): Float {
        return mathConfig.getIconXCoordinates(align)
    }

    /** Возвращает X-координату заголовка уровня. */
    fun getTitleXCoordinates(align: Paint.Align): Float {
        return mathConfig.getTitleXCoordinates(align)
    }

    /** Возвращает Y-координату для иконки уровня. */
    fun getIconYCoordinates(i: Int): Float {
        return mathConfig.getIconYCoordinates(i)
    }

    /** Возвращает Y-координату заголовка уровня. */
    fun getTitleYCoordinates(i: Int): Float {
        return mathConfig.getTitleYCoordinates(i)
    }

    /** Возвращает Y-координату описания уровня. */
    fun getDescriptionYCoordinates(i: Int): Float {
        return mathConfig.getDescriptionYCoordinates(i)
    }
}