package com.dmitrypokrasov.timelineview.model

import android.content.Context

/**
 * Преобразует старую модель [TimelineStep] в новую [TimelineStepData] для обратной совместимости.
 */
fun TimelineStep.toTimelineStepData(context: Context): TimelineStepData {
    return TimelineStepData(
        title = context.getString(title),
        description = context.getString(description),
        iconRes = icon.takeIf { it != 0 },
        progress = percents
    )
}

/**
 * Преобразует список [TimelineStep] в список [TimelineStepData].
 */
fun List<TimelineStep>.toTimelineStepData(context: Context): List<TimelineStepData> {
    return map { it.toTimelineStepData(context) }
}
