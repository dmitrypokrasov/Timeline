package com.dmitrypokrasov.timelineview.strategy

import android.util.Log
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

class TimelineStrategyResolver(
    private val registry: TimelineStrategyRegistryContract = TimelineStrategyRegistry
) {
    companion object {
        private const val TAG = "TimelineStrategyResolver"
    }

    fun resolveMath(config: TimelineConfig): TimelineMathEngine {
        return resolveMath(config.mathStrategyKey, config.mathStrategy, config.math)
    }

    fun resolveUi(config: TimelineConfig): TimelineUiRenderer {
        return resolveUi(config.uiStrategyKey, config.uiStrategy, config.ui)
    }

    fun resolveMath(
        strategyKey: StrategyKey?,
        fallbackStrategy: TimelineMathStrategy,
        config: TimelineMathConfig
    ): TimelineMathEngine {
        if (strategyKey != null) {
            val provider = registry.getMathProvider(strategyKey)
            if (provider != null) {
                return provider.create(config)
            }
            Log.w(
                TAG,
                "No math strategy registered for key: ${strategyKey.value}. Falling back to $fallbackStrategy."
            )
        }
        return TimelineMathFactory.create(fallbackStrategy, config)
    }

    fun resolveUi(
        strategyKey: StrategyKey?,
        fallbackStrategy: TimelineUiStrategy,
        config: TimelineUiConfig
    ): TimelineUiRenderer {
        if (strategyKey != null) {
            val provider = registry.getUiProvider(strategyKey)
            if (provider != null) {
                return provider.create(config)
            }
            Log.w(
                TAG,
                "No UI strategy registered for key: ${strategyKey.value}. Falling back to $fallbackStrategy."
            )
        }
        return TimelineUiFactory.create(fallbackStrategy, config)
    }
}
