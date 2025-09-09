package com.dmitrypokrasov.timelineview.linear

import com.dmitrypokrasov.timelineview.config.TimelineConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig

/**
 * Default configuration for linear timeline strategy.
 */
data class LinearConfig(
    override val math: TimelineMathConfig,
    override val ui: TimelineUiConfig,
) : TimelineConfig
