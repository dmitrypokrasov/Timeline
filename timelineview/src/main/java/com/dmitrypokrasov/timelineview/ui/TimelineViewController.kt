package com.dmitrypokrasov.timelineview.ui

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineConfigParser
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.math.data.TimelineProgressIcon
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer
import com.dmitrypokrasov.timelineview.strategy.TimelineStrategyRegistry
import com.dmitrypokrasov.timelineview.strategy.TimelineStrategyRegistryContract
import com.dmitrypokrasov.timelineview.strategy.TimelineViewStrategyController

class TimelineViewController(
    private val ownerView: View,
    private val context: Context,
    attrs: AttributeSet?,
    registry: TimelineStrategyRegistryContract = TimelineStrategyRegistry,
) {
    companion object {
        private const val TAG = "TimelineView"
    }

    private val initialConfig = TimelineConfigParser(context).parse(attrs)
    private var state = TimelineRuntimeState.from(initialConfig)
    private var timelineMath: TimelineMathEngine
    private var timelineUi: TimelineUiRenderer
    private var layout: TimelineLayout? = null
    private var strategyController = TimelineViewStrategyController(registry)
    private val heightCalculator = TimelineHeightCalculator()
    private val lottieOverlayManager = TimelineLottieOverlayManager(ownerView)
    private var onStepClickListener: ((index: Int, step: TimelineStepData) -> Unit)? = null
    private var onProgressIconClickListener: (() -> Unit)? = null

    init {
        val resolved = state.resolve(strategyController)
        timelineMath = resolved.math
        timelineUi = resolved.ui
        initTools()
    }

    fun replaceSteps(steps: List<TimelineStepData>) {
        timelineMath.replaceSteps(steps)
        state = state.withSteps(steps).withMathEngine(timelineMath)
    }

    fun setMathEngine(engine: TimelineMathEngine) {
        timelineMath = engine
        state = state.withMathEngine(engine)
        initTools()
    }

    fun setUiRenderer(renderer: TimelineUiRenderer) {
        timelineUi = renderer
        state = state.withUiRenderer(renderer)
        initTools()
    }

    fun setStrategy(
        mathStrategy: TimelineMathStrategy,
        uiStrategy: TimelineUiStrategy,
    ) {
        state = state.withStrategy(TimelineStrategy(mathStrategy, uiStrategy))
        applyResolvedState()
    }

    fun setStrategy(strategy: TimelineStrategy) {
        state = state.withStrategy(strategy)
        applyResolvedState()
    }

    fun setStrategy(
        mathStrategyKey: StrategyKey?,
        uiStrategyKey: StrategyKey?,
    ) {
        state = state.withStrategyKeys(mathStrategyKey, uiStrategyKey)
        applyResolvedState()
    }

    fun setStrategies(
        mathEngine: TimelineMathEngine,
        uiRenderer: TimelineUiRenderer,
    ) {
        timelineMath = mathEngine
        timelineUi = uiRenderer
        state = state.withMathEngine(mathEngine).withUiRenderer(uiRenderer)
        initTools()
    }

    fun setStrategyRegistry(registry: TimelineStrategyRegistryContract) {
        strategyController = TimelineViewStrategyController(registry)
        applyResolvedState()
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

    fun handleClick(
        x: Float,
        y: Float,
    ): Boolean {
        val sizes = timelineMath.getConfig().sizes
        val minimumTouchTargetPx = 48f * context.resources.displayMetrics.density
        val translatedX = x - timelineMath.getStartPosition()
        return when (
            val hit =
                TimelineHitTestHelper.findHit(
                    layout = layout,
                    stepIconSize = sizes.sizeImageLvl,
                    progressIconSize = sizes.sizeIconProgress,
                    x = translatedX,
                    y = y,
                    minStepTouchSize = minimumTouchTargetPx,
                    minProgressTouchSize = minimumTouchTargetPx,
                )
        ) {
            is TimelineHitTestHelper.HitResult.Step -> {
                onStepClickListener?.invoke(hit.index, hit.step)
                onStepClickListener != null
            }

            TimelineHitTestHelper.HitResult.ProgressIcon -> {
                onProgressIconClickListener?.invoke()
                onProgressIconClickListener != null
            }

            null -> false
        }
    }

    fun getAccessibilityDescription(): String? {
        val parts = mutableListOf<String>()
        if (onStepClickListener != null) {
            val steps = timelineMath.getSteps()
            if (steps.isNotEmpty()) {
                val titles =
                    steps.mapIndexed { index, step ->
                        val title = step.title?.toString()?.takeIf { it.isNotBlank() } ?: "step ${index + 1}"
                        "${index + 1}. $title"
                    }
                parts += "Clickable timeline steps: ${titles.joinToString(", ")}"
            }
        }

        if (onProgressIconClickListener != null && hasProgressIcon(layout?.progressIcon)) {
            parts += "Progress icon is clickable"
        }

        return parts.takeIf { it.isNotEmpty() }?.joinToString(separator = ". ")
    }

    fun measure(width: Int): Int {
        timelineMath.setMeasuredWidth(width)
        timelineMath.buildPath(timelineUi.getCompletedPath(), timelineUi.getRemainingPath())
        layout = timelineMath.buildLayout()
        return heightCalculator.calculateHeight(layout, timelineMath, timelineUi)
    }

    fun draw(canvas: Canvas) {
        timelineUi.prepareStrokePaint()

        canvas.save()
        canvas.translate(timelineMath.getStartPosition(), 0f)
        timelineUi.drawCompletedPath(canvas)
        timelineUi.drawRemainingPath(canvas)

        timelineUi.prepareTextPaint()
        timelineUi.prepareIconPaint()

        drawProgressIcon(canvas, layout)

        val resolvedTextBlocks =
            TimelineTextBlockResolver.resolve(
                layout = layout,
                mathEngine = timelineMath,
                uiRenderer = timelineUi,
            )

        layout?.steps?.forEachIndexed { index, stepLayout ->
            val textBlock = resolvedTextBlocks.getOrNull(index) ?: return@forEachIndexed
            val title = stepLayout.step.title ?: ""
            val description = stepLayout.step.description ?: ""

            timelineUi.drawTitle(
                canvas,
                title,
                stepLayout.titleX,
                textBlock.titleTop,
                stepLayout.textAlign,
                stepLayout.titleWidth,
            )
            timelineUi.drawDescription(
                canvas,
                description,
                stepLayout.descriptionX,
                textBlock.descriptionTop,
                stepLayout.textAlign,
                stepLayout.descriptionWidth,
            )
            timelineUi.drawStepIcon(
                stepLayout.step,
                canvas,
                stepLayout.textAlign,
                context,
                stepLayout.iconX,
                stepLayout.iconY,
            )
            lottieOverlayManager.draw(
                canvas = canvas,
                context = context,
                spec = stepLayout.step.badgeAnimation,
                left = stepLayout.iconX,
                top = stepLayout.iconY,
                size = timelineMath.getConfig().sizes.sizeImageLvl,
            )
        }

        canvas.restore()
    }

    fun release() {
        lottieOverlayManager.clear()
    }

    private fun drawProgressIcon(
        canvas: Canvas,
        layout: TimelineLayout?,
    ) {
        val progress = layout?.progressIcon ?: return
        timelineUi.drawProgressIcon(canvas, progress.left, progress.top)
        val progressStep =
            layout.progressStepIndex?.let { index ->
                timelineMath.getSteps().getOrNull(index)
            }
        lottieOverlayManager.draw(
            canvas = canvas,
            context = context,
            spec = progressStep?.progressAnimation,
            left = progress.left,
            top = progress.top,
            size = timelineMath.getConfig().sizes.sizeIconProgress,
        )
    }

    private fun applyResolvedState() {
        val resolved = state.resolve(strategyController)
        timelineMath = resolved.math
        timelineUi = resolved.ui
        state = state.withMathEngine(timelineMath).withUiRenderer(timelineUi)
        initTools()
    }

    private fun hasProgressIcon(progressIcon: TimelineProgressIcon?): Boolean = progressIcon != null

    private fun initTools() {
        Log.d(
            TAG,
            "initTools timelineMathConfig: ${timelineMath.getConfig()}, timelineUiConfig: ${timelineUi.getConfig()}",
        )
        timelineUi.initTools(timelineMath.getConfig(), context)
    }
}
