package com.example.consumer

import android.content.Context
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.ui.TimelineView

/** Compiled only against the staged Maven AAR, never against the source project. */
fun timeline(context: Context): TimelineView = TimelineView(context).apply {
    setStrategy(TimelineMathStrategy.LinearVertical, TimelineUiStrategy.Linear)
    val math = TimelineMathConfig(steps = listOf(TimelineStepData(title = "Ready", progress = 100)))
    setConfig(math.copy(sizes = math.sizes.copy(sizeImageLvl = 24f)), TimelineUiConfig())
    setOnStepClickListener { _, _ -> }
    setOnStepClickListener(null)
}
