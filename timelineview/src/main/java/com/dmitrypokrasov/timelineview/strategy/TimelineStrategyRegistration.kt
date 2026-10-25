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
) = registerMath(key, TimelineCachePolicy.NONE, factory)

/** Регистрирует math с явными гарантиями детерминизма и повторного использования текста. */
fun TimelineStrategyRegistryContract.registerMath(
    key: StrategyKey,
    policy: TimelineCachePolicy,
    factory: (TimelineMathConfig) -> TimelineMathEngine,
) {
    registerMath(
        object : TimelineMathProvider, TimelineCachePolicyProvider {
            override val cachePolicy = policy
            override val key: StrategyKey = key

            override fun create(config: TimelineMathConfig): TimelineMathEngine = factory(config)
        },
    )
}

/** Регистрирует фабрику UI; каждый вызов должен создавать независимый renderer. */
fun TimelineStrategyRegistryContract.registerUi(
    key: StrategyKey,
    factory: (TimelineUiConfig) -> TimelineUiRenderer,
) = registerUi(key, TimelineCachePolicy.NONE, factory)

/** Регистрирует renderer с явным согласием на кэширование рассчитанного frame. */
fun TimelineStrategyRegistryContract.registerUi(
    key: StrategyKey,
    policy: TimelineCachePolicy,
    factory: (TimelineUiConfig) -> TimelineUiRenderer,
) {
    registerUi(
        object : TimelineUiProvider, TimelineCachePolicyProvider {
            override val cachePolicy = policy
            override val key: StrategyKey = key

            override fun create(config: TimelineUiConfig): TimelineUiRenderer = factory(config)
        },
    )
}
