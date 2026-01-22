package com.dmitrypokrasov.timelineview.config

import org.junit.Assert.assertEquals
import org.junit.Test

class TimelineConfigNormalizationTest {
    @Test
    fun `math config clamps negative values to zero`() {
        val config = TimelineMathConfig(
            stepY = -10f,
            stepYFirst = -5f,
            marginTopDescription = -1f,
            marginTopTitle = -2f,
            marginTopProgressIcon = -3f,
            marginHorizontalImage = -4f,
            marginHorizontalText = -5f,
            marginHorizontalStroke = -6f,
            sizeIconProgress = -7f,
            sizeImageLvl = 0f
        )

        assertEquals(0f, config.stepY, 0.0f)
        assertEquals(0f, config.stepYFirst, 0.0f)
        assertEquals(0f, config.marginTopDescription, 0.0f)
        assertEquals(0f, config.marginTopTitle, 0.0f)
        assertEquals(0f, config.marginTopProgressIcon, 0.0f)
        assertEquals(0f, config.marginHorizontalImage, 0.0f)
        assertEquals(0f, config.marginHorizontalText, 0.0f)
        assertEquals(0f, config.marginHorizontalStroke, 0.0f)
        assertEquals(0f, config.sizeIconProgress, 0.0f)
        assertEquals(0f, config.sizeImageLvl, 0.0f)
    }

    @Test
    fun `ui config clamps negative values to zero`() {
        val config = TimelineUiConfig(
            iconDisableLvl = -1,
            iconProgress = -2,
            sizeDescription = -10f,
            sizeTitle = 0f,
            radius = -5f,
            sizeStroke = -1f
        )

        assertEquals(0, config.iconDisableLvl)
        assertEquals(0, config.iconProgress)
        assertEquals(0f, config.sizeDescription, 0.0f)
        assertEquals(0f, config.sizeTitle, 0.0f)
        assertEquals(0f, config.radius, 0.0f)
        assertEquals(0f, config.sizeStroke, 0.0f)
    }
}
