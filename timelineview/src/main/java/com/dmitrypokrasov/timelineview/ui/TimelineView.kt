package com.dmitrypokrasov.timelineview.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.dmitrypokrasov.timelineview.data.TimelineStep
import com.dmitrypokrasov.timelineview.domain.LinearTimelineMath
import com.dmitrypokrasov.timelineview.domain.LinearTimelineUi
import com.dmitrypokrasov.timelineview.domain.SnakeTimelineMath
import com.dmitrypokrasov.timelineview.domain.SnakeTimelineUi
import com.dmitrypokrasov.timelineview.domain.TimelineMathEngine
import com.dmitrypokrasov.timelineview.domain.TimelineUiRenderer
import com.dmitrypokrasov.timelineview.domain.data.TimelineMathConfig
import com.dmitrypokrasov.timelineview.domain.data.TimelineUiConfig
import com.dmitrypokrasov.timelineview.ui.TimelineOrientation

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
 * Использует реализации [TimelineMathEngine] и [TimelineUiRenderer], выбираемые
 * в зависимости от [TimelineOrientation].
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

    /** Рендерер таймлайна. */
    private var timelineUi: TimelineUiRenderer

    /** Текущая ориентация таймлайна. */
    private var orientation: TimelineOrientation

    /** Список шагов для отрисовки. */
    private var steps: List<TimelineStep> = emptyList()

    /** Текущая сторона отрисовки (LEFT/RIGHT). */
    private var currentSide: Paint.Align = Paint.Align.RIGHT

    init {
        val (mathConfig, uiConfig, parsedOrientation) = ConfigParser(context).parse(attrs)
        orientation = parsedOrientation

        timelineMath = when (orientation) {
            TimelineOrientation.SNAKE_VERTICAL -> SnakeTimelineMath(mathConfig)
            TimelineOrientation.LINEAR_VERTICAL ->
                LinearTimelineMath(mathConfig, LinearTimelineMath.Orientation.VERTICAL)
            TimelineOrientation.LINEAR_HORIZONTAL ->
                LinearTimelineMath(mathConfig, LinearTimelineMath.Orientation.HORIZONTAL)
        }

        timelineUi = when (orientation) {
            TimelineOrientation.SNAKE_VERTICAL -> SnakeTimelineUi(uiConfig)
            TimelineOrientation.LINEAR_VERTICAL,
            TimelineOrientation.LINEAR_HORIZONTAL -> LinearTimelineUi(uiConfig)
        }

        steps = mathConfig.steps
        initTools(mathConfig, uiConfig)
    }

    /**
     * Обновляет список шагов и перерисовывает таймлайн.
     */
    fun replaceSteps(steps: List<TimelineStep>) {
        this.steps = steps
        timelineMath.replaceSteps(steps)
        requestLayout()
    }

    /**
     * Устанавливает новую конфигурацию таймлайна.
     *
     * Позволяет передать собственные реализации движка математики и рендерера UI.
     * Если они не указаны, будут использованы стандартные реализации в зависимости
     * от [orientation].
     */
    fun setConfig(
        timelineMathConfig: TimelineMathConfig,
        timelineUiConfig: TimelineUiConfig,
        mathEngine: TimelineMathEngine? = null,
        uiRenderer: TimelineUiRenderer? = null
    ) {
        timelineMath = mathEngine ?: when (orientation) {
            TimelineOrientation.SNAKE_VERTICAL -> SnakeTimelineMath(timelineMathConfig)
            TimelineOrientation.LINEAR_VERTICAL ->
                LinearTimelineMath(timelineMathConfig, LinearTimelineMath.Orientation.VERTICAL)
            TimelineOrientation.LINEAR_HORIZONTAL ->
                LinearTimelineMath(timelineMathConfig, LinearTimelineMath.Orientation.HORIZONTAL)
        }

        timelineUi = uiRenderer ?: when (orientation) {
            TimelineOrientation.SNAKE_VERTICAL -> SnakeTimelineUi(timelineUiConfig)
            TimelineOrientation.LINEAR_VERTICAL,
            TimelineOrientation.LINEAR_HORIZONTAL -> LinearTimelineUi(timelineUiConfig)
        }

        steps = timelineMathConfig.steps

        initTools(timelineMathConfig, timelineUiConfig)

        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredWidth = MeasureSpec.getSize(widthMeasureSpec)
        timelineMath.setMeasuredWidth(measuredWidth)
        timelineMath.buildPath(timelineUi.pathEnable, timelineUi.pathDisable)
        setMeasuredDimension(measuredWidth, timelineMath.getMeasuredHeight())
    }

    override fun onDraw(canvas: Canvas) {
        timelineUi.resetFromPaintTools()

        canvas.translate(timelineMath.getStartPosition(), 0f)
        timelineUi.drawProgressPath(canvas)
        timelineUi.drawDisablePath(canvas)

        // Отрисовка шагов: иконки, заголовки, описания
        timelineUi.resetFromTextTools()
        timelineUi.resetFromIconTools()

        currentSide = Paint.Align.RIGHT
        var printProgressIcon = false
        var align = Paint.Align.LEFT

        steps.forEachIndexed { i, lvl ->
            if (i == 0) {
                timelineUi.printTitle(
                    canvas,
                    resources.getString(lvl.title),
                    timelineMath.getTitleXCoordinates(align),
                    timelineMath.getTitleYCoordinates(i),
                    align
                )
                timelineUi.printDescription(
                    canvas,
                    resources.getString(lvl.description),
                    timelineMath.getTitleXCoordinates(align),
                    timelineMath.getDescriptionYCoordinates(i),
                    align
                )
                timelineUi.printIcon(
                    lvl,
                    canvas,
                    align,
                    context,
                    timelineMath.getIconXCoordinates(align),
                    timelineMath.getIconYCoordinates(i)
                )

                if (lvl.percents != 100) {
                    timelineUi.drawProgressBitmap(
                        canvas,
                        timelineMath.getLeftCoordinates(lvl),
                        timelineMath.getTopCoordinates(lvl)
                    )
                    printProgressIcon = true
                }
            } else {
                if (lvl.percents != 100 && !printProgressIcon) {
                    timelineUi.drawProgressBitmap(
                        canvas,
                        timelineMath.getHorizontalIconOffset(i),
                        timelineMath.getVerticalOffset(i)
                    )
                    printProgressIcon = true
                }

                align = if (align == Paint.Align.LEFT) Paint.Align.RIGHT else Paint.Align.LEFT

                timelineUi.printTitle(
                    canvas,
                    resources.getString(lvl.title),
                    timelineMath.getTitleXCoordinates(align),
                    timelineMath.getTitleYCoordinates(i),
                    align
                )
                timelineUi.printDescription(
                    canvas,
                    resources.getString(lvl.description),
                    timelineMath.getTitleXCoordinates(align),
                    timelineMath.getDescriptionYCoordinates(i),
                    align
                )
                timelineUi.printIcon(
                    lvl,
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
    private fun initTools(
        timelineMathConfig: TimelineMathConfig,
        timelineUiConfig: TimelineUiConfig
    ) {
        Log.d(
            TAG,
            "initTools timelineMathConfig: $timelineMathConfig, timelineUiConfig: $timelineUiConfig"
        )

        timelineUi.initTools(timelineMathConfig, context)
    }

}
