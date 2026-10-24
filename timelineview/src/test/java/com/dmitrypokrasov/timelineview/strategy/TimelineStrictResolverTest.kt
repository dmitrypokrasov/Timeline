package com.dmitrypokrasov.timelineview.strategy

import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.math.LinearTimelineMath
import com.dmitrypokrasov.timelineview.render.LinearTimelineUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TimelineStrictResolverTest {
    private val key = StrategyKey("application.custom")

    @Test
    fun `strict rejects unknown keys while legacy resolver falls back`() {
        val registry = TimelineStrategyRegistry.createLocalRegistry()
        val strict = TimelineStrategyResolver(registry, true)
        val error =
            assertThrows(IllegalArgumentException::class.java) {
                strict.resolveMath(key, TimelineMathStrategy.LinearVertical, TimelineMathConfig())
            }
        assertTrue(error.message.orEmpty().contains(key.value))
        assertThrows(IllegalArgumentException::class.java) {
            strict.resolveUi(key, TimelineUiStrategy.Linear, TimelineUiConfig())
        }
        assertTrue(TimelineStrategyResolver(registry).resolveMath(key, TimelineMathStrategy.LinearVertical, TimelineMathConfig()) is LinearTimelineMath)
    }

    @Test
    fun `lambda registrations retain isolation duplicate removal and provider errors`() {
        val registry = TimelineStrategyRegistry.createLocalRegistry()
        registry.registerMath(key) { LinearTimelineMath(it) }
        registry.registerUi(key) { LinearTimelineUi(it) }
        val resolver = TimelineStrategyResolver(registry, true)
        assertTrue(resolver.resolveMath(key, TimelineMathStrategy.Snake, TimelineMathConfig()) is LinearTimelineMath)
        assertTrue(resolver.resolveUi(key, TimelineUiStrategy.Snake, TimelineUiConfig()) is LinearTimelineUi)
        assertThrows(IllegalArgumentException::class.java) { registry.registerMath(key) { LinearTimelineMath(it) } }
        assertThrows(IllegalArgumentException::class.java) { registry.registerUi(key) { LinearTimelineUi(it) } }
        assertEquals(null, TimelineStrategyRegistry.createLocalRegistry().getMathProvider(key))
        registry.unregisterMath(key)
        registry.unregisterUi(key)
        assertEquals(null, registry.getUiProvider(key))
        registry.registerMath(key) { error("provider failed") }
        val failure = assertThrows(IllegalStateException::class.java) { resolver.resolveMath(key, TimelineMathStrategy.Snake, TimelineMathConfig()) }
        assertEquals("provider failed", failure.message)
    }
}
