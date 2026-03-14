package com.dmitrypokrasov.timelineview.ui

import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.model.TimelineStepData

internal object TimelineAccessibilitySnapshotBuilder {
    private const val MIN_TOUCH_TARGET_DP = 48f
    private const val PROGRESS_VIRTUAL_ID_OFFSET = 10_000

    fun build(
        layout: TimelineLayout?,
        stepIconSize: Float,
        progressIconSize: Float,
        density: Float,
        paddingLeft: Int,
        paddingTop: Int,
        stepClickable: Boolean,
        progressClickable: Boolean,
    ): TimelineAccessibilitySnapshot {
        if (layout == null) return TimelineAccessibilitySnapshot.Empty

        val minTouchTarget = MIN_TOUCH_TARGET_DP * density
        val nodes = mutableListOf<TimelineAccessibilityNode>()

        layout.steps.forEachIndexed { index, stepLayout ->
            nodes +=
                TimelineAccessibilityNode.Step(
                    virtualId = index,
                    index = index,
                    step = stepLayout.step,
                    boundsInParent =
                        offsetBounds(
                            TimelineHitTestHelper
                                .buildBounds(
                                    left = stepLayout.iconX,
                                    top = stepLayout.iconY,
                                    size = stepIconSize,
                                    minTouchSize = minTouchTarget,
                                ),
                            paddingLeft = paddingLeft,
                            paddingTop = paddingTop,
                        ),
                    contentDescription = buildStepDescription(index, stepLayout.step),
                    isClickable = stepClickable,
                )
        }

        layout.progressIcon?.let { progressIcon ->
            nodes +=
                TimelineAccessibilityNode.ProgressIcon(
                    virtualId = PROGRESS_VIRTUAL_ID_OFFSET,
                    progressStepIndex = layout.progressStepIndex,
                    boundsInParent =
                        offsetBounds(
                            TimelineHitTestHelper
                                .buildBounds(
                                    left = progressIcon.left,
                                    top = progressIcon.top,
                                    size = progressIconSize,
                                    minTouchSize = minTouchTarget,
                                ),
                            paddingLeft = paddingLeft,
                            paddingTop = paddingTop,
                        ),
                    contentDescription =
                        buildProgressDescription(layout.progressStepIndex, layout.steps.map { it.step }),
                    isClickable = progressClickable,
                )
        }

        return TimelineAccessibilitySnapshot(nodes)
    }

    private fun buildStepDescription(
        index: Int,
        step: TimelineStepData,
    ): String {
        val parts = mutableListOf("Step ${index + 1}")
        step.title?.toString()?.takeIf { it.isNotBlank() }?.let(parts::add)
        step.description?.toString()?.takeIf { it.isNotBlank() }?.let(parts::add)
        parts += "Progress ${step.progress} percent"
        return parts.joinToString(". ")
    }

    private fun buildProgressDescription(
        progressStepIndex: Int?,
        steps: List<TimelineStepData>,
    ): String {
        val parts = mutableListOf("Timeline progress icon")
        val step =
            progressStepIndex
                ?.let(steps::getOrNull)
                ?: steps.firstOrNull { it.progress in 1..99 }
                ?: steps.lastOrNull()

        if (progressStepIndex != null) {
            parts += "Step ${progressStepIndex + 1}"
        }
        step?.title?.toString()?.takeIf { it.isNotBlank() }?.let(parts::add)
        step?.let { parts += "Progress ${it.progress} percent" }
        return parts.joinToString(". ")
    }

    private fun offsetBounds(
        bounds: TimelineBounds,
        paddingLeft: Int,
        paddingTop: Int,
    ): TimelineBounds = bounds.offset(paddingLeft, paddingTop)
}
