package com.dmitrypokrasov.timelineview.render

import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy

/**
 * Factory for creating UI renderers based on strategy.
 */
object TimelineUiFactory {
    /** Creates a renderer for the supplied built-in [strategy]. */
    fun create(
        strategy: TimelineUiStrategy,
        config: TimelineUiConfig,
    ): TimelineUiRenderer {
        return when (strategy) {
            TimelineUiStrategy.Snake -> SnakeTimelineUi(config)
            TimelineUiStrategy.Linear -> LinearTimelineUi(config)
        }
    }
}
