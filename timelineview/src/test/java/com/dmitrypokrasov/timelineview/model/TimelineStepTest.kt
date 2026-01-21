package com.dmitrypokrasov.timelineview.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TimelineStepTest {
    @Test
    fun `percents returns zero when maxCount is zero`() {
        val step = TimelineStep(
            title = 0,
            description = 0,
            icon = 0,
            count = 10,
            maxCount = 0
        )

        assertEquals(0, step.percents)
    }

    @Test
    fun `percents clamps values between zero and one hundred`() {
        val stepOver = TimelineStep(
            title = 0,
            description = 0,
            icon = 0,
            count = 200,
            maxCount = 100
        )
        val stepUnder = TimelineStep(
            title = 0,
            description = 0,
            icon = 0,
            count = -10,
            maxCount = 100
        )

        assertEquals(100, stepOver.percents)
        assertEquals(0, stepUnder.percents)
    }
}
