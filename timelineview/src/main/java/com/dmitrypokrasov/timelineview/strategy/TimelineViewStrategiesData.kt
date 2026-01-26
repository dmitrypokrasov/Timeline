package com.dmitrypokrasov.timelineview.strategy

import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer

data class TimelineViewStrategiesData(
    val math: TimelineMathEngine,
    val ui: TimelineUiRenderer
)