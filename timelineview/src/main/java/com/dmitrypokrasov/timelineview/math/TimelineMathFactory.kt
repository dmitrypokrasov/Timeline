package com.dmitrypokrasov.timelineview.math

import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy

/**
 * Factory for creating math engines based on strategy.
 */
object TimelineMathFactory {
    fun create(strategy: TimelineMathStrategy, config: TimelineMathConfig): TimelineMathEngine {
        return when (strategy) {
            TimelineMathStrategy.Snake -> SnakeTimelineMath(config)
            TimelineMathStrategy.LinearVertical ->
                LinearTimelineMath(config, LinearTimelineMath.Orientation.VERTICAL)
            TimelineMathStrategy.LinearHorizontal ->
                LinearTimelineMath(config, LinearTimelineMath.Orientation.HORIZONTAL)
        }
    }
}
