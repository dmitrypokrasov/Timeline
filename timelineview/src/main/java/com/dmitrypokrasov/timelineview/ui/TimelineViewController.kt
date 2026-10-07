package com.dmitrypokrasov.timelineview.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineConfigParser
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.model.identity
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer
import com.dmitrypokrasov.timelineview.strategy.TimelineStrategyRegistry
import com.dmitrypokrasov.timelineview.strategy.TimelineStrategyRegistryContract
import com.dmitrypokrasov.timelineview.strategy.TimelineViewStrategyController

class TimelineViewController(
    private val ownerView: View,
    private val context: Context,
    attrs: AttributeSet?,
    registry: TimelineStrategyRegistryContract = TimelineStrategyRegistry,
    defStyleAttr: Int = 0,
) {
    companion object {
        internal const val PROGRESS_ID = Int.MAX_VALUE
    }

    internal data class Target(val id: Int, val bounds: RectF, val description: String, val clickable: Boolean)

    private var frame: TimelineFrame? = null
    private val layout get() = frame?.layout
    private val textBlocks get() = frame?.blocks.orEmpty()
    private val topInset get() = frame?.topInset ?: 0f
    private val measuredWidth get() = frame?.width ?: 0
    private val rtl get() = frame?.rtl ?: false
    private val origin get() = frame?.origin ?: 0f

    private val initialConfig = TimelineConfigParser(context).parse(attrs, defStyleAttr)
    private var state = TimelineRuntimeState.from(initialConfig)
    private var timelineMath: TimelineMathEngine
    private var timelineUi: TimelineUiRenderer
    private val virtualIds = mutableMapOf<String, Int>()
    private var nextVirtualId = 0
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

    fun setConfig(
        math: com.dmitrypokrasov.timelineview.config.TimelineMathConfig,
        ui: com.dmitrypokrasov.timelineview.config.TimelineUiConfig,
    ) {
        transition(state.copy(mathConfig = math.copy(steps = math.steps.toList()), uiConfig = ui))
    }

    @Suppress("TooGenericExceptionCaught") // Roll back and rethrow any exception from a custom engine.
    fun replaceSteps(steps: List<TimelineStepData>) {
        val candidate = state.withSteps(steps)
        val previous = timelineMath.getConfig()
        try {
            timelineMath.replaceSteps(candidate.mathConfig.steps)
        } catch (failure: Exception) {
            runCatching { timelineMath.setConfig(previous) }.exceptionOrNull()?.let(failure::addSuppressed)
            throw failure
        }
        state = candidate.copy(mathConfig = timelineMath.getConfig())
        invalidateGeometry()
    }

    fun setMathEngine(engine: TimelineMathEngine) = transition(state.withMathEngine(engine))

    fun setUiRenderer(renderer: TimelineUiRenderer) = transition(state.withUiRenderer(renderer))

    fun setStrategy(
        mathStrategy: TimelineMathStrategy,
        uiStrategy: TimelineUiStrategy,
    ) = setStrategy(TimelineStrategy(mathStrategy, uiStrategy))

    fun setStrategy(strategy: TimelineStrategy) = transition(state.withStrategy(strategy))

    fun setStrategy(
        mathStrategyKey: StrategyKey?,
        uiStrategyKey: StrategyKey?,
    ) = transition(state.withStrategyKeys(mathStrategyKey, uiStrategyKey))

    fun setStrategies(
        mathEngine: TimelineMathEngine,
        uiRenderer: TimelineUiRenderer,
    ) = transition(state.withMathEngine(mathEngine).withUiRenderer(uiRenderer))

    fun setStrategyRegistry(registry: TimelineStrategyRegistryContract) = transition(state, TimelineViewStrategyController(registry))

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

    internal fun targets(): List<Target> =
        frame?.targets().orEmpty().map {
            it.copy(clickable = if (it.id == PROGRESS_ID) onProgressIconClickListener != null else onStepClickListener != null)
        }

    private fun buildTargets(
        current: TimelineLayout,
        config: com.dmitrypokrasov.timelineview.config.TimelineMathConfig,
        origin: Float,
        measuredWidth: Int,
        rtl: Boolean,
        topInset: Float,
        ids: Map<String, Int>,
    ): List<Target> {
        val minSize = 48f * context.resources.displayMetrics.density

        fun bounds(
            x: Float,
            y: Float,
            size: Float,
        ): RectF {
            val extra = (minSize - size).coerceAtLeast(0f) / 2f
            val left = if (rtl) measuredWidth - origin - x - size else origin + x
            return RectF(
                (left - extra).coerceAtLeast(0f),
                (y + topInset - extra).coerceAtLeast(0f),
                (left + size + extra).coerceAtMost(measuredWidth.toFloat()),
                y + topInset + size + extra,
            )
        }
        val result =
            current.steps.mapIndexed { index, step ->
                val title =
                    step.step.title?.toString()?.takeIf { it.isNotBlank() }
                        ?: context.getString(com.dmitrypokrasov.timelineview.R.string.timeline_step, index + 1)
                val description =
                    listOfNotNull(
                        title,
                        step.step.description?.toString()?.takeIf { it.isNotBlank() },
                        context.getString(com.dmitrypokrasov.timelineview.R.string.timeline_percent, step.step.progress),
                    ).joinToString(". ")
                Target(ids.getValue(step.step.identity(index)), bounds(step.iconX, step.iconY, config.sizes.sizeImageLvl), description, onStepClickListener != null)
            }.toMutableList()
        current.progressIcon?.let { progress ->
            result +=
                Target(
                    PROGRESS_ID, bounds(progress.left, progress.top, config.sizes.sizeIconProgress),
                    context.getString(com.dmitrypokrasov.timelineview.R.string.timeline_progress), onProgressIconClickListener != null,
                )
        }
        return result.filter { !it.bounds.isEmpty }
    }

    internal fun targetAt(
        x: Float,
        y: Float,
        clickableOnly: Boolean = true,
    ): Int? =
        targets().filter { (!clickableOnly || it.clickable) && it.bounds.contains(x, y) }
            .minByOrNull {
                val dx = x - it.bounds.centerX()
                val dy = y - it.bounds.centerY()
                dx * dx + dy * dy
            }?.id

    internal fun clickTarget(id: Int): Boolean {
        if (id == PROGRESS_ID && layout?.progressIcon != null) {
            val listener = onProgressIconClickListener ?: return false
            listener()
            return true
        }
        val steps = layout?.steps ?: return false
        val index = steps.indices.firstOrNull { virtualIds[steps[it].step.identity(it)] == id } ?: return false
        val listener = onStepClickListener ?: return false
        listener(index, steps[index].step)
        return true
    }

    fun handleClick(
        x: Float,
        y: Float,
    ): Boolean = targetAt(x, y)?.let(::clickTarget) ?: false

    fun getAccessibilityDescription(): String? = targets().map { it.description }.takeIf { it.isNotEmpty() }?.joinToString(". ")

    fun desiredWidth(): Int = timelineMath.getDesiredWidth()

    fun measure(width: Int): Int {
        val direction = ownerView.layoutDirection == View.LAYOUT_DIRECTION_RTL
        val resolved = TimelineLayoutResolver.resolve(timelineMath, timelineUi, width, 4f * context.resources.displayMetrics.density)
        val completed = Path()
        val remaining = Path()
        timelineMath.buildPath(completed, remaining)
        val identities = resolved.steps.mapIndexed { index, step -> step.step.identity(index) }.toSet()
        val ids = virtualIds.filterKeys { it in identities }.toMutableMap()
        var nextId = nextVirtualId
        identities.forEach { ids.getOrPut(it) { nextId++ } }
        val blocks = TimelineTextBlockResolver.resolve(resolved, timelineMath, timelineUi)
        val bounds = heightCalculator.calculateBounds(resolved, timelineMath, timelineUi, blocks, listOf(completed, remaining))
        val math = timelineMath.getConfig()
        val start = timelineMath.getStartPosition()
        val targets = buildTargets(resolved, math, start, width, direction, bounds.topInset, ids)
        val candidate = TimelineFrame(resolved, blocks, math, start, width, direction, bounds.topInset, bounds.height, completed, remaining, targets)
        lottieOverlayManager.submit(math.steps)
        virtualIds.clear()
        virtualIds.putAll(ids)
        nextVirtualId = nextId
        frame = candidate
        return candidate.height
    }

    fun draw(canvas: Canvas) {
        val current = frame ?: return
        current.restorePaths(timelineUi)
        timelineUi.prepareStrokePaint()

        canvas.save()
        canvas.translate(0f, topInset)
        canvas.save()
        if (rtl) {
            canvas.translate(measuredWidth.toFloat(), 0f)
            canvas.scale(-1f, 1f)
        }
        canvas.translate(origin, 0f)
        timelineUi.drawCompletedPath(canvas)
        timelineUi.drawRemainingPath(canvas)
        canvas.restore()
        canvas.translate(origin, 0f)

        timelineUi.prepareTextPaint()
        timelineUi.prepareIconPaint()

        drawProgressIcon(canvas, layout)

        val clip = canvas.clipBounds
        layout?.steps?.forEachIndexed { index, stepLayout ->
            val textBlock = textBlocks.getOrNull(index) ?: return@forEachIndexed
            val size = current.config.sizes.sizeImageLvl
            val overlayInset = size * ((stepLayout.step.badgeAnimation?.scale ?: 1f).coerceAtLeast(1f) - 1f) / 2f
            val top = minOf(stepLayout.iconY - overlayInset, textBlock.titleTop, textBlock.descriptionTop)
            val bottom = maxOf(stepLayout.iconY + size + overlayInset, textBlock.titleTop + textBlock.titleHeight, textBlock.descriptionTop + textBlock.descriptionHeight)
            if (bottom < clip.top || top > clip.bottom) return@forEachIndexed
            val title = stepLayout.step.title ?: ""
            val description = stepLayout.step.description ?: ""

            timelineUi.drawTitle(
                canvas,
                title,
                textX(stepLayout.titleX),
                textBlock.titleTop,
                textAlign(stepLayout.textAlign),
                stepLayout.titleWidth,
            )
            timelineUi.drawDescription(
                canvas,
                description,
                textX(stepLayout.descriptionX),
                textBlock.descriptionTop,
                textAlign(stepLayout.textAlign),
                stepLayout.descriptionWidth,
            )
            timelineUi.drawStepIcon(
                stepLayout.step,
                canvas,
                textAlign(stepLayout.textAlign),
                context,
                iconX(stepLayout.iconX, size),
                stepLayout.iconY,
            )
            lottieOverlayManager.draw(
                canvas = canvas,
                key = TimelineLottieOverlayManager.Key(stepLayout.step.identity(index)),
                left = iconX(stepLayout.iconX, size),
                top = stepLayout.iconY,
                size = size,
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
        val size = frame?.config?.sizes?.sizeIconProgress ?: return
        timelineUi.drawProgressIcon(canvas, iconX(progress.left, size), progress.top)
        val index = layout.progressStepIndex ?: return
        val step = layout.steps.getOrNull(index)?.step ?: return
        lottieOverlayManager.draw(
            canvas = canvas,
            key = TimelineLottieOverlayManager.Key(step.identity(index), true),
            left = iconX(progress.left, size),
            top = progress.top,
            size = size,
        )
    }

    @Suppress("TooGenericExceptionCaught") // Transaction boundary: restore reused instances, then rethrow.
    private fun transition(
        candidate: TimelineRuntimeState,
        resolver: TimelineViewStrategyController = strategyController,
    ) {
        // Factories may reject input. Do not change the installed selection or registry yet.
        val resolved = candidate.resolve(resolver)
        val oldMath = timelineMath.getConfig()
        val oldUi = timelineUi.getConfig()
        try {
            if (resolved.math.getConfig() != candidate.mathConfig) resolved.math.setConfig(candidate.mathConfig)
            if (resolved.ui.getConfig() != candidate.uiConfig) resolved.ui.setConfig(candidate.uiConfig)
            prepareTools(resolved.math, resolved.ui)
        } catch (failure: Exception) {
            // Direct instances can be reused by a candidate; restore their previous configuration.
            runCatching {
                timelineMath.setConfig(oldMath)
                timelineUi.setConfig(oldUi)
                prepareTools(timelineMath, timelineUi)
            }.exceptionOrNull()?.let(failure::addSuppressed)
            throw failure
        }
        timelineMath = resolved.math
        timelineUi = resolved.ui
        state = candidate.copy(mathConfig = timelineMath.getConfig(), uiConfig = timelineUi.getConfig())
        strategyController = resolver
        invalidateGeometry()
    }

    private fun prepareTools(
        math: TimelineMathEngine,
        ui: TimelineUiRenderer,
    ) {
        math.setCornerRadius(ui.getConfig().stroke.radius)
        ui.setGeometryRounded(math.hasRoundedGeometry)
        ui.initTools(math.getConfig(), context)
    }

    internal fun setActive(active: Boolean) {
        if (active) lottieOverlayManager.submit(timelineMath.getSteps())
        lottieOverlayManager.setActive(active)
    }

    private fun textX(x: Float): Float = if (rtl) measuredWidth - 2f * origin - x else x

    private fun iconX(
        x: Float,
        size: Float,
    ): Float = if (rtl) textX(x) - size else x

    private fun textAlign(align: Paint.Align): Paint.Align =
        if (!rtl) {
            align
        } else {
            when (align) {
                Paint.Align.LEFT -> Paint.Align.RIGHT
                Paint.Align.RIGHT -> Paint.Align.LEFT
                Paint.Align.CENTER -> Paint.Align.CENTER
            }
        }

    private fun invalidateGeometry() {
        frame = null
        lottieOverlayManager.submit(timelineMath.getSteps())
    }

    private fun initTools() {
        prepareTools(timelineMath, timelineUi)
        invalidateGeometry()
    }
}
