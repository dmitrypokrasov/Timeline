package com.dmitrypokrasov.timelineview.core

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.dmitrypokrasov.timelineview.linear.LinearMathConfig
import com.dmitrypokrasov.timelineview.snake.SnakeMathConfig
import com.dmitrypokrasov.timelineview.snake.SnakeTimelineMath
import com.dmitrypokrasov.timelineview.snake.SnakeTimelineUi
import com.dmitrypokrasov.timelineview.snake.SnakeUiConfig

/**
 * Кастомное View для отображения вертикального таймлайна с уровнями прогресса.
 *
 * Поддерживает настройку через XML и код:
 * - визуальные параметры (цвета, размеры, иконки)
 * - математические параметры (отступы, шаги, смещения)
 * - автоматическую отрисовку линий прогресса и иконок
 *
 * Отрисовка включает:
 * - пройденные шаги (enable path)
 * - непройденные шаги (disable path)
 * - иконку текущего шага (progress icon)
 * - заголовки и описания
 *
 * Использует [SnakeTimelineMath] как движок для всех вычислений и генерации координат.
 *
 * @constructor Создаёт [TimelineView], читая параметры из XML или по умолчанию.
 */
class TimelineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "TimelineView"
    }

    /** Математический движок таймлайна. */
    private var timelineMath: TimelineMathEngine

    /** Рендерер визуальной части таймлайна. */
    private var timelineUi: TimelineUiRenderer

    /** Текущая сторона отрисовки (LEFT/RIGHT). */
    private var currentSide: Paint.Align = Paint.Align.RIGHT

    init {
        val (mathCfg, uiCfg) = TimelineConfigParser(context).parse(attrs)
        timelineMath = SnakeTimelineMath(mathCfg as SnakeMathConfig)
        timelineUi = SnakeTimelineUi(uiCfg as SnakeUiConfig)

        initTools()
    }

    /**
     * Обновляет список шагов и перерисовывает таймлайн.
     */
    fun replaceSteps(steps: List<TimelineStep>) {
        timelineMath.replaceSteps(steps)
        requestLayout()
    }

    /**
     * Устанавливает пользовательский математический движок.
     */
    fun setMathEngine(engine: TimelineMathEngine) {
        timelineMath = engine
        initTools()
        requestLayout()
    }

    /**
     * Устанавливает пользовательский рендерер интерфейса.
     */
    fun setUiRenderer(renderer: TimelineUiRenderer) {
        timelineUi = renderer
        initTools()
        requestLayout()
    }

    /**
     * Programmatically sets the start position of the timeline.
     */
    fun setStartPosition(position: TimelineMathConfig.StartPosition) {
        when (val cfg = timelineMath.getConfig()) {
            is SnakeMathConfig -> timelineMath.setConfig(cfg.copy(startPosition = position))
            is LinearMathConfig -> timelineMath.setConfig(cfg.copy(startPosition = position))
        }
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredWidth = MeasureSpec.getSize(widthMeasureSpec)
        timelineMath.setMeasuredWidth(measuredWidth)
        timelineMath.buildPath(timelineUi.getCompletedPath(), timelineUi.getRemainingPath())
        setMeasuredDimension(measuredWidth, timelineMath.getMeasuredHeight())
    }

    override fun onDraw(canvas: Canvas) {
        timelineUi.prepareStrokePaint()

        canvas.translate(timelineMath.getStartPosition(), 0f)
        timelineUi.drawCompletedPath(canvas)
        timelineUi.drawRemainingPath(canvas)

        // Отрисовка шагов: иконки, заголовки, описания
        timelineUi.prepareTextPaint()
        timelineUi.prepareIconPaint()

        currentSide = Paint.Align.RIGHT
        var printProgressIcon = false
        var align = Paint.Align.LEFT

        timelineMath.getSteps().forEachIndexed { i, step ->
            if (i == 0) {
                timelineUi.drawTitle(
                    canvas,
                    resources.getString(step.title),
                    timelineMath.getTitleXCoordinates(align),
                    timelineMath.getTitleYCoordinates(i),
                    align
                )
                timelineUi.drawDescription(
                    canvas,
                    resources.getString(step.description),
                    timelineMath.getTitleXCoordinates(align),
                    timelineMath.getDescriptionYCoordinates(i),
                    align
                )
                timelineUi.drawStepIcon(
                    step,
                    canvas,
                    align,
                    context,
                    timelineMath.getIconXCoordinates(align),
                    timelineMath.getIconYCoordinates(i)
                )

                if (step.percents != 100) {
                    timelineUi.drawProgressIcon(
                        canvas,
                        timelineMath.getLeftCoordinates(step),
                        timelineMath.getTopCoordinates(step)
                    )
                    printProgressIcon = true
                }
            } else {
                if (step.percents != 100 && !printProgressIcon) {
                    timelineUi.drawProgressIcon(
                        canvas,
                        timelineMath.getHorizontalIconOffset(i),
                        timelineMath.getVerticalOffset(i)
                    )
                    printProgressIcon = true
                }

                align = if (align == Paint.Align.LEFT) Paint.Align.RIGHT else Paint.Align.LEFT

                timelineUi.drawTitle(
                    canvas,
                    resources.getString(step.title),
                    timelineMath.getTitleXCoordinates(align),
                    timelineMath.getTitleYCoordinates(i),
                    align
                )
                timelineUi.drawDescription(
                    canvas,
                    resources.getString(step.description),
                    timelineMath.getTitleXCoordinates(align),
                    timelineMath.getDescriptionYCoordinates(i),
                    align
                )
                timelineUi.drawStepIcon(
                    step,
                    canvas,
                    align,
                    context,
                    timelineMath.getIconXCoordinates(align),
                    timelineMath.getIconYCoordinates(i)
                )
            }

            currentSide =
                if (currentSide == Paint.Align.LEFT) Paint.Align.RIGHT else Paint.Align.LEFT
        }
    }

    /**
     * Инициализирует визуальные элементы (битмапы, pathEffect).
     */
    private fun initTools() {
        Log.d(
            TAG,
            "initTools timelineMathConfig: ${timelineMath.getConfig()}, timelineUiConfig: ${timelineUi.getConfig()}"
        )

        timelineUi.initTools(timelineMath.getConfig(), context)
    }

}
