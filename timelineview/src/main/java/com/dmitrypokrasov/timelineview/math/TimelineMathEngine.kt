package com.dmitrypokrasov.timelineview.math

import android.graphics.Paint
import android.graphics.Path
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.model.TimelineStepData

/**
 * Strategy interface that converts timeline input into drawable geometry and coordinates.
 */
interface TimelineMathEngine {
    /** Replaces the current math configuration. */
    fun setConfig(config: TimelineMathConfig)

    /** Returns the current math configuration. */
    fun getConfig(): TimelineMathConfig

    /** Replaces the current step list while preserving the rest of the configuration. */
    fun replaceSteps(steps: List<TimelineStepData>)

    /**
     * Rebuilds the completed and remaining line paths.
     *
     * Implementations are expected to clear and fill both paths.
     */
    fun buildPath(
        pathEnable: Path,
        pathDisable: Path,
    )

    /** Returns the X offset that should be applied before drawing the timeline geometry. */
    fun getStartPosition(): Float

    /** Stores the available width so the engine can recalculate dependent coordinates. */
    fun setMeasuredWidth(measuredWidth: Int)

    /** Returns the horizontal progress-icon offset for step [i]. */
    fun getHorizontalIconOffset(i: Int): Float

    /** Returns the vertical offset for step [i]. */
    fun getVerticalOffset(i: Int): Float

    /** Returns the steps currently used by this engine. */
    fun getSteps(): List<TimelineStepData>

    /** Returns the left coordinate for the progress icon bound to [step]. */
    fun getLeftCoordinates(step: TimelineStepData): Float

    /** Returns the top coordinate for the progress icon bound to [step]. */
    fun getTopCoordinates(step: TimelineStepData): Float

    /** Returns the Y coordinate of the badge icon for step [i]. */
    fun getIconYCoordinates(i: Int): Float

    /** Returns the title X coordinate for a given text [align]ment. */
    fun getTitleXCoordinates(align: Paint.Align): Float

    /** Returns the badge-icon X coordinate for a given text [align]ment. */
    fun getIconXCoordinates(align: Paint.Align): Float

    /** Returns the title baseline Y coordinate for step [i]. */
    fun getTitleYCoordinates(i: Int): Float

    /** Returns the description baseline Y coordinate for step [i]. */
    fun getDescriptionYCoordinates(i: Int): Float

    /** Builds layout metadata for badges, text, and the active progress icon. */
    fun buildLayout(): TimelineLayout
}
