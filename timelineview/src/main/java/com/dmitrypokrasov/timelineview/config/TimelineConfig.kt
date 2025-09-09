package com.dmitrypokrasov.timelineview.config

/**
 * Aggregates math and UI configurations for the timeline view.
 *
 * @property math configuration for sizes and positioning.
 * @property ui configuration for visual appearance.
 */
data class TimelineConfig(
    val math: TimelineMathConfig,
    val ui: TimelineUiConfig
)
