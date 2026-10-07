package com.dmitrypokrasov.timeline

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.viewpager2.widget.ViewPager2
import com.dmitrypokrasov.timelineview.ui.TimelineView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimelineSampleDeviceTest {
    @Test
    fun sampleRestoresProgressAndOpensEveryStrategy() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            var updated = ""
            scenario.onActivity { activity ->
                val timeline = timeline(activity)
                val provider = requireNotNull(timeline.accessibilityNodeProvider)
                val before = provider.createAccessibilityNodeInfo(1)!!.contentDescription.toString()
                assertTrue(provider.performAction(1, AccessibilityNodeInfo.ACTION_CLICK, Bundle()))
                timeline(activity)
                updated = provider.createAccessibilityNodeInfo(1)!!.contentDescription.toString()
                assertNotEquals(before, updated)
            }
            scenario.recreate()
            scenario.onActivity { activity ->
                assertEquals(updated, timeline(activity).accessibilityNodeProvider!!.createAccessibilityNodeInfo(1)!!.contentDescription.toString())
            }
            TimelineSample.entries.indices.forEach { index ->
                scenario.onActivity { it.findViewById<ViewPager2>(R.id.timeline_pager).setCurrentItem(index, false) }
                InstrumentationRegistry.getInstrumentation().waitForIdleSync()
                scenario.onActivity { assertEquals(index, it.findViewById<ViewPager2>(R.id.timeline_pager).currentItem) }
            }
        }
    }

    private fun timeline(activity: MainActivity): TimelineView {
        activity.supportFragmentManager.executePendingTransactions()
        return requireNotNull(activity.findViewById<TimelineView>(R.id.timeline)).apply {
            measure(View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
            layout(0, 0, measuredWidth, measuredHeight)
        }
    }
}
