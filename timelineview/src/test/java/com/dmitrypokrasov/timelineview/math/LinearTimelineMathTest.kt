package com.dmitrypokrasov.timelineview.math

import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import org.junit.Assert.assertEquals
import org.junit.Test

class LinearTimelineMathTest {
    @Test
    fun `horizontal layout uses step position at path end`() {
        val config = TimelineMathConfig(
            steps = steps(),
            stepY = 100f,
            stepYFirst = 20f,
            marginTopTitle = 10f
        )
        val math = LinearTimelineMath(config, LinearTimelineMath.Orientation.HORIZONTAL)

        val layout = math.buildLayout()

        val expectedEnd = config.stepYFirst + config.stepY * 2
        val lastStepPosition = layout.steps.last().titleX

        assertEquals(expectedEnd, lastStepPosition, 0.01f)
    }

    @Test
    fun `vertical layout uses step position at path end`() {
        val config = TimelineMathConfig(
            steps = steps(),
            stepY = 100f,
            stepYFirst = 20f,
            marginTopTitle = 10f
        )
        val math = LinearTimelineMath(config, LinearTimelineMath.Orientation.VERTICAL)

        val layout = math.buildLayout()

        val expectedEnd = config.stepYFirst + config.stepY * 2
        val lastStepPosition = layout.steps.last().titleY - config.marginTopTitle

        assertEquals(expectedEnd, lastStepPosition, 0.01f)
    }

    @Test
    fun `layout handles empty steps`() {
        val config = TimelineMathConfig(
            steps = emptyList(),
            stepY = 0f,
            stepYFirst = 0f,
            marginTopTitle = 0f
        )
        val math = LinearTimelineMath(config, LinearTimelineMath.Orientation.VERTICAL)

        val layout = math.buildLayout()

        assertEquals(0, layout.steps.size)
        assertEquals(null, layout.progressIcon)
    }

    @Test
    fun `vertical progress icon matches 0 percent position`() {
        val config = progressConfig(progresses = listOf(0, 100, 100))
        val math = LinearTimelineMath(config, LinearTimelineMath.Orientation.VERTICAL)

        val layout = math.buildLayout()

        val expectedTop = config.marginTopProgressIcon - config.sizeIconProgress / 2f
        assertEquals(expectedTop, layout.progressIcon?.top, 0.01f)
    }

    @Test
    fun `vertical progress icon matches 50 percent position`() {
        val config = progressConfig(progresses = listOf(50, 100, 100))
        val math = LinearTimelineMath(config, LinearTimelineMath.Orientation.VERTICAL)

        val layout = math.buildLayout()

        val expectedTop = config.stepYFirst * 0.5f +
            config.marginTopProgressIcon - config.sizeIconProgress / 2f
        assertEquals(expectedTop, layout.progressIcon?.top, 0.01f)
    }

    @Test
    fun `vertical progress icon is null for 100 percent`() {
        val config = progressConfig(progresses = listOf(100, 100, 100))
        val math = LinearTimelineMath(config, LinearTimelineMath.Orientation.VERTICAL)

        val layout = math.buildLayout()

        assertEquals(null, layout.progressIcon)
    }

    @Test
    fun `horizontal progress icon matches 0 percent position`() {
        val config = progressConfig(progresses = listOf(0, 100, 100))
        val math = LinearTimelineMath(config, LinearTimelineMath.Orientation.HORIZONTAL)

        val layout = math.buildLayout()

        val expectedLeft = -config.sizeIconProgress / 2f
        assertEquals(expectedLeft, layout.progressIcon?.left, 0.01f)
    }

    @Test
    fun `horizontal progress icon matches 50 percent position`() {
        val config = progressConfig(progresses = listOf(50, 100, 100))
        val math = LinearTimelineMath(config, LinearTimelineMath.Orientation.HORIZONTAL)

        val layout = math.buildLayout()

        val expectedLeft = config.stepYFirst * 0.5f - config.sizeIconProgress / 2f
        assertEquals(expectedLeft, layout.progressIcon?.left, 0.01f)
    }

    @Test
    fun `horizontal progress icon is null for 100 percent`() {
        val config = progressConfig(progresses = listOf(100, 100, 100))
        val math = LinearTimelineMath(config, LinearTimelineMath.Orientation.HORIZONTAL)

        val layout = math.buildLayout()

        assertEquals(null, layout.progressIcon)
    }

    @Test
    fun `repeated calculations return same results without changes`() {
        val config = progressConfig(progresses = listOf(25, 100, 100))
        val math = LinearTimelineMath(config, LinearTimelineMath.Orientation.HORIZONTAL)

        val firstLayout = math.buildLayout()
        val secondLayout = math.buildLayout()

        assertEquals(firstLayout, secondLayout)

        val step = config.steps[1]
        val firstLeft = math.getLeftCoordinates(step)
        val secondLeft = math.getLeftCoordinates(step)

        assertEquals(firstLeft, secondLeft, 0.01f)
    }

    private fun steps(): List<TimelineStepData> = listOf(
        TimelineStepData(title = "1", description = "1", iconRes = 1, progress = 0),
        TimelineStepData(title = "2", description = "2", iconRes = 2, progress = 50),
        TimelineStepData(title = "3", description = "3", iconRes = 3, progress = 100)
    )

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
            stepY = 100f,
            stepYFirst = 20f,
            marginTopProgressIcon = 8f,
            sizeIconProgress = 10f,
            sizeImageLvl = 20f
        )
    }
}
