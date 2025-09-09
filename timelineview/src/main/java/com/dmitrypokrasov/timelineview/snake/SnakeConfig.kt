package com.dmitrypokrasov.timelineview.snake

import com.dmitrypokrasov.timelineview.core.TimelineConfig
import com.dmitrypokrasov.timelineview.core.TimelineMathConfig
import com.dmitrypokrasov.timelineview.core.TimelineUiConfig

/**
 * Default configuration for snake timeline strategy.
 */
data class SnakeConfig(
    override val math: TimelineMathConfig,
    override val ui: TimelineUiConfig,
) : TimelineConfig
