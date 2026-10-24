package com.dmitrypokrasov.timelineview.geometry

import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimelineBuiltInCoordinatesTest {
    @Test
    fun `pure built in helpers preserve order finite coordinates and connected segments`() {
        TimelineMathConfig.StartPosition.entries.forEach { start ->
            listOf(0, 1, 320).forEach { width ->
                assertSizes(start, width)
            }
        }
    }

    private fun assertSizes(
        start: TimelineMathConfig.StartPosition,
        width: Int,
    ) {
        listOf(0, 1, 2, 5).forEach { count ->
            listOf(0, 1, 99, 100).forEach { progress -> assertGeometry(start, width, count, progress) }
        }
    }

    private fun assertGeometry(
        start: TimelineMathConfig.StartPosition,
        width: Int,
        count: Int,
        progress: Int,
    ) {
        val steps = List(count) { TimelineStepData(id = "$it", progress = progress) }
        val config = TimelineMathConfig(startPosition = start, steps = steps)
        val snake = TimelineSnakeCoordinates(config, width, floatArrayOf())
        val layout = snake.layout()
        assertEquals(layout, snake.layout())
        assertEquals(steps, layout.steps.map { it.step })
        steps.indices.forEach { index ->
            val segment = snake.segment(index)
            assertEquals(snake.anchor(index), segment.last())
            if (index > 0) assertEquals(snake.segment(index - 1).last(), segment.first())
            TimelineRoundedPath.commands(segment, 10000f)
        }
        listOf(false, true).forEach { horizontal ->
            val linear = TimelineLinearCoordinates(config, horizontal, width, floatArrayOf())
            val result = linear.buildLayout()
            val input = TimelineGeometryInput(config, width)
            TimelineGeometryValidation.validate(input, TimelineGeometryResult(result, emptyList(), emptyList(), TimelineGeometryBounds(0f, 0f, 0f, 0f)), null)
            assertEquals(result, linear.buildLayout())
            linear.getSegments().zipWithNext().forEach { (a, b) -> assertEquals(a.end, b.start) }
            linear.getSegments().forEachIndexed { index, segment ->
                assertEquals(segment.start + segment.length * progress / 100f, linear.getProgressPosition(index))
            }
        }
        assertTrue(layout.steps.all { it.titleWidth > 0 && it.descriptionWidth > 0 })
    }
}
