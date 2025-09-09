package com.dmitrypokrasov.timelineview.config

/**
 * Base configuration combining math and UI settings. Library users may
 * provide their own implementations for specific strategies.
 */
interface TimelineConfig {
    val math: TimelineMathConfig
    val ui: TimelineUiConfig
}
