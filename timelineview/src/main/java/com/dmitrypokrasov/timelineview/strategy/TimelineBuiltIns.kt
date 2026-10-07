package com.dmitrypokrasov.timelineview.strategy

import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.math.AdaptiveGridTimelineMath
import com.dmitrypokrasov.timelineview.math.AlternatingTimelineMath
import com.dmitrypokrasov.timelineview.math.LinearTimelineMath
import com.dmitrypokrasov.timelineview.math.SnakeTimelineMath
import com.dmitrypokrasov.timelineview.math.TimeScaledTimelineMath
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.render.LinearTimelineUi
import com.dmitrypokrasov.timelineview.render.SnakeTimelineUi
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer

/** Single source for factory construction and default registry providers. */
internal object TimelineBuiltIns {
    val math: Map<StrategyKey, (TimelineMathConfig) -> TimelineMathEngine> =
        linkedMapOf(
            TimelineMathStrategy.Snake.key to ::SnakeTimelineMath,
            TimelineMathStrategy.LinearVertical.key to { LinearTimelineMath(it, LinearTimelineMath.Orientation.VERTICAL) },
            TimelineMathStrategy.LinearHorizontal.key to { LinearTimelineMath(it, LinearTimelineMath.Orientation.HORIZONTAL) },
            TimelineMathStrategy.Alternating.key to ::AlternatingTimelineMath,
            TimelineMathStrategy.AdaptiveGrid.key to ::AdaptiveGridTimelineMath,
            TimelineMathStrategy.TimeScaled.key to ::TimeScaledTimelineMath,
        )
    val ui: Map<StrategyKey, (TimelineUiConfig) -> TimelineUiRenderer> =
        linkedMapOf(
            TimelineUiStrategy.Snake.key to ::SnakeTimelineUi,
            TimelineUiStrategy.Linear.key to ::LinearTimelineUi,
        )

    fun register(registry: TimelineStrategyRegistryContract) {
        math.forEach { (id, factory) ->
            registry.registerMath(
                object : TimelineMathProvider {
                    override val key = id

                    override fun create(config: TimelineMathConfig): TimelineMathEngine = factory(config)
                },
            )
        }
        ui.forEach { (id, factory) ->
            registry.registerUi(
                object : TimelineUiProvider {
                    override val key = id

                    override fun create(config: TimelineUiConfig): TimelineUiRenderer = factory(config)
                },
            )
        }
    }
}
