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

    private fun steps(): List<TimelineStepData> = listOf(
        TimelineStepData(title = "1", description = "1", iconRes = 1, progress = 0),
        TimelineStepData(title = "2", description = "2", iconRes = 2, progress = 50),
        TimelineStepData(title = "3", description = "3", iconRes = 3, progress = 100)
    )
}
