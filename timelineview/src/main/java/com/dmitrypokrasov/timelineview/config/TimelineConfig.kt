package com.dmitrypokrasov.timelineview.config

/**
 * Aggregates math and UI configurations for the timeline view.
 *
 * @property math configuration for sizes and positioning.
 * @property ui configuration for visual appearance.
 * @property mathStrategy strategy for math calculations.
 * @property uiStrategy strategy for UI rendering.
 */
data class TimelineConfig(
    val math: TimelineMathConfig,
    val ui: TimelineUiConfig,
    val mathStrategy: TimelineMathStrategy = TimelineMathStrategy.SNAKE,
    val uiStrategy: TimelineUiStrategy = TimelineUiStrategy.SNAKE
)
