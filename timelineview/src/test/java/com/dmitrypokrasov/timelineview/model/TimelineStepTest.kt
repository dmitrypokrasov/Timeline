package com.dmitrypokrasov.timelineview.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TimelineStepTest {

    @Test
    fun percentsReturnsZeroWhenMaxCountIsZero() {
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
    fun percentsIsClampedWithinRange() {
        val overLimit = TimelineStep(
            title = 0,
            description = 0,
            icon = 0,
            count = 200,
            maxCount = 100
        )

        val normal = TimelineStep(
            title = 0,
            description = 0,
            icon = 0,
            count = 50,
            maxCount = 100
        )

        assertEquals(100, overLimit.percents)
        assertEquals(50, normal.percents)
    }
}
