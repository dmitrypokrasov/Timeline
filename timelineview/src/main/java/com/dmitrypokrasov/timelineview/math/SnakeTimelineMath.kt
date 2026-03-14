package com.dmitrypokrasov.timelineview.math

import android.graphics.Paint
import android.graphics.Path
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.math.data.TimelineLayoutStep
import com.dmitrypokrasov.timelineview.math.data.TimelineProgressIcon
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import kotlin.math.abs

/**
 * Snake-like [TimelineMathEngine] implementation.
 */
class SnakeTimelineMath(private var mathConfig: TimelineMathConfig) : TimelineMathEngine {
    companion object {
        private const val CORNER_SPLIT_OVERLAP = 4f
    }

    private var startPositionX = 0f
    private var startPositionDisableStrokeX = 0f
    private var measuredWidth = 0

    override fun setConfig(config: TimelineMathConfig) {
        mathConfig = config
    }

    override fun getConfig(): TimelineMathConfig = mathConfig

    override fun replaceSteps(steps: List<TimelineStepData>) {
        mathConfig = mathConfig.copy(steps = steps)
    }

    override fun buildPath(
        pathEnable: Path,
        pathDisable: Path,
    ) {
        pathDisable.reset()
        pathEnable.reset()

        var enable = mathConfig.steps.isNotEmpty() && mathConfig.steps[0].progress != 0
        var path: Path = if (enable) pathEnable else pathDisable

        mathConfig.steps.forEachIndexed { i, step ->
            var horizontalStep = if (i % 2 == 0) -getStepX() else getStepX()

            when {
                step.progress == 100 -> {
                    if (i == 0) {
                        path.rLineTo(0f, mathConfig.spacing.stepYFirst)
                        path.rLineTo(-getStepXFirst(), 0f)
                        path.rLineTo(0f, mathConfig.spacing.stepY)
                    } else {
                        path.rLineTo(horizontalStep, 0f)
                        path.rLineTo(0f, getStandardDyMove(i))
                    }
                }

                i == 0 -> {
                    path.rLineTo(0f, mathConfig.spacing.stepYFirst)
                    val startPosDisable = calculateStartPositionDisableStrokeX(step, i)
                    val overlap = resolveCornerOverlap(startPosDisable, getStepXFirst())

                    if (enable) {
                        path.rLineTo(-(startPosDisable + overlap), 0f)
                        path = pathDisable
                        enable = false
                        path.moveTo(-(startPosDisable + overlap), mathConfig.spacing.stepYFirst)
                    }
                    path.rLineTo(-(getStepXFirst() - startPosDisable - overlap), 0f)
                    path.rLineTo(0f, mathConfig.spacing.stepY)
                }

                enable -> {
                    val startPosDisable = calculateStartPositionDisableStrokeX(step, i)
                    horizontalStep = if (i % 2 == 0) -getStepX() else getStepX()
                    val overlap = resolveCornerOverlap(startPosDisable, abs(horizontalStep))
                    val finishPositionLineXEnable =
                        horizontalStep / abs(horizontalStep) * (startPosDisable + overlap)

                    path.rLineTo(finishPositionLineXEnable, 0f)
                    path = pathDisable
                    enable = false

                    val startPositionLineYDisable = mathConfig.spacing.stepYFirst + mathConfig.spacing.stepY * i
                    val startPositionLineXDisable = calculateHorizontalOffset(i, overlap)

                    path.moveTo(startPositionLineXDisable, startPositionLineYDisable)

                    val finishPositionLineYDisable = getStandardDyMove(i)
                    val finishPositionLineXDisable =
                        if (i % 2 == 0) {
                            -(getStepX() - startPosDisable - overlap)
                        } else {
                            getStepX() - startPosDisable - overlap
                        }

                    path.rLineTo(finishPositionLineXDisable, 0f)
                    path.rLineTo(0f, finishPositionLineYDisable)
                }

                else -> {
                    path.rLineTo(horizontalStep, 0f)
                    path.rLineTo(0f, getStandardDyMove(i))
                }
            }
        }
    }

    override fun getStartPosition(): Float = startPositionX

    override fun setMeasuredWidth(measuredWidth: Int) {
        this.measuredWidth = measuredWidth
        startPositionX =
            when (mathConfig.startPosition) {
                TimelineMathConfig.StartPosition.START -> mathConfig.spacing.marginHorizontalStroke
                TimelineMathConfig.StartPosition.CENTER -> measuredWidth / 2f
                TimelineMathConfig.StartPosition.END ->
                    measuredWidth.toFloat() - mathConfig.spacing.marginHorizontalStroke
            }
    }

    override fun getHorizontalIconOffset(i: Int): Float =
        calculateHorizontalOffset(i) - mathConfig.sizes.sizeIconProgress / 2f

    override fun getVerticalOffset(i: Int): Float =
        mathConfig.spacing.stepY * i + mathConfig.spacing.marginTopProgressIcon

    override fun getSteps(): List<TimelineStepData> = mathConfig.steps

    override fun getLeftCoordinates(step: TimelineStepData): Float =
        if (step.progress == 0) {
            -mathConfig.sizes.sizeIconProgress / 2f
        } else {
            -startPositionDisableStrokeX - mathConfig.sizes.sizeIconProgress / 2f
        }

    override fun getTopCoordinates(step: TimelineStepData): Float =
        if (step.progress == 0) {
            -mathConfig.sizes.sizeIconProgress / 2f
        } else {
            mathConfig.spacing.stepYFirst - mathConfig.sizes.sizeIconProgress / 2f
        }

