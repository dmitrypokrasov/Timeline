package com.dmitrypokrasov.timelineview.strategy

import com.dmitrypokrasov.timelineview.config.StrategyKey

/**
 * Default registry for timeline strategies to allow custom strategy extensions.
 */
object TimelineStrategyRegistry : TimelineStrategyRegistryContract {
    private val delegate = TimelineStrategyRegistryImpl(registerDefaults = true)

    override fun registerMath(provider: TimelineMathProvider) = delegate.registerMath(provider)

    override fun registerUi(provider: TimelineUiProvider) = delegate.registerUi(provider)

    override fun unregisterMath(key: StrategyKey): TimelineMathProvider? =
        delegate.unregisterMath(
            key,
        )

    override fun unregisterUi(key: StrategyKey): TimelineUiProvider? = delegate.unregisterUi(key)

    override fun getMathProvider(key: StrategyKey): TimelineMathProvider? =
        delegate.getMathProvider(
            key,
        )

    override fun getUiProvider(key: StrategyKey): TimelineUiProvider? = delegate.getUiProvider(key)

    /** Creates a new registry that is not shared globally. */
    fun createLocalRegistry(registerDefaults: Boolean = true): TimelineStrategyRegistryContract {
        return TimelineStrategyRegistryImpl(registerDefaults = registerDefaults)
    }
}

/** Registers the built-in math and UI strategies into [registry]. */
fun registerDefaults(registry: TimelineStrategyRegistryContract) = TimelineBuiltIns.register(registry)
