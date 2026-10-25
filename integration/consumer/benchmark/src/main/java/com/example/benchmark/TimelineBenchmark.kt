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
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.ByteArrayOutputStream

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
            // Предыдущий instrumentation suite может оставить устройство заблокированным.
            device.wakeUp()
            device.executeShellCommand("wm dismiss-keyguard")
            startActivityAndWait(Intent().apply {
                component = ComponentName("com.example.consumer", "com.example.consumer.BenchmarkActivity")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("strategy", strategy)
                putExtra("count", count)
                putExtra("animated", animated)
            })
            check(device.wait(Until.hasObject(By.desc("timeline-scroll")), 10_000)) {
                val hierarchy = ByteArrayOutputStream().also { device.dumpWindowHierarchy(it) }.toString("UTF-8")
                "Benchmark content missing in ${device.currentPackageName}: $hierarchy"
            }
        },
    ) {
        // Каждая прокрутка обновляет accessibility-дерево: не удерживаем UiObject2 между жестами.
        fun swipe(down: Boolean) {
            val bounds = checkNotNull(device.findObject(By.desc("timeline-scroll"))).visibleBounds
            val inset = (bounds.height() * 0.1f).toInt()
            val top = bounds.top + inset
            val bottom = bounds.bottom - inset
            check(device.swipe(bounds.centerX(), if (down) bottom else top, bounds.centerX(), if (down) top else bottom, 20)) {
                "Benchmark swipe injection failed"
            }
        }
        repeat(3) { swipe(true) }
        repeat(3) { swipe(false) }
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
