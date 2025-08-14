package com.dmitrypokrasov.timelineview.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.dmitrypokrasov.timelineview.data.TimelineStep
import com.dmitrypokrasov.timelineview.domain.TimelineMath
import com.dmitrypokrasov.timelineview.domain.TimelineUi
import com.dmitrypokrasov.timelineview.domain.data.TimelineMathConfig
import com.dmitrypokrasov.timelineview.domain.data.TimelineUiConfig

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
 * Использует [TimelineMath] как движок для всех вычислений и генерации координат.
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

    /** Математическая конфигурация таймлайна. */
    private var timelineMath: TimelineMath

    /** Визуальная конфигурация таймлайна. */
    private var timelineUi: TimelineUi

    /** Текущая сторона отрисовки (LEFT/RIGHT). */
    private var currentSide: Paint.Align = Paint.Align.RIGHT

    init {
        val (mathConfig, uiConfig) = ConfigParser(context).parse(attrs)
        timelineMath = TimelineMath(mathConfig)
        timelineUi = TimelineUi(uiConfig)
        initTools(mathConfig, uiConfig)
    }

    /**
     * Обновляет список шагов и перерисовывает таймлайн.
     */
    fun replaceSteps(steps: List<TimelineStep>) {
        timelineMath.replaceSteps(steps)
        requestLayout()
    }

    /**
     * Устанавливает новую конфигурацию таймлайна.
     */
    fun setConfig(timelineMathConfig: TimelineMathConfig, timelineUiConfig: TimelineUiConfig) {
        if (timelineMath.mathConfig == timelineMathConfig && timelineUi.uiConfig == timelineUiConfig) return

        timelineMath = TimelineMath(timelineMathConfig)
        timelineUi = TimelineUi(timelineUiConfig)

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

        timelineMath.mathConfig.steps.forEachIndexed { i, lvl ->
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
