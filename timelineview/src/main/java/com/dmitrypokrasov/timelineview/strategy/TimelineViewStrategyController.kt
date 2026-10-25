package com.dmitrypokrasov.timelineview.strategy

import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy

/**
 * Composes math and UI engines for the [com.dmitrypokrasov.timelineview.ui.TimelineView].
 */
class TimelineViewStrategyController(
    registry: TimelineStrategyRegistryContract = TimelineStrategyRegistry,
) {
    private var resolver = TimelineStrategyResolver(registry)

    /** Сохраняет совместимый конструктор и явно включает строгую проверку ключей. */
    constructor(registry: TimelineStrategyRegistryContract, strict: Boolean) : this(registry) {
        resolver = TimelineStrategyResolver(registry, strict)
    }

    fun resolve(config: TimelineConfig): TimelineViewStrategiesData {
        return TimelineViewStrategiesData(
            math = resolver.resolveMath(config),
            ui = resolver.resolveUi(config),
        )
    }

    fun resolve(
        mathStrategy: TimelineMathStrategy,
        uiStrategy: TimelineUiStrategy,
        mathConfig: TimelineMathConfig,
        uiConfig: TimelineUiConfig,
    ): TimelineViewStrategiesData {
        return TimelineViewStrategiesData(
            math = resolver.resolveMath(mathStrategy, mathConfig),
            ui = resolver.resolveUi(uiStrategy, uiConfig),
        )
    }

    fun resolve(
        mathStrategyKey: StrategyKey?,
        uiStrategyKey: StrategyKey?,
        fallbackMath: TimelineMathStrategy,
        fallbackUi: TimelineUiStrategy,
        explicitMath: com.dmitrypokrasov.timelineview.math.TimelineMathEngine? = null,
        explicitUi: com.dmitrypokrasov.timelineview.render.TimelineUiRenderer? = null,
        mathConfig: TimelineMathConfig,
        uiConfig: TimelineUiConfig,
    ): TimelineViewStrategiesData {
        return TimelineViewStrategiesData(
            math = explicitMath ?: resolver.resolveMath(mathStrategyKey, fallbackMath, mathConfig),
            ui = explicitUi ?: resolver.resolveUi(uiStrategyKey, fallbackUi, uiConfig),
        )
    }

    /** Политика снимается одновременно с созданием экземпляров, без повторного обращения к registry. */
    internal fun resolveWithPolicy(
        mathStrategyKey: StrategyKey?,
        uiStrategyKey: StrategyKey?,
        fallbackMath: TimelineMathStrategy,
        fallbackUi: TimelineUiStrategy,
        explicitMath: com.dmitrypokrasov.timelineview.math.TimelineMathEngine?,
        explicitUi: com.dmitrypokrasov.timelineview.render.TimelineUiRenderer?,
        mathConfig: TimelineMathConfig,
        uiConfig: TimelineUiConfig,
    ): TimelineResolvedStrategies {
        val math =
            explicitMath?.let { TimelineResolvedStrategy(it, TimelineCachePolicy.NONE) }
                ?: resolver.resolveMathWithPolicy(mathStrategyKey, fallbackMath, mathConfig)
        val ui =
            explicitUi?.let { TimelineResolvedStrategy(it, TimelineCachePolicy.NONE) }
                ?: resolver.resolveUiWithPolicy(uiStrategyKey, fallbackUi, uiConfig)
        return TimelineResolvedStrategies(math.instance, ui.instance, minOf(math.policy, ui.policy))
    }
}
