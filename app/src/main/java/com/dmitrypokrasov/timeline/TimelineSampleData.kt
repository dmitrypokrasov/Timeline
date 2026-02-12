package com.dmitrypokrasov.timeline

import android.content.Context
import androidx.core.content.ContextCompat
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.model.TimelineStepData

object TimelineSampleData {
    fun buildSteps(context: Context): List<TimelineStepData> {
        fun progress(count: Int, maxCount: Int): Int {
            return if (maxCount <= 0) {
                0
            } else {
                (count.toDouble() / (maxCount.toDouble() / 100)).toInt().coerceIn(0, 100)
            }
        }

        return listOf(
            TimelineStepData(
                title = context.getString(R.string.title_1_lvl),
                description = context.getString(R.string.description_1_9_steps),
                iconRes = R.drawable.ic_active,
                iconDisabledRes = R.drawable.ic_unactive,
                progress = progress(5, 9)
            ),
            TimelineStepData(
                title = context.getString(R.string.title_2_lvl),
                description = context.getString(R.string.description_10_99_steps),
                iconRes = R.drawable.ic_active,
                iconDisabledRes = R.drawable.ic_unactive,
                progress = progress(0, 99)
            ),
            TimelineStepData(
                title = context.getString(R.string.title_3_lvl),
                description = context.getString(R.string.description_100_999_steps),
                iconRes = R.drawable.ic_active,
                iconDisabledRes = R.drawable.ic_unactive,
                progress = progress(0, 999)
            ),
            TimelineStepData(
                title = context.getString(R.string.title_4_lvl),
                description = context.getString(R.string.description_1000_9999_steps),
                iconRes = R.drawable.ic_active,
                iconDisabledRes = R.drawable.ic_unactive,
                progress = progress(0, 9999)
            ),
            TimelineStepData(
                title = context.getString(R.string.title_5_lvl),
                description = context.getString(R.string.description_10000_99999_steps),
                iconRes = R.drawable.ic_unactive,
                iconDisabledRes = R.drawable.ic_unactive,
                progress = progress(0, 99999)
            )
        )
    }

    fun buildMathConfig(context: Context, steps: List<TimelineStepData>): TimelineMathConfig {
        return TimelineMathConfig(
            steps = steps,
            startPosition = TimelineMathConfig.StartPosition.START,
            spacing = TimelineMathConfig.Spacing(
                stepY = context.resources.getDimension(R.dimen.dimen_80dp),
                marginHorizontalStroke = context.resources.getDimension(R.dimen.dimen_40dp),
                marginHorizontalText = context.resources.getDimension(R.dimen.dimen_80dp),
                marginHorizontalImage = context.resources.getDimension(R.dimen.dimen_16dp),
                marginTopTitle = context.resources.getDimension(R.dimen.dimen_52dp),
                marginTopDescription = context.resources.getDimension(R.dimen.dimen_16dp),
                marginTopProgressIcon = context.resources.getDimension(R.dimen.dimen_6dp),
                stepYFirst = context.resources.getDimension(R.dimen.dimen_20dp)
            ),
            sizes = TimelineMathConfig.Sizes(
                sizeImageLvl = context.resources.getDimension(R.dimen.dimen_48dp),
                sizeIconProgress = context.resources.getDimension(R.dimen.dimen_28dp)
            )
        )
    }

    fun buildUiConfig(context: Context): TimelineUiConfig {
        return TimelineUiConfig(
            icons = TimelineUiConfig.Icons(
                iconProgress = R.drawable.ic_progress_timeline,
                iconDisableLvl = R.drawable.ic_unactive
            ),
            colors = TimelineUiConfig.Colors(
                colorTitle = ContextCompat.getColor(context, R.color.black),
                colorDescription = ContextCompat.getColor(context, R.color.black),
                colorStroke = ContextCompat.getColor(context, R.color.teal_700),
                colorProgress = ContextCompat.getColor(context, R.color.teal_200)
            ),
            textSizes = TimelineUiConfig.TextSizes(
                sizeTitle = context.resources.getDimension(R.dimen.dimen_12sp),
                sizeDescription = context.resources.getDimension(R.dimen.dimen_12sp)
            ),
            stroke = TimelineUiConfig.Stroke(
                sizeStroke = context.resources.getDimension(R.dimen.dimen_6dp),
                radius = context.resources.getDimension(R.dimen.dimen_48dp)
            )
        )
    }
}
