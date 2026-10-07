package com.dmitrypokrasov.timelineview.ui

import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer
import com.dmitrypokrasov.timelineview.strategy.TimelineViewStrategiesData
import com.dmitrypokrasov.timelineview.strategy.TimelineViewStrategyController

internal sealed interface MathSelection {
    val fallback: TimelineMathStrategy

    data class ByKey(val key: StrategyKey?, override val fallback: TimelineMathStrategy) : MathSelection

    data class Instance(val engine: TimelineMathEngine, override val fallback: TimelineMathStrategy) : MathSelection
}

internal sealed interface UiSelection {
    val fallback: TimelineUiStrategy

    data class ByKey(val key: StrategyKey?, override val fallback: TimelineUiStrategy) : UiSelection

    data class Instance(val renderer: TimelineUiRenderer, override val fallback: TimelineUiStrategy) : UiSelection
}

/** A requested state. Resolution and resource preparation happen before it becomes current. */
internal data class TimelineRuntimeState(
    val mathConfig: TimelineMathConfig,
    val uiConfig: TimelineUiConfig,
    val math: MathSelection,
    val ui: UiSelection,
) {
    fun withSteps(steps: List<TimelineStepData>): TimelineRuntimeState = copy(mathConfig = mathConfig.copy(steps = steps.toList()))

    fun withMathEngine(engine: TimelineMathEngine): TimelineRuntimeState = copy(mathConfig = engine.getConfig(), math = MathSelection.Instance(engine, math.fallback))

    fun withUiRenderer(renderer: TimelineUiRenderer): TimelineRuntimeState = copy(uiConfig = renderer.getConfig(), ui = UiSelection.Instance(renderer, ui.fallback))

    fun withStrategy(strategy: TimelineStrategy): TimelineRuntimeState = copy(math = MathSelection.ByKey(null, strategy.math), ui = UiSelection.ByKey(null, strategy.ui))

    fun withStrategyKeys(
        mathKey: StrategyKey?,
        uiKey: StrategyKey?,
    ): TimelineRuntimeState = copy(math = MathSelection.ByKey(mathKey, math.fallback), ui = UiSelection.ByKey(uiKey, ui.fallback))

    fun resolve(controller: TimelineViewStrategyController): TimelineViewStrategiesData =
        controller.resolve(
            mathStrategyKey = (math as? MathSelection.ByKey)?.key,
            uiStrategyKey = (ui as? UiSelection.ByKey)?.key,
            fallbackMath = math.fallback,
            fallbackUi = ui.fallback,
            explicitMath = (math as? MathSelection.Instance)?.engine,
            explicitUi = (ui as? UiSelection.Instance)?.renderer,
            mathConfig = mathConfig,
            uiConfig = uiConfig,
        )

    companion object {
        fun from(config: TimelineConfig): TimelineRuntimeState = TimelineRuntimeState(config.math, config.ui, MathSelection.ByKey(config.mathStrategyKey, config.mathStrategy), UiSelection.ByKey(config.uiStrategyKey, config.uiStrategy))
    }
}
