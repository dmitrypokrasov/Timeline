package com.example.timelinecompose

import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import com.dmitrypokrasov.timelineview.config.TimelineConfigParser
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.ui.TimelineView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TimelineBindingTest {
    @Test fun updatesRefreshDataAndCallbacksWithoutMutatingHostState() {
        val context = RuntimeEnvironment.getApplication()
        val view = TimelineView(context)
        val initial = TimelineConfigParser(context).parse(null)
        val config = initial.copy(math = initial.math.copy(steps = listOf(TimelineStepData(id = "order", title = "Order", progress = 0))))
        var callback = ""
        updateTimeline(view, config) { _, _ -> callback = "old" }
        val updated = config.copy(math = config.math.copy(steps = config.math.steps.map { it.copy(progress = 50) }))
        updateTimeline(view, updated) { _, step -> callback = "new:${step.progress}" }
        view.measure(View.MeasureSpec.makeMeasureSpec(320, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        view.layout(0, 0, 320, view.measuredHeight)
        assertTrue(requireNotNull(view.accessibilityNodeProvider).performAction(0, AccessibilityNodeInfo.ACTION_CLICK, null))
        assertEquals("new:50", callback)
        assertEquals(0, config.math.steps.single().progress)
        updateTimeline(view, updated, null)
        assertFalse(requireNotNull(view.accessibilityNodeProvider).performAction(0, AccessibilityNodeInfo.ACTION_CLICK, null))
    }
}
