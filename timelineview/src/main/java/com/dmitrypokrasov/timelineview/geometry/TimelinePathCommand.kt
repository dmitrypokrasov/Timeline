package com.dmitrypokrasov.timelineview.geometry

/** Команды пути в локальных пикселях; Android Path создаёт адаптер UI. */
sealed interface TimelinePathCommand {
    /** Начало независимого контура. */
    data class Move(val x: Float, val y: Float) : TimelinePathCommand

    /** Прямой сегмент от текущей точки. */
    data class Line(val x: Float, val y: Float) : TimelinePathCommand

    /** Квадратичная кривая с контрольной и конечной точками. */
    data class Quad(val controlX: Float, val controlY: Float, val x: Float, val y: Float) : TimelinePathCommand
}

/** Физическое выравнивание текста, независимое от Android. */
enum class TimelineTextAlignment { LEFT, CENTER, RIGHT }
