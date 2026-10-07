package com.example.timelinecompose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.dmitrypokrasov.timelineview.config.TimelineConfig
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.ui.TimelineView

/** Host-owned immutable state; this example works with the public 2.0 API. */
@Composable
fun Timeline(
    config: TimelineConfig,
    modifier: Modifier = Modifier,
    onStepClick: ((Int, TimelineStepData) -> Unit)? = null,
) {
    AndroidView(
        factory = { context -> TimelineView(context) },
        modifier = modifier,
        onReset = null,
        onRelease = { it.setOnStepClickListener(null) },
        update = { updateTimeline(it, config, onStepClick) },
    )
}

/** Data-only recomposition retains engine/renderer resources; callbacks are always refreshed. */
internal fun updateTimeline(view: TimelineView, config: TimelineConfig, onStepClick: ((Int, TimelineStepData) -> Unit)?) {
    val previous = view.getConfig()
    if (previous != config) {
        if (previous.copy(math = previous.math.copy(steps = config.math.steps)) == config) {
            view.replaceSteps(config.math.steps)
        } else {
            view.setConfig(config)
        }
    }
    view.setOnStepClickListener(onStepClick)
}
