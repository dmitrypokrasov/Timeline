package com.dmitrypokrasov.timelineview.geometry

/** Снимок геометрии: список шагов, активная иконка и индекс её шага. */
data class TimelineGeometryLayout(
    val steps: List<TimelineGeometryStep>,
    val progressIcon: TimelineGeometryProgressIcon?,
    val progressStepIndex: Int?,
)
