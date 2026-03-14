package com.dmitrypokrasov.timelineview.strategy

import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer

/**
 * Factory provider for custom UI renderers.
 */
interface TimelineUiProvider {
    /** Unique key used to register and resolve this provider. */
    val key: StrategyKey

    /** Creates a renderer for the supplied [config]. */
    fun create(config: TimelineUiConfig): TimelineUiRenderer
}
