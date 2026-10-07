package com.dmitrypokrasov.timelineview.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TimelineConfigNormalizationTest {
    @Test
    fun `invalid dimensions fail before rendering`() {
        listOf(-1f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY).forEach { value ->
            assertThrows(IllegalArgumentException::class.java) { TimelineMathConfig.Sizes(sizeImageLvl = value) }
            assertThrows(IllegalArgumentException::class.java) { TimelineMathConfig.Spacing(stepY = value) }
            assertThrows(IllegalArgumentException::class.java) { TimelineUiConfig.TextSizes(sizeTitle = value) }
            assertThrows(IllegalArgumentException::class.java) { TimelineUiConfig.Stroke(radius = value) }
        }
    }

    @Test
    fun `copy validates immutable configuration and leaves original unchanged`() {
        val sizes = TimelineMathConfig.Sizes(sizeImageLvl = 24f)
        assertThrows(IllegalArgumentException::class.java) { sizes.copy(sizeImageLvl = -1f) }
        assertEquals(24f, sizes.sizeImageLvl, 0f)
        assertEquals(0f, sizes.copy(sizeImageLvl = 0f).sizeImageLvl, 0f)
        assertEquals(-1, TimelineUiConfig.Icons(iconProgress = -1).iconProgress)
    }
}
