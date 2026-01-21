package com.dmitrypokrasov.timeline

import android.content.Context
import androidx.core.content.ContextCompat
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.model.TimelineStep

object TimelineSampleData {
    fun buildSteps(): List<TimelineStep> {
        return listOf(
            TimelineStep(
                title = R.string.title_1_lvl,
                description = R.string.description_1_9_steps,
                icon = R.drawable.ic_active,
                count = 5,
                maxCount = 9
            ),
            TimelineStep(
                title = R.string.title_2_lvl,
                description = R.string.description_10_99_steps,
                icon = R.drawable.ic_active,
                count = 0,
                maxCount = 99
            ),
            TimelineStep(
                title = R.string.title_3_lvl,
                description = R.string.description_100_999_steps,
                icon = R.drawable.ic_active,
                count = 0,
                maxCount = 999
            ),
            TimelineStep(
                title = R.string.title_4_lvl,
                description = R.string.description_1000_9999_steps,
                icon = R.drawable.ic_active,
                count = 0,
                maxCount = 9999
            ),
            TimelineStep(
                title = R.string.title_5_lvl,
                description = R.string.description_10000_99999_steps,
                icon = R.drawable.ic_unactive,
                count = 0,
                maxCount = 99999
            )
        )
    }

    fun buildMathConfig(context: Context, steps: List<TimelineStep>): TimelineMathConfig {
        return TimelineMathConfig(
            steps = steps,
            stepY = context.resources.getDimension(R.dimen.dimen_80dp),
            startPosition = TimelineMathConfig.StartPosition.START,
            marginHorizontalStroke = context.resources.getDimension(R.dimen.dimen_40dp),
            marginHorizontalText = context.resources.getDimension(R.dimen.dimen_80dp),
            marginHorizontalImage = context.resources.getDimension(R.dimen.dimen_16dp),
            marginTopTitle = context.resources.getDimension(R.dimen.dimen_52dp),
            marginTopDescription = context.resources.getDimension(R.dimen.dimen_16dp),
            marginTopProgressIcon = context.resources.getDimension(R.dimen.dimen_6dp),
            stepYFirst = context.resources.getDimension(R.dimen.dimen_20dp),
            sizeImageLvl = context.resources.getDimension(R.dimen.dimen_48dp),
            sizeIconProgress = context.resources.getDimension(R.dimen.dimen_28dp),
        )
    }

    fun buildUiConfig(context: Context): TimelineUiConfig {
        return TimelineUiConfig(
            iconProgress = R.drawable.ic_progress_timeline,
            iconDisableLvl = R.drawable.ic_unactive,
            colorTitle = ContextCompat.getColor(context, R.color.black),
            sizeTitle = context.resources.getDimension(R.dimen.dimen_12sp),
            sizeDescription = context.resources.getDimension(R.dimen.dimen_12sp),
            sizeStroke = context.resources.getDimension(R.dimen.dimen_6dp),
            colorDescription = ContextCompat.getColor(context, R.color.black),
            radius = context.resources.getDimension(R.dimen.dimen_48dp),
            colorStroke = ContextCompat.getColor(context, R.color.teal_700),
            colorProgress = ContextCompat.getColor(context, R.color.teal_200),
        )
    }
}
