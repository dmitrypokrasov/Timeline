package com.dmitrypokrasov.timelineview.ui

import android.graphics.Paint
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.math.data.TimelineLayoutStep
import com.dmitrypokrasov.timelineview.math.data.TimelineProgressIcon
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimelineAccessibilitySnapshotBuilderTest {
    @Test
    fun `builds step and progress nodes with padded bounds`() {
        val step = TimelineStepData(title = "Title", description = "Description", progress = 45)
        val snapshot =
            TimelineAccessibilitySnapshotBuilder.build(
                layout =
                    TimelineLayout(
                        steps = listOf(stepLayout(step = step, iconX = 10f, iconY = 20f)),
                        progressIcon = TimelineProgressIcon(left = 30f, top = 40f),
                        progressStepIndex = 0,
                    ),
                stepIconSize = 20f,
                progressIconSize = 16f,
                density = 1f,
                paddingLeft = 8,
                paddingTop = 12,
                stepClickable = true,
                progressClickable = false,
            )

        assertEquals(2, snapshot.nodes.size)

        val stepNode = snapshot.findById(0) as TimelineAccessibilityNode.Step
        val progressNode = snapshot.findById(10_000) as TimelineAccessibilityNode.ProgressIcon

        assertTrue(stepNode.isClickable)
        assertFalse(progressNode.isClickable)
        assertTrue(stepNode.contentDescription.contains("Step 1"))
        assertTrue(stepNode.contentDescription.contains("Progress 45 percent"))
        assertEquals(4f, stepNode.boundsInParent.left, 0.01f)
        assertEquals(18f, stepNode.boundsInParent.top, 0.01f)
        assertTrue(progressNode.contentDescription.contains("Timeline progress icon"))
    }

    @Test
    fun `finds nodes by virtual id and coordinates`() {
        val step = TimelineStepData(title = "Step", progress = 100)
        val snapshot =
            TimelineAccessibilitySnapshotBuilder.build(
                layout =
                    TimelineLayout(
                        steps = listOf(stepLayout(step = step, iconX = 0f, iconY = 0f)),
                        progressIcon = null,
                        progressStepIndex = null,
                    ),
                stepIconSize = 24f,
                progressIconSize = 16f,
                density = 1f,
                paddingLeft = 0,
                paddingTop = 0,
                stepClickable = true,
                progressClickable = false,
            )

        assertEquals(0, snapshot.findAt(5f, 5f)?.virtualId)
        assertEquals("Step 1. Step. Progress 100 percent", snapshot.findById(0)?.contentDescription)
    }

    private fun stepLayout(
        step: TimelineStepData,
        iconX: Float,
        iconY: Float,
    ) = TimelineLayoutStep(
        step = step,
        titleX = 0f,
        titleY = 0f,
        titleWidth = 100,
        descriptionX = 0f,
        descriptionY = 0f,
        descriptionWidth = 100,
        iconX = iconX,
        iconY = iconY,
        textAlign = Paint.Align.LEFT,
    )
}
