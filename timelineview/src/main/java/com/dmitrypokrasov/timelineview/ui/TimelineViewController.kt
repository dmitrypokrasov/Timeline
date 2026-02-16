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
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.math.data.TimelineLayoutStep
import com.dmitrypokrasov.timelineview.math.data.TimelineProgressIcon
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer
import com.dmitrypokrasov.timelineview.strategy.TimelineStrategyRegistry
import com.dmitrypokrasov.timelineview.strategy.TimelineStrategyRegistryContract
import com.dmitrypokrasov.timelineview.strategy.TimelineViewStrategyController

class TimelineViewController(
    private val context: Context,
    attrs: AttributeSet?,
    registry: TimelineStrategyRegistryContract = TimelineStrategyRegistry
) {

    companion object {
        private const val TAG = "TimelineView"
    }

    private val config = TimelineConfigParser(context).parse(attrs)
    private var timelineMath: TimelineMathEngine
    private var timelineUi: TimelineUiRenderer
    private var layout: TimelineLayout? = null
    private var strategyController = TimelineViewStrategyController(registry)
    private val heightCalculator = TimelineHeightCalculator()
    private var onStepClickListener: ((index: Int, step: TimelineStepData) -> Unit)? = null
    private var onProgressIconClickListener: (() -> Unit)? = null
    private var onProgressIconClickListenerWithStep: ((step: TimelineStepData?) -> Unit)? = null

    init {
        val resolved = strategyController.resolve(config)
        timelineMath = resolved.math
        timelineUi = resolved.ui
        initTools()
    }

    fun replaceSteps(steps: List<TimelineStepData>) {
        timelineMath.replaceSteps(steps)
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

    fun setStrategyRegistry(registry: TimelineStrategyRegistryContract) {
        strategyController = TimelineViewStrategyController(registry)
        val resolved = strategyController.resolve(config)
        timelineMath = resolved.math
        timelineUi = resolved.ui
        initTools()
    }

    fun setStrategyRegistry(configure: TimelineStrategyRegistryContract.() -> Unit) {
        val registry = TimelineStrategyRegistry.createLocalRegistry()
        configure(registry)
        setStrategyRegistry(registry)
    }

    fun setOnStepClickListener(listener: ((index: Int, step: TimelineStepData) -> Unit)?) {
        onStepClickListener = listener
    }

    fun setOnProgressIconClickListener(listener: (() -> Unit)?) {
        onProgressIconClickListener = listener
    }

    fun setOnProgressIconClickListenerWithStep(listener: ((step: TimelineStepData?) -> Unit)?) {
        onProgressIconClickListenerWithStep = listener
    }

    fun handleClick(x: Float, y: Float): Boolean {
        val sizes = timelineMath.getConfig().sizes
        val minimumTouchTargetPx = 48f * context.resources.displayMetrics.density
        val translatedX = x - timelineMath.getStartPosition()
        return when (
            val hit = TimelineHitTestHelper.findHit(
                layout = layout,
                stepIconSize = sizes.sizeImageLvl,
                progressIconSize = sizes.sizeIconProgress,
                x = translatedX,
                y = y,
                minStepTouchSize = minimumTouchTargetPx,
                minProgressTouchSize = minimumTouchTargetPx
            )
        ) {
            is TimelineHitTestHelper.HitResult.Step -> {
                onStepClickListener?.invoke(hit.index, hit.step)
                onStepClickListener != null
            }

            TimelineHitTestHelper.HitResult.ProgressIcon -> {
                val progressStep = resolveProgressStep(layout)
                onProgressIconClickListener?.invoke()
                onProgressIconClickListenerWithStep?.invoke(progressStep)
                onProgressIconClickListener != null || onProgressIconClickListenerWithStep != null
            }

            null -> false
        }
    }

    fun getAccessibilityDescription(): String? {
        val parts = mutableListOf<String>()
        if (onStepClickListener != null) {
            val steps = timelineMath.getSteps()
            if (steps.isNotEmpty()) {
                val titles = steps.mapIndexed { index, step ->
                    val title = step.title?.toString()?.takeIf { it.isNotBlank() } ?: "step ${index + 1}"
                    "${index + 1}. $title"
                }
                parts += "Clickable timeline steps: ${titles.joinToString(", ")}"
            }
        }

        if ((onProgressIconClickListener != null || onProgressIconClickListenerWithStep != null) &&
            hasProgressIcon(layout?.progressIcon)
        ) {
            val progressStepTitle = resolveProgressStep(layout)
                ?.title
                ?.toString()
                ?.takeIf { it.isNotBlank() }
            parts += if (progressStepTitle != null) {
                "Progress icon is clickable for $progressStepTitle"
            } else {
                "Progress icon is clickable"
            }
        }

        return parts.takeIf { it.isNotEmpty() }?.joinToString(separator = ". ")
    }

    private fun hasProgressIcon(progressIcon: TimelineProgressIcon?): Boolean = progressIcon != null

    private fun resolveProgressStep(layout: TimelineLayout?): TimelineStepData? {
        val steps = layout?.steps.orEmpty()
        val progressIcon = layout?.progressIcon ?: return null
        if (steps.isEmpty()) return null

        return steps
            .minByOrNull { distanceSquared(it, progressIcon) }
            ?.step
    }

    private fun distanceSquared(stepLayout: TimelineLayoutStep, progressIcon: TimelineProgressIcon): Float {
        val dx = stepLayout.iconX - progressIcon.left
        val dy = stepLayout.iconY - progressIcon.top
        return dx * dx + dy * dy
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

        layout?.steps?.forEach { stepLayout ->
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
