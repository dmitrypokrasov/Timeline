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
) {
    init {
        require(progress in 0..100) { "progress must be in 0..100" }
    }
}
