package com.dmitrypokrasov.timelineview.render

import android.graphics.Paint
import android.graphics.Path
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryLayout
import com.dmitrypokrasov.timelineview.geometry.TimelinePathCommand
import com.dmitrypokrasov.timelineview.geometry.TimelineTextAlignment
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.math.data.TimelineLayoutStep
import com.dmitrypokrasov.timelineview.math.data.TimelineProgressIcon

/** Применяет готовые команды к Android Path, не вычисляя геометрию. */
internal fun Path.setCommands(commands: List<TimelinePathCommand>) {
    reset()
    commands.forEach { command ->
        when (command) {
            is TimelinePathCommand.Move -> moveTo(command.x, command.y)
            is TimelinePathCommand.Line -> lineTo(command.x, command.y)
            is TimelinePathCommand.Quad -> quadTo(command.controlX, command.controlY, command.x, command.y)
        }
    }
}

internal fun TimelineTextAlignment.toAndroid(): Paint.Align = Paint.Align.valueOf(name)

internal fun Paint.Align.toGeometry(): TimelineTextAlignment = TimelineTextAlignment.valueOf(name)

internal fun TimelineGeometryLayout.toAndroid(): TimelineLayout =
    TimelineLayout(
        steps.map { step ->
            TimelineLayoutStep(
                step.step, step.titleX, step.titleY, step.titleWidth,
                step.descriptionX, step.descriptionY, step.descriptionWidth,
                step.iconX, step.iconY, step.textAlign.toAndroid(),
            )
        },
        progressIcon?.let { TimelineProgressIcon(it.left, it.top) },
        progressStepIndex,
    )
