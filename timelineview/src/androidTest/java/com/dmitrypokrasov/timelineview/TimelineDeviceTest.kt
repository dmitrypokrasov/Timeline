package com.dmitrypokrasov.timelineview

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.math.LinearTimelineMath
import com.dmitrypokrasov.timelineview.model.TimelineSection
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.render.LinearTimelineUi
import com.dmitrypokrasov.timelineview.ui.GroupedTimelineView
import com.dmitrypokrasov.timelineview.ui.TimelineView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimelineDeviceTest {
    @Test
    fun groupedReorderKeepsKeyboardFocusAndAttachment() {
        ActivityScenario.launch(TimelineTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val grouped = GroupedTimelineView(activity)
                activity.setContentView(grouped)
                val steps = listOf(TimelineStepData(id = "event", title = "Event", progress = 100))
                val first = TimelineSection("first", "First", steps)
                val second = TimelineSection("second", "Second", steps)
                grouped.replaceSections(listOf(first, second))

                fun measure() {
                    grouped.measure(View.MeasureSpec.makeMeasureSpec(320, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
                    grouped.layout(0, 0, 320, grouped.measuredHeight)
                }
                measure()
                val container = grouped.getChildAt(1) as android.widget.LinearLayout
                val timeline = container.getChildAt(1) as TimelineView
                timeline.isFocusableInTouchMode = true
                var detaches = 0
                timeline.addOnAttachStateChangeListener(
                    object : View.OnAttachStateChangeListener {
                        override fun onViewAttachedToWindow(v: View) = Unit

                        override fun onViewDetachedFromWindow(v: View) {
                            detaches++
                        }
                    },
                )
                val provider = requireNotNull(timeline.accessibilityNodeProvider)
                assertTrue(timeline.requestFocus())
                provider.performAction(0, AccessibilityNodeInfo.ACTION_FOCUS, null)
                assertTrue(requireNotNull(provider.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)).contentDescription.contains("Event"))
                grouped.replaceSections(listOf(second, first))
                measure()
                assertSame(container, grouped.getChildAt(0))
                assertEquals(0, detaches)
                assertTrue(requireNotNull(provider.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)).contentDescription.contains("Event"))
                timeline.minimumHeight = 300
                measure()
                assertTrue(timeline.measuredHeight >= 300)
            }
        }
    }

    @Test
    fun gesturesAccessibilityAndReattachment() {
        ActivityScenario.launch(TimelineTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val view = activity.timeline
                val steps = listOf(TimelineStepData(id = "first", title = "First event", progress = 100), TimelineStepData(id = "second", title = "Second event", progress = 0))
                val config = TimelineMathConfig(startPosition = TimelineMathConfig.StartPosition.START, steps = steps, spacing = TimelineMathConfig.Spacing(stepY = 80f, stepYFirst = 20f, marginHorizontalStroke = 24f, marginHorizontalImage = 12f, marginHorizontalText = 48f), sizes = TimelineMathConfig.Sizes(12f, 24f))
                view.setStrategies(LinearTimelineMath(config), LinearTimelineUi(TimelineUiConfig()))
                view.measure(View.MeasureSpec.makeMeasureSpec(320, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
                view.layout(0, 0, 320, view.measuredHeight)
                var clicks = 0
                view.setOnStepClickListener { _, _ -> clicks++ }

                fun touch(
                    action: Int,
                    x: Float,
                    y: Float,
                ) {
                    val time = SystemClock.uptimeMillis()
                    MotionEvent.obtain(time, time, action, x, y, 0).also {
                        view.dispatchTouchEvent(it)
                        it.recycle()
                    }
                }
                val provider = requireNotNull(view.accessibilityNodeProvider)
                val node = requireNotNull(provider.createAccessibilityNodeInfo(0))
                assertTrue(node.contentDescription.contains("First event"))
                val bounds = android.graphics.Rect()
                node.getBoundsInParent(bounds)
                touch(MotionEvent.ACTION_DOWN, bounds.centerX().toFloat(), bounds.centerY().toFloat())
                touch(MotionEvent.ACTION_UP, bounds.centerX().toFloat(), bounds.centerY().toFloat())
                assertEquals(1, clicks)
                touch(MotionEvent.ACTION_DOWN, bounds.centerX().toFloat(), bounds.centerY().toFloat())
                touch(MotionEvent.ACTION_MOVE, 300f, 300f)
                touch(MotionEvent.ACTION_UP, 300f, 300f)
                assertEquals(1, clicks)
                assertTrue(provider.performAction(0, AccessibilityNodeInfo.ACTION_CLICK, null))
                assertEquals(2, clicks)
                view.visibility = View.GONE
                view.visibility = View.VISIBLE
                val parent = view.parent as android.view.ViewGroup
                parent.removeView(view)
                parent.addView(view)
                view.layoutDirection = View.LAYOUT_DIRECTION_RTL
                view.measure(View.MeasureSpec.makeMeasureSpec(320, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
                view.layout(0, 0, 320, view.measuredHeight)
                val bitmap = Bitmap.createBitmap(320, view.height, Bitmap.Config.ARGB_8888)
                view.draw(Canvas(bitmap))
                assertTrue(view.isAttachedToWindow)
                bitmap.recycle()
            }
            scenario.recreate()
            scenario.onActivity { assertTrue(it.timeline.isAttachedToWindow) }
        }
    }
}
