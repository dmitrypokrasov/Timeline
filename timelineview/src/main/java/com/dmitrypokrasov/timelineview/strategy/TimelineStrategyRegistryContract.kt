package com.dmitrypokrasov.timelineview.strategy

import com.dmitrypokrasov.timelineview.config.StrategyKey

/**
 * Contract for timeline strategy registries.
 */
interface TimelineStrategyRegistryContract {
    fun registerMath(provider: TimelineMathProvider)
    fun registerUi(provider: TimelineUiProvider)
    fun unregisterMath(key: StrategyKey): TimelineMathProvider?
    fun unregisterUi(key: StrategyKey): TimelineUiProvider?
    fun getMathProvider(key: StrategyKey): TimelineMathProvider?
    fun getUiProvider(key: StrategyKey): TimelineUiProvider?
}