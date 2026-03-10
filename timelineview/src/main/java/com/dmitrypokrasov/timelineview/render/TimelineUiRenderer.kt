package com.dmitrypokrasov.timelineview.render

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.model.TimelineStepData

/**
 * Interface for timeline UI renderers.
 */
interface TimelineUiRenderer {
    fun setConfig(config: TimelineUiConfig)

    fun getConfig(): TimelineUiConfig

    fun initTools(timelineMathConfig: TimelineMathConfig, context: Context)

    fun prepareStrokePaint()

    fun prepareTextPaint()

    fun prepareIconPaint()

    fun drawProgressIcon(canvas: Canvas, leftCoordinates: Float, topCoordinates: Float)

    fun drawCompletedPath(canvas: Canvas)

    fun drawRemainingPath(canvas: Canvas)

    fun getCompletedPath(): Path

    fun getRemainingPath(): Path

    fun drawTitle(
        canvas: Canvas,
        title: CharSequence,
        x: Float,
        y: Float,
        align: Paint.Align,
        maxWidth: Int
    )

    fun drawDescription(
        canvas: Canvas,
        description: CharSequence,
        x: Float,
        y: Float,
        align: Paint.Align,
        maxWidth: Int
    )

    fun measureTitleHeight(title: CharSequence, maxWidth: Int, align: Paint.Align): Int

    fun measureDescriptionHeight(description: CharSequence, maxWidth: Int, align: Paint.Align): Int

    fun getTitleBaselineOffset(): Float

    fun getDescriptionBaselineOffset(): Float

    fun drawStepIcon(
        step: TimelineStepData,
        canvas: Canvas,
        align: Paint.Align,
        context: Context,
        x: Float,
        y: Float
    )

    fun getTextAlignment(): Paint.Align
}
