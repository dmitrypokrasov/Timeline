package com.dmitrypokrasov.timelineview.ui

import android.graphics.Path
import android.view.View
import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryBounds
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryInput
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryLayout
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryResult
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryStep
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryStrategy
import com.dmitrypokrasov.timelineview.geometry.TimelinePathCommand
import com.dmitrypokrasov.timelineview.geometry.TimelineTextAlignment
import com.dmitrypokrasov.timelineview.geometry.TimelineTextMetrics
import com.dmitrypokrasov.timelineview.math.TimelineGeometryMathAdapter
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.render.LinearTimelineUi
import com.dmitrypokrasov.timelineview.strategy.TimelineStrategyRegistry
import com.dmitrypokrasov.timelineview.strategy.registerMath
import com.dmitrypokrasov.timelineview.strategy.registerUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TimelineGeometryAdapterTest {
    private var calls = 0
    private val geometry =
        TimelineGeometryStrategy { input ->
            calls++
            val steps =
                input.config.steps.map { step ->
                    TimelineGeometryStep(step, 20f, 20f, 100, 20f, 50f, 100, 0f, 0f, TimelineTextAlignment.LEFT)
                }
            TimelineGeometryResult(TimelineGeometryLayout(steps, null, null), listOf(TimelinePathCommand.Move(0f, 0f), TimelinePathCommand.Quad(0f, 10f, 20f, 20f)), emptyList(), TimelineGeometryBounds(0f, -10f, 100f, 300f), 12f)
        }

    @Test
    fun `layout paths bounds share cache and changes invalidate it`() {
        val adapter = TimelineGeometryMathAdapter(TimelineMathConfig(), geometry)
        val result = adapter.geometryResult()
        adapter.buildLayout()
        adapter.buildPath(Path(), Path())
        assertSame(result, adapter.geometryResult())
        assertEquals(1, calls)
        assertEquals(12f, adapter.getStartPosition())
        adapter.setMeasuredWidth(40)
        adapter.geometryResult()
        adapter.setCornerRadius(4f)
        adapter.geometryResult()
        adapter.replaceSteps(listOf(TimelineStepData(progress = 1)))
        adapter.geometryResult()
        adapter.setTextMetrics(listOf(TimelineTextMetrics(20, 10)))
        adapter.geometryResult()
        adapter.setTextMetrics(listOf(TimelineTextMetrics(20, 10)))
        adapter.geometryResult()
        val measured = adapter.geometryResult()
        adapter.setConfig(adapter.getConfig().copy())
        adapter.setMeasuredWidth(40)
        assertSame(measured, adapter.geometryResult())
        assertThrows(IllegalArgumentException::class.java) { adapter.setTextMetrics(List(2) { TimelineTextMetrics(1, 1) }) }
        assertThrows(IllegalArgumentException::class.java) { adapter.setCornerRadius(Float.NaN) }
        assertThrows(IllegalArgumentException::class.java) { adapter.setCornerRadius(-1f) }
        assertSame(measured, adapter.geometryResult())
        assertEquals(5, calls)
    }

    @Test
    fun `cached snapshot owns copies of strategy collections`() {
        val config = TimelineMathConfig(steps = listOf(TimelineStepData(progress = 1)))
        val source = geometry.calculate(TimelineGeometryInput(config, 320))
        val steps = source.layout.steps.toMutableList()
        val completed = source.completed.toMutableList()
        val adapter = TimelineGeometryMathAdapter(config) { source.copy(layout = source.layout.copy(steps = steps), completed = completed) }
        val snapshot = adapter.geometryResult()
        steps.clear()
        completed.clear()
        assertEquals(1, snapshot.layout.steps.size)
        assertEquals(2, snapshot.completed.size)
        assertSame(snapshot, adapter.geometryResult())
    }

    @Test
    fun `view measures text uses declared bounds and keeps state after strict failure`() {
        var receivedMetrics = false
        val key = StrategyKey("test.geometry")
        val registry =
            TimelineStrategyRegistry.createLocalRegistry().apply {
                registerMath(key) { config ->
                    TimelineGeometryMathAdapter(config) { input ->
                        receivedMetrics = receivedMetrics || input.textMetrics.any { it.titleHeight > 0 }
                        geometry.calculate(input)
                    }
                }
                registerUi(key) { LinearTimelineUi(it) }
            }
        val view = TimelineView(RuntimeEnvironment.getApplication())
        view.setStrategyRegistry(registry, true)
        val config = TimelineConfig(TimelineMathConfig(steps = listOf(TimelineStepData(title = "Measured title", progress = 50))), TimelineUiConfig(), mathStrategyKey = key, uiStrategyKey = key)
        view.setConfig(config)
        view.measure(View.MeasureSpec.makeMeasureSpec(320, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        assertTrue(receivedMetrics)
        assertTrue(view.measuredHeight >= 310)
        val measuredCalls = calls
        view.forceLayout()
        view.measure(View.MeasureSpec.makeMeasureSpec(320, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        assertEquals(measuredCalls, calls)
        assertThrows(IllegalArgumentException::class.java) { view.setStrategyRegistry(TimelineStrategyRegistry.createLocalRegistry(), true) }
        assertEquals(key, view.getConfig().mathStrategyKey)
        assertThrows(IllegalArgumentException::class.java) { view.setConfig(config.copy(mathStrategyKey = StrategyKey("missing"))) }
        assertEquals(key, view.getConfig().mathStrategyKey)
        view.setStrategy(TimelineMathStrategy.LinearVertical, TimelineUiStrategy.Linear)
        assertEquals(TimelineMathStrategy.LinearVertical, view.getConfig().mathStrategy)
    }
}
