@file:Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")

package com.dmitrypokrasov.timelineview.math

import android.graphics.Paint
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.model.indexOfStep

/**
 * Base for layout-first custom strategies. Implement [buildLayout] and [buildPath].
 * Configuration and width are supplied before layout. Override [onGeometryChanged] to clear caches.
 * Legacy progress getters only expose the active progress marker; built-ins retain their own implementations.
 */
abstract class TimelineMathAdapter(config: TimelineMathConfig) : TimelineMathEngine {
    protected var input: TimelineMathConfig = config.copy(steps = config.steps.toList())
        private set
    protected var width: Int = 0
        private set

    override fun setConfig(config: TimelineMathConfig) {
        this.input = config.copy(steps = config.steps.toList())
        onGeometryChanged()
    }

    override fun getConfig(): TimelineMathConfig = input

    override fun replaceSteps(steps: List<TimelineStepData>) = setConfig(input.copy(steps = steps))

    override fun getSteps(): List<TimelineStepData> = input.steps

    override fun setMeasuredWidth(measuredWidth: Int) {
        width = measuredWidth.coerceAtLeast(0)
        onGeometryChanged()
    }

    override fun getStartPosition(): Float = 0f

    protected open fun onGeometryChanged() = Unit

    private fun active(index: Int): com.dmitrypokrasov.timelineview.math.data.TimelineProgressIcon {
        val layout = buildLayout()
        require(index == layout.progressStepIndex) { "Legacy adapter only exposes the active marker; use buildLayout()" }
        return requireNotNull(layout.progressIcon)
    }

    override fun getHorizontalIconOffset(i: Int): Float = active(i).left

    override fun getVerticalOffset(i: Int): Float = active(i).top

    override fun getLeftCoordinates(step: TimelineStepData): Float = active(input.steps.indexOfStep(step)).left

    override fun getTopCoordinates(step: TimelineStepData): Float = active(input.steps.indexOfStep(step)).top

    override fun getIconYCoordinates(i: Int): Float = buildLayout().steps[i].iconY

    override fun getTitleYCoordinates(i: Int): Float = buildLayout().steps[i].titleY

    override fun getDescriptionYCoordinates(i: Int): Float = buildLayout().steps[i].descriptionY

    override fun getTitleXCoordinates(align: Paint.Align): Float = buildLayout().steps.firstOrNull { it.textAlign == align }?.titleX ?: 0f

    override fun getIconXCoordinates(align: Paint.Align): Float = buildLayout().steps.firstOrNull { it.textAlign == align }?.iconX ?: 0f
}
