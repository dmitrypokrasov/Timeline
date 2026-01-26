package com.dmitrypokrasov.timelineview.strategy

import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine

/**
 * Factory provider for math strategy implementations.
 */
interface TimelineMathProvider {
    val key: StrategyKey
    fun create(config: TimelineMathConfig): TimelineMathEngine
}