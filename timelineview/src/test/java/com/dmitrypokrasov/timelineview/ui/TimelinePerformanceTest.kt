package com.dmitrypokrasov.timelineview.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.math.AlternatingTimelineMath
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import kotlin.system.measureNanoTime

/** Diagnostic timings, not device frame-rate claims or flaky wall-clock gates. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TimelinePerformanceTest {
    @Test
    fun `report measure and clipped draw costs for increasing datasets`() {
        val rows = mutableListOf("steps,measure_median_ms,draw_median_ms")
        listOf(10, 100, 1000).forEach { count ->
            val view = TimelineView(RuntimeEnvironment.getApplication())
            view.setMathEngine(AlternatingTimelineMath(TimelineMathConfig(steps = List(count) { TimelineStepData(id = "$it", title = "Event $it", description = "A description for event $it", progress = 0) })))
            val bitmap = Bitmap.createBitmap(480, 800, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val measure = mutableListOf<Long>()
            val draw = mutableListOf<Long>()
            repeat(7) { iteration ->
                view.forceLayout()
                val measured =
                    measureNanoTime {
                        view.measure(View.MeasureSpec.makeMeasureSpec(480, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
                        view.layout(0, 0, 480, view.measuredHeight)
                    }
                val drawn = measureNanoTime { view.draw(canvas) }
                if (iteration >= 2) {
                    measure += measured
                    draw += drawn
                }
            }
            rows += "$count,${measure.sorted()[2] / 1_000_000.0},${draw.sorted()[2] / 1_000_000.0}"
            bitmap.recycle()
        }
        File("build/reports/performance/timings.csv").apply {
            parentFile!!.mkdirs()
            writeText(rows.joinToString("\n") + "\n")
        }
    }
}
