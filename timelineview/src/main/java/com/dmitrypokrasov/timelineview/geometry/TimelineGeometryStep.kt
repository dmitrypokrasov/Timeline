package com.dmitrypokrasov.timelineview.geometry

import com.dmitrypokrasov.timelineview.model.TimelineStepData

/** Размещение шага в локальных пикселях. titleY и descriptionY задают baseline; ширины положительны. */
data class TimelineGeometryStep(
    val step: TimelineStepData,
    val titleX: Float,
    val titleY: Float,
    val titleWidth: Int,
    val descriptionX: Float,
    val descriptionY: Float,
    val descriptionWidth: Int,
    val iconX: Float,
    val iconY: Float,
    val textAlign: TimelineTextAlignment,
)
