package com.dmitrypokrasov.timelineview.ui

import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.model.TimelineStepData

internal object TimelineHitTestHelper {

    sealed interface HitResult {
        data class Step(val index: Int, val step: TimelineStepData) : HitResult
        object ProgressIcon : HitResult
    }

    fun findHit(
        layout: TimelineLayout?,
        stepIconSize: Float,
        progressIconSize: Float,
        x: Float,
        y: Float
    ): HitResult? {
        if (layout == null) return null

        layout.steps.forEachIndexed { index, stepLayout ->
            if (contains(x, y, stepLayout.iconX, stepLayout.iconY, stepIconSize)) {
                return HitResult.Step(index, stepLayout.step)
            }
        }

        val progressIcon = layout.progressIcon
        if (progressIcon != null &&
            contains(x, y, progressIcon.left, progressIcon.top, progressIconSize)
        ) {
            return HitResult.ProgressIcon
        }

        return null
    }

    private fun contains(
        x: Float,
        y: Float,
        left: Float,
        top: Float,
        size: Float
    ): Boolean {
        val right = left + size
        val bottom = top + size
        return x in left..right && y in top..bottom
    }
}
