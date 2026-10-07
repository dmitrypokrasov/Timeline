package com.dmitrypokrasov.timelineview.config

import android.content.res.Configuration
import android.util.TypedValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28, 34])
class TimelineDefaultsTest {
    @Test
    fun `defaults match XML and conversions use the context density and system font scale`() {
        val app = RuntimeEnvironment.getApplication()
        val context =
            app.createConfigurationContext(
                Configuration(app.resources.configuration).apply {
                    densityDpi = 320
                    fontScale = 2f
                },
            )
        assertEquals(TimelineConfigParser(context).parse(null), TimelineDefaults.config(context))
        assertEquals(48f, TimelineDefaults.dp(context, 24f), 0.01f)
        val expected = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16f, context.resources.displayMetrics)
        assertEquals(expected, TimelineDefaults.sp(context, 16f), 0.01f)
        listOf(-1f, Float.NaN, Float.POSITIVE_INFINITY, Float.MAX_VALUE).forEach { value ->
            assertThrows(IllegalArgumentException::class.java) { TimelineDefaults.dp(context, value) }
        }
    }
}
