package com.dmitrypokrasov.timelineview.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.SpannableString
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.text.style.AlignmentSpan

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
        val paragraphs = alignedParagraphs(text, align)
        val staticLayout =
            StaticLayout.Builder
                .obtain(paragraphs, 0, paragraphs.length, textPaint, width.coerceAtLeast(1))
                .setIncludePad(false)
                .build()

        return object : TimelineTextLayout {
            override val height: Int = staticLayout.height

            override fun draw(canvas: Canvas) {
                staticLayout.draw(canvas)
            }
        }
    }

    private fun alignedParagraphs(
        text: CharSequence,
        align: Paint.Align,
    ): CharSequence {
        val result = SpannableString(text)
        var start = 0
        while (start < text.length) {
            val newline = text.indexOf('\n', start)
            val end = if (newline < 0) text.length else newline + 1
            if (result.getSpans(start, end, AlignmentSpan::class.java).isEmpty()) {
                val rtl = TextDirectionHeuristics.FIRSTSTRONG_LTR.isRtl(text, start, end - start)
                result.setSpan(AlignmentSpan.Standard(paragraphAlignment(align, rtl)), start, end, Spanned.SPAN_PARAGRAPH)
            }
            start = end
        }
        return result
    }

    private fun paragraphAlignment(
        align: Paint.Align,
        rtl: Boolean,
    ): Layout.Alignment =
        when (align) {
            Paint.Align.LEFT -> if (rtl) Layout.Alignment.ALIGN_OPPOSITE else Layout.Alignment.ALIGN_NORMAL
            Paint.Align.CENTER -> Layout.Alignment.ALIGN_CENTER
            Paint.Align.RIGHT -> if (rtl) Layout.Alignment.ALIGN_NORMAL else Layout.Alignment.ALIGN_OPPOSITE
        }
}

/** Bounded cache shared by measuring and drawing, cleared when a renderer is reconfigured. */
internal class CachingTimelineTextLayoutBuilder(
    private val delegate: TimelineTextLayoutBuilder = StaticTimelineTextLayoutBuilder(),
) : TimelineTextLayoutBuilder {
    private data class Key(
        val text: CharSequence,
        val size: Float,
        val typeface: Typeface,
        val color: Int,
        val align: Paint.Align,
        val width: Int,
    )

    private val cache =
        object : LinkedHashMap<Key, TimelineTextLayout>(16, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Key, TimelineTextLayout>): Boolean = size > 256
        }

    fun clear() = cache.clear()

    override fun build(
        text: CharSequence,
        textSize: Float,
        typeface: Typeface,
        color: Int,
        align: Paint.Align,
        width: Int,
    ): TimelineTextLayout {
        val snapshot = if (text is android.text.Spanned) android.text.SpannedString(text) else text.toString()
        val key = Key(snapshot, textSize, typeface, color, align, width)
        return cache.getOrPut(key) { delegate.build(key.text, textSize, typeface, color, align, width) }
    }
}
