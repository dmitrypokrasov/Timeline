package com.dmitrypokrasov.timelineview.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.graphics.PathMeasure
import android.view.View
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.math.AdaptiveGridTimelineMath
import com.dmitrypokrasov.timelineview.math.LinearTimelineMath
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.model.TimelineLottieSpec
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.render.BaseTimelineUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TimelineReleaseRegressionTest {
    private val context get() = RuntimeEnvironment.getApplication()

    @Test fun sequentialZeroLengthFirstSegmentMustNotCompleteFollowingSegment() {
        TimelineMathConfig.ProgressMode.entries.forEach { mode ->
            listOf(0, 50, 100).forEach { firstProgress ->
                val math =
                    TimelineMathConfig(
                        steps = listOf(TimelineStepData(progress = firstProgress), TimelineStepData(progress = 100)),
                        progressMode = mode,
                        spacing = TimelineMathConfig.Spacing(stepY = 80f, stepYFirst = 0f),
                        sizes = TimelineMathConfig.Sizes(0f, 0f),
                    )
                val engine = LinearTimelineMath(math)
                TimelineLayoutResolver.resolve(engine, BaseTimelineUi(TimelineUiConfig()), 320, 4f)
                val complete = Path()
                val pending = Path()
                engine.buildPath(complete, pending)
                val expected = if (mode == TimelineMathConfig.ProgressMode.INDEPENDENT || firstProgress == 100) 80f else 0f
                assertEquals("$mode with first progress $firstProgress", expected, PathMeasure(complete, false).length, 0.01f)
                assertEquals(80f - expected, PathMeasure(pending, false).length, 0.01f)
            }
        }
    }

    @Test fun documentedMigrationStyleShouldRenderVisibleContent() {
        val view = TimelineView(context)
        view.setConfig(TimelineMathConfig(steps = listOf(TimelineStepData(title = "Order", description = "Delivery", progress = 20))), TimelineUiConfig())
        view.measure(View.MeasureSpec.makeMeasureSpec(320, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        view.layout(0, 0, 320, view.measuredHeight)
        val bitmap = Bitmap.createBitmap(320, view.height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.WHITE)
        view.draw(Canvas(bitmap))
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        assertTrue("Default Kotlin UI config renders only background", pixels.any { it != Color.WHITE })
    }

    @Test fun textBelowBadgeShouldClearScaledOverlay() {
        val size = 24f
        listOf(0.5f, 1f, 3f).forEach { scale ->
            val math =
                TimelineMathConfig(
                    steps =
                        List(4) {
                            TimelineStepData(
                                title = "Title",
                                description = "A long description ".repeat(8),
                                progress = 0,
                                badgeAnimation = TimelineLottieSpec(1, scale = scale),
                            )
                        },
                    sizes = TimelineMathConfig.Sizes(12f, size),
                )
            val engines =
                listOf(AdaptiveGridTimelineMath(math)) +
                    TimelineMathConfig.HorizontalLayout.entries.map {
                        LinearTimelineMath(math.copy(horizontalLayout = it), LinearTimelineMath.Orientation.HORIZONTAL)
                    }
            engines.forEach { engine ->
                listOf(160, 480).forEach { width ->
                    assertOverlayClearance(engine, width, size, scale)
                }
            }
        }
    }

    private fun assertOverlayClearance(
        engine: TimelineMathEngine,
        width: Int,
        size: Float,
        scale: Float,
    ) {
        val renderer = BaseTimelineUi(TimelineUiConfig())
        renderer.initTools(engine.getConfig(), context)
        val layout = TimelineLayoutResolver.resolve(engine, renderer, width, 4f)
        val blocks = TimelineTextBlockResolver.resolve(layout, engine, renderer)
        layout.steps.forEachIndexed { index, step ->
            val overlayBottom = step.iconY + size + size * (scale.coerceAtLeast(1f) - 1f) / 2f
            assertTrue("${engine.javaClass.simpleName}: text overlaps badge at scale $scale", blocks[index].titleTop >= overlayBottom + 4f)
        }
        engine.getContentRows()?.let { rows ->
            rows.distinct().zipWithNext().forEach { (previous, next) ->
                val bottom = rows.indices.filter { rows[it] == previous }.maxOf { blocks[it].descriptionTop + blocks[it].descriptionHeight }
                val top = rows.indices.filter { rows[it] == next }.minOf { layout.steps[it].iconY - size * (scale.coerceAtLeast(1f) - 1f) / 2f }
                assertTrue("Next row overlaps measured text", top >= bottom + 3.99f)
            }
        }
    }
}
