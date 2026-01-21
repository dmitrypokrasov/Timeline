package com.dmitrypokrasov.timelineview.math

import android.graphics.Paint
import com.dmitrypokrasov.timelineview.model.TimelineStep

/**
 * Layout data for rendering the timeline.
 */
data class TimelineLayout(
    val steps: List<TimelineLayoutStep>,
    val progressIcon: TimelineProgressIcon?
)

/**
 * Layout data for a single timeline step.
 */
data class TimelineLayoutStep(
    val step: TimelineStep,
    val titleX: Float,
    val titleY: Float,
    val descriptionX: Float,
    val descriptionY: Float,
    val iconX: Float,
    val iconY: Float,
    val textAlign: Paint.Align
)

/**
 * Layout data for the progress icon.
 */
data class TimelineProgressIcon(
    val left: Float,
    val top: Float
)
