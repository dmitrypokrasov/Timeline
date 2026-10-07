package com.dmitrypokrasov.timelineview.math

import android.graphics.Paint
import com.dmitrypokrasov.timelineview.model.TimelineStepData

/** Compatibility surface for existing engines. New engines can extend [TimelineMathAdapter]. */
interface TimelineMathEngine : TimelineLayoutEngine {
    /** Returns the horizontal progress-icon offset for step [i]. */
    @Deprecated("Use buildLayout() coordinates; new engines can extend TimelineMathAdapter")
    fun getHorizontalIconOffset(i: Int): Float

    /** Returns the vertical offset for step [i]. */
    @Deprecated("Use buildLayout() coordinates; new engines can extend TimelineMathAdapter")
    fun getVerticalOffset(i: Int): Float

    /** Returns the left coordinate for the progress icon bound to [step]. */
    @Deprecated("Use buildLayout() coordinates; new engines can extend TimelineMathAdapter")
    fun getLeftCoordinates(step: TimelineStepData): Float

    /** Returns the top coordinate for the progress icon bound to [step]. */
    @Deprecated("Use buildLayout() coordinates; new engines can extend TimelineMathAdapter")
    fun getTopCoordinates(step: TimelineStepData): Float

    /** Returns the Y coordinate of the badge icon for step [i]. */
    @Deprecated("Use buildLayout() coordinates; new engines can extend TimelineMathAdapter")
    fun getIconYCoordinates(i: Int): Float

    /** Returns the title X coordinate for a given text [align]ment. */
    @Deprecated("Use buildLayout() coordinates; new engines can extend TimelineMathAdapter")
    fun getTitleXCoordinates(align: Paint.Align): Float

    /** Returns the badge-icon X coordinate for a given text [align]ment. */
    @Deprecated("Use buildLayout() coordinates; new engines can extend TimelineMathAdapter")
    fun getIconXCoordinates(align: Paint.Align): Float

    /** Returns the title baseline Y coordinate for step [i]. */
    @Deprecated("Use buildLayout() coordinates; new engines can extend TimelineMathAdapter")
    fun getTitleYCoordinates(i: Int): Float

    /** Returns the description baseline Y coordinate for step [i]. */
    @Deprecated("Use buildLayout() coordinates; new engines can extend TimelineMathAdapter")
    fun getDescriptionYCoordinates(i: Int): Float
}