    override fun getIconXCoordinates(align: Paint.Align): Float {
        val stepX = getStepX()
        return when (align) {
            Paint.Align.LEFT -> -(startPositionX - mathConfig.spacing.marginHorizontalImage)
            Paint.Align.CENTER -> startPositionX
            Paint.Align.RIGHT ->
                if (mathConfig.startPosition == TimelineMathConfig.StartPosition.CENTER) {
                    startPositionX - mathConfig.spacing.marginHorizontalImage - mathConfig.sizes.sizeImageLvl
                } else {
                    -startPositionX + stepX + mathConfig.spacing.marginHorizontalImage
                }
        }
    }

    override fun getTitleXCoordinates(align: Paint.Align): Float {
        val stepX = getStepX()
        return when (align) {
            Paint.Align.LEFT -> -(startPositionX - mathConfig.spacing.marginHorizontalText)
            Paint.Align.CENTER -> startPositionX
            Paint.Align.RIGHT ->
                if (mathConfig.startPosition == TimelineMathConfig.StartPosition.CENTER) {
                    startPositionX - mathConfig.spacing.marginHorizontalText
                } else {
                    -startPositionX + stepX
                }
        }
    }

    override fun getIconYCoordinates(i: Int): Float =
        mathConfig.spacing.stepYFirst + mathConfig.spacing.stepY * i +
            mathConfig.spacing.stepY / 2f - mathConfig.sizes.sizeImageLvl / 2f

    override fun getTitleYCoordinates(i: Int): Float = calculateTitleYCoordinates(i)

    override fun getDescriptionYCoordinates(i: Int): Float =
        calculateTitleYCoordinates(i) + mathConfig.spacing.marginTopDescription

    private fun getStandardDyMove(i: Int): Float =
        if (i == mathConfig.steps.lastIndex) mathConfig.spacing.stepY / 2 else mathConfig.spacing.stepY

    private fun calculateTitleYCoordinates(i: Int): Float =
        mathConfig.spacing.stepY * i + mathConfig.spacing.marginTopTitle +
            mathConfig.spacing.stepYFirst / 2f + mathConfig.spacing.stepYFirst / 10f + mathConfig.spacing.stepYFirst / 20f

    private fun calculateStartPositionDisableStrokeX(
        step: TimelineStepData,
        i: Int,
    ): Float {
        val startPosition =
            if (i == 0) getStepXFirst() / 100 * step.progress else getStepX() / 100 * step.progress
        startPositionDisableStrokeX = startPosition
        return startPosition
    }

    private fun resolveCornerOverlap(
        progressOffset: Float,
        segmentWidth: Float,
    ): Float {
        if (progressOffset > 0f) return 0f
        return CORNER_SPLIT_OVERLAP.coerceAtMost(segmentWidth / 2f)
    }

    private fun getStepX(): Float = measuredWidth - mathConfig.spacing.marginHorizontalStroke * 2

    private fun getStepXFirst(): Float = startPositionX - mathConfig.spacing.marginHorizontalStroke

    private fun calculateHorizontalOffset(
        i: Int,
        overlap: Float = 0f,
    ): Float {
        return if (i % 2 == 0) {
            when (mathConfig.startPosition) {
                TimelineMathConfig.StartPosition.START -> -startPositionDisableStrokeX + getStepX() - overlap
                TimelineMathConfig.StartPosition.CENTER ->
                    -startPositionDisableStrokeX + startPositionX -
                        mathConfig.spacing.marginHorizontalStroke - overlap
                TimelineMathConfig.StartPosition.END -> -startPositionDisableStrokeX - overlap
            }
        } else {
            startPositionDisableStrokeX - startPositionX + mathConfig.spacing.marginHorizontalStroke +
                overlap
        }
    }

    override fun buildLayout(): TimelineLayout {
        val steps = mathConfig.steps
        val layoutSteps =
            steps.mapIndexed { index, step ->
                val align = if (index % 2 == 0) Paint.Align.LEFT else Paint.Align.RIGHT
                val titleX = getTitleXCoordinates(align)
                val descriptionX = getTitleXCoordinates(align)
                TimelineLayoutStep(
                    step = step,
                    titleX = titleX,
                    titleY = getTitleYCoordinates(index),
                    titleWidth =
                        TimelineTextWidthResolver.resolve(
                            measuredWidth,
                            startPositionX,
                            titleX,
                            align,
                        ),
                    descriptionX = descriptionX,
                    descriptionY = getDescriptionYCoordinates(index),
                    descriptionWidth =
                        TimelineTextWidthResolver.resolve(
                            measuredWidth,
                            startPositionX,
                            descriptionX,
                            align,
                        ),
                    iconX = getIconXCoordinates(align),
                    iconY = getIconYCoordinates(index),
                    textAlign = align,
                )
            }

        val progressIndex = steps.indexOfFirst { it.progress != 100 }.takeIf { it >= 0 }
        val progressIcon =
            progressIndex?.let { index ->
                val step = steps[index]
                if (index == 0) {
                    TimelineProgressIcon(
                        left = getLeftCoordinates(step),
                        top = getTopCoordinates(step),
                    )
                } else {
                    TimelineProgressIcon(
                        left = getHorizontalIconOffset(index),
                        top = getVerticalOffset(index),
                    )
                }
            }

        return TimelineLayout(
            steps = layoutSteps,
            progressIcon = progressIcon,
            progressStepIndex = progressIndex,
        )
    }
}
