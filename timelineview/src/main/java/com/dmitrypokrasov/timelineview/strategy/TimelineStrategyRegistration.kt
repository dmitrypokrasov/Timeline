package com.dmitrypokrasov.timelineview.strategy

import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer

/** Регистрирует фабрику math, сохраняя прежний provider API и проверку дубликатов. */
fun TimelineStrategyRegistryContract.registerMath(
    key: StrategyKey,
    factory: (TimelineMathConfig) -> TimelineMathEngine,
) {
    registerMath(
        object : TimelineMathProvider {
            override val key: StrategyKey = key

            override fun create(config: TimelineMathConfig): TimelineMathEngine = factory(config)
        },
    )
}

/** Регистрирует фабрику UI; каждый вызов должен создавать независимый renderer. */
fun TimelineStrategyRegistryContract.registerUi(
    key: StrategyKey,
    factory: (TimelineUiConfig) -> TimelineUiRenderer,
) {
    registerUi(
        object : TimelineUiProvider {
            override val key: StrategyKey = key

            override fun create(config: TimelineUiConfig): TimelineUiRenderer = factory(config)
        },
    )
}
