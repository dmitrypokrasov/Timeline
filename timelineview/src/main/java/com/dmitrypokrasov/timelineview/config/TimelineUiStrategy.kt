package com.dmitrypokrasov.timelineview.config

/**
 * Strategy type for UI rendering.
 */
sealed interface TimelineUiStrategy {
    val key: StrategyKey

    data object Snake : TimelineUiStrategy {
        override val key: StrategyKey = StrategyKey("snake")
    }

    data object Linear : TimelineUiStrategy {
        override val key: StrategyKey = StrategyKey("linear")
    }

    companion object {
        val entries: List<TimelineUiStrategy> = listOf(Snake, Linear)

        fun fromOrdinal(ordinal: Int): TimelineUiStrategy =
            entries.getOrElse(ordinal) { Snake }
    }
}