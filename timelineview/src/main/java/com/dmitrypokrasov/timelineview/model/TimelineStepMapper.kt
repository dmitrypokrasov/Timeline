package com.dmitrypokrasov.timelineview.model

import android.content.Context

/**
 * Преобразует старую модель [TimelineStep] в новую [TimelineStepData] для обратной совместимости.
 */
fun TimelineStep.toTimelineStepData(context: Context): TimelineStepData {
    return TimelineStepData(
        title = context.safeString(title),
        description = context.safeString(description),
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

private fun Context.safeString(resourceId: Int): CharSequence? {
    if (resourceId == 0) return null
    val value = getString(resourceId)
    return value.takeIf { it.isNotBlank() }
}
