package com.example.consumer

import android.app.Activity
import android.os.Bundle
import android.os.Trace
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.model.TimelineLottieSpec
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.ui.TimelineView

/** Device benchmark host in the test-only, minified published-AAR consumer. */
class BenchmarkActivity : Activity() {
    private lateinit var timeline: TimelineView
    private var strategy = 0
    private var revision = 0
    private var count = 100
    private var animated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        strategy = intent.getIntExtra("strategy", 0).coerceIn(TimelineMathStrategy.entries.indices)
        count = intent.getIntExtra("count", 100).coerceIn(1, 1000)
        animated = intent.getBooleanExtra("animated", false)
        timeline = TimelineView(this)
        timeline.setConfig(timeline.getConfig().let {
            it.copy(math = it.math.copy(horizontalLayout = TimelineMathConfig.HorizontalLayout.WRAP))
        })
        timeline.replaceSteps(steps())
        selectStrategy()
        val controls = LinearLayout(this).apply {
            addView(button("progress") {
                timeline.replaceSteps(timeline.getConfig().math.steps.map { it.copy(progress = (it.progress + 10) % 101) })
            })
            addView(button("data") { revision++; timeline.replaceSteps(steps()) })
            addView(button("strategy") { strategy = (strategy + 1) % TimelineMathStrategy.entries.size; selectStrategy() })
        }
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(controls)
            addView(ScrollView(this@BenchmarkActivity).apply {
                contentDescription = "timeline-scroll"
                addView(timeline)
            }, LinearLayout.LayoutParams(-1, 0, 1f))
        })
    }

    private fun steps() = List(count) { index ->
        TimelineStepData(
            id = "event-$index", title = "Event $index, revision $revision",
            description = "Device performance fixture", progress = if (index < count / 2) 100 else 20,
            timestampMillis = index * 60_000L,
            badgeAnimation = if (animated) TimelineLottieSpec(R.raw.timeline_badge_pulse) else null,
        )
    }

    private fun selectStrategy() {
        val math = TimelineMathStrategy.entries[strategy]
        timeline.setStrategy(math, if (math == TimelineMathStrategy.Snake) TimelineUiStrategy.Snake else TimelineUiStrategy.Linear)
    }

    private fun button(action: String, block: () -> Unit) = Button(this).apply {
        text = action
        contentDescription = action
        setOnClickListener {
            Trace.beginSection("timeline-$action")
            try { block() } finally { Trace.endSection() }
        }
    }
}
