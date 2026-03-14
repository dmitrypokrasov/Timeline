package com.dmitrypokrasov.timelineview.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint

internal interface TimelineTextLayout {
    val height: Int

    fun draw(canvas: Canvas)
}

internal interface TimelineTextLayoutBuilder {
    fun build(
        text: CharSequence,
        textSize: Float,
        typeface: Typeface,
        color: Int,
        align: Paint.Align,
        width: Int,
    ): TimelineTextLayout
}

internal class StaticTimelineTextLayoutBuilder : TimelineTextLayoutBuilder {
    override fun build(
        text: CharSequence,
        textSize: Float,
        typeface: Typeface,
        color: Int,
        align: Paint.Align,
        width: Int,
    ): TimelineTextLayout {
        val textPaint =
            TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                this.textSize = textSize
                this.typeface = typeface
                this.color = color
            }
        val staticLayout =
            StaticLayout.Builder
                .obtain(text, 0, text.length, textPaint, width.coerceAtLeast(1))
                .setAlignment(
                    when (align) {
                        Paint.Align.LEFT -> Layout.Alignment.ALIGN_NORMAL
                        Paint.Align.CENTER -> Layout.Alignment.ALIGN_CENTER
                        Paint.Align.RIGHT -> Layout.Alignment.ALIGN_OPPOSITE
                    },
                )
                .setIncludePad(false)
                .build()

        return object : TimelineTextLayout {
            override val height: Int = staticLayout.height

            override fun draw(canvas: Canvas) {
                staticLayout.draw(canvas)
            }
        }
    }
}
