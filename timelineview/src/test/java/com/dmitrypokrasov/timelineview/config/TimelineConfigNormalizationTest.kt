package com.dmitrypokrasov.timelineview.config

import org.junit.Assert.assertEquals
import org.junit.Test

class TimelineConfigNormalizationTest {
    @Test
    fun `math config clamps negative values to zero`() {
        val config = TimelineMathConfig(
            spacing = TimelineMathConfig.Spacing(
                stepY = -10f,
                stepYFirst = -5f,
                marginTopDescription = -1f,
                marginTopTitle = -2f,
                marginTopProgressIcon = -3f,
                marginHorizontalImage = -4f,
                marginHorizontalText = -5f,
                marginHorizontalStroke = -6f
            ),
            sizes = TimelineMathConfig.Sizes(
                sizeIconProgress = -7f,
                sizeImageLvl = -8f
            )
        )

        assertEquals(0f, config.spacing.stepY, 0.0f)
        assertEquals(0f, config.spacing.stepYFirst, 0.0f)
        assertEquals(0f, config.spacing.marginTopDescription, 0.0f)
        assertEquals(0f, config.spacing.marginTopTitle, 0.0f)
        assertEquals(0f, config.spacing.marginTopProgressIcon, 0.0f)
        assertEquals(0f, config.spacing.marginHorizontalImage, 0.0f)
        assertEquals(0f, config.spacing.marginHorizontalText, 0.0f)
        assertEquals(0f, config.spacing.marginHorizontalStroke, 0.0f)
        assertEquals(0f, config.sizes.sizeIconProgress, 0.0f)
        assertEquals(0f, config.sizes.sizeImageLvl, 0.0f)
    }

    @Test
    fun `ui config clamps negative values to zero`() {
        val config = TimelineUiConfig(
            icons = TimelineUiConfig.Icons(
                iconDisableLvl = -1,
                iconProgress = -2
            ),
            textSizes = TimelineUiConfig.TextSizes(
                sizeDescription = -10f,
                sizeTitle = -5f
            ),
            stroke = TimelineUiConfig.Stroke(
                radius = -5f,
                sizeStroke = -1f
            )
        )

        assertEquals(0, config.icons.iconDisableLvl)
        assertEquals(0, config.icons.iconProgress)
        assertEquals(0f, config.textSizes.sizeDescription, 0.0f)
        assertEquals(0f, config.textSizes.sizeTitle, 0.0f)
        assertEquals(0f, config.stroke.radius, 0.0f)
        assertEquals(0f, config.stroke.sizeStroke, 0.0f)
    }
}
