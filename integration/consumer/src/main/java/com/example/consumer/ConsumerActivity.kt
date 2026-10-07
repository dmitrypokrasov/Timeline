package com.example.consumer

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import com.dmitrypokrasov.timelineview.model.TimelineLottieSpec
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.ui.TimelineView

/** Runtime smoke host for the minified APK built against the staged AAR. */
class ConsumerActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val status = TextView(this).apply { tag = "status"; text = "Ready" }
        val timeline = layoutInflater.inflate(R.layout.migrated_timeline, null) as TimelineView
        timeline.tag = "timeline"
        val animation = TimelineLottieSpec(R.raw.timeline_badge_pulse, scale = 3f)
        timeline.replaceSteps(listOf(
            TimelineStepData(id = "order", title = "Order", progress = 20, badgeAnimation = animation),
            TimelineStepData(id = "delivery", title = "Delivery", progress = 0),
        ))
        timeline.setOnStepClickListener { _, step -> status.text = "Clicked ${step.id}" }
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            addView(status)
            addView(timeline)
        })
    }
}
