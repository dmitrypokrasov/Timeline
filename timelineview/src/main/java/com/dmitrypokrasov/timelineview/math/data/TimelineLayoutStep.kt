package com.dmitrypokrasov.timelineview.math.data

import android.graphics.Paint
import com.dmitrypokrasov.timelineview.model.TimelineStepData

/**
 * Layout data for a single timeline step.
 */
data class TimelineLayoutStep(
    val step: TimelineStepData,
    val titleX: Float,
    val titleY: Float,
    val titleWidth: Int,
    val descriptionX: Float,
    val descriptionY: Float,
    val descriptionWidth: Int,
    val iconX: Float,
    val iconY: Float,
    val textAlign: Paint.Align,
)
