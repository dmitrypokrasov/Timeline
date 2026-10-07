package com.dmitrypokrasov.timelineview.strategy
import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer

class TimelineStrategyResolver(
    private val registry: TimelineStrategyRegistryContract = TimelineStrategyRegistry,
) {
    fun resolveMath(config: TimelineConfig): TimelineMathEngine {
        return resolveMath(config.mathStrategyKey, config.mathStrategy, config.math)
    }

    fun resolveUi(config: TimelineConfig): TimelineUiRenderer {
        return resolveUi(config.uiStrategyKey, config.uiStrategy, config.ui)
    }

    fun resolveMath(
        strategy: TimelineMathStrategy,
        config: TimelineMathConfig,
    ): TimelineMathEngine {
        return resolveMath(strategy.key, strategy, config)
    }

    fun resolveUi(
        strategy: TimelineUiStrategy,
        config: TimelineUiConfig,
    ): TimelineUiRenderer {
        return resolveUi(strategy.key, strategy, config)
    }

    fun resolveMath(
        strategyKey: StrategyKey?,
        fallbackStrategy: TimelineMathStrategy,
        config: TimelineMathConfig,
    ): TimelineMathEngine {
        val preferredKey = strategyKey ?: fallbackStrategy.key
        registry.getMathProvider(preferredKey)?.let { provider ->
            return provider.create(config)
        }
        return requireNotNull(registry.getMathProvider(fallbackStrategy.key)) {
            "No built-in math strategy registered for key: ${fallbackStrategy.key.value}"
        }.create(config)
    }

    fun resolveUi(
        strategyKey: StrategyKey?,
        fallbackStrategy: TimelineUiStrategy,
        config: TimelineUiConfig,
    ): TimelineUiRenderer {
        val preferredKey = strategyKey ?: fallbackStrategy.key
        registry.getUiProvider(preferredKey)?.let { provider ->
            return provider.create(config)
        }
        return requireNotNull(registry.getUiProvider(fallbackStrategy.key)) {
            "No built-in UI strategy registered for key: ${fallbackStrategy.key.value}"
        }.create(config)
    }
}
