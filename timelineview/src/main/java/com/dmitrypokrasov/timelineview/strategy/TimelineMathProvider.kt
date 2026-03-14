package com.dmitrypokrasov.timelineview.strategy

import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine

/**
 * Factory provider for custom math strategies.
 */
interface TimelineMathProvider {
    /** Unique key used to register and resolve this provider. */
    val key: StrategyKey

    /** Creates a math engine for the supplied [config]. */
    fun create(config: TimelineMathConfig): TimelineMathEngine
}
