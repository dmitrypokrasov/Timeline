package com.dmitrypokrasov.timelineview.config

/**
 * Built-in strategy type for math calculations.
 */
sealed interface TimelineMathStrategy {
    /** Stable key that can be reused in registries and XML configuration. */
    val key: StrategyKey

    data object Snake : TimelineMathStrategy {
        override val key: StrategyKey = StrategyKey("snake")
    }

    data object LinearVertical : TimelineMathStrategy {
        override val key: StrategyKey = StrategyKey("linear_vertical")
    }

    data object LinearHorizontal : TimelineMathStrategy {
        override val key: StrategyKey = StrategyKey("linear_horizontal")
    }

    data object Alternating : TimelineMathStrategy {
        override val key = StrategyKey("alternating")
    }

    data object AdaptiveGrid : TimelineMathStrategy {
        override val key = StrategyKey("adaptive_grid")
    }

    data object TimeScaled : TimelineMathStrategy {
        override val key = StrategyKey("time_scaled")
    }

    companion object {
        /** Ordered list used for XML enum parsing and public iteration. */
        val entries: List<TimelineMathStrategy> = listOf(Snake, LinearVertical, LinearHorizontal, Alternating, AdaptiveGrid, TimeScaled)

        /** Resolves a strategy from an XML enum ordinal, defaulting to [Snake]. */
        fun fromOrdinal(ordinal: Int): TimelineMathStrategy = entries.getOrElse(ordinal) { Snake }
    }
}
