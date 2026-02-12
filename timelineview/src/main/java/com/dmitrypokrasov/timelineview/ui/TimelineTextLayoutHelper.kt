package com.dmitrypokrasov.timelineview.ui

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import kotlin.math.max

class TimelineTextLayoutHelper {

    fun buildDescriptionLayout(
        text: CharSequence,
        paint: TextPaint,
        maxWidth: Int,
        uiConfig: TimelineUiConfig
    ): StaticLayout {
        val mode = uiConfig.textLayout.descriptionMode
        val normalizedWidth = max(1, maxWidth)
        val builder = StaticLayout.Builder.obtain(text, 0, text.length, paint, normalizedWidth)
            .setAlignment(paintAlignToLayoutAlignment(paint.textAlign))
            .setIncludePad(false)

        when (mode) {
            TimelineUiConfig.TextMode.SINGLE_LINE -> {
                builder.setMaxLines(1)
            }

            TimelineUiConfig.TextMode.MULTI_LINE -> {
                uiConfig.textLayout.maxLinesDescription?.let { builder.setMaxLines(it) }
            }

            TimelineUiConfig.TextMode.ELLIPSIZE_END -> {
                builder.setEllipsize(TextUtils.TruncateAt.END)
                builder.setMaxLines(uiConfig.textLayout.maxLinesDescription ?: Int.MAX_VALUE)
            }
        }

        return builder.build()
    }

    fun measureDescriptionHeight(
        text: CharSequence,
        paint: TextPaint,
        maxWidth: Int,
        uiConfig: TimelineUiConfig
    ): Float = buildDescriptionLayout(text, paint, maxWidth, uiConfig).height.toFloat()

    fun drawDescription(
        canvas: Canvas,
        text: CharSequence,
        paint: TextPaint,
        x: Float,
        y: Float,
        maxWidth: Int,
        uiConfig: TimelineUiConfig
    ) {
        val layout = buildDescriptionLayout(text, paint, maxWidth, uiConfig)
        val left = when (paint.textAlign) {
            Paint.Align.LEFT -> x
            Paint.Align.RIGHT -> x - layout.width
            Paint.Align.CENTER -> x - layout.width / 2f
        }
        val top = y + paint.fontMetrics.ascent

        canvas.save()
        canvas.translate(left, top)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun paintAlignToLayoutAlignment(align: Paint.Align): Layout.Alignment = when (align) {
        Paint.Align.LEFT -> Layout.Alignment.ALIGN_NORMAL
        Paint.Align.RIGHT -> Layout.Alignment.ALIGN_OPPOSITE
        Paint.Align.CENTER -> Layout.Alignment.ALIGN_CENTER
    }
}
