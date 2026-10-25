package com.dmitrypokrasov.timelineview.ui

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineConfig
import com.dmitrypokrasov.timelineview.config.TimelineConfigParser
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.model.identity
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer
import com.dmitrypokrasov.timelineview.strategy.TimelineCachePolicy
import com.dmitrypokrasov.timelineview.strategy.TimelineStrategyRegistry
import com.dmitrypokrasov.timelineview.strategy.TimelineStrategyRegistryContract
import com.dmitrypokrasov.timelineview.strategy.TimelineViewStrategyController

@Suppress("TooManyFunctions") // Includes compatibility entry points retained for existing callers.
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
    private var measuredFrame: TimelineFrame? = null
    private var measuredUi: TimelineUiConfig? = null
    private var measuredResources: Configuration? = null
    private var measuredExtents = emptyList<Float>()
    private var stepIndices = emptyMap<Int, Int>()
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
    private var cachePolicy: TimelineCachePolicy
    private val virtualIds = mutableMapOf<String, Int>()
    private var nextVirtualId = 0
    private var strategyController = TimelineViewStrategyController(registry)
    private val frameCalculator = TimelineFrameCalculator(context)
    private val lottieOverlayManager = TimelineLottieOverlayManager(ownerView)
    private val visibleBounds = Rect()
    private val overlayViewport = RectF()
    private var onStepClickListener: ((index: Int, step: TimelineStepData) -> Unit)? = null
    private var onProgressIconClickListener: (() -> Unit)? = null

    init {
        val resolved = state.resolveWithPolicy(strategyController)
        timelineMath = resolved.math
        timelineUi = resolved.ui
        cachePolicy = resolved.policy
        initTools()
    }

    fun getConfig(): TimelineConfig = state.toConfig()

    fun setConfig(config: TimelineConfig) = transition(state.withConfig(config))

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
        invalidateGeometry(keepMeasurements = canReuseProgress() && sameContent(state.mathConfig))
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

    /** Устанавливает resolver атомарно: ошибка ключа сохраняет прежнее рабочее состояние. */
    fun setStrategyRegistry(
        registry: TimelineStrategyRegistryContract,
        strict: Boolean,
    ) =
        transition(state, TimelineViewStrategyController(registry, strict))

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

    fun isInteractive(): Boolean = onStepClickListener != null || onProgressIconClickListener != null

    fun performStepClick(index: Int): Boolean {
        val step = layout?.steps?.getOrNull(index)?.step ?: return false
        val id = virtualIds[step.identity(index)] ?: return false
        return clickTarget(id)
    }

    fun performProgressIconClick(): Boolean = clickTarget(PROGRESS_ID)

    internal fun targets(): List<Target> =
        TimelineTargets.withClicks(frame?.targets().orEmpty(), onStepClickListener != null, onProgressIconClickListener != null)

    internal fun targetAt(
        x: Float,
        y: Float,
        clickableOnly: Boolean = true,
    ): Int? = TimelineTargets.hit(targets(), x, y, clickableOnly)

    internal fun clickTarget(id: Int): Boolean {
        if (id == PROGRESS_ID && layout?.progressIcon != null) {
            return onProgressIconClickListener?.let {
                it()
                true
            } ?: false
        }
        val steps = layout?.steps.orEmpty()
        val index = stepIndices[id]
        val listener = onStepClickListener
        return if (index != null && listener != null) {
            listener(index, steps[index].step)
            true
        } else {
            false
        }
    }

    fun handleClick(
        x: Float,
        y: Float,
    ): Boolean = targetAt(x, y)?.let(::clickTarget) ?: false

    fun getAccessibilityDescription(): String? = targets().map { it.description }.takeIf { it.isNotEmpty() }?.joinToString(". ")

    fun desiredWidth(): Int = timelineMath.getDesiredWidth()

    fun measure(width: Int): Int {
        val direction = ownerView.layoutDirection == View.LAYOUT_DIRECTION_RTL
        val previous = reusableFrame(width, direction)
        // A caller retains access to direct engines' geometry setters (for example corner radius).
        val ownedFrame = previous?.takeIf { frame != null && state.math is MathSelection.ByKey }
        if (ownedFrame != null && ownedFrame.config == timelineMath.getConfig()) return ownedFrame.height
        val reuse = canReuseProgress() && previous != null && sameContent(timelineMath.getConfig())
        val calculation =
            frameCalculator.calculate(
                timelineMath, timelineUi, width, direction, if (reuse) previous else null,
                measuredExtents, virtualIds, nextVirtualId, onStepClickListener != null, onProgressIconClickListener != null, canCacheMeasurements(),
            )
        val candidate = calculation.frame
        val resolved = candidate.layout
        val ids = calculation.ids
        lottieOverlayManager.submit(candidate.layout.steps.map { it.step }, candidate.layout.progressStepIndex)
        virtualIds.clear()
        virtualIds.putAll(ids)
        nextVirtualId = calculation.nextId
        stepIndices = resolved.steps.indices.associateBy { ids.getValue(resolved.steps[it].step.identity(it)) }
        frame = candidate
        if (canCacheMeasurements()) {
            measuredExtents = calculation.extents
            measuredUi = timelineUi.getConfig()
            measuredResources = Configuration(context.resources.configuration)
            measuredFrame = candidate
        }
        return candidate.height
    }

    private fun canCacheMeasurements(): Boolean =
        cachePolicy != TimelineCachePolicy.NONE

    private fun canReuseProgress(): Boolean = cachePolicy == TimelineCachePolicy.CONFIGURATION_WITH_PROGRESS_REUSE

    private fun reusableFrame(
        width: Int,
        direction: Boolean,
    ): TimelineFrame? {
        val previous = measuredFrame ?: return null
        val sameConstraints = previous.width == width && previous.rtl == direction
        val sameConfiguration = measuredUi == timelineUi.getConfig() && measuredResources == context.resources.configuration
        return previous.takeIf { canCacheMeasurements() && sameConstraints && sameConfiguration }
    }

    private fun sameContent(config: TimelineMathConfig): Boolean {
        val previous = measuredFrame?.config ?: return false
        return config.copy(steps = emptyList()) == previous.copy(steps = emptyList()) && config.steps.size == previous.steps.size &&
            config.steps.indices.all { config.steps[it].copy(progress = previous.steps[it].progress) == previous.steps[it] }
    }

    fun draw(canvas: Canvas) {
        val current = frame ?: return
        visibleBounds.setEmpty()
        ownerView.getLocalVisibleRect(visibleBounds)
        overlayViewport.set(visibleBounds)
        // Overlay coordinates include the controller origin and content/padding translations.
        overlayViewport.offset(-ownerView.paddingLeft - origin, -ownerView.paddingTop - topInset)
        lottieOverlayManager.beginFrame(overlayViewport)
        try {
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
                if (bottom >= clip.top && top <= clip.bottom) {
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
                }
                lottieOverlayManager.draw(
                    canvas = canvas,
                    key = TimelineLottieOverlayManager.Key(stepLayout.step.identity(index)),
                    left = iconX(stepLayout.iconX, size),
                    top = stepLayout.iconY,
                    size = size,
                )
            }

            canvas.restore()
        } finally {
            lottieOverlayManager.endFrame()
        }
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
        val resolved = candidate.resolveWithPolicy(resolver)
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
        cachePolicy = resolved.policy
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
        if (active) frame?.let { lottieOverlayManager.submit(it.layout.steps.map { step -> step.step }, it.layout.progressStepIndex) }
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

    private fun invalidateGeometry(keepMeasurements: Boolean = false) {
        frame = null
        stepIndices = emptyMap()
        if (!keepMeasurements) {
            measuredFrame = null
            measuredUi = null
            measuredResources = null
            measuredExtents = emptyList()
        }
        lottieOverlayManager.prepare(timelineMath.getSteps())
    }

    private fun initTools() {
        prepareTools(timelineMath, timelineUi)
        invalidateGeometry()
    }
}
