package com.dmitrypokrasov.timelineview.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.SpannableString
import android.text.Spanned
import android.text.StaticLayout
import android.text.style.AlignmentSpan
import android.text.style.ForegroundColorSpan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk = [28])
class TimelineTextCacheTest {
    @Test
    @org.robolectric.annotation.Config(sdk = [28, 34])
    fun `physical alignment applies independently to mixed direction paragraphs`() {
        listOf("English\nمرحبا", "مرحبا\nEnglish", "\nEnglish\nمرحبا\n").forEach { text ->
            listOf(16f, 32f).forEach { size ->
                Paint.Align.values().forEach { align ->
                    val layout = layout(text, align, size)
                    repeat(layout.lineCount) { line ->
                        if (text.substring(layout.getLineStart(line), layout.getLineEnd(line)).isBlank()) return@repeat
                        when (align) {
                            Paint.Align.LEFT -> assertEquals(0f, layout.getLineLeft(line), 0.1f)
                            Paint.Align.RIGHT -> assertEquals(240f, layout.getLineRight(line), 0.1f)
                            Paint.Align.CENTER -> assertEquals(120f, (layout.getLineLeft(line) + layout.getLineRight(line)) / 2f, 0.1f)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `authored paragraph alignment and character spans are preserved`() {
        val text = SpannableString("English\nمرحبا")
        val alignment = AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER)
        val color = ForegroundColorSpan(android.graphics.Color.RED)
        text.setSpan(alignment, 0, 8, Spanned.SPAN_PARAGRAPH)
        text.setSpan(color, 8, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val layout = layout(text, Paint.Align.LEFT, 20f)
        assertEquals(120f, (layout.getLineLeft(0) + layout.getLineRight(0)) / 2f, 0.1f)
        assertEquals(0f, layout.getLineLeft(1), 0.1f)
        val rendered = layout.text as Spanned
        assertSame(alignment, rendered.getSpans(0, 8, AlignmentSpan::class.java).single())
        assertSame(color, rendered.getSpans(8, text.length, ForegroundColorSpan::class.java).single())
        assertTrue(text.getSpans(8, text.length, AlignmentSpan::class.java).isEmpty())
    }

    private fun layout(
        text: CharSequence,
        align: Paint.Align,
        size: Float,
    ): StaticLayout {
        val block = StaticTimelineTextLayoutBuilder().build(text, size, Typeface.DEFAULT, 0, align, 240)
        return block.javaClass.declaredFields.single { it.type == StaticLayout::class.java }.let {
            it.isAccessible = true
            it.get(block) as StaticLayout
        }
    }

    @Test
    fun `measuring and drawing share cache until width style or contents change`() {
        var builds = 0
        val builder =
            CachingTimelineTextLayoutBuilder(
                object : TimelineTextLayoutBuilder {
                    override fun build(
                        text: CharSequence,
                        textSize: Float,
                        typeface: Typeface,
                        color: Int,
                        align: Paint.Align,
                        width: Int,
                    ): TimelineTextLayout {
                        builds++
                        return object : TimelineTextLayout {
                            override val height = 12

                            override fun draw(canvas: Canvas) = Unit
                        }
                    }
                },
            )

        // Typeface defaults are Android objects, so this test runs with Robolectric below.
        fun build(width: Int) = builder.build("Title", 12f, Typeface.DEFAULT, 0, Paint.Align.LEFT, width)
        build(100)
        build(100)
        assertEquals(1, builds)
        build(200)
        assertEquals(2, builds)
        builder.clear()
        build(100)
        assertEquals(3, builds)
    }
}
