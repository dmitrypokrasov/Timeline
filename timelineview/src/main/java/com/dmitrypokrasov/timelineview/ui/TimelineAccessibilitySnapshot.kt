package com.dmitrypokrasov.timelineview.ui

import com.dmitrypokrasov.timelineview.model.TimelineStepData

internal sealed interface TimelineAccessibilityNode {
    val virtualId: Int
    val boundsInParent: TimelineBounds
    val contentDescription: String
    val isClickable: Boolean

    data class Step(
        override val virtualId: Int,
        val index: Int,
        val step: TimelineStepData,
        override val boundsInParent: TimelineBounds,
        override val contentDescription: String,
        override val isClickable: Boolean,
    ) : TimelineAccessibilityNode

    data class ProgressIcon(
        override val virtualId: Int,
        val progressStepIndex: Int?,
        override val boundsInParent: TimelineBounds,
        override val contentDescription: String,
        override val isClickable: Boolean,
    ) : TimelineAccessibilityNode
}

internal data class TimelineAccessibilitySnapshot(
    val nodes: List<TimelineAccessibilityNode>,
) {
    fun findById(
        virtualId: Int,
    ): TimelineAccessibilityNode? = nodes.firstOrNull { it.virtualId == virtualId }

    fun findAt(
        x: Float,
        y: Float,
    ): TimelineAccessibilityNode? =
        nodes.firstOrNull { it.boundsInParent.contains(x, y) }

    companion object {
        val Empty = TimelineAccessibilitySnapshot(emptyList())
    }
}
