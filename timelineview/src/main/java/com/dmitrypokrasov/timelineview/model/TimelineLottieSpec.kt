package com.dmitrypokrasov.timelineview.model

import androidx.annotation.RawRes

/**
 * Describes a Lottie animation overlay for a timeline element.
 */
data class TimelineLottieSpec(
    @RawRes val rawRes: Int,
    val repeat: Boolean = true,
    val autoPlay: Boolean = true,
    val scale: Float = 1f
) {
    init {
        require(rawRes != 0) { "rawRes must be a valid raw resource id" }
        require(scale > 0f) { "scale must be greater than 0" }
    }
}
