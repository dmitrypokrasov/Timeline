package com.dmitrypokrasov.timelineview.strategy

import com.dmitrypokrasov.timelineview.config.StrategyKey

/**
 * Independent registry instance for custom strategy collections.
 */
class TimelineStrategyRegistryImpl(
    registerDefaults: Boolean = true
) : TimelineStrategyRegistryContract {
    private val mathProviders = mutableMapOf<StrategyKey, TimelineMathProvider>()
    private val uiProviders = mutableMapOf<StrategyKey, TimelineUiProvider>()

    init {
        if (registerDefaults) {
            registerDefaults(this)
        }
    }

    override fun registerMath(provider: TimelineMathProvider) {
        val key = provider.key
        require(!mathProviders.containsKey(key)) {
            "Math strategy already registered for key: ${key.value}"
        }
        mathProviders[key] = provider
    }

    override fun registerUi(provider: TimelineUiProvider) {
        val key = provider.key
        require(!uiProviders.containsKey(key)) {
            "UI strategy already registered for key: ${key.value}"
        }
        uiProviders[key] = provider
    }

    override fun unregisterMath(key: StrategyKey): TimelineMathProvider? = mathProviders.remove(key)

    override fun unregisterUi(key: StrategyKey): TimelineUiProvider? = uiProviders.remove(key)

    override fun getMathProvider(key: StrategyKey): TimelineMathProvider? = mathProviders[key]

    override fun getUiProvider(key: StrategyKey): TimelineUiProvider? = uiProviders[key]
}