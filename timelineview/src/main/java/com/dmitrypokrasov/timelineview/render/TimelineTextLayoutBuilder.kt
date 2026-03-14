package com.dmitrypokrasov.timelineview.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import java.util.LinkedHashMap

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
    private data class LayoutCacheKey(
        val text: CharSequence,
        val textSize: Float,
        val typefaceStyle: Int,
        val color: Int,
        val align: Paint.Align,
        val width: Int,
    )

    private val cache =
        object : LinkedHashMap<LayoutCacheKey, TimelineTextLayout>(64, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<LayoutCacheKey, TimelineTextLayout>?): Boolean {
                return size > 64
            }
        }

    override fun build(
        text: CharSequence,
        textSize: Float,
        typeface: Typeface,
        color: Int,
        align: Paint.Align,
        width: Int,
    ): TimelineTextLayout {
        val resolvedWidth = width.coerceAtLeast(1)
        val cacheKey =
            LayoutCacheKey(
                text = text.toString(),
                textSize = textSize,
                typefaceStyle = typeface.style,
                color = color,
                align = align,
                width = resolvedWidth,
            )
        cache[cacheKey]?.let { return it }

        val textPaint =
            TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                this.textSize = textSize
                this.typeface = typeface
                this.color = color
            }
        val staticLayout =
            StaticLayout.Builder
                .obtain(text, 0, text.length, textPaint, resolvedWidth)
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
        }.also { cache[cacheKey] = it }
    }
}
