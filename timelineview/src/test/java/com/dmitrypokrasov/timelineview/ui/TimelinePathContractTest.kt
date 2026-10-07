package com.dmitrypokrasov.timelineview.ui

import android.graphics.Path
import android.graphics.PathMeasure
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.math.TimeScaledTimelineMath
import com.dmitrypokrasov.timelineview.math.TimelineMathFactory
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Shared numerical contracts: useful starting points for third-party layout engines. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TimelinePathContractTest {
    @Test
    fun `all strategies conserve path length and put the marker at the color boundary`() {
        TimelineMathStrategy.entries.forEach { strategy ->
            listOf(0, 1, 120, 480).forEach { width ->
                listOf(0, 1, 6).forEach { count ->
                    verifyBoundary(strategy, width, count)
                }
            }
        }
    }

    private fun verifyBoundary(
        strategy: TimelineMathStrategy,
        width: Int,
        count: Int,
    ) {
        val steps = List(count) { TimelineStepData(id = "$it", timestampMillis = it * 1_000L, progress = 100) }
        val config = TimelineMathConfig(steps = steps, minCellWidth = 80f)
        val engine = TimelineMathFactory.create(strategy, config)
        engine.setMeasuredWidth(width)
        engine.setCornerRadius(1000f)
        val full = Path()
        engine.buildPath(full, Path())
        val fullLength = length(full)
        assertNull(engine.buildLayout().progressIcon)
        val cases = TimelineMathConfig.ProgressMode.entries.flatMap { mode -> listOf(0, 1, 50, 99).map { mode to it } }
        cases.forEach { (mode, progress) ->
            engine.setConfig(
                config.copy(
                    progressMode = mode,
                    steps =
                        steps.mapIndexed { index, step ->
                            step.copy(progress = progressAt(index, count, progress))
                        },
                ),
            )
            engine.setMeasuredWidth(width)
            val completed = Path()
            val pending = Path()
            engine.buildPath(completed, pending)
            val label = "$strategy width=$width count=$count mode=$mode progress=$progress"
            assertEquals(label, fullLength, length(completed) + length(pending), 0.5f)
            val marker = engine.buildLayout().progressIcon
            if (count == 0) {
                assertNull(marker)
            } else {
                requireNotNull(marker)
                val x = marker.left + config.sizes.sizeIconProgress / 2f
                val y = marker.top + config.sizes.sizeIconProgress / 2f
                endpoint(completed, last = true)?.let { assertPoint(label, x, y, it) }
                endpoint(pending, last = false)?.let { assertPoint(label, x, y, it) }
            }
        }
    }

    @Test
    fun `independent alternating completion preserves total length for every strategy`() {
        TimelineMathStrategy.entries.forEach { strategy ->
            val config = TimelineMathConfig(steps = List(7) { TimelineStepData(timestampMillis = it * 1_000L, progress = 100) }, progressMode = TimelineMathConfig.ProgressMode.INDEPENDENT)
            val engine = TimelineMathFactory.create(strategy, config)
            engine.setMeasuredWidth(240)
            val full = Path()
            engine.buildPath(full, Path())
            val total = length(full)
            engine.replaceSteps(config.steps.mapIndexed { index, step -> step.copy(progress = if (index % 2 == 0) 0 else 100) })
            val completed = Path()
            val remaining = Path()
            engine.buildPath(completed, remaining)
            assertEquals(strategy.key.value, total, length(completed) + length(remaining), 0.5f)
            assertTrue(strategy.key.value, length(completed) > 0f)
            assertTrue(strategy.key.value, length(remaining) > 0f)
        }
    }

    @Test
    fun `time scale handles equal and pre epoch timestamps and rejects invalid ranges atomically`() {
        val config = TimelineMathConfig(steps = listOf(-1_000L, -1_000L, 0L).map { TimelineStepData(timestampMillis = it, progress = 0) })
        val engine = TimeScaledTimelineMath(config)
        engine.setMeasuredWidth(320)
        assertTrue(engine.buildLayout().steps.all { it.iconY.isFinite() })
        listOf(listOf(1L, 0L), listOf(Long.MIN_VALUE, Long.MAX_VALUE)).forEach { timestamps ->
            assertThrows(IllegalArgumentException::class.java) {
                engine.setConfig(config.copy(steps = timestamps.map { TimelineStepData(timestampMillis = it, progress = 0) }))
            }
            assertEquals(config, engine.getConfig())
        }
    }

    private fun progressAt(
        index: Int,
        count: Int,
        progress: Int,
    ): Int =
        when {
            index < count / 2 -> 100
            index == count / 2 -> progress
            else -> 0
        }

    private fun length(path: Path): Float {
        val measure = PathMeasure(path, false)
        var total = 0f
        do {
            total += measure.length
        } while (measure.nextContour())
        assertTrue(total.isFinite())
        return total
    }

    private fun endpoint(
        path: Path,
        last: Boolean,
    ): FloatArray? {
        val measure = PathMeasure(path, false)
        var result: FloatArray? = null
        do {
            if (measure.length > 0f) {
                val point = FloatArray(2)
                assertTrue(measure.getPosTan(if (last) measure.length else 0f, point, null))
                result = point
                if (!last) return result
            }
        } while (measure.nextContour())
        return result
    }

    private fun assertPoint(
        label: String,
        x: Float,
        y: Float,
        point: FloatArray,
    ) {
        assertEquals(label, x, point[0], 0.15f)
        assertEquals(label, y, point[1], 0.15f)
    }
}
