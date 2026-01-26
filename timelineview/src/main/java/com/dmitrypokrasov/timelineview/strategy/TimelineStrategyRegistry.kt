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

fun registerDefaults(registry: TimelineStrategyRegistryContract) {
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

