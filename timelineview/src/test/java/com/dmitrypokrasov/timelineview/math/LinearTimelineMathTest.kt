package com.dmitrypokrasov.timelineview.math

import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LinearTimelineMathTest {
    @Test
    fun `vertical progress starts before first badge`() {
        val config = progressConfig(progresses = listOf(0, 100, 100))
        val math = LinearTimelineMath(config, LinearTimelineMath.Orientation.VERTICAL)

        val layout = math.buildLayout()

        val expectedTop = config.spacing.marginTopProgressIcon - config.sizes.sizeIconProgress / 2f
        assertEquals(expectedTop, requireNotNull(layout.progressIcon).top, 0.01f)
    }

    @Test
    fun `vertical last segment ends at last badge anchor`() {
        val config = progressConfig(progresses = listOf(100, 100, 100))
        val math = LinearTimelineMath(config, LinearTimelineMath.Orientation.VERTICAL)

        val lastIndex = config.steps.lastIndex
        val lastAnchor = math.getVerticalOffset(lastIndex) - config.spacing.marginTopProgressIcon +
            config.sizes.sizeIconProgress / 2f
        val expectedAnchor = maxOf(config.sizes.sizeImageLvl, config.sizes.sizeIconProgress) / 2f +
            config.spacing.stepYFirst + config.spacing.stepY * lastIndex

        assertEquals(expectedAnchor, lastAnchor, 0.01f)
    }

    @Test
    fun `horizontal progress starts before first badge`() {
        val config = progressConfig(progresses = listOf(0, 100, 100))
        val math = LinearTimelineMath(config, LinearTimelineMath.Orientation.HORIZONTAL)

        val layout = math.buildLayout()

        val expectedLeft = -config.sizes.sizeIconProgress / 2f
        assertEquals(expectedLeft, requireNotNull(layout.progressIcon).left, 0.01f)
    }

    @Test
    fun `horizontal last segment ends at last badge anchor`() {
        val config = progressConfig(progresses = listOf(100, 100, 100))
        val math = LinearTimelineMath(config, LinearTimelineMath.Orientation.HORIZONTAL)

        val lastIndex = config.steps.lastIndex
        val lastAnchor = math.getHorizontalIconOffset(lastIndex) + config.sizes.sizeIconProgress / 2f
        val expectedAnchor = config.spacing.stepYFirst + config.spacing.stepY * lastIndex

        assertEquals(expectedAnchor, lastAnchor, 0.01f)
    }

    @Test
    fun `layout handles empty steps`() {
        val config = TimelineMathConfig()
        val math = LinearTimelineMath(config, LinearTimelineMath.Orientation.VERTICAL)

        val layout = math.buildLayout()

        assertTrue(layout.steps.isEmpty())
        assertNull(layout.progressIcon)
        assertNull(layout.progressStepIndex)
    }

    @Test
    fun `vertical layout keeps first step anchor and text offsets stable`() {
        val config = TimelineMathConfig(
            steps = listOf(
                TimelineStepData(title = "1", description = "1", iconRes = 1, progress = 0),
                TimelineStepData(title = "2", description = "2", iconRes = 2, progress = 100)
            ),
            spacing = TimelineMathConfig.Spacing(
                stepY = 90f,
                stepYFirst = 32f,
                marginTopTitle = 12f,
                marginTopDescription = 7f,
                marginTopProgressIcon = 18f
            ),
            sizes = TimelineMathConfig.Sizes(
                sizeIconProgress = 14f,
                sizeImageLvl = 20f
            )
        )
        val math = LinearTimelineMath(config, LinearTimelineMath.Orientation.VERTICAL)

        val layout = math.buildLayout()
        val firstStep = layout.steps.first()
        val firstStepAnchorY = firstStep.iconY + config.sizes.sizeImageLvl / 2f
        val topInset = maxOf(config.sizes.sizeImageLvl, config.sizes.sizeIconProgress) / 2f
        val expectedFirstStepAnchorY = topInset + config.spacing.stepYFirst
        val expectedProgressTop = config.spacing.marginTopProgressIcon - config.sizes.sizeIconProgress / 2f

        assertEquals(expectedFirstStepAnchorY, firstStepAnchorY, 0.01f)
        assertEquals(config.spacing.stepYFirst + config.spacing.marginTopTitle, firstStep.titleY, 0.01f)
        assertEquals(firstStep.titleY + config.spacing.marginTopDescription, firstStep.descriptionY, 0.01f)
        assertEquals(expectedProgressTop, requireNotNull(layout.progressIcon).top, 0.01f)
        assertEquals(expectedProgressTop, math.getVerticalOffset(0), 0.01f)
        assertEquals(expectedProgressTop, math.getTopCoordinates(config.steps.first()), 0.01f)
    }

    @Test
    fun `horizontal layout computes bounded text widths after measuring width`() {
        val config = progressConfig(progresses = listOf(25, 100, 100))
        val math = LinearTimelineMath(config, LinearTimelineMath.Orientation.HORIZONTAL)
        math.setMeasuredWidth(220)

        val layout = math.buildLayout()

        layout.steps.forEach { step ->
            assertTrue(step.titleWidth in 1..220)
            assertTrue(step.descriptionWidth in 1..220)
        }
    }

    private fun progressConfig(progresses: List<Int>): TimelineMathConfig {
        return TimelineMathConfig(
            steps = progresses.mapIndexed { index, progress ->
                TimelineStepData(
                    title = "Step ${index + 1}",
                    description = "Step ${index + 1}",
                    iconRes = index + 1,
                    progress = progress
                )
            },
            spacing = TimelineMathConfig.Spacing(
                stepY = 100f,
                stepYFirst = 20f,
                marginTopTitle = 0f,
                marginTopProgressIcon = 8f
            ),
            sizes = TimelineMathConfig.Sizes(
                sizeIconProgress = 10f,
                sizeImageLvl = 20f
            )
        )
    }
}
