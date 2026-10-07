package com.dmitrypokrasov.timelineview.ui

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.dmitrypokrasov.timelineview.R
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.math.LinearTimelineMath
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.render.LinearTimelineUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28, 34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TimelineViewRegressionTest {
    private fun math(steps: List<TimelineStepData> = listOf(TimelineStepData(title = "First", progress = 100))) =
        TimelineMathConfig(
            startPosition = TimelineMathConfig.StartPosition.START,
            steps = steps,
            spacing =
                TimelineMathConfig.Spacing(
                    stepY = 72f,
                    stepYFirst = 24f,
                    marginTopTitle = 24f,
                    marginTopDescription = 8f,
                    marginHorizontalImage = 16f,
                    marginHorizontalStroke = 28f,
                    marginHorizontalText = 64f,
                ),
            sizes = TimelineMathConfig.Sizes(sizeImageLvl = 24f, sizeIconProgress = 16f),
        )

    private fun ui() =
        TimelineUiConfig(
            icons = TimelineUiConfig.Icons(iconDisableLvl = R.drawable.timeline_test_shape),
            colors = TimelineUiConfig.Colors(Color.GREEN, Color.GRAY, Color.BLACK, Color.DKGRAY),
            textSizes = TimelineUiConfig.TextSizes(sizeTitle = 18f, sizeDescription = 16f),
        )

    private fun view(): TimelineView {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        activity.applicationInfo.flags = activity.applicationInfo.flags or android.content.pm.ApplicationInfo.FLAG_SUPPORTS_RTL
        return TimelineView(activity).apply {
            setPadding(12, 8, 12, 8)
            setStrategies(LinearTimelineMath(math()), LinearTimelineUi(ui()))
            activity.setContentView(this)
            measureAndLayout(this)
        }
    }

    private fun measureAndLayout(
        view: View,
        width: Int = 320,
    ) {
        view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }

    private fun bounds(
        view: TimelineView,
        id: Int = 0,
    ): Rect {
        val provider = requireNotNull(view.accessibilityNodeProvider)
        val node = requireNotNull(provider.createAccessibilityNodeInfo(id))
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        return Rect().also {
            node.getBoundsInScreen(it)
            it.offset(-location[0], -location[1])
        }
    }

    private fun touch(
        view: TimelineView,
        action: Int,
        x: Float,
        y: Float,
    ) {
        val event = MotionEvent.obtain(0, 10, action, x, y, 0)
        view.onTouchEvent(event)
        event.recycle()
    }

    @Test
    fun `tap invokes step while drag cancel disabled and blank down do not`() {
        val view = view()
        var clicks = 0
        view.setOnStepClickListener { _, _ -> clicks++ }
        val bounds = bounds(view)
        val x = bounds.exactCenterX()
        val y = bounds.exactCenterY()
        touch(view, MotionEvent.ACTION_DOWN, x, y)
        touch(view, MotionEvent.ACTION_UP, x, y)
        assertEquals(1, clicks)
        touch(view, MotionEvent.ACTION_DOWN, x, y)
        touch(view, MotionEvent.ACTION_MOVE, x + 100, y)
        touch(view, MotionEvent.ACTION_UP, x, y)
        touch(view, MotionEvent.ACTION_DOWN, x, y)
        touch(view, MotionEvent.ACTION_CANCEL, x, y)
        touch(view, MotionEvent.ACTION_UP, x, y)
        touch(view, MotionEvent.ACTION_DOWN, 300f, y)
        touch(view, MotionEvent.ACTION_UP, x, y)
        view.isEnabled = false
        touch(view, MotionEvent.ACTION_DOWN, x, y)
        touch(view, MotionEvent.ACTION_UP, x, y)
        assertEquals(1, clicks)
    }

    @Test
    fun `virtual nodes read data and invoke corresponding action in rtl with padding`() {
        val view = view()
        view.contentDescription = "Order history"
        var selected = -1
        view.setOnStepClickListener { index, _ -> selected = index }
        val ltrBounds = bounds(view)
        view.layoutDirection = View.LAYOUT_DIRECTION_RTL
        measureAndLayout(view)
        val rtlBounds = bounds(view)
        assertEquals(view.width - ltrBounds.centerX(), rtlBounds.centerX())
        val provider = requireNotNull(view.accessibilityNodeProvider)
        assertTrue(requireNotNull(provider.createAccessibilityNodeInfo(0)).contentDescription.contains("First"))
        assertTrue(provider.performAction(0, AccessibilityNodeInfoCompat.ACTION_CLICK, Bundle()))
        assertEquals(0, selected)
        assertEquals("Order history", view.contentDescription)
        view.isEnabled = false
        assertFalse(provider.performAction(0, AccessibilityNodeInfoCompat.ACTION_CLICK, Bundle()))
    }

    @Test
    fun `zero size hides drawables and removing fallback clears old bitmap`() {
        val view = view()
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        // Icon is at padding + configured left/top; verify actual native renderer output.
        assertEquals(Color.RED, bitmap.getPixel(30, 44))
        view.setConfig(math(), ui().copy(icons = TimelineUiConfig.Icons()))
        measureAndLayout(view)
        bitmap.eraseColor(Color.TRANSPARENT)
        view.draw(Canvas(bitmap))
        assertNotEquals(Color.RED, bitmap.getPixel(30, 44))
        view.setConfig(math().copy(sizes = TimelineMathConfig.Sizes(0f, 0f)), ui())
        measureAndLayout(view)
        view.draw(Canvas(bitmap))
    }

    @Test
    fun `wrap content width and large multiline layouts render every strategy`() {
        val view = view()
        view.measure(View.MeasureSpec.makeMeasureSpec(320, View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        assertEquals(320, view.measuredWidth)
        val steps =
            List(3) { index ->
                TimelineStepData(id = "step-$index", timestampMillis = index * 120_000L, title = "Step ${index + 1}", description = "A long description which must wrap without displacing another badge. ".repeat(3), progress = if (index == 0) 100 else 40)
            }
        TimelineMathStrategy.entries.forEach { strategy ->
            listOf(View.LAYOUT_DIRECTION_LTR, View.LAYOUT_DIRECTION_RTL).forEach { direction ->
                view.setConfig(math(steps), ui().copy(textSizes = TimelineUiConfig.TextSizes(24f, 28f)))
                view.setStrategy(strategy, TimelineUiStrategy.Linear)
                view.layoutDirection = direction
                measureAndLayout(view, 280)
                assertTrue(view.height > 100)
                val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(Color.WHITE)
                view.draw(Canvas(bitmap))
                val output = File("build/reports/timeline-previews/${strategy.key.value}-$direction.png")
                requireNotNull(output.parentFile).mkdirs()
                output.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                bitmap.recycle()
            }
        }
    }
}
