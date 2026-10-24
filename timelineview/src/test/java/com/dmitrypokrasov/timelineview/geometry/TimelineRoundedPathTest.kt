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
    fun `non finite coordinates and invalid radius fail at boundary`() {
        assertThrows(IllegalArgumentException::class.java) { TimelineRoundedPath.commands(emptyList(), -1f) }
        assertThrows(IllegalArgumentException::class.java) { TimelineRoundedPath.commands(listOf(TimelineGeometryPoint(Float.NaN, 0f)), 0f) }
    }
}
