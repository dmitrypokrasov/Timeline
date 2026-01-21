package com.dmitrypokrasov.timelineview.config

/**
 * Aggregates math and UI configurations for the timeline view.
 *
 * @property math configuration for sizes and positioning.
 * @property ui configuration for visual appearance.
 * @property mathStrategy strategy for math calculations.
 * @property uiStrategy strategy for UI rendering.
 * @property mathStrategyId optional ID for custom math strategy.
 * @property uiStrategyId optional ID for custom UI strategy.
 */
data class TimelineConfig(
    val math: TimelineMathConfig,
    val ui: TimelineUiConfig,
    val mathStrategy: TimelineMathStrategy = TimelineMathStrategy.SNAKE,
    val uiStrategy: TimelineUiStrategy = TimelineUiStrategy.SNAKE,
    val mathStrategyId: String? = null,
    val uiStrategyId: String? = null
)
