package com.dmitrypokrasov.timelineview.config

/**
 * Composite strategy for selecting math and UI implementations together.
 *
 * @property math math strategy to use.
 * @property ui renderer strategy to use.
 */
data class TimelineStrategy(
    val math: TimelineMathStrategy,
    val ui: TimelineUiStrategy,
)
