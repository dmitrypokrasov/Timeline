package com.dmitrypokrasov.timelineview.math.data

/**
 * Layout data for rendering the timeline.
 */
data class TimelineLayout(
    val steps: List<TimelineLayoutStep>,
    val progressIcon: TimelineProgressIcon?
)