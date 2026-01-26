package com.dmitrypokrasov.timelineview.config

/**
 * Strategy type for math calculations.
 */
sealed interface TimelineMathStrategy {
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

    companion object {
        val entries: List<TimelineMathStrategy> = listOf(Snake, LinearVertical, LinearHorizontal)

        fun fromOrdinal(ordinal: Int): TimelineMathStrategy =
            entries.getOrElse(ordinal) { Snake }
    }
}

