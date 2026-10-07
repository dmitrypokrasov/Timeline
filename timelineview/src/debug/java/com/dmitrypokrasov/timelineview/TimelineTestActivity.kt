package com.dmitrypokrasov.timelineview

import android.app.Activity
import android.os.Bundle
import android.widget.ScrollView
import com.dmitrypokrasov.timelineview.ui.TimelineView

/** Debug-only host for device lifecycle and input regression tests. */
class TimelineTestActivity : Activity() {
    lateinit var timeline: TimelineView
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        timeline = TimelineView(this)
        setContentView(ScrollView(this).apply { addView(timeline) })
    }
}
