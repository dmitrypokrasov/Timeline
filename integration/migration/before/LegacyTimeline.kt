package com.example.migration.legacy

import android.content.Context
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.math.LinearTimelineMath
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.render.LinearTimelineUi
import com.dmitrypokrasov.timelineview.ui.TimelineView

/** This source is compiled against the unchanged published 1.1.0 AAR. */
fun legacyTimeline(context: Context): TimelineView {
    val math = TimelineMathConfig(steps = listOf(TimelineStepData(title = "Order", progress = 20)))
    math.spacing.stepY = 80f
    math.sizes.sizeImageLvl = 24f
    return TimelineView(context).apply {
        setMathEngine(LinearTimelineMath(math))
        setUiRenderer(LinearTimelineUi(TimelineUiConfig()))
    }
}
