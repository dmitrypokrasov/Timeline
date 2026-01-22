package com.dmitrypokrasov.timelineview.math

import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.model.TimelineStep
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

        val expectedEnd = config.stepYFirst + config.stepY + config.stepY / 2f
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

        val expectedEnd = config.stepYFirst + config.stepY + config.stepY / 2f
        val lastStepPosition = layout.steps.last().titleY - config.marginTopTitle

        assertEquals(expectedEnd, lastStepPosition, 0.01f)
    }

    private fun steps(): List<TimelineStep> = listOf(
        TimelineStep(title = 1, description = 1, icon = 1, count = 0, maxCount = 10),
        TimelineStep(title = 2, description = 2, icon = 2, count = 5, maxCount = 10),
        TimelineStep(title = 3, description = 3, icon = 3, count = 10, maxCount = 10)
    )
}
