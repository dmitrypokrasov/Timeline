package com.dmitrypokrasov.timelineview.strategy

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
    private val mathProviders = mutableMapOf<String, TimelineMathProvider>()
    private val uiProviders = mutableMapOf<String, TimelineUiProvider>()

    init {
        registerMath(object : TimelineMathProvider {
            override val id: String = TimelineMathStrategy.SNAKE.id
            override fun create(config: TimelineMathConfig): TimelineMathEngine = SnakeTimelineMath(config)
        })
        registerMath(object : TimelineMathProvider {
            override val id: String = TimelineMathStrategy.LINEAR_VERTICAL.id
            override fun create(config: TimelineMathConfig): TimelineMathEngine =
                LinearTimelineMath(config, LinearTimelineMath.Orientation.VERTICAL)
        })
        registerMath(object : TimelineMathProvider {
            override val id: String = TimelineMathStrategy.LINEAR_HORIZONTAL.id
            override fun create(config: TimelineMathConfig): TimelineMathEngine =
                LinearTimelineMath(config, LinearTimelineMath.Orientation.HORIZONTAL)
        })

        registerUi(object : TimelineUiProvider {
            override val id: String = TimelineUiStrategy.SNAKE.id
            override fun create(config: TimelineUiConfig): TimelineUiRenderer = SnakeTimelineUi(config)
        })
        registerUi(object : TimelineUiProvider {
            override val id: String = TimelineUiStrategy.LINEAR.id
            override fun create(config: TimelineUiConfig): TimelineUiRenderer = LinearTimelineUi(config)
        })
    }

    fun registerMath(provider: TimelineMathProvider) {
        mathProviders[provider.id] = provider
    }

    fun registerUi(provider: TimelineUiProvider) {
        uiProviders[provider.id] = provider
    }

    fun getMathProvider(id: String): TimelineMathProvider? = mathProviders[id]

    fun getUiProvider(id: String): TimelineUiProvider? = uiProviders[id]
}

/**
 * Factory provider for math strategy implementations.
 */
interface TimelineMathProvider {
    val id: String
    fun create(config: TimelineMathConfig): TimelineMathEngine
}

/**
 * Factory provider for UI strategy implementations.
 */
interface TimelineUiProvider {
    val id: String
    fun create(config: TimelineUiConfig): TimelineUiRenderer
}
