package com.dmitrypokrasov.timelineview.strategy

import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer

/**
 * Factory provider for UI strategy implementations.
 */
interface TimelineUiProvider {
    val key: StrategyKey
    fun create(config: TimelineUiConfig): TimelineUiRenderer
}