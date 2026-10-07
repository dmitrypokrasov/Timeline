package com.dmitrypokrasov.timelineview.ui

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.math.SnakeTimelineMath
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.render.BaseTimelineUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28, 34])
class TimelineFocusRegressionTest {
    @Test
    fun `large snake titles clear the horizontal connector`() {
        val math = TimelineMathConfig(steps = listOf(TimelineStepData(title = "Large title", progress = 0)))
        val engine = SnakeTimelineMath(math)
        val renderer = BaseTimelineUi(TimelineUiConfig(textSizes = TimelineUiConfig.TextSizes(sizeTitle = 32f)))
        renderer.initTools(math, org.robolectric.RuntimeEnvironment.getApplication())
        val layout = TimelineLayoutResolver.resolve(engine, renderer, 320, 4f)
        val text = TimelineTextBlockResolver.resolve(layout, engine, renderer).single()
        assertTrue(text.titleTop >= math.spacing.stepYFirst + renderer.getConfig().stroke.sizeStroke / 2f + 4f)
    }

    @Test
    fun `keyboard focus follows stable identity and clears when the step is removed`() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup()
        try {
            val view = TimelineView(activity.get())
            val manager = activity.get().getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            shadowOf(manager).setEnabled(true)
            shadowOf(manager).setTouchExplorationEnabled(true)
            activity.get().setContentView(view)
            val first = TimelineStepData(id = "first", title = "First", progress = 100)
            val second = TimelineStepData(id = "second", title = "Second", progress = 100)
            view.setConfig(TimelineMathConfig(steps = listOf(first, second)), TimelineUiConfig())
            measure(view)
            val provider = requireNotNull(view.accessibilityNodeProvider)
            provider.performAction(0, AccessibilityNodeInfo.ACTION_FOCUS, null)
            assertTrue(provider.performAction(0, AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null))
            view.replaceSteps(listOf(second, first))
            measure(view)
            assertTrue(requireNotNull(provider.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)).contentDescription.contains("First"))
            assertTrue(requireNotNull(provider.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY)).contentDescription.contains("First"))
            var clicked: String? = null
            view.setOnStepClickListener { index, step ->
                assertEquals(1, index)
                clicked = step.id
            }
            assertTrue(provider.performAction(0, AccessibilityNodeInfo.ACTION_CLICK, null))
            assertEquals("first", clicked)
            view.replaceSteps(listOf(second))
            measure(view)
            assertNull(provider.findFocus(AccessibilityNodeInfo.FOCUS_INPUT))
            assertNull(provider.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY))
        } finally {
            activity.pause().stop().destroy()
        }
    }

    private fun measure(view: View) {
        view.measure(View.MeasureSpec.makeMeasureSpec(320, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        view.layout(0, 0, 320, view.measuredHeight)
    }
}
