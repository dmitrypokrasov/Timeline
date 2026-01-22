package com.dmitrypokrasov.timelineview.strategy

import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.math.LinearTimelineMath
import com.dmitrypokrasov.timelineview.math.SnakeTimelineMath
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.render.LinearTimelineUi
import com.dmitrypokrasov.timelineview.render.SnakeTimelineUi
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer

/**
 * Contract for timeline strategy registries.
 */
interface TimelineStrategyRegistryContract {
    fun registerMath(provider: TimelineMathProvider)
    fun registerUi(provider: TimelineUiProvider)
    fun unregisterMath(key: StrategyKey): TimelineMathProvider?
    fun unregisterUi(key: StrategyKey): TimelineUiProvider?
    fun getMathProvider(key: StrategyKey): TimelineMathProvider?
    fun getUiProvider(key: StrategyKey): TimelineUiProvider?
}

/**
 * Default registry for timeline strategies to allow custom strategy extensions.
 */
object TimelineStrategyRegistry : TimelineStrategyRegistryContract {
    private val delegate = TimelineStrategyRegistryImpl(registerDefaults = true)

    override fun registerMath(provider: TimelineMathProvider) = delegate.registerMath(provider)

    override fun registerUi(provider: TimelineUiProvider) = delegate.registerUi(provider)

    override fun unregisterMath(key: StrategyKey): TimelineMathProvider? = delegate.unregisterMath(key)

    override fun unregisterUi(key: StrategyKey): TimelineUiProvider? = delegate.unregisterUi(key)

    override fun getMathProvider(key: StrategyKey): TimelineMathProvider? = delegate.getMathProvider(key)

    override fun getUiProvider(key: StrategyKey): TimelineUiProvider? = delegate.getUiProvider(key)

    /**
     * Creates a new registry that is not shared globally.
     */
    fun createLocalRegistry(registerDefaults: Boolean = true): TimelineStrategyRegistryContract {
        return TimelineStrategyRegistryImpl(registerDefaults = registerDefaults)
    }
}

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

private fun registerDefaults(registry: TimelineStrategyRegistryContract) {
    registry.registerMath(object : TimelineMathProvider {
        override val key: StrategyKey = TimelineMathStrategy.Snake.key
        override fun create(config: TimelineMathConfig): TimelineMathEngine = SnakeTimelineMath(config)
    })
    registry.registerMath(object : TimelineMathProvider {
        override val key: StrategyKey = TimelineMathStrategy.LinearVertical.key
        override fun create(config: TimelineMathConfig): TimelineMathEngine =
            LinearTimelineMath(config, LinearTimelineMath.Orientation.VERTICAL)
    })
    registry.registerMath(object : TimelineMathProvider {
        override val key: StrategyKey = TimelineMathStrategy.LinearHorizontal.key
        override fun create(config: TimelineMathConfig): TimelineMathEngine =
            LinearTimelineMath(config, LinearTimelineMath.Orientation.HORIZONTAL)
    })

    registry.registerUi(object : TimelineUiProvider {
        override val key: StrategyKey = TimelineUiStrategy.Snake.key
        override fun create(config: TimelineUiConfig): TimelineUiRenderer = SnakeTimelineUi(config)
    })
    registry.registerUi(object : TimelineUiProvider {
        override val key: StrategyKey = TimelineUiStrategy.Linear.key
        override fun create(config: TimelineUiConfig): TimelineUiRenderer = LinearTimelineUi(config)
    })
}

/**
 * Factory provider for math strategy implementations.
 */
interface TimelineMathProvider {
    val key: StrategyKey
    fun create(config: TimelineMathConfig): TimelineMathEngine
}

/**
 * Factory provider for UI strategy implementations.
 */
interface TimelineUiProvider {
    val key: StrategyKey
    fun create(config: TimelineUiConfig): TimelineUiRenderer
}
