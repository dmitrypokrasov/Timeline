package com.dmitrypokrasov.timelineview.strategy
import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer

/** Выбирает provider по ключу; по умолчанию сохраняет fallback встроенных стратегий. */
class TimelineStrategyResolver(
    private val registry: TimelineStrategyRegistryContract = TimelineStrategyRegistry,
) {
    private var strict = false

    /** Неизвестный ключ в строгом режиме вызывает ошибку вместо встроенного fallback. */
    constructor(registry: TimelineStrategyRegistryContract, strict: Boolean) : this(registry) {
        this.strict = strict
    }

    fun resolveMath(config: TimelineConfig): TimelineMathEngine {
        return resolveMath(config.mathStrategyKey, config.mathStrategy, config.math)
    }

    fun resolveUi(config: TimelineConfig): TimelineUiRenderer {
        return resolveUi(config.uiStrategyKey, config.uiStrategy, config.ui)
    }

    fun resolveMath(
        strategy: TimelineMathStrategy,
        config: TimelineMathConfig,
    ): TimelineMathEngine {
        return resolveMath(strategy.key, strategy, config)
    }

    fun resolveUi(
        strategy: TimelineUiStrategy,
        config: TimelineUiConfig,
    ): TimelineUiRenderer {
        return resolveUi(strategy.key, strategy, config)
    }

    fun resolveMath(
        strategyKey: StrategyKey?,
        fallbackStrategy: TimelineMathStrategy,
        config: TimelineMathConfig,
    ): TimelineMathEngine = resolveMathWithPolicy(strategyKey, fallbackStrategy, config).instance

    fun resolveUi(
        strategyKey: StrategyKey?,
        fallbackStrategy: TimelineUiStrategy,
        config: TimelineUiConfig,
    ): TimelineUiRenderer = resolveUiWithPolicy(strategyKey, fallbackStrategy, config).instance

    internal fun resolveMathWithPolicy(
        strategyKey: StrategyKey?,
        fallbackStrategy: TimelineMathStrategy,
        config: TimelineMathConfig,
    ): TimelineResolvedStrategy<TimelineMathEngine> {
        val preferred = strategyKey ?: fallbackStrategy.key
        val provider =
            registry.getMathProvider(preferred) ?: run {
                require(!strict) { "Unknown math strategy key: ${preferred.value}" }
                requireNotNull(registry.getMathProvider(fallbackStrategy.key)) { "No built-in math strategy registered for key: ${fallbackStrategy.key.value}" }
            }
        val policy = (provider as? TimelineCachePolicyProvider)?.cachePolicy ?: TimelineCachePolicy.NONE
        return TimelineResolvedStrategy(provider.create(config), policy)
    }

    internal fun resolveUiWithPolicy(
        strategyKey: StrategyKey?,
        fallbackStrategy: TimelineUiStrategy,
        config: TimelineUiConfig,
    ): TimelineResolvedStrategy<TimelineUiRenderer> {
        val preferred = strategyKey ?: fallbackStrategy.key
        val provider =
            registry.getUiProvider(preferred) ?: run {
                require(!strict) { "Unknown UI strategy key: ${preferred.value}" }
                requireNotNull(registry.getUiProvider(fallbackStrategy.key)) { "No built-in UI strategy registered for key: ${fallbackStrategy.key.value}" }
            }
        val policy = (provider as? TimelineCachePolicyProvider)?.cachePolicy ?: TimelineCachePolicy.NONE
        return TimelineResolvedStrategy(provider.create(config), policy)
    }
}
