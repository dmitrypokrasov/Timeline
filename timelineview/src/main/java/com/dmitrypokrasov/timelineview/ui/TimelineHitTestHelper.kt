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
        y: Float,
        minStepTouchSize: Float = 0f,
        minProgressTouchSize: Float = 0f
    ): HitResult? {
        if (layout == null) return null

        layout.steps.forEachIndexed { index, stepLayout ->
            if (contains(x, y, stepLayout.iconX, stepLayout.iconY, stepIconSize, minStepTouchSize)) {
                return HitResult.Step(index, stepLayout.step)
            }
        }

        val progressIcon = layout.progressIcon
        if (progressIcon != null &&
            contains(x, y, progressIcon.left, progressIcon.top, progressIconSize, minProgressTouchSize)
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
        size: Float,
        minTouchSize: Float
    ): Boolean {
        val hitSize = maxOf(size, minTouchSize)
        val extra = (hitSize - size) / 2f
        val hitLeft = left - extra
        val hitTop = top - extra
        val right = hitLeft + hitSize
        val bottom = hitTop + hitSize
        return x in hitLeft..right && y in hitTop..bottom
    }
}
