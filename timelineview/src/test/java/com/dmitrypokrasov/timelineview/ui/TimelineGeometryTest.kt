package com.dmitrypokrasov.timelineview.ui

import android.graphics.Path
import android.graphics.PathMeasure
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.math.LinearTimelineMath
import com.dmitrypokrasov.timelineview.math.SnakeTimelineMath
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28, 34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TimelineGeometryTest {
    private fun config() =
        TimelineMathConfig(
            startPosition = TimelineMathConfig.StartPosition.START,
            steps =
                listOf(
                    TimelineStepData(title = "Long", description = "Long description", progress = 100),
                    TimelineStepData(title = "Next", description = "Next description", progress = 50),
                    TimelineStepData(title = "Last", progress = 0),
                ),
            spacing = TimelineMathConfig.Spacing(stepY = 40f, stepYFirst = 10f, marginTopTitle = 12f, marginTopDescription = 5f),
            sizes = TimelineMathConfig.Sizes(sizeImageLvl = 24f, sizeIconProgress = 12f),
        )

    @Test
    fun `long descriptions move text badges paths and progress together`() {
        val renderer = FakeMeasuringRenderer(20, 160, 12f, 12f)
        listOf(LinearTimelineMath(config()), SnakeTimelineMath(config())).forEach { engine ->
            engine.setMeasuredWidth(320)
            val initial = engine.buildLayout()
            val layout = TimelineLayoutResolver.resolve(engine, renderer, 320, 4f)
            val blocks = TimelineTextBlockResolver.resolve(layout, engine, renderer)
            assertTrue(blocks[1].titleTop >= blocks[0].descriptionTop + blocks[0].descriptionHeight + 4f)
            val badgeShift = layout.steps[1].iconY - initial.steps[1].iconY
            assertTrue(badgeShift > 0f)
            assertEquals(badgeShift, layout.steps[1].titleY - initial.steps[1].titleY, 0.01f)
            val completed = Path()
            val remaining = Path()
            engine.buildPath(completed, remaining)
            val end = FloatArray(2)
            val path = PathMeasure(completed, false)
            assertTrue(path.getPosTan(path.length, end, null))
            val progress = requireNotNull(layout.progressIcon)
            assertEquals(progress.left + 6f, end[0], 0.1f)
            assertEquals(progress.top + 6f - if (engine is LinearTimelineMath) engine.getConfig().spacing.marginTopProgressIcon else 0f, end[1], 0.1f)
        }
    }

    @Test
    fun `horizontal text cells do not overlap on narrow screens`() {
        val engine = LinearTimelineMath(config(), LinearTimelineMath.Orientation.HORIZONTAL)
        engine.setMeasuredWidth(180)
        val steps = engine.buildLayout().steps
        steps.zipWithNext().forEach { (a, b) ->
            assertTrue(a.titleX + a.titleWidth / 2f <= b.titleX - b.titleWidth / 2f)
        }
    }

    @Test
    fun `snake handles empty single and zero width without nonfinite coordinates`() {
        val cases =
            listOf(0, 1, 320).flatMap { width ->
                listOf(0, 1, 3).flatMap { count -> listOf(0, 50, 100).map { progress -> Triple(width, count, progress) } }
            }
        cases.forEach { (width, count, progress) ->
            val engine = SnakeTimelineMath(config().copy(steps = List(count) { TimelineStepData(progress = progress) }))
            engine.setMeasuredWidth(width)
            engine.buildPath(Path(), Path())
            val layout = engine.buildLayout()
            assertEquals(count, layout.steps.size)
            layout.progressIcon?.let { assertTrue(it.left.isFinite() && it.top.isFinite()) }
        }
    }
}
