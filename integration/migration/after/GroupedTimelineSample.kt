package com.example.migration

import android.content.Context
import com.dmitrypokrasov.timelineview.config.TimelineConfigParser
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.model.TimelineSection
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.ui.GroupedTimelineView

/** The host chooses section order, labels and the time zone used for date grouping. */
fun groupedTimeline(context: Context): GroupedTimelineView {
    val config = TimelineConfigParser(context).parse(null).copy(
        mathStrategy = TimelineMathStrategy.LinearVertical,
        uiStrategy = TimelineUiStrategy.Linear,
    )
    return GroupedTimelineView(context).apply {
        setSections(listOf(
            TimelineSection("today", "Today", listOf(TimelineStepData(id = "delivery", title = "Delivered", progress = 100))),
            TimelineSection("yesterday", "Yesterday", listOf(TimelineStepData(id = "dispatch", title = "Dispatched", progress = 100))),
        ), config)
    }
}
