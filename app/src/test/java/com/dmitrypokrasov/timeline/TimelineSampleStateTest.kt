package com.dmitrypokrasov.timeline

import android.os.Bundle
import android.os.Looper
import android.view.View
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.dmitrypokrasov.timelineview.ui.TimelineView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TimelineSampleStateTest {
    @Test
    fun `clicked progress survives activity recreation`() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()

        fun timeline(): TimelineView {
            controller.get().supportFragmentManager.executePendingTransactions()
            shadowOf(Looper.getMainLooper()).idle()
            return controller.get().findViewById<TimelineView>(R.id.timeline).apply {
                measure(View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
                layout(0, 0, measuredWidth, measuredHeight)
            }
        }

        fun description(): String =
            requireNotNull(
                timeline().accessibilityNodeProvider?.createAccessibilityNodeInfo(1),
            ).contentDescription.toString()
        val before = description()
        val provider = requireNotNull(timeline().accessibilityNodeProvider)
        assertTrue(provider.performAction(1, AccessibilityNodeInfoCompat.ACTION_CLICK, Bundle()))
        val updated = description()
        assertNotEquals(before, updated)
        controller.recreate()
        assertEquals(updated, description())
        controller.close()
    }
}
