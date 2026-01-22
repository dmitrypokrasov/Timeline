package com.dmitrypokrasov.timelineview.config

/**
 * Stable identifier for timeline strategy providers.
 */
@JvmInline
value class StrategyKey(val value: String) {
    init {
        require(value.isNotBlank()) { "StrategyKey value cannot be blank." }
    }

    override fun toString(): String = value
}
