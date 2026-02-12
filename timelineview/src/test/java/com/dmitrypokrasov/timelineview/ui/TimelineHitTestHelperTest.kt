package com.dmitrypokrasov.timelineview.ui

import android.graphics.Paint
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.math.data.TimelineLayoutStep
import com.dmitrypokrasov.timelineview.math.data.TimelineProgressIcon
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimelineHitTestHelperTest {

    @Test
    fun `returns step hit when touch inside step icon bounds`() {
        val step = TimelineStepData(title = "Step 1", progress = 10)
        val layout = TimelineLayout(
            steps = listOf(stepLayout(step = step, iconX = 20f, iconY = 40f)),
            progressIcon = null
        )

        val result = TimelineHitTestHelper.findHit(
            layout = layout,
            stepIconSize = 24f,
            progressIconSize = 16f,
            x = 30f,
            y = 50f
        )

        assertEquals(TimelineHitTestHelper.HitResult.Step(index = 0, step = step), result)
    }

    @Test
    fun `returns progress icon hit when touch inside progress icon bounds`() {
        val layout = TimelineLayout(
            steps = emptyList(),
            progressIcon = TimelineProgressIcon(left = 100f, top = 200f)
        )

        val result = TimelineHitTestHelper.findHit(
            layout = layout,
            stepIconSize = 24f,
            progressIconSize = 20f,
            x = 110f,
            y = 210f
        )

        assertEquals(TimelineHitTestHelper.HitResult.ProgressIcon, result)
    }

    @Test
    fun `prioritizes step hit over progress icon when areas overlap`() {
        val step = TimelineStepData(title = "Step 1", progress = 50)
        val layout = TimelineLayout(
            steps = listOf(stepLayout(step = step, iconX = 10f, iconY = 10f)),
            progressIcon = TimelineProgressIcon(left = 10f, top = 10f)
        )

        val result = TimelineHitTestHelper.findHit(
            layout = layout,
            stepIconSize = 20f,
            progressIconSize = 20f,
            x = 15f,
            y = 15f
        )

        assertEquals(TimelineHitTestHelper.HitResult.Step(index = 0, step = step), result)
    }

    @Test
    fun `uses minimum touch size for step and progress icon`() {
        val step = TimelineStepData(title = "Step 1", progress = 0)
        val layout = TimelineLayout(
            steps = listOf(stepLayout(step = step, iconX = 100f, iconY = 100f)),
            progressIcon = TimelineProgressIcon(left = 200f, top = 200f)
        )

        val stepResult = TimelineHitTestHelper.findHit(
            layout = layout,
            stepIconSize = 20f,
            progressIconSize = 20f,
            x = 86f,
            y = 110f,
            minStepTouchSize = 48f,
            minProgressTouchSize = 48f
        )

        val progressResult = TimelineHitTestHelper.findHit(
            layout = layout,
            stepIconSize = 20f,
            progressIconSize = 20f,
            x = 186f,
            y = 210f,
            minStepTouchSize = 48f,
            minProgressTouchSize = 48f
        )

        assertEquals(TimelineHitTestHelper.HitResult.Step(index = 0, step = step), stepResult)
        assertEquals(TimelineHitTestHelper.HitResult.ProgressIcon, progressResult)
    }

    @Test
    fun `returns null when touch outside all hit areas`() {
        val layout = TimelineLayout(
            steps = listOf(stepLayout(step = TimelineStepData(progress = 0), iconX = 10f, iconY = 10f)),
            progressIcon = TimelineProgressIcon(left = 100f, top = 100f)
        )

        val result = TimelineHitTestHelper.findHit(
            layout = layout,
            stepIconSize = 20f,
            progressIconSize = 20f,
            x = 80f,
            y = 80f
        )

        assertNull(result)
    }

    private fun stepLayout(step: TimelineStepData, iconX: Float, iconY: Float) = TimelineLayoutStep(
        step = step,
        titleX = 0f,
        titleY = 0f,
        descriptionX = 0f,
        descriptionY = 0f,
        iconX = iconX,
        iconY = iconY,
        textAlign = Paint.Align.LEFT,
        descriptionMaxWidth = 100
    )
}
