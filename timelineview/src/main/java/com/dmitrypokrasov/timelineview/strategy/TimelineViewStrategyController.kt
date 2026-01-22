package com.dmitrypokrasov.timelineview.strategy

import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.math.TimelineMathFactory
import com.dmitrypokrasov.timelineview.render.TimelineUiFactory
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer

/**
 * Composes math and UI engines for the [com.dmitrypokrasov.timelineview.ui.TimelineView].
 */
class TimelineViewStrategyController(
    private val resolver: TimelineStrategyResolver = TimelineStrategyResolver()
) {
    fun resolve(config: TimelineConfig): TimelineViewStrategies {
        return TimelineViewStrategies(
            math = resolver.resolveMath(config),
            ui = resolver.resolveUi(config)
        )
    }

    fun resolve(
        mathStrategy: TimelineMathStrategy,
        uiStrategy: TimelineUiStrategy,
        mathConfig: TimelineMathConfig,
        uiConfig: TimelineUiConfig
    ): TimelineViewStrategies {
        return TimelineViewStrategies(
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
    ): TimelineViewStrategies {
        return TimelineViewStrategies(
            math = resolver.resolveMath(mathStrategyKey, fallbackMath, mathConfig),
            ui = resolver.resolveUi(uiStrategyKey, fallbackUi, uiConfig)
        )
    }
}

data class TimelineViewStrategies(
    val math: TimelineMathEngine,
    val ui: TimelineUiRenderer
)
