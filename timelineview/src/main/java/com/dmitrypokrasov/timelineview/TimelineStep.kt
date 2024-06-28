package com.dmitrypokrasov.timelineview

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class TimelineStep(
    @StringRes val title: Int,
    @StringRes val description: Int,
    @DrawableRes val icon: Int,
    val count: Int = 0,
    val maxCount: Int
) {
    val percents: Int
        get() = (count.toDouble() / (maxCount.toDouble() / 100)).toInt()
}
