package com.dmitrypokrasov.timelineview.domain

import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import com.dmitrypokrasov.timelineview.data.TimelineStep
import com.dmitrypokrasov.timelineview.domain.data.TimelineMathConfig
import com.dmitrypokrasov.timelineview.domain.data.TimelineUiConfig
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
 * @property uiConfig Конфигурация UI-стиля (цвета, иконки).
 */
internal class TimelineMath(
    var mathConfig: TimelineMathConfig,
    var uiConfig: TimelineUiConfig,
) {

    private var startPositionX = 0f
    private var startPositionDisableStrokeX = 0f
    private var measuredWidth = 0

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
            val horizontalStep = getHorizontalStep(i)

            when {
                lvl.percents == 100 -> {
                    if (i == 0) {
                        path.rLineTo(0f, mathConfig.stepYFirst)
                        path.rLineTo(-getStepXFirst(), 0f)
                        path.rLineTo(0f, mathConfig.stepY)
                    } else {
                        path.rLineTo(horizontalStep, 0f)
                        path.rLineTo(0f, getStandartDYMove(i))
                    }
                }

                i == 0 -> {
                    path.rLineTo(0f, mathConfig.stepYFirst)
                    if (enable) {
                        initStartPositionDisableStrokeX(lvl, i)
                        path.rLineTo(-startPositionDisableStrokeX, 0f)
                        path = pathDisable
                        enable = false
                        path.moveTo(-startPositionDisableStrokeX, mathConfig.stepYFirst)
                    }
                    path.rLineTo(-(getStepXFirst() - startPositionDisableStrokeX), 0f)
                    path.rLineTo(0f, mathConfig.stepY)
                }

                enable -> {
                    initStartPositionDisableStrokeX(lvl, i)

                    val finishPositionLineXEnable =
                        getHorizontalStep(i) / abs(getHorizontalStep(i)) * startPositionDisableStrokeX

                    path.rLineTo(finishPositionLineXEnable, 0f)
                    path = pathDisable
                    enable = false

                    val startPositionLineYDisable = mathConfig.stepYFirst + mathConfig.stepY * i
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

    /**
     * Устанавливает ширину View и рассчитывает X-координату начала рисования.
     */
    fun setMeasuredWidth(measuredWidth: Int) {
        this.measuredWidth = measuredWidth

        startPositionX = when (mathConfig.startPosition) {
            TimelineMathConfig.StartPosition.START -> mathConfig.marginHorizontalStroke
            TimelineMathConfig.StartPosition.CENTER -> measuredWidth / 2f
            TimelineMathConfig.StartPosition.END -> measuredWidth.toFloat() - mathConfig.marginHorizontalStroke
        }

        Log.d(TAG, "setMeasuredWidth startPositionX: $startPositionX")
    }

    /** Получает X-координату начала рисования таймлайна. */
    fun getStartPositionX() = startPositionX

    /** Возвращает горизонтальное смещение иконки прогресса на шаге [i]. */
    fun getHorizontalIconOffset(i: Int): Float {
        val offset = getHorizontalOffset(i) - mathConfig.sizeIconProgress / 2f

        Log.d(
            TAG,
            "getHorizontalOffset i: $i, offset: $offset, startPositionDisableStrokeX: $startPositionDisableStrokeX, startPositionX: $startPositionX, marginHorizontalStroke: ${mathConfig.marginHorizontalStroke}"
        )

        return offset
    }

    /** Возвращает вертикальное смещение на шаге [i]. */
    fun getVerticalOffset(i: Int): Float {
        return (mathConfig.stepY * i) + mathConfig.marginTopProgressIcon
    }

    /** Возвращает X-координату левой границы иконки прогресса. */
    fun getLeftCoordinates(lvl: TimelineStep): Float {
        return if (lvl.count == 0) -mathConfig.sizeIconProgress / 2f else -startPositionDisableStrokeX - mathConfig.sizeIconProgress / 2f
    }

    /** Возвращает Y-координату верхней границы иконки прогресса. */
    fun getTopCoordinates(lvl: TimelineStep): Float {
        return if (lvl.count == 0) -mathConfig.sizeIconProgress / 2f else mathConfig.top
    }

    /** Возвращает X-координату для иконки уровня. */
    fun getIconXCoordinates(align: Paint.Align): Float {
        val stepX = getStepX()
        return when (align) {
            Paint.Align.LEFT -> if (mathConfig.startPosition == TimelineMathConfig.StartPosition.CENTER) startPositionX - mathConfig.marginHorizontalImage - mathConfig.sizeImageLvl else -startPositionX + stepX + mathConfig.marginHorizontalImage
            Paint.Align.CENTER -> startPositionX
            Paint.Align.RIGHT -> -(startPositionX - mathConfig.marginHorizontalImage)
        }
    }

    /** Возвращает Y-координату для иконки уровня. */
    fun getIconYCoordinates(i: Int): Float {
        return (mathConfig.stepY * i) + mathConfig.marginTopTitle - (mathConfig.stepY - mathConfig.sizeImageLvl) / 2
    }

    /** Возвращает X-координату заголовка уровня. */
    fun getTitleXCoordinates(align: Paint.Align): Float {
        val stepX = getStepX()
        return when (align) {
            Paint.Align.LEFT -> if (mathConfig.startPosition == TimelineMathConfig.StartPosition.CENTER) startPositionX - mathConfig.marginHorizontalText else -startPositionX + stepX
            Paint.Align.CENTER -> startPositionX
            Paint.Align.RIGHT -> -(startPositionX - mathConfig.marginHorizontalText)
        }
    }

    /** Возвращает Y-координату заголовка уровня. */
    fun getTitleYCoordinates(i: Int): Float {
        return (mathConfig.stepY * i) + mathConfig.marginTopTitle
    }

    /** Возвращает Y-координату описания уровня. */
    fun getDescriptionYCoordinates(i: Int): Float {
        return (mathConfig.stepY * i) + mathConfig.marginTopTitle + mathConfig.sizeTitle + mathConfig.marginTopDescription
    }

    /** Вспомогательная функция для расчёта смещения X-координаты для текущего шага. */
    private fun getHorizontalOffset(i: Int): Float {
        val offset = if (i % 2 == 0)
            when (mathConfig.startPosition) {
                TimelineMathConfig.StartPosition.START -> getStepX() - startPositionDisableStrokeX
                TimelineMathConfig.StartPosition.CENTER -> startPositionX - startPositionDisableStrokeX - mathConfig.marginHorizontalStroke
                TimelineMathConfig.StartPosition.END -> -startPositionDisableStrokeX
            }
        else startPositionDisableStrokeX - startPositionX + mathConfig.marginHorizontalStroke

        Log.d(
            TAG,
            "getHorizontalOffset i: $i, offset: $offset, startPositionDisableStrokeX: $startPositionDisableStrokeX, startPositionX: $startPositionX, marginHorizontalStroke: ${mathConfig.marginHorizontalStroke}"
        )

        return offset
    }

    /** Инициализирует значение [startPositionDisableStrokeX] на основе прогресса шага. */
    private fun initStartPositionDisableStrokeX(lvl: TimelineStep, i: Int) {
        startPositionDisableStrokeX =
            when (mathConfig.startPosition) {
                TimelineMathConfig.StartPosition.START -> if (i == 0) getStepXFirst() / 100 * lvl.percents else getStepX() / 100 * lvl.percents
                TimelineMathConfig.StartPosition.CENTER -> if (i == 0) getStepXFirst() / 100 * lvl.percents else getStepX() / 100 * lvl.percents
                TimelineMathConfig.StartPosition.END -> if (i == 0) getStepX() / 100 * lvl.percents else getStepX() / 100 * lvl.percents
            }

        Log.d(
            TAG,
            "initStartPositionDisableStrokeX i: $i, startPositionDisableStrokeX: $startPositionDisableStrokeX"
        )
    }

    /** Возвращает стандартное вертикальное смещение между шагами. */
    private fun getStandartDYMove(i: Int): Float {
        return if (i == mathConfig.steps.size - 1) mathConfig.stepY / 2 else mathConfig.stepY
    }

    /** Возвращает ширину горизонтального шага между элементами (без учёта отступов). */
    private fun getStepX(): Float {
        return (measuredWidth - mathConfig.marginHorizontalStroke * 2)
    }

    /** Возвращает ширину первого шага (для расчёта первой линии). */
    private fun getStepXFirst(): Float {
        return startPositionX - mathConfig.marginHorizontalStroke
    }

    /** Определяет направление горизонтального смещения (влево/вправо) для текущего шага. */
    private fun getHorizontalStep(i: Int): Float {
        val stepX = getStepX()

        val step = if (i % 2 == 0) -stepX else stepX

        Log.d(TAG, "getHorizontalStep i: $i, step: $step")

        return step
    }
}