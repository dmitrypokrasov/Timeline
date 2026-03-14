package com.dmitrypokrasov.timelineview.ui

import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer
import com.dmitrypokrasov.timelineview.strategy.TimelineViewStrategiesData
import com.dmitrypokrasov.timelineview.strategy.TimelineViewStrategyController

internal data class TimelineRuntimeState(
    val config: TimelineConfig,
    val mathStrategy: TimelineMathStrategy,
    val uiStrategy: TimelineUiStrategy,
    val mathStrategyKey: StrategyKey?,
    val uiStrategyKey: StrategyKey?,
) {
    fun withSteps(
        steps: List<com.dmitrypokrasov.timelineview.model.TimelineStepData>,
    ): TimelineRuntimeState {
        return copy(config = config.copy(math = config.math.copy(steps = steps)))
    }

    fun withMathEngine(engine: TimelineMathEngine): TimelineRuntimeState {
        return copy(config = config.copy(math = engine.getConfig()))
    }

    fun withUiRenderer(renderer: TimelineUiRenderer): TimelineRuntimeState {
        return copy(config = config.copy(ui = renderer.getConfig()))
    }

    fun withStrategy(strategy: TimelineStrategy): TimelineRuntimeState {
        return copy(
            mathStrategy = strategy.math,
            uiStrategy = strategy.ui,
            mathStrategyKey = null,
            uiStrategyKey = null,
        )
    }

    fun withStrategyKeys(
        mathKey: StrategyKey?,
        uiKey: StrategyKey?,
    ): TimelineRuntimeState {
        return copy(mathStrategyKey = mathKey, uiStrategyKey = uiKey)
    }

    fun resolve(strategyController: TimelineViewStrategyController): TimelineViewStrategiesData {
        return strategyController.resolve(
            mathStrategyKey = mathStrategyKey,
            uiStrategyKey = uiStrategyKey,
            fallbackMath = mathStrategy,
            fallbackUi = uiStrategy,
            mathConfig = config.math,
            uiConfig = config.ui,
        )
    }

    companion object {
        fun from(config: TimelineConfig): TimelineRuntimeState {
            return TimelineRuntimeState(
                config = config,
                mathStrategy = config.mathStrategy,
                uiStrategy = config.uiStrategy,
                mathStrategyKey = config.mathStrategyKey,
                uiStrategyKey = config.uiStrategyKey,
            )
        }
    }
}
