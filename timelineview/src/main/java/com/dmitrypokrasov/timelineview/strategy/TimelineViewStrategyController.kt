package com.dmitrypokrasov.timelineview.strategy

import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.math.TimelineMathFactory
import com.dmitrypokrasov.timelineview.render.TimelineUiFactory

/**
 * Composes math and UI engines for the [com.dmitrypokrasov.timelineview.ui.TimelineView].
 */
class TimelineViewStrategyController(
    registry: TimelineStrategyRegistryContract = TimelineStrategyRegistry
) {
    private val resolver = TimelineStrategyResolver(registry)
    fun resolve(config: TimelineConfig): TimelineViewStrategiesData {
        return TimelineViewStrategiesData(
            math = resolver.resolveMath(config),
            ui = resolver.resolveUi(config)
        )
    }

    fun resolve(
        mathStrategy: TimelineMathStrategy,
        uiStrategy: TimelineUiStrategy,
        mathConfig: TimelineMathConfig,
        uiConfig: TimelineUiConfig
    ): TimelineViewStrategiesData {
        return TimelineViewStrategiesData(
            math = TimelineMathFactory.create(mathStrategy, mathConfig),
            ui = TimelineUiFactory.create(uiStrategy, uiConfig)
        )
    }

    fun resolve(
        mathStrategyKey: StrategyKey?,
        uiStrategyKey: StrategyKey?,
        fallbackMath: TimelineMathStrategy,
        fallbackUi: TimelineUiStrategy,
        mathConfig: TimelineMathConfig,
        uiConfig: TimelineUiConfig
    ): TimelineViewStrategiesData {
        return TimelineViewStrategiesData(
            math = resolver.resolveMath(mathStrategyKey, fallbackMath, mathConfig),
            ui = resolver.resolveUi(uiStrategyKey, fallbackUi, uiConfig)
        )
    }
}

