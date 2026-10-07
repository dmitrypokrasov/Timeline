package com.example.migration

import android.content.Context
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.math.LinearTimelineMath
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.render.LinearTimelineUi
import com.dmitrypokrasov.timelineview.ui.TimelineView

/** The host owns immutable input; view updates are explicit. */
class MigratedTimeline(context: Context) {
    var steps = listOf(TimelineStepData(id = "order", title = "Order", progress = 20), TimelineStepData(id = "delivery", title = "Delivery", progress = 0))
        private set
    private var math = TimelineMathConfig(steps = steps, spacing = TimelineMathConfig.Spacing(stepY = 80f), sizes = TimelineMathConfig.Sizes(sizeImageLvl = 24f))
    private val ui = TimelineUiConfig()
    val view = TimelineView(context).apply {
        setStrategies(LinearTimelineMath(math), LinearTimelineUi(ui))
        setOnStepClickListener { _, step -> updateProgress(requireNotNull(step.id), (step.progress + 10).coerceAtMost(100)) }
    }

    fun updateProgress(id: String, progress: Int) {
        steps = steps.map { if (it.id == id) it.copy(progress = progress) else it }
        math = math.copy(steps = steps)
        view.replaceSteps(steps)
    }

    fun resize(rowDistancePx: Float) {
        math = math.copy(spacing = math.spacing.copy(stepY = rowDistancePx))
        view.setConfig(math, ui)
    }

    fun reorder() {
        steps = steps.reversed()
        math = math.copy(steps = steps)
        view.replaceSteps(steps)
    }
}
