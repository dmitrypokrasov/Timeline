package com.dmitrypokrasov.timelineview.math

import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy

/**
 * Factory for creating math engines based on strategy.
 */
object TimelineMathFactory {
    /** Creates a math engine for the supplied built-in [strategy]. */
    fun create(
        strategy: TimelineMathStrategy,
        config: TimelineMathConfig,
    ): TimelineMathEngine {
        return com.dmitrypokrasov.timelineview.strategy.TimelineBuiltIns.math.getValue(strategy.key)(config)
    }
}
