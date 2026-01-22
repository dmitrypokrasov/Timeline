package com.dmitrypokrasov.timelineview.config

/**
 * Aggregates math and UI configurations for the timeline view.
 *
 * @property math configuration for sizes and positioning.
 * @property ui configuration for visual appearance.
 * @property mathStrategy strategy for math calculations.
 * @property uiStrategy strategy for UI rendering.
 * @property mathStrategyKey optional key for custom math strategy.
 * @property uiStrategyKey optional key for custom UI strategy.
 */
data class TimelineConfig(
    val math: TimelineMathConfig,
    val ui: TimelineUiConfig,
    val mathStrategy: TimelineMathStrategy = TimelineMathStrategy.Snake,
    val uiStrategy: TimelineUiStrategy = TimelineUiStrategy.Snake,
    val mathStrategyKey: StrategyKey? = null,
    val uiStrategyKey: StrategyKey? = null
)
