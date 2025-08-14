package com.dmitrypokrasov.timelineview.domain

import android.graphics.Paint
import android.graphics.Path
import com.dmitrypokrasov.timelineview.data.TimelineStep
import com.dmitrypokrasov.timelineview.domain.data.TimelineMathConfig

/**
 * Простая реализация [TimelineMathEngine], строящая путь без
 * чередования сторон. Линия может располагаться вертикально или
 * горизонтально в зависимости от [orientation].
 */
class LinearTimelineMath(
    var mathConfig: TimelineMathConfig,
    val orientation: Orientation = Orientation.VERTICAL,
) : TimelineMathEngine {

    /** Возможные направления построения линии. */
    enum class Orientation { VERTICAL, HORIZONTAL }

    override fun replaceSteps(steps: List<TimelineStep>) {
        mathConfig = mathConfig.copy(steps = steps)
    }

    override fun buildPath(pathEnable: Path, pathDisable: Path) {
        pathEnable.reset()
        pathDisable.reset()

        var x = 0f
        var y = 0f
        pathEnable.moveTo(x, y)
        pathDisable.moveTo(x, y)

        var drawEnable = true

        mathConfig.steps.forEachIndexed { index, step ->
            val segment = if (orientation == Orientation.VERTICAL) {
                if (index == 0) mathConfig.stepYFirst else mathConfig.getStandardDyMove(index)
            } else {
                if (index == 0) mathConfig.getStepXFirst() else mathConfig.getStepX()
            }

            if (drawEnable) {
                val progress = segment * step.percents / 100f
                if (orientation == Orientation.VERTICAL) {
                    pathEnable.rLineTo(0f, progress)
                    if (progress < segment) {
                        pathDisable.moveTo(x, y + progress)
                        pathDisable.rLineTo(0f, segment - progress)
                        drawEnable = false
                    }
                    y += segment
                } else {
                    pathEnable.rLineTo(progress, 0f)
                    if (progress < segment) {
                        pathDisable.moveTo(x + progress, y)
                        pathDisable.rLineTo(segment - progress, 0f)
                        drawEnable = false
                    }
                    x += segment
                }
            } else {
                if (orientation == Orientation.VERTICAL) {
                    pathDisable.rLineTo(0f, segment)
                    y += segment
                } else {
                    pathDisable.rLineTo(segment, 0f)
                    x += segment
                }
            }
        }
    }

    override fun getStartPosition(): Float = mathConfig.getStartPosition()

    override fun setMeasuredWidth(measuredWidth: Int) {
        mathConfig.setMeasuredWidth(measuredWidth)
    }

    override fun getHorizontalIconOffset(i: Int): Float {
        return if (orientation == Orientation.VERTICAL) -mathConfig.sizeIconProgress / 2f
        else mathConfig.getVerticalOffset(i)
    }

    override fun getVerticalOffset(i: Int): Float {
        return if (orientation == Orientation.VERTICAL) mathConfig.getVerticalOffset(i)
        else -mathConfig.sizeIconProgress / 2f
    }

    override fun getMeasuredHeight(): Int = mathConfig.getMeasuredHeight()

    override fun getLeftCoordinates(lvl: TimelineStep): Float {
        return -mathConfig.sizeIconProgress / 2f
    }

    override fun getTopCoordinates(lvl: TimelineStep): Float {
        return -mathConfig.sizeIconProgress / 2f
    }

    override fun getTitleXCoordinates(align: Paint.Align): Float {
        return mathConfig.getTitleXCoordinates(align)
    }

    override fun getIconXCoordinates(align: Paint.Align): Float {
        return mathConfig.getIconXCoordinates(align)
    }

    override fun getTitleYCoordinates(i: Int): Float {
        return mathConfig.getTitleYCoordinates(i)
    }

    override fun getDescriptionYCoordinates(i: Int): Float {
        return mathConfig.getDescriptionYCoordinates(i)
    }
}

