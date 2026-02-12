package com.dmitrypokrasov.timelineview.ui

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer
import com.dmitrypokrasov.timelineview.strategy.TimelineStrategyRegistry
import com.dmitrypokrasov.timelineview.strategy.TimelineStrategyRegistryContract

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
        updateAccessibilityDescription()
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

    /**
     * Устанавливает локальный реестр стратегий для этого экземпляра.
     */
    fun setStrategyRegistry(registry: TimelineStrategyRegistryContract) {
        controller.setStrategyRegistry(registry)
        requestLayout()
        invalidate()
    }

    /**
     * Создаёт локальный реестр стратегий и применяет конфигурацию через builder.
     */
    fun setStrategyRegistry(configure: TimelineStrategyRegistryContract.() -> Unit) {
        val registry = TimelineStrategyRegistry.createLocalRegistry()
        configure(registry)
        setStrategyRegistry(registry)
    }

    /**
     * Устанавливает callback на клик по иконке шага.
     */
    fun setOnStepClickListener(listener: (index: Int, step: TimelineStepData) -> Unit) {
        controller.setOnStepClickListener(listener)
        isClickable = true
        updateAccessibilityDescription()
    }

    /**
     * Устанавливает callback на клик по progress-иконке.
     */
    fun setOnProgressIconClickListener(listener: () -> Unit) {
        controller.setOnProgressIconClickListener(listener)
        isClickable = true
        updateAccessibilityDescription()
    }

    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measuredHeight = controller.measure(measuredWidth)
        updateAccessibilityDescription()
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        controller.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> true
            MotionEvent.ACTION_UP -> {
                val handled = controller.handleClick(event.x, event.y)
                if (handled) {
                    performClick()
                    true
                } else {
                    super.onTouchEvent(event)
                }
            }

            else -> super.onTouchEvent(event)
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun updateAccessibilityDescription() {
        contentDescription = controller.getAccessibilityDescription()
    }

}
