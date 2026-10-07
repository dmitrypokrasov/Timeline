package com.dmitrypokrasov.timelineview.model

/** A host-defined date or stage group. Step IDs are scoped to this section. */
data class TimelineSection(
    val id: String,
    val title: CharSequence,
    val steps: List<TimelineStepData>,
) {
    init {
        require(id.isNotBlank()) { "Section ID must not be blank" }
    }
}
