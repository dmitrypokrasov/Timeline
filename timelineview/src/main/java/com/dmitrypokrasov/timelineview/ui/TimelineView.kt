package com.dmitrypokrasov.timelineview.ui

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.model.TimelineStep
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer

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
 * Использует выбранный [TimelineMathEngine] как движок для вычислений и генерации координат.
 *
 * @constructor Создаёт [TimelineView], читая параметры из XML или по умолчанию.
 */
class TimelineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val controller = TimelineViewController(context, attrs)

    init {
        // Intentionally empty. Initialization is delegated to the controller.
    }

    /**
     * Обновляет список шагов и перерисовывает таймлайн.
     */
    fun replaceSteps(steps: List<TimelineStepData>) {
        controller.replaceSteps(steps)
        requestLayout()
        invalidate()
    }

    /**
     * Обновляет список шагов старой модели и перерисовывает таймлайн.
     */
    @JvmName("replaceLegacySteps")
    fun replaceSteps(steps: List<TimelineStep>) {
        controller.replaceLegacySteps(steps)
        requestLayout()
        invalidate()
    }

    /**
     * Устанавливает пользовательский математический движок.
     */
    fun setMathEngine(engine: TimelineMathEngine) {
        controller.setMathEngine(engine)
        requestLayout()
        invalidate()
    }

    /**
     * Устанавливает пользовательский рендерер интерфейса.
     */
    fun setUiRenderer(renderer: TimelineUiRenderer) {
        controller.setUiRenderer(renderer)
        requestLayout()
        invalidate()
    }

    /**
     * Устанавливает стратегии расчётов и отрисовки.
     */
    fun setStrategy(mathStrategy: TimelineMathStrategy, uiStrategy: TimelineUiStrategy) {
        controller.setStrategy(mathStrategy, uiStrategy)
        requestLayout()
        invalidate()
    }

    /**
     * Устанавливает стратегии расчётов и отрисовки через композитную модель.
     */
    fun setStrategy(strategy: TimelineStrategy) {
        controller.setStrategy(strategy)
        requestLayout()
        invalidate()
    }

    /**
     * Устанавливает стратегии расчётов и отрисовки через зарегистрированные ключи.
     */
    fun setStrategy(mathStrategyKey: StrategyKey?, uiStrategyKey: StrategyKey?) {
        controller.setStrategy(mathStrategyKey, uiStrategyKey)
        requestLayout()
        invalidate()
    }

    /**
     * Устанавливает одновременно математический движок и рендерер интерфейса.
     */
    fun setStrategies(mathEngine: TimelineMathEngine, uiRenderer: TimelineUiRenderer) {
        controller.setStrategies(mathEngine, uiRenderer)
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measuredHeight = controller.measure(measuredWidth)
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        controller.draw(canvas)
    }

}
