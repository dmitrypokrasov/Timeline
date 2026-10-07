package com.example.benchmark

import android.content.ComponentName
import android.content.Intent
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MemoryUsageMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** Same interactions on each strategy: scroll, progress, replace data, and switch strategy. */
@RunWith(Parameterized::class)
class TimelineBenchmark(private val strategy: Int, private val count: Int, private val animated: Boolean) {
    @get:Rule val benchmark = MacrobenchmarkRule()

    @OptIn(ExperimentalMetricApi::class)
    @Test fun interactions() = benchmark.measureRepeated(
        packageName = "com.example.consumer",
        metrics = listOf(FrameTimingMetric(), MemoryUsageMetric(MemoryUsageMetric.Mode.Max)),
        compilationMode = CompilationMode.Full(),
        startupMode = StartupMode.WARM,
        iterations = iterationCount,
        setupBlock = {
            startActivityAndWait(Intent().apply {
                component = ComponentName("com.example.consumer", "com.example.consumer.BenchmarkActivity")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("strategy", strategy)
                putExtra("count", count)
                putExtra("animated", animated)
            })
            check(device.wait(Until.hasObject(By.desc("timeline-scroll")), 10_000))
        },
    ) {
        val scroll = checkNotNull(device.findObject(By.desc("timeline-scroll")))
        scroll.setGestureMargin(device.displayWidth / 10)
        repeat(3) { scroll.scroll(Direction.DOWN, 0.8f) }
        repeat(3) { scroll.scroll(Direction.UP, 0.8f) }
        listOf("progress", "data", "strategy").forEach { action ->
            checkNotNull(device.findObject(By.desc(action))).click()
            device.waitForIdle()
        }
    }

    companion object {
        private val smoke get() = InstrumentationRegistry.getArguments().getString("timelineSmoke") == "true"

        private val iterationCount get() = InstrumentationRegistry.getArguments().getString("timelineIterations")
            ?.toInt()?.also { require(it in 1..20) } ?: if (smoke) 1 else 5

        @JvmStatic @Parameterized.Parameters(name = "strategy={0},count={1},lottie={2}")
        fun cases(): List<Array<Any>> = if (smoke) listOf(arrayOf(0, 100, true)) else
            (0..5).flatMap { strategy ->
                listOf(arrayOf(strategy, 100, false), arrayOf(strategy, 1000, false), arrayOf(strategy, 100, true))
            }
    }
}
