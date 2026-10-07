package com.example.migration

import android.view.LayoutInflater
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import com.example.consumer.R
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.ui.TimelineView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MigrationTest {
    private fun measure(view: TimelineView) {
        view.measure(View.MeasureSpec.makeMeasureSpec(320, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        view.layout(0, 0, 320, view.measuredHeight)
    }
    @Test fun `migrated host updates configuration and preserves identity on reorder`() {
        val host = MigratedTimeline(RuntimeEnvironment.getApplication())
        host.resize(120f)
        measure(host.view)
        assertTrue(host.view.accessibilityNodeProvider!!.performAction(0, AccessibilityNodeInfo.ACTION_CLICK, null))
        assertEquals(30, host.steps.first().progress)
        host.reorder()
        measure(host.view)
        assertTrue(host.view.accessibilityNodeProvider!!.performAction(0, AccessibilityNodeInfo.ACTION_CLICK, null))
        assertEquals(40, host.steps.last().progress)
    }
    @Test fun `documented custom strategy and XML work against the published AAR`() {
        val context = RuntimeEnvironment.getApplication()
        val view = LayoutInflater.from(context).inflate(R.layout.migrated_timeline, null) as TimelineView
        val steps = List(3) { TimelineStepData(id = "$it", title = "Step", description = "Long description ".repeat(20), progress = 0) }
        view.setMathEngine(CompactCustomMath(TimelineMathConfig(steps = steps)))
        measure(view)
        assertTrue("Measured custom strategy height: ${view.height}", view.height > 300)
        measure(JavaConsumer.create(context))
    }
}
