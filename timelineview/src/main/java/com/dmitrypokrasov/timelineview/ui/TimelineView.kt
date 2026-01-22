package com.dmitrypokrasov.timelineview.ui

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.dmitrypokrasov.timelineview.config.TimelineConfigParser
import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.model.TimelineStep
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.math.TimelineLayout
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer
import com.dmitrypokrasov.timelineview.strategy.TimelineViewStrategyController

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

    companion object {
        private const val TAG = "TimelineView"
    }

    /** Математический движок таймлайна. */
    private var timelineMath: TimelineMathEngine

    /** Рендерер визуальной части таймлайна. */
    private var timelineUi: TimelineUiRenderer

    /** Текущая сторона отрисовки (LEFT/RIGHT). */
    private var layout: TimelineLayout? = null

    private var stepTextCache: List<StepText> = emptyList()
    private val strategyController = TimelineViewStrategyController()

    private data class StepText(
        val title: String,
        val description: String
    )

    init {
        val config = TimelineConfigParser(context).parse(attrs)
        val resolved = strategyController.resolve(config)
        timelineMath = resolved.math
        timelineUi = resolved.ui

        initTools()
        rebuildTextCache()
    }

    /**
     * Обновляет список шагов и перерисовывает таймлайн.
     */
    fun replaceSteps(steps: List<TimelineStep>) {
        timelineMath.replaceSteps(steps)
        rebuildTextCache()
        requestLayout()
        invalidate()
    }

    /**
     * Устанавливает пользовательский математический движок.
     */
    fun setMathEngine(engine: TimelineMathEngine) {
        timelineMath = engine
        initTools()
        rebuildTextCache()
        requestLayout()
        invalidate()
    }

    /**
     * Устанавливает пользовательский рендерер интерфейса.
     */
    fun setUiRenderer(renderer: TimelineUiRenderer) {
        timelineUi = renderer
        initTools()
        requestLayout()
        invalidate()
    }

    /**
     * Устанавливает стратегии расчётов и отрисовки.
     */
    fun setStrategy(mathStrategy: TimelineMathStrategy, uiStrategy: TimelineUiStrategy) {
        val mathConfig = timelineMath.getConfig()
        val uiConfig = timelineUi.getConfig()
        val resolved = strategyController.resolve(mathStrategy, uiStrategy, mathConfig, uiConfig)
        timelineMath = resolved.math
        timelineUi = resolved.ui
        initTools()
        rebuildTextCache()
        requestLayout()
        invalidate()
    }

    /**
     * Устанавливает стратегии расчётов и отрисовки через композитную модель.
     */
    fun setStrategy(strategy: TimelineStrategy) {
        setStrategy(strategy.math, strategy.ui)
    }

    /**
     * Устанавливает стратегии расчётов и отрисовки через зарегистрированные ключи.
     */
    fun setStrategy(mathStrategyKey: StrategyKey?, uiStrategyKey: StrategyKey?) {
        val mathConfig = timelineMath.getConfig()
        val uiConfig = timelineUi.getConfig()
        val resolved = strategyController.resolve(
            mathStrategyKey = mathStrategyKey,
            uiStrategyKey = uiStrategyKey,
            fallbackMath = TimelineMathStrategy.Snake,
            fallbackUi = TimelineUiStrategy.Snake,
            mathConfig = mathConfig,
            uiConfig = uiConfig
        )
        timelineMath = resolved.math
        timelineUi = resolved.ui
        initTools()
        rebuildTextCache()
        requestLayout()
        invalidate()
    }

    /**
     * Устанавливает одновременно математический движок и рендерер интерфейса.
     */
    fun setStrategies(mathEngine: TimelineMathEngine, uiRenderer: TimelineUiRenderer) {
        timelineMath = mathEngine
        timelineUi = uiRenderer
        initTools()
        rebuildTextCache()
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredWidth = MeasureSpec.getSize(widthMeasureSpec)
        timelineMath.setMeasuredWidth(measuredWidth)
        timelineMath.buildPath(timelineUi.getCompletedPath(), timelineUi.getRemainingPath())
        layout = timelineMath.buildLayout()
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

        layout?.progressIcon?.let { progress ->
            timelineUi.drawProgressIcon(canvas, progress.left, progress.top)
        }

        layout?.steps?.forEachIndexed { index, stepLayout ->
            val stepText = stepTextCache.getOrNull(index)
            timelineUi.drawTitle(
                canvas,
                stepText?.title ?: resources.getString(stepLayout.step.title),
                stepLayout.titleX,
                stepLayout.titleY,
                stepLayout.textAlign
            )
            timelineUi.drawDescription(
                canvas,
                stepText?.description ?: resources.getString(stepLayout.step.description),
                stepLayout.descriptionX,
                stepLayout.descriptionY,
                stepLayout.textAlign
            )
            timelineUi.drawStepIcon(
                stepLayout.step,
                canvas,
                stepLayout.textAlign,
                context,
                stepLayout.iconX,
                stepLayout.iconY
            )
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

    private fun rebuildTextCache() {
        stepTextCache = timelineMath.getSteps().map { step ->
            StepText(
                title = resources.getString(step.title),
                description = resources.getString(step.description)
            )
        }
    }

}
