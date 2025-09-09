package com.dmitrypokrasov.timelineview.snake

import com.dmitrypokrasov.timelineview.config.TimelineConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig

/**
 * Default configuration for snake timeline strategy.
 */
data class SnakeConfig(
    override val math: TimelineMathConfig,
    override val ui: TimelineUiConfig,
) : TimelineConfig
