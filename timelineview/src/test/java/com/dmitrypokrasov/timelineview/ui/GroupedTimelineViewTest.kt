package com.dmitrypokrasov.timelineview.ui

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.accessibility.AccessibilityManager
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class GroupedTimelineViewTest {
    @Test
    @Config(sdk = [28, 34])
    fun `attached section reorder preserves focus and overlay instances without detaching`() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().visible()
        try {
            val manager = activity.get().getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            shadowOf(manager).setEnabled(true)
            shadowOf(manager).setTouchExplorationEnabled(true)
            val view = GroupedTimelineView(activity.get())
            activity.get().setContentView(view)
            val animated =
                section("b").let { section ->
                    section.copy(steps = section.steps.map { it.copy(badgeAnimation = com.dmitrypokrasov.timelineview.model.TimelineLottieSpec(com.dmitrypokrasov.timelineview.R.raw.timeline_test_animation)) })
                }
            view.setSections(listOf(section("a"), animated, section("c")), config)
            measure(view)
            val container = view.getChildAt(1) as LinearLayout
            val timeline = container.getChildAt(1) as TimelineView
            var detaches = 0
            timeline.addOnAttachStateChangeListener(
                object : View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View) = Unit

                    override fun onViewDetachedFromWindow(v: View) {
                        detaches++
                    }
                },
            )

            fun overlay(): Any {
                val controllerField = TimelineView::class.java.getDeclaredField("controller").apply { isAccessible = true }
                val controller = controllerField.get(timeline)
                val overlayField = TimelineViewController::class.java.getDeclaredField("lottieOverlayManager").apply { isAccessible = true }
                val overlays = overlayField.get(controller)
                val entriesField = TimelineLottieOverlayManager::class.java.getDeclaredField("entries").apply { isAccessible = true }
                return (entriesField.get(overlays) as Map<*, *>).values.single()!!
            }
            val originalOverlay = overlay()
            val provider = requireNotNull(timeline.accessibilityNodeProvider)
            assertTrue(provider.performAction(0, AccessibilityNodeInfo.ACTION_FOCUS, null))
            assertTrue(provider.performAction(0, AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null))
            var clicked = ""
            view.setOnStepClickListener { section, index, step -> clicked = "$section:$index:${step.id}" }
            view.replaceSections(listOf(animated, section("new"), section("c"), section("a")))
            measure(view)
            assertSame(container, view.getChildAt(0))
            assertEquals(0, detaches)
            assertSame(originalOverlay, overlay())
            assertTrue(requireNotNull(provider.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)).contentDescription.contains("Event"))
            assertTrue(requireNotNull(provider.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY)).contentDescription.contains("Event"))
            assertTrue(provider.performAction(0, AccessibilityNodeInfo.ACTION_CLICK, null))
            assertEquals("b:0:same-step-id", clicked)
            view.replaceSections(listOf(animated.copy(steps = emptyList())))
            measure(view)
            assertNull(provider.findFocus(AccessibilityNodeInfo.FOCUS_INPUT))
            assertNull(provider.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY))
        } finally {
            activity.pause().stop().destroy()
        }
    }

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

    @Test
    fun `blank headings are rejected including mutated text before replacing sections`() {
        listOf("", "  ", "\t\n").forEach { title ->
            assertThrows(IllegalArgumentException::class.java) { TimelineSection("id", title, emptyList()) }
        }
        val view = GroupedTimelineView(RuntimeEnvironment.getApplication())
        view.setSections(listOf(section("today")), config)
        val original = view.getChildAt(0)
        val title = StringBuilder("Tomorrow")
        val candidate = TimelineSection("tomorrow", title, emptyList())
        title.clear()
        assertThrows(IllegalArgumentException::class.java) { view.replaceSections(listOf(candidate)) }
        assertSame(original, view.getChildAt(0))
        assertEquals("today", view.getSections().single().id)
    }

    private fun measure(view: View) {
        view.measure(View.MeasureSpec.makeMeasureSpec(320, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        view.layout(0, 0, 320, view.measuredHeight)
    }
}
