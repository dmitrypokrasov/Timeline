package com.dmitrypokrasov.timelineview.ui

import android.graphics.Path
import android.graphics.PathMeasure
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.math.LinearTimelineMath
import com.dmitrypokrasov.timelineview.math.SnakeTimelineMath
import com.dmitrypokrasov.timelineview.math.TimeScaledTimelineMath
import com.dmitrypokrasov.timelineview.math.TimelineMathFactory
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TimelineStrategyPropertiesTest {
    @Test
    fun `seeded geometry cases remain finite and measured rows never overlap`() {
        val random = Random(3817)
        repeat(100) {
            val config =
                TimelineMathConfig(
                    steps = List(random.nextInt(0, 18)) { index -> TimelineStepData(id = "$index", timestampMillis = index * 80_000L, title = "Step", description = "Description", progress = random.nextInt(101)) },
                    spacing = TimelineMathConfig.Spacing(stepY = random.nextInt(0, 150).toFloat(), stepYFirst = 16f, marginTopTitle = 20f),
                    minCellWidth = 100f,
                    sizes = TimelineMathConfig.Sizes(12f, 24f),
                )
            TimelineMathStrategy.entries.forEach { strategy ->
                val engine = TimelineMathFactory.create(strategy, config)
                engine.setCornerRadius(random.nextInt(0, 80).toFloat())
                val renderer = FakeMeasuringRenderer(random.nextInt(10, 50), random.nextInt(0, 300), 12f, 12f)
                val layout = TimelineLayoutResolver.resolve(engine, renderer, random.nextInt(180, 801), 4f)
                val blocks = TimelineTextBlockResolver.resolve(layout, engine, renderer)
                val rows = engine.getContentRows()
                layout.steps.forEach { step -> assertTrue(listOf(step.iconX, step.iconY, step.titleX, step.titleY).all { it.isFinite() }) }
                if (rows != null) {
                    val groups = rows.indices.groupBy { rows[it] }.values.toList()
                    groups.zipWithNext().forEach { (a, b) ->
                        val bottom = a.maxOf { maxOf(layout.steps[it].iconY + 24f, blocks[it].descriptionTop + blocks[it].descriptionHeight, blocks[it].titleTop + blocks[it].titleHeight) }
                        val top = b.minOf { minOf(layout.steps[it].iconY, blocks[it].titleTop) }
                        assertTrue("$strategy: $top < $bottom", top + 0.1f >= bottom + 4f)
                    }
                }
                val complete = Path()
                val remaining = Path()
                engine.buildPath(complete, remaining)
                layout.progressIcon?.let { point -> assertTrue(point.left.isFinite() && point.top.isFinite()) }
            }
        }
    }

    @Test
    fun `rounded progress is the actual split of the drawn path`() {
        listOf(0f, 12f, 80f, 1000f).forEach { radius ->
            (0..99).forEach { progress ->
                val config = TimelineMathConfig(steps = listOf(TimelineStepData(progress = 100), TimelineStepData(progress = progress)))
                val engine = SnakeTimelineMath(config)
                engine.setMeasuredWidth(320)
                engine.setCornerRadius(radius)
                val completed = Path()
                val pending = Path()
                engine.buildPath(completed, pending)
                val icon = requireNotNull(engine.buildLayout().progressIcon)
                val measured = PathMeasure(completed, false)
                val position = FloatArray(2)
                assertTrue(measured.getPosTan(measured.length, position, null))
                assertEquals(icon.left + config.sizes.sizeIconProgress / 2f, position[0], 0.1f)
                assertEquals(icon.top + config.sizes.sizeIconProgress / 2f, position[1], 0.1f)
                val rest = PathMeasure(pending, false)
                assertTrue(rest.getPosTan(0f, position, null))
                assertEquals(icon.left + config.sizes.sizeIconProgress / 2f, position[0], 0.1f)
                assertEquals(icon.top + config.sizes.sizeIconProgress / 2f, position[1], 0.1f)
            }
        }
    }

    @Test
    fun `independent progress colors segments beyond the first incomplete step`() {
        fun length(path: Path): Float {
            val measure = PathMeasure(path, false)
            var total = 0f
            do {
                total += measure.length
            } while (measure.nextContour())
            return total
        }
        listOf(TimelineMathStrategy.Snake, TimelineMathStrategy.LinearVertical, TimelineMathStrategy.AdaptiveGrid).forEach { strategy ->
            val config = TimelineMathConfig(steps = listOf(TimelineStepData(progress = 0), TimelineStepData(progress = 100)))
            val engine = TimelineMathFactory.create(strategy, config)
            engine.setMeasuredWidth(320)
            val path = Path()
            engine.buildPath(path, Path())
            assertEquals(0f, length(path), 0.01f)
            engine.setConfig(config.copy(progressMode = TimelineMathConfig.ProgressMode.INDEPENDENT))
            engine.setMeasuredWidth(320)
            engine.buildPath(path, Path())
            assertTrue(length(path) > 0f)
        }
    }

    @Test
    fun `wrapped horizontal shares grid measurement and time scale preserves intervals`() {
        val steps = List(6) { TimelineStepData(id = "$it", timestampMillis = it * 1000L, title = "Title", description = "Long", progress = 0) }
        val config = TimelineMathConfig(steps = steps, horizontalLayout = TimelineMathConfig.HorizontalLayout.WRAP, minCellWidth = 120f)
        val engine = LinearTimelineMath(config, LinearTimelineMath.Orientation.HORIZONTAL)
        val layout = TimelineLayoutResolver.resolve(engine, FakeMeasuringRenderer(20, 200, 12f, 12f), 300, 4f)
        assertEquals(listOf(0, 0, 1, 1, 2, 2), engine.getContentRows())
        assertTrue(layout.steps[2].iconY > layout.steps[0].iconY + 200)
        val timed = TimeScaledTimelineMath(config.copy(pixelsPerMillisecond = 1.0))
        val timedLayout = TimelineLayoutResolver.resolve(timed, FakeMeasuringRenderer(20, 10, 12f, 12f), 320, 4f)
        assertEquals(1000f, timedLayout.steps[1].iconY - timedLayout.steps[0].iconY, 0.1f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `time scale rejects missing timestamps`() {
        TimeScaledTimelineMath(TimelineMathConfig(steps = listOf(TimelineStepData(progress = 0))))
    }
}
