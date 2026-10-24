package com.dmitrypokrasov.timelineview.geometry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TimelineRoundedPathTest {
    @Test
    fun `empty singleton and repeated points have deterministic commands`() {
        assertEquals(emptyList<TimelinePathCommand>(), TimelineRoundedPath.commands(emptyList(), 1000f))
        val point = TimelineGeometryPoint(3f, 7f)
        assertEquals(listOf(TimelinePathCommand.Move(3f, 7f), TimelinePathCommand.Line(3f, 7f)), TimelineRoundedPath.commands(List(3) { point }, 1000f))
    }

    @Test
    fun `radius is limited by both neighboring segments`() {
        val points = listOf(TimelineGeometryPoint(0f, 0f), TimelineGeometryPoint(10f, 0f), TimelineGeometryPoint(10f, 4f))
        val commands = TimelineRoundedPath.commands(points, 1000f)
        assertEquals(listOf(TimelinePathCommand.Move(0f, 0f), TimelinePathCommand.Line(8f, 0f), TimelinePathCommand.Quad(10f, 0f, 10f, 2f), TimelinePathCommand.Line(10f, 4f)), commands)
        assertEquals(commands, TimelineRoundedPath.commands(points, 1000f))
        assertEquals(TimelinePathCommand.Line(10f, 0f), TimelineRoundedPath.commands(points, 0f)[1])
    }

    @Test
    fun `finite extreme coordinates never overflow rounded commands`() {
        val points = listOf(TimelineGeometryPoint(-Float.MAX_VALUE, 0f), TimelineGeometryPoint(Float.MAX_VALUE, 0f), TimelineGeometryPoint(Float.MAX_VALUE, 10f))
        val commands = TimelineRoundedPath.commands(points, 8f)
        assertEquals(TimelinePathCommand.Line(Float.MAX_VALUE, 0f), commands[1])
        assertEquals(TimelinePathCommand.Quad(Float.MAX_VALUE, 0f, Float.MAX_VALUE, 5f), commands[2])
        val reversed = TimelineRoundedPath.commands(points.reversed(), 8f)
        assertEquals(TimelinePathCommand.Line(Float.MAX_VALUE, 5f), reversed[1])
        assertEquals(TimelinePathCommand.Quad(Float.MAX_VALUE, 0f, Float.MAX_VALUE, 0f), reversed[2])
        val productOverflow =
            TimelineRoundedPath.commands(
                listOf(TimelineGeometryPoint(0f, 0f), TimelineGeometryPoint(2e30f, 0f), TimelineGeometryPoint(2e30f, 2e30f)),
                1e30f,
            )
        assertEquals(TimelinePathCommand.Line(1e30f, 0f), productOverflow[1])
        assertEquals(TimelinePathCommand.Quad(2e30f, 0f, 2e30f, 1e30f), productOverflow[2])
    }

    @Test
    fun `non finite coordinates and invalid radius fail at boundary`() {
        assertThrows(IllegalArgumentException::class.java) { TimelineRoundedPath.commands(emptyList(), -1f) }
        assertThrows(IllegalArgumentException::class.java) { TimelineRoundedPath.commands(listOf(TimelineGeometryPoint(Float.NaN, 0f)), 0f) }
    }
}
