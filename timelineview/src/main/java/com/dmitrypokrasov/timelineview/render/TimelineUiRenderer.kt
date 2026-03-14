package com.dmitrypokrasov.timelineview.render

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.model.TimelineStepData

/**
 * Renderer contract for drawing a timeline once the math engine has produced its geometry.
 */
interface TimelineUiRenderer {
    /** Replaces the current renderer configuration. */
    fun setConfig(config: TimelineUiConfig)

    /** Returns the current renderer configuration. */
    fun getConfig(): TimelineUiConfig

    /** Initializes renderer resources that depend on math config or Android context. */
    fun initTools(
        timelineMathConfig: TimelineMathConfig,
        context: Context,
    )

    /** Prepares the paint used to draw the line stroke. */
    fun prepareStrokePaint()

    /** Prepares the paint used to draw text. */
    fun prepareTextPaint()

    /** Prepares the paint used to draw icons. */
    fun prepareIconPaint()

    /** Draws the active progress icon. */
    fun drawProgressIcon(
        canvas: Canvas,
        leftCoordinates: Float,
        topCoordinates: Float,
    )

    /** Draws the completed portion of the timeline line. */
    fun drawCompletedPath(canvas: Canvas)

    /** Draws the remaining portion of the timeline line. */
    fun drawRemainingPath(canvas: Canvas)

    /** Returns the path instance used for the completed line. */
    fun getCompletedPath(): Path

    /** Returns the path instance used for the remaining line. */
    fun getRemainingPath(): Path

    /** Draws a step title. */
    fun drawTitle(
        canvas: Canvas,
        title: CharSequence,
        x: Float,
        y: Float,
        align: Paint.Align,
        maxWidth: Int,
    )

    /** Draws a step description. */
    fun drawDescription(
        canvas: Canvas,
        description: CharSequence,
        x: Float,
        y: Float,
        align: Paint.Align,
        maxWidth: Int,
    )

    /** Measures the title block height for a constrained width. */
    fun measureTitleHeight(
        title: CharSequence,
        maxWidth: Int,
        align: Paint.Align,
    ): Int

    /** Measures the description block height for a constrained width. */
    fun measureDescriptionHeight(
        description: CharSequence,
        maxWidth: Int,
        align: Paint.Align,
    ): Int

    /** Returns the baseline-to-top offset for title text. */
    fun getTitleBaselineOffset(): Float

    /** Returns the baseline-to-top offset for description text. */
    fun getDescriptionBaselineOffset(): Float

    /** Draws the badge icon for a step. */
    fun drawStepIcon(
        step: TimelineStepData,
        canvas: Canvas,
        align: Paint.Align,
        context: Context,
        x: Float,
        y: Float,
    )

    /** Returns the current text alignment used by the renderer. */
    fun getTextAlignment(): Paint.Align
}
