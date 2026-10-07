package com.dmitrypokrasov.timelineview.ui

import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.dmitrypokrasov.timelineview.config.TimelineConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.model.TimelineSection
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class GroupedTimelineViewTest {
    private val config = TimelineConfig(TimelineMathConfig(), TimelineUiConfig(), mathStrategy = TimelineMathStrategy.LinearVertical)

    private fun section(id: String) = TimelineSection(id, "Day $id", listOf(TimelineStepData(id = "same-step-id", title = "Event", progress = 0)))

    @Test
    fun `headings and click identity follow reordered groups while views are reused`() {
        val view = GroupedTimelineView(RuntimeEnvironment.getApplication())
        view.setSections(listOf(section("today"), section("yesterday")), config)
        val today = view.getChildAt(0)
        val yesterday = view.getChildAt(1)
        assertTrue(ViewCompat.isAccessibilityHeading((today as LinearLayout).getChildAt(0)))
        var clicked = ""
        view.setOnStepClickListener { group, index, step -> clicked = "$group:$index:${step.id}" }
        view.replaceSections(listOf(section("yesterday"), section("today")))
        assertSame(yesterday, view.getChildAt(0))
        assertSame(today, view.getChildAt(1))
        measure(view)
        val timeline = today.getChildAt(1) as TimelineView
        assertTrue(requireNotNull(timeline.accessibilityNodeProvider).performAction(0, AccessibilityNodeInfo.ACTION_CLICK, null))
        assertEquals("today:0:same-step-id", clicked)
        view.setOnStepClickListener(null)
        assertFalse(requireNotNull(timeline.accessibilityNodeProvider).performAction(0, AccessibilityNodeInfo.ACTION_CLICK, null))
        view.replaceSections(listOf(section("today").copy(steps = emptyList())))
        assertEquals(1, view.childCount)
        assertEquals("Day today", ((view.getChildAt(0) as LinearLayout).getChildAt(0) as TextView).text.toString())
    }

    @Test
    fun `sections snapshot inputs and reject duplicate ids before changing children`() {
        val view = GroupedTimelineView(RuntimeEnvironment.getApplication())
        val steps = mutableListOf(TimelineStepData(title = "Original", progress = 0))
        view.setSections(listOf(TimelineSection("today", "Today", steps)), config)
        steps.clear()
        assertEquals(1, view.getSections().single().steps.size)
        val original = view.getChildAt(0)
        assertThrows(IllegalArgumentException::class.java) { view.replaceSections(listOf(section("duplicate"), section("duplicate"))) }
        assertSame(original, view.getChildAt(0))
        assertEquals("today", view.getSections().single().id)
    }

    @Test
    fun `all built in layouts support groups and invalid time ranges leave groups intact`() {
        val view = GroupedTimelineView(RuntimeEnvironment.getApplication())
        val groups = listOf(section("a").copy(steps = listOf(TimelineStepData(timestampMillis = 1L, progress = 0))), section("empty").copy(steps = emptyList()))
        TimelineMathStrategy.entries.forEach { strategy ->
            view.setSections(groups, config.copy(mathStrategy = strategy))
            measure(view)
            assertTrue(view.measuredHeight > 0)
        }
        val original = view.getChildAt(0)
        assertThrows(IllegalArgumentException::class.java) { view.replaceSections(listOf(section("missing-timestamp"))) }
        assertSame(original, view.getChildAt(0))
        view.replaceSections(emptyList())
        assertEquals(0, view.childCount)
    }

    private fun measure(view: View) {
        view.measure(View.MeasureSpec.makeMeasureSpec(320, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        view.layout(0, 0, 320, view.measuredHeight)
    }
}
