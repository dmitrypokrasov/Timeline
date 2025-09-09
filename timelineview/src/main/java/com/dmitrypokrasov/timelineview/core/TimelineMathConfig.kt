package com.dmitrypokrasov.timelineview.core

/**
 * Base interface describing math parameters required for timeline rendering.
 * Contains only essential properties; concrete strategies may extend it
 * with additional fields.
 */
interface TimelineMathConfig {
    val startPosition: StartPosition
    val steps: List<TimelineStep>
    val stepY: Float
    val stepYFirst: Float
    val marginTopDescription: Float
    val marginTopTitle: Float
    val marginTopProgressIcon: Float
    val marginHorizontalImage: Float
    val marginHorizontalText: Float
    val marginHorizontalStroke: Float
    val sizeIconProgress: Float
    val sizeImageLvl: Float

    /** Position of first step relative to container. */
    enum class StartPosition { START, CENTER, END }
}
