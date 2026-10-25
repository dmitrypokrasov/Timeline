package com.dmitrypokrasov.timelineview.ui

import android.content.res.Configuration
import android.graphics.Paint
import android.view.View
import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.math.LinearTimelineMath
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.render.BaseTimelineUi
import com.dmitrypokrasov.timelineview.strategy.TimelineCachePolicy
import com.dmitrypokrasov.timelineview.strategy.TimelineCachePolicyProvider
import com.dmitrypokrasov.timelineview.strategy.TimelineMathProvider
import com.dmitrypokrasov.timelineview.strategy.TimelineStrategyRegistry
import com.dmitrypokrasov.timelineview.strategy.registerMath
import com.dmitrypokrasov.timelineview.strategy.registerUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TimelineCachePolicyTest {
    @Test
    fun `both providers must opt in and weakest policy governs progress reuse`() {
        TimelineCachePolicy.entries.forEach { mathPolicy ->
            TimelineCachePolicy.entries.forEach { uiPolicy ->
                assertPolicies(mathPolicy, uiPolicy)
            }
        }
    }

    private fun assertPolicies(
        mathPolicy: TimelineCachePolicy,
        uiPolicy: TimelineCachePolicy,
    ) {
        val context = RuntimeEnvironment.getApplication()
        context.applicationInfo.flags = context.applicationInfo.flags or android.content.pm.ApplicationInfo.FLAG_SUPPORTS_RTL
        val owner = View(context)
        val key = StrategyKey("test.cache")
        var layouts = 0
        var measurements = 0
        val registry =
            TimelineStrategyRegistry.createLocalRegistry().apply {
                registerMath(key, mathPolicy) { config ->
                    val delegate = LinearTimelineMath(config)
                    object : TimelineMathEngine by delegate {
                        override fun buildLayout() = delegate.buildLayout().also { layouts++ }
                    }
                }
                registerUi(key, uiPolicy) { config ->
                    object : BaseTimelineUi(config) {
                        override fun measureTitleHeight(
                            title: CharSequence,
                            maxWidth: Int,
                            align: Paint.Align,
                        ): Int {
                            measurements++
                            return super.measureTitleHeight(title, maxWidth, align)
                        }
                    }
                }
            }
        val controller = TimelineViewController(owner, context, null, registry)
        val math = TimelineMathConfig(steps = listOf(TimelineStepData(id = "a", title = "Title", progress = 1)))
        controller.setConfig(TimelineConfig(math, TimelineUiConfig(), mathStrategyKey = key, uiStrategyKey = key))
        val policy = minOf(mathPolicy, uiPolicy)
        try {
            controller.measure(320)
            val initialLayouts = layouts
            controller.measure(320)
            if (policy == TimelineCachePolicy.NONE) assertTrue(layouts > initialLayouts) else assertEquals(initialLayouts, layouts)
            val beforeProgress = measurements
            controller.replaceSteps(math.steps.map { it.copy(progress = 99) })
            controller.measure(320)
            if (policy == TimelineCachePolicy.CONFIGURATION_WITH_PROGRESS_REUSE) assertEquals(beforeProgress, measurements) else assertTrue(measurements > beforeProgress)
            var before = layouts
            controller.measure(1)
            assertTrue(layouts > before)
            before = layouts
            owner.layoutDirection = View.LAYOUT_DIRECTION_RTL
            controller.measure(1)
            assertTrue(layouts > before)
            val resources = context.resources
            val original = Configuration(resources.configuration)
            try {
                val changed = Configuration(original).apply { fontScale += 0.1f }
                @Suppress("DEPRECATION") // Simulates a host configuration update without restarting the test process.
                resources.updateConfiguration(changed, resources.displayMetrics)
                before = layouts
                controller.measure(1)
                assertTrue(layouts > before)
            } finally {
                @Suppress("DEPRECATION") // Restore shared Robolectric resources for subsequent cases.
                resources.updateConfiguration(original, resources.displayMetrics)
            }
            val beforeContent = measurements
            controller.replaceSteps(math.steps.map { it.copy(title = "Changed") })
            controller.measure(1)
            assertTrue(measurements > beforeContent)
        } finally {
            controller.release()
        }
    }

    @Test
    fun `resolved policy is a snapshot and direct instances retain full callbacks`() {
        val context = RuntimeEnvironment.getApplication()
        val key = StrategyKey("test.snapshot")
        var policy = TimelineCachePolicy.CONFIGURATION
        var layouts = 0
        lateinit var engine: TimelineMathEngine
        val registry =
            TimelineStrategyRegistry.createLocalRegistry().apply {
                registerMath(
                    object : TimelineMathProvider, TimelineCachePolicyProvider {
                        override val key = key
                        override val cachePolicy get() = policy

                        override fun create(config: TimelineMathConfig): TimelineMathEngine {
                            val delegate = LinearTimelineMath(config)
                            return object : TimelineMathEngine by delegate {
                                override fun buildLayout() = delegate.buildLayout().also { layouts++ }
                            }.also { engine = it }
                        }
                    },
                )
                registerUi(key, TimelineCachePolicy.CONFIGURATION) { BaseTimelineUi(it) }
            }
        val controller = TimelineViewController(View(context), context, null, registry)
        controller.setConfig(TimelineConfig(TimelineMathConfig(), TimelineUiConfig(), mathStrategyKey = key, uiStrategyKey = key))
        controller.measure(320)
        policy = TimelineCachePolicy.NONE
        val before = layouts
        controller.measure(320)
        assertEquals(before, layouts)
        controller.setStrategies(engine, BaseTimelineUi(TimelineUiConfig()))
        controller.measure(320)
        val direct = layouts
        controller.measure(320)
        assertTrue(layouts > direct)
        controller.release()
    }
}
