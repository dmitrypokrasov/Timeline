package com.dmitrypokrasov.timelineview.config

/**
 * Composite strategy for selecting math and UI implementations together.
 */
data class TimelineStrategy(
    val math: TimelineMathStrategy,
    val ui: TimelineUiStrategy
)
