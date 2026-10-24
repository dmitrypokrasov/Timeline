package com.dmitrypokrasov.timelineview.geometry

import kotlin.math.hypot

/** Точка чистого алгоритма; координаты заданы в локальных пикселях. */
data class TimelineGeometryPoint(val x: Float, val y: Float)

/** Скругление полилинии, общее для встроенных и пользовательских стратегий. */
object TimelineRoundedPath {
    /** Соседние дубликаты удаляются; радиус ограничен половиной каждого прилегающего сегмента. */
    fun commands(
        points: List<TimelineGeometryPoint>,
        radius: Float,
    ): List<TimelinePathCommand> {
        require(radius.isFinite() && radius >= 0f) { "Radius must be finite and non-negative" }
        require(points.all { it.x.isFinite() && it.y.isFinite() }) { "Coordinates must be finite" }
        val vertices = points.filterIndexed { index, point -> index == 0 || point != points[index - 1] }
        if (vertices.isEmpty()) return emptyList()
        return buildList {
            add(TimelinePathCommand.Move(vertices.first().x, vertices.first().y))
            for (index in 1 until vertices.lastIndex) {
                val a = vertices[index - 1]
                val b = vertices[index]
                val c = vertices[index + 1]
                val incoming = hypot(b.x - a.x, b.y - a.y)
                val outgoing = hypot(c.x - b.x, c.y - b.y)
                val inset = minOf(radius, incoming / 2f, outgoing / 2f)
                add(TimelinePathCommand.Line(b.x - (b.x - a.x) * inset / incoming, b.y - (b.y - a.y) * inset / incoming))
                add(TimelinePathCommand.Quad(b.x, b.y, b.x + (c.x - b.x) * inset / outgoing, b.y + (c.y - b.y) * inset / outgoing))
            }
            add(TimelinePathCommand.Line(vertices.last().x, vertices.last().y))
        }
    }
}
