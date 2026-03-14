package com.dmitrypokrasov.timelineview.config

/**
 * Built-in strategy type for UI rendering.
 */
sealed interface TimelineUiStrategy {
    /** Stable key that can be reused in registries and XML configuration. */
    val key: StrategyKey

    data object Snake : TimelineUiStrategy {
        override val key: StrategyKey = StrategyKey("snake")
    }

    data object Linear : TimelineUiStrategy {
        override val key: StrategyKey = StrategyKey("linear")
    }

    companion object {
        /** Ordered list used for XML enum parsing and public iteration. */
        val entries: List<TimelineUiStrategy> = listOf(Snake, Linear)

        /** Resolves a strategy from an XML enum ordinal, defaulting to [Snake]. */
        fun fromOrdinal(ordinal: Int): TimelineUiStrategy = entries.getOrElse(ordinal) { Snake }
    }
}
