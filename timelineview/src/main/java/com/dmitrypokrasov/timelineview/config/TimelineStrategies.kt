package com.dmitrypokrasov.timelineview.config

/**
 * Strategy type for math calculations.
 */
enum class TimelineMathStrategy {
    SNAKE,
    LINEAR_VERTICAL,
    LINEAR_HORIZONTAL;

    val id: String = name.lowercase()
}

/**
 * Strategy type for UI rendering.
 */
enum class TimelineUiStrategy {
    SNAKE,
    LINEAR;

    val id: String = name.lowercase()
}
