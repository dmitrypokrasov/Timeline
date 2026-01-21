package com.dmitrypokrasov.timelineview.render

import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy

/**
 * Factory for creating UI renderers based on strategy.
 */
object TimelineUiFactory {
    fun create(strategy: TimelineUiStrategy, config: TimelineUiConfig): TimelineUiRenderer {
        return when (strategy) {
            TimelineUiStrategy.SNAKE -> SnakeTimelineUi(config)
            TimelineUiStrategy.LINEAR -> LinearTimelineUi(config)
        }
    }
}
