package com.dmitrypokrasov.timelineview.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.view.View
import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.math.LinearTimelineMath
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.math.TimelineMathFactory
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.render.BaseTimelineUi
import com.dmitrypokrasov.timelineview.strategy.TimelineMathProvider
import com.dmitrypokrasov.timelineview.strategy.TimelineStrategyRegistry
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TimelineAtomicStateTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()

    private fun math() = TimelineMathConfig(steps = listOf(TimelineStepData(id = "a", title = "A", progress = 40)))

    private fun controller() = TimelineViewController(View(context), context, null)

    @Test
    fun `invalid strategy keeps the explicit selection and measured targets`() {
        val engine = LinearTimelineMath(math())
        val controller = controller()
        controller.setMathEngine(engine)
        controller.measure(320)
        val previous = controller.targets()
        assertThrows(IllegalArgumentException::class.java) { controller.setStrategy(TimelineMathStrategy.TimeScaled, TimelineUiStrategy.Linear) }
        assertEquals(previous, controller.targets())
        controller.setStrategyRegistry(TimelineStrategyRegistry.createLocalRegistry())
        controller.replaceSteps(listOf(TimelineStepData(id = "new", progress = 100)))
        assertEquals("new", engine.getSteps().single().id)
    }

    @Test
    fun `failed registry replacement does not poison subsequent resolution`() {
        val key = StrategyKey("transactional")

        fun registry(fails: Boolean) =
            TimelineStrategyRegistry.createLocalRegistry().apply {
                registerMath(
                    object : TimelineMathProvider {
                        override val key = StrategyKey("transactional")

                        override fun create(config: TimelineMathConfig): TimelineMathEngine {
                            check(!fails) { "Factory failed" }
                            return LinearTimelineMath(config)
                        }
                    },
                )
            }
        val controller = controller()
        controller.setStrategyRegistry(registry(false))
        controller.setStrategy(key, TimelineUiStrategy.Linear.key)
        assertThrows(IllegalStateException::class.java) { controller.setStrategyRegistry(registry(true)) }
        controller.setStrategy(key, TimelineUiStrategy.Linear.key)
        controller.measure(320)
    }

    @Test
    fun `renderer preparation failure preserves the current frame`() {
        val controller = controller()
        controller.setMathEngine(LinearTimelineMath(math()))
        controller.measure(320)
        val before = controller.targets()
        val renderer =
            object : BaseTimelineUi(TimelineUiConfig()) {
                override fun initTools(
                    timelineMathConfig: TimelineMathConfig,
                    context: Context,
                ) {
                    error("Cannot prepare renderer")
                }
            }
        assertThrows(IllegalStateException::class.java) { controller.setUiRenderer(renderer) }
        assertEquals(before, controller.targets())
        controller.measure(320)
        assertEquals(before, controller.targets())
    }

    @Test
    fun `failed configuration restores a reused engine after partial mutation`() {
        val original = math()
        val delegate = LinearTimelineMath(original)
        val engine =
            object : TimelineMathEngine by delegate {
                override fun setConfig(config: TimelineMathConfig) {
                    delegate.setConfig(config)
                    check(config.steps.firstOrNull()?.id != "rejected")
                }
            }
        val controller = controller()
        controller.setMathEngine(engine)
        controller.measure(320)
        val before = controller.targets()
        assertThrows(IllegalStateException::class.java) {
            controller.setConfig(original.copy(steps = listOf(TimelineStepData(id = "rejected", progress = 0))), TimelineUiConfig())
        }
        assertEquals(original, engine.getConfig())
        assertEquals(before, controller.targets())
        controller.measure(320)
        assertEquals(before, controller.targets())
        controller.replaceSteps(listOf(TimelineStepData(id = "accepted", progress = 0)))
        assertEquals("accepted", engine.getSteps().single().id)
    }

    @Test
    fun `failed measurement retains paths transforms and interaction of the last frame`() {
        var fail = false
        val delegate = LinearTimelineMath(math())
        val engine =
            object : TimelineMathEngine by delegate {
                override fun buildPath(
                    pathEnable: Path,
                    pathDisable: Path,
                ) {
                    check(!fail)
                    delegate.buildPath(pathEnable, pathDisable)
                }
            }
        val controller = controller()
        controller.setStrategies(engine, BaseTimelineUi(TimelineUiConfig(colors = TimelineUiConfig.Colors(Color.BLUE, Color.GRAY, Color.BLACK, Color.BLACK))))
        val height = controller.measure(320)

        fun pixels(): IntArray {
            val bitmap = Bitmap.createBitmap(320, height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.WHITE)
            controller.draw(Canvas(bitmap))
            return IntArray(320 * height).also {
                bitmap.getPixels(it, 0, 320, 0, 0, 320, height)
                bitmap.recycle()
            }
        }
        val before = pixels()
        val targets = controller.targets()
        controller.targets().first().bounds.setEmpty()
        assertFalse(controller.targets().first().bounds.isEmpty)
        fail = true
        assertThrows(IllegalStateException::class.java) { controller.measure(800) }
        assertEquals(targets, controller.targets())
        assertArrayEquals(before, pixels())
    }

    @Test
    fun `every built in has matching factory and registry creation`() {
        val registry = TimelineStrategyRegistry.createLocalRegistry()
        val config = math().copy(steps = listOf(TimelineStepData(timestampMillis = 0, progress = 0)))
        TimelineMathStrategy.entries.forEach { strategy ->
            assertEquals(TimelineMathFactory.create(strategy, config)::class, registry.getMathProvider(strategy.key)!!.create(config)::class)
        }
    }
}
