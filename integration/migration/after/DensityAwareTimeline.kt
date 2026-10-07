package com.example.migration

import android.content.Context
import com.dmitrypokrasov.timelineview.config.TimelineDefaults
import com.dmitrypokrasov.timelineview.ui.TimelineView

/** Development API: convert dp/sp at the host boundary; configuration still stores pixels. */
fun densityAwareTimeline(context: Context): TimelineView {
    val defaults = TimelineDefaults.config(context)
    val math = defaults.math.copy(spacing = defaults.math.spacing.copy(stepY = TimelineDefaults.dp(context, 80f)))
    val ui = defaults.ui.copy(textSizes = defaults.ui.textSizes.copy(sizeTitle = TimelineDefaults.sp(context, 18f)))
    return TimelineView(context).apply { setConfig(defaults.copy(math = math, ui = ui)) }
}
