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
 * Registry for timeline strategies to allow custom strategy extensions.
 */
object TimelineStrategyRegistry {
    private val mathProviders = mutableMapOf<StrategyKey, TimelineMathProvider>()
    private val uiProviders = mutableMapOf<StrategyKey, TimelineUiProvider>()

    init {
        registerMath(object : TimelineMathProvider {
            override val key: StrategyKey = TimelineMathStrategy.Snake.key
            override fun create(config: TimelineMathConfig): TimelineMathEngine = SnakeTimelineMath(config)
        })
        registerMath(object : TimelineMathProvider {
            override val key: StrategyKey = TimelineMathStrategy.LinearVertical.key
            override fun create(config: TimelineMathConfig): TimelineMathEngine =
                LinearTimelineMath(config, LinearTimelineMath.Orientation.VERTICAL)
        })
        registerMath(object : TimelineMathProvider {
            override val key: StrategyKey = TimelineMathStrategy.LinearHorizontal.key
            override fun create(config: TimelineMathConfig): TimelineMathEngine =
                LinearTimelineMath(config, LinearTimelineMath.Orientation.HORIZONTAL)
        })

        registerUi(object : TimelineUiProvider {
            override val key: StrategyKey = TimelineUiStrategy.Snake.key
            override fun create(config: TimelineUiConfig): TimelineUiRenderer = SnakeTimelineUi(config)
        })
        registerUi(object : TimelineUiProvider {
            override val key: StrategyKey = TimelineUiStrategy.Linear.key
            override fun create(config: TimelineUiConfig): TimelineUiRenderer = LinearTimelineUi(config)
        })
    }

    fun registerMath(provider: TimelineMathProvider) {
        val key = provider.key
        require(!mathProviders.containsKey(key)) {
            "Math strategy already registered for key: ${key.value}"
        }
        mathProviders[key] = provider
    }

    fun registerUi(provider: TimelineUiProvider) {
        val key = provider.key
        require(!uiProviders.containsKey(key)) {
            "UI strategy already registered for key: ${key.value}"
        }
        uiProviders[key] = provider
    }

    fun unregisterMath(key: StrategyKey): TimelineMathProvider? = mathProviders.remove(key)

    fun unregisterUi(key: StrategyKey): TimelineUiProvider? = uiProviders.remove(key)

    fun getMathProvider(key: StrategyKey): TimelineMathProvider? = mathProviders[key]

    fun getUiProvider(key: StrategyKey): TimelineUiProvider? = uiProviders[key]
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
