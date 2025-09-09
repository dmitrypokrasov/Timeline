package com.dmitrypokrasov.timelineview.linear

import com.dmitrypokrasov.timelineview.core.TimelineConfig
import com.dmitrypokrasov.timelineview.core.TimelineMathConfig
import com.dmitrypokrasov.timelineview.core.TimelineUiConfig

/**
 * Default configuration for linear timeline strategy.
 */
data class LinearConfig(
    override val math: TimelineMathConfig,
    override val ui: TimelineUiConfig,
) : TimelineConfig
