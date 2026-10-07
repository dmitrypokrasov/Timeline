package com.dmitrypokrasov.timelineview.model

import androidx.annotation.DrawableRes

/**
 * Represents timeline step data ready for rendering.
 */
data class TimelineStepData(
    val title: CharSequence? = null,
    val description: CharSequence? = null,
    @DrawableRes val iconRes: Int? = null,
    @DrawableRes val iconDisabledRes: Int? = null,
    val badgeAnimation: TimelineLottieSpec? = null,
    val progressAnimation: TimelineLottieSpec? = null,
    val progress: Int,
    /** Stable identity across updates and reordering; unique within a timeline. Null uses position. */
    val id: String? = null,
    /** Optional epoch milliseconds for a time-scaled strategy. */
    val timestampMillis: Long? = null,
) {
    init {
        require(id == null || id.isNotBlank()) { "id must not be blank" }
        require(progress in 0..100) { "progress must be in 0..100" }
    }
}

/** Distinguishes explicit identities from the positional fallback. */
internal fun TimelineStepData.identity(index: Int): String = id?.let { "id:$it" } ?: "index:$index"

internal fun List<TimelineStepData>.indexOfStep(step: TimelineStepData): Int {
    val index = if (step.id != null) indexOfFirst { it.id == step.id } else indexOfFirst { it === step }.takeIf { it >= 0 } ?: indexOf(step)
    require(index >= 0) { "Step does not belong to this timeline" }
    return index
}
