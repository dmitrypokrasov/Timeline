package com.dmitrypokrasov.timelineview.model

/** A host-defined date or stage group with a nonblank ID and heading. Step IDs are scoped to this section. */
data class TimelineSection(
    val id: String,
    val title: CharSequence,
    val steps: List<TimelineStepData>,
) {
    init {
        require(title.isNotBlank()) { "Section title must not be blank" }
        require(id.isNotBlank()) { "Section ID must not be blank" }
    }
}
