package com.dmitrypokrasov.timelineview.ui

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineConfigParser
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.math.TimelineLayout
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.model.TimelineStep
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.model.toTimelineStepData
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer
import com.dmitrypokrasov.timelineview.strategy.TimelineViewStrategyController

class TimelineViewController(
    private val context: Context,
    attrs: AttributeSet?
) {

    companion object {
        private const val TAG = "TimelineView"
    }

    private var timelineMath: TimelineMathEngine
    private var timelineUi: TimelineUiRenderer
    private var layout: TimelineLayout? = null
    private val strategyController = TimelineViewStrategyController()
    private val heightCalculator = TimelineHeightCalculator()

    init {
        val config = TimelineConfigParser(context).parse(attrs)
        val resolved = strategyController.resolve(config)
        timelineMath = resolved.math
        timelineUi = resolved.ui
        initTools()
    }

    fun replaceSteps(steps: List<TimelineStepData>) {
        timelineMath.replaceSteps(steps)
    }

    fun replaceLegacySteps(steps: List<TimelineStep>) {
        replaceSteps(steps.toTimelineStepData(context))
    }

    fun setMathEngine(engine: TimelineMathEngine) {
        timelineMath = engine
        initTools()
    }

    fun setUiRenderer(renderer: TimelineUiRenderer) {
        timelineUi = renderer
        initTools()
    }

    fun setStrategy(mathStrategy: TimelineMathStrategy, uiStrategy: TimelineUiStrategy) {
        val mathConfig = timelineMath.getConfig()
        val uiConfig = timelineUi.getConfig()
        val resolved = strategyController.resolve(mathStrategy, uiStrategy, mathConfig, uiConfig)
        timelineMath = resolved.math
        timelineUi = resolved.ui
        initTools()
    }

    fun setStrategy(strategy: TimelineStrategy) {
        setStrategy(strategy.math, strategy.ui)
    }

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
    }

    fun setStrategies(mathEngine: TimelineMathEngine, uiRenderer: TimelineUiRenderer) {
        timelineMath = mathEngine
        timelineUi = uiRenderer
        initTools()
    }

    fun measure(width: Int): Int {
        timelineMath.setMeasuredWidth(width)
        timelineMath.buildPath(timelineUi.getCompletedPath(), timelineUi.getRemainingPath())
        layout = timelineMath.buildLayout()
        return heightCalculator.calculateHeight(timelineMath, timelineUi.getConfig())
    }

    fun draw(canvas: Canvas) {
        timelineUi.prepareStrokePaint()

        canvas.translate(timelineMath.getStartPosition(), 0f)
        timelineUi.drawCompletedPath(canvas)
        timelineUi.drawRemainingPath(canvas)

        timelineUi.prepareTextPaint()
        timelineUi.prepareIconPaint()

        layout?.progressIcon?.let { progress ->
            timelineUi.drawProgressIcon(canvas, progress.left, progress.top)
        }

        layout?.steps?.forEachIndexed { index, stepLayout ->
            timelineUi.drawTitle(
                canvas,
                stepLayout.step.title ?: "",
                stepLayout.titleX,
                stepLayout.titleY,
                stepLayout.textAlign
            )
            timelineUi.drawDescription(
                canvas,
                stepLayout.step.description ?: "",
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

    private fun initTools() {
        Log.d(
            TAG,
            "initTools timelineMathConfig: ${timelineMath.getConfig()}, timelineUiConfig: ${timelineUi.getConfig()}"
        )

        timelineUi.initTools(timelineMath.getConfig(), context)
    }
}
