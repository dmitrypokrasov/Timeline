package com.dmitrypokrasov.timelineview.math

import android.graphics.Path
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.model.TimelineStepData

/**
 * Core strategy contract: configuration, constraints, paths and a complete layout.
 */
interface TimelineLayoutEngine {
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

    /** Sets measured distances between successive rows; empty resets to configured spacing. */
    fun setStepExtents(extents: List<Float>) = Unit

    /** Row membership for content measurement. Null opts out of automatic row expansion. */
    fun getContentRows(): List<Int>? = null

    /** Whether text should clear the badge vertically. */
    val textBelowBadge: Boolean get() = false

    /** True when paths already include their curves and must not receive a paint corner effect. */
    val hasRoundedGeometry: Boolean get() = false

    /** Supplies the renderer's corner radius to engines that construct curved paths. */
    fun setCornerRadius(radius: Float) = Unit

    /** Intrinsic width for use in an unconstrained horizontal scroll container. */
    fun getDesiredWidth(): Int = 0

    /** Returns the X offset that should be applied before drawing the timeline geometry. */
    fun getStartPosition(): Float

    /** Stores the available width so the engine can recalculate dependent coordinates. */
    fun setMeasuredWidth(measuredWidth: Int)

    /** Returns the steps currently used by this engine. */
    fun getSteps(): List<TimelineStepData>

    /** Builds layout metadata for badges, text, and the active progress icon. */
    fun buildLayout(): TimelineLayout
}
