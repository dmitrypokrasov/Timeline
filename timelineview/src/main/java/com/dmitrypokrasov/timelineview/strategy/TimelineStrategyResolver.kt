package com.dmitrypokrasov.timelineview.strategy

import com.dmitrypokrasov.timelineview.config.TimelineConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.math.TimelineMathFactory
import com.dmitrypokrasov.timelineview.render.TimelineUiFactory
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer

class TimelineStrategyResolver(
    private val registry: TimelineStrategyRegistry = TimelineStrategyRegistry
) {
    fun resolveMath(config: TimelineConfig): TimelineMathEngine {
        return resolveMath(config.mathStrategyId, config.mathStrategy, config.math)
    }

    fun resolveUi(config: TimelineConfig): TimelineUiRenderer {
        return resolveUi(config.uiStrategyId, config.uiStrategy, config.ui)
    }

    fun resolveMath(
        strategyId: String?,
        fallbackStrategy: TimelineMathStrategy,
        config: TimelineMathConfig
    ): TimelineMathEngine {
        if (!strategyId.isNullOrBlank()) {
            val provider = registry.getMathProvider(strategyId)
                ?: error("No math strategy registered for id: $strategyId")
            return provider.create(config)
        }
        return TimelineMathFactory.create(fallbackStrategy, config)
    }

    fun resolveUi(
        strategyId: String?,
        fallbackStrategy: TimelineUiStrategy,
        config: TimelineUiConfig
    ): TimelineUiRenderer {
        if (!strategyId.isNullOrBlank()) {
            val provider = registry.getUiProvider(strategyId)
                ?: error("No UI strategy registered for id: $strategyId")
            return provider.create(config)
        }
        return TimelineUiFactory.create(fallbackStrategy, config)
    }
}
