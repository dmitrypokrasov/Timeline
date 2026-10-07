package com.dmitrypokrasov.timelineview.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import org.junit.Assert.assertEquals
import org.junit.Test

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk = [28])
class TimelineTextCacheTest {
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
