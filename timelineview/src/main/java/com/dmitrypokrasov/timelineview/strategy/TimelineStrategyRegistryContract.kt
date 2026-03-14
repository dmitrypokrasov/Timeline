package com.dmitrypokrasov.timelineview.strategy

import com.dmitrypokrasov.timelineview.config.StrategyKey

/**
 * Contract for timeline strategy registries.
 */
interface TimelineStrategyRegistryContract {
    /** Registers a math provider under its [TimelineMathProvider.key]. */
    fun registerMath(provider: TimelineMathProvider)

    /** Registers a UI provider under its [TimelineUiProvider.key]. */
    fun registerUi(provider: TimelineUiProvider)

    /** Unregisters a math provider and returns the removed entry, if any. */
    fun unregisterMath(key: StrategyKey): TimelineMathProvider?

    /** Unregisters a UI provider and returns the removed entry, if any. */
    fun unregisterUi(key: StrategyKey): TimelineUiProvider?

    /** Returns the math provider registered for [key], or `null`. */
    fun getMathProvider(key: StrategyKey): TimelineMathProvider?

    /** Returns the UI provider registered for [key], or `null`. */
    fun getUiProvider(key: StrategyKey): TimelineUiProvider?
}
