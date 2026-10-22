package com.dmitrypokrasov.timelineview.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.view.View
import com.dmitrypokrasov.timelineview.R
import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.math.LinearTimelineMath
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.math.TimelineMathFactory
import com.dmitrypokrasov.timelineview.model.TimelineLottieSpec
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.render.BaseTimelineUi
import com.dmitrypokrasov.timelineview.render.CachingTimelineTextLayoutBuilder
import com.dmitrypokrasov.timelineview.render.StaticTimelineTextLayoutBuilder
import com.dmitrypokrasov.timelineview.render.TimelineTextLayout
import com.dmitrypokrasov.timelineview.render.TimelineTextLayoutBuilder
import com.dmitrypokrasov.timelineview.strategy.TimelineMathProvider
import com.dmitrypokrasov.timelineview.strategy.TimelineStrategyRegistry
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
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
class TimelineAtomicStateTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()

    private fun math() = TimelineMathConfig(steps = listOf(TimelineStepData(id = "a", title = "A", progress = 40)))

    private fun controller() = TimelineViewController(View(context), context, null)

    @Test
    fun `factory owned built ins reuse the committed frame on unchanged measurement`() {
        val controller = controller()
        controller.setConfig(TimelineConfig(math(), TimelineUiConfig(), mathStrategy = TimelineMathStrategy.LinearVertical))
        controller.measure(320)
        val original = measuredFrame(controller)
        controller.measure(320)
        assertSame(original, measuredFrame(controller))
    }

    @Test
    fun `direct built in engine geometry changes are reflected by the next measure`() {
        val config = TimelineMathConfig(steps = List(3) { TimelineStepData(id = "$it", title = "Event $it", progress = 100) })
        val engine = com.dmitrypokrasov.timelineview.math.SnakeTimelineMath(config)
        val controller = controller()
        controller.setStrategies(engine, BaseTimelineUi(TimelineUiConfig()))
        controller.measure(320)
        engine.setCornerRadius(40f)
        val height = controller.measure(320)
        val reference = controller()
        reference.setStrategies(com.dmitrypokrasov.timelineview.math.SnakeTimelineMath(config), BaseTimelineUi(TimelineUiConfig(stroke = TimelineUiConfig.Stroke(radius = 40f))))
        assertEquals(reference.measure(320), height)
        assertArrayEquals(pixels(reference, 320, height), pixels(controller, 320, height))
    }

    private class TextBuilds : TimelineTextLayoutBuilder {
        var count = 0
        private val delegate = StaticTimelineTextLayoutBuilder()
        val cache = CachingTimelineTextLayoutBuilder(this)

        override fun build(
            text: CharSequence,
            textSize: Float,
            typeface: Typeface,
            color: Int,
            align: Paint.Align,
            width: Int,
        ): TimelineTextLayout {
            count++
            return delegate.build(text, textSize, typeface, color, align, width)
        }

        fun reset() {
            count = 0
            cache.clear()
        }
    }

    private fun countText(renderer: BaseTimelineUi): TextBuilds =
        TextBuilds().also { counter ->
            BaseTimelineUi::class.java.getDeclaredField("textLayoutBuilder").apply { isAccessible = true }.set(renderer, counter.cache)
        }

    private fun measuredFrame(controller: TimelineViewController): Any? =
        TimelineViewController::class.java.getDeclaredField("frame").apply { isAccessible = true }.get(controller)

    private fun pixels(
        controller: TimelineViewController,
        width: Int,
        height: Int,
    ): IntArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.WHITE)
        controller.draw(Canvas(bitmap))
        return IntArray(width * height).also {
            bitmap.getPixels(it, 0, width, 0, 0, width, height)
            bitmap.recycle()
        }
    }

    @Test
    fun `built in progress updates reuse text and match full layout paths targets and rendering`() {
        context.applicationInfo.flags = context.applicationInfo.flags or android.content.pm.ApplicationInfo.FLAG_SUPPORTS_RTL
        TimelineMathStrategy.entries.forEach { strategy ->
            val policies = if (strategy == TimelineMathStrategy.LinearHorizontal) TimelineMathConfig.HorizontalLayout.entries else listOf(TimelineMathConfig.HorizontalLayout.FIT)
            policies.forEach { policy ->
                TimelineMathConfig.ProgressMode.entries.forEach { mode ->
                    assertProgressReuse(strategy, policy, mode)
                }
            }
        }
    }

    private fun assertProgressReuse(
        strategy: TimelineMathStrategy,
        policy: TimelineMathConfig.HorizontalLayout,
        mode: TimelineMathConfig.ProgressMode,
    ) {
        listOf(View.LAYOUT_DIRECTION_LTR, View.LAYOUT_DIRECTION_RTL).forEach { direction ->
            val owner = View(context).apply { layoutDirection = direction }
            val controller = TimelineViewController(owner, context, null)
            val reference = TimelineViewController(View(context).apply { layoutDirection = direction }, context, null)
            val steps = List(4) { index -> TimelineStepData(id = "$index", title = "Event $index", description = "Long description that wraps across lines. ".repeat(3), timestampMillis = index * 60_000L, progress = 0, progressAnimation = TimelineLottieSpec(R.raw.timeline_test_animation, scale = index + 1f)) }
            val config = TimelineMathConfig(steps = steps, horizontalLayout = policy, progressMode = mode, minCellWidth = 120f)
            val renderer = BaseTimelineUi(TimelineUiConfig())
            val builds = countText(renderer)
            controller.setStrategies(TimelineMathFactory.create(strategy, config), renderer)
            val delegate = TimelineMathFactory.create(strategy, config)
            reference.setStrategies(object : TimelineMathEngine by delegate {}, BaseTimelineUi(TimelineUiConfig()))
            try {
                controller.measure(320)
                builds.reset()
                controller.measure(320)
                assertEquals(0, builds.count)
                listOf(listOf(100, 0, 50, 20), listOf(100, 100, 50, 100), List(4) { 100 }, List(4) { 0 }).forEach { progress ->
                    val changed = steps.mapIndexed { index, step -> step.copy(progress = progress[index]) }
                    builds.reset()
                    controller.replaceSteps(changed)
                    reference.replaceSteps(changed)
                    assertTrue(controller.targets().isEmpty())
                    val height = controller.measure(320)
                    assertEquals("$strategy/$policy/$mode", 0, builds.count)
                    assertEquals(reference.measure(320), height)
                    assertEquals(reference.targets(), controller.targets())
                    assertArrayEquals(pixels(reference, 320, height), pixels(controller, 320, height))
                }
            } finally {
                controller.release()
                reference.release()
            }
        }
    }

    @Test
    fun `width direction content and configuration changes invalidate measured text`() {
        context.applicationInfo.flags = context.applicationInfo.flags or android.content.pm.ApplicationInfo.FLAG_SUPPORTS_RTL
        val owner = View(context)
        val controller = TimelineViewController(owner, context, null)
        val renderer = BaseTimelineUi(TimelineUiConfig())
        val builds = countText(renderer)
        controller.setStrategies(LinearTimelineMath(math()), renderer)
        controller.measure(320)

        fun changed(
            update: () -> Unit,
            width: Int = 320,
        ) {
            builds.reset()
            update()
            controller.measure(width)
            assertTrue("Text must be measured after inputs change", builds.count > 0)
        }
        changed({}, 280)
        changed({ owner.layoutDirection = View.LAYOUT_DIRECTION_RTL }, 280)
        changed({ controller.replaceSteps(listOf(TimelineStepData(id = "a", title = "Changed", progress = 40))) }, 280)
        changed({ controller.setConfig(controller.getConfig().math, TimelineUiConfig(textSizes = TimelineUiConfig.TextSizes(28f, 24f))) }, 280)
        changed({ controller.replaceSteps(controller.getConfig().math.steps.map { it.copy(id = "renamed") }) }, 280)
        changed({ controller.replaceSteps(controller.getConfig().math.steps.map { it.copy(timestampMillis = 1000) }) }, 280)
        changed({ controller.replaceSteps(controller.getConfig().math.steps.map { it.copy(badgeAnimation = TimelineLottieSpec(R.raw.timeline_test_animation, scale = 2f)) }) }, 280)
        // Installed mutable text is snapshotted by the frame; mutations must not yield a stale reuse.
        val title = StringBuilder("Original")
        changed({ controller.replaceSteps(listOf(TimelineStepData(id = "a", title = title, progress = 40))) }, 280)
        changed({
            title.append(" updated")
            controller.replaceSteps(controller.getConfig().math.steps)
        }, 280)
    }

    @Test
    fun `resource configuration changes invalidate measurements and localized targets`() {
        val resources = android.content.res.Configuration(context.resources.configuration).apply { setLocale(java.util.Locale.US) }
        val localized = context.createConfigurationContext(resources)
        val controller = TimelineViewController(View(localized), localized, null)
        val renderer = BaseTimelineUi(TimelineUiConfig())
        val builds = countText(renderer)
        controller.setStrategies(LinearTimelineMath(math()), renderer)
        controller.measure(320)
        val description = requireNotNull(TimelineTargets.target(controller.targets(), 0)).description
        builds.reset()
        val russian = android.content.res.Configuration(resources).apply { setLocale(java.util.Locale("ru")) }
        @Suppress("DEPRECATION")
        localized.resources.updateConfiguration(russian, localized.resources.displayMetrics)
        controller.measure(320)
        assertTrue(builds.count > 0)
        val updated = requireNotNull(TimelineTargets.target(controller.targets(), 0)).description
        assertTrue(description != updated)
        assertTrue(updated.contains(localized.getString(R.string.timeline_percent, 40)))
    }

    @Test
    fun `custom engines retain full measurement callbacks on identical measures`() {
        val delegate = LinearTimelineMath(math())
        var layouts = 0
        val engine =
            object : TimelineMathEngine by delegate {
                override fun buildLayout(): com.dmitrypokrasov.timelineview.math.data.TimelineLayout {
                    layouts++
                    return delegate.buildLayout()
                }
            }
        val controller = controller()
        val renderer = BaseTimelineUi(TimelineUiConfig())
        val builds = countText(renderer)
        controller.setStrategies(engine, renderer)
        controller.measure(320)
        val before = layouts
        builds.reset()
        controller.measure(320)
        assertTrue(layouts > before)
        assertTrue(builds.count > 0)
    }

    @Test
    fun `custom renderers may change their measurement without a configuration update`() {
        val controller = controller()
        var textHeight = 12
        val renderer =
            object : BaseTimelineUi(TimelineUiConfig()) {
                override fun measureTitleHeight(
                    title: CharSequence,
                    maxWidth: Int,
                    align: Paint.Align,
                ): Int = textHeight
            }
        controller.setStrategies(LinearTimelineMath(math()), renderer)
        val before = controller.measure(320)
        textHeight = 240
        assertTrue(controller.measure(320) > before)
    }

    @Test
    fun `indexed targets remain defensive and use live listener and current index`() {
        val controller = controller()
        val steps = List(3) { TimelineStepData(id = "$it", title = "Step $it", progress = 100) }
        controller.setMathEngine(LinearTimelineMath(math().copy(steps = steps)))
        controller.measure(320)
        val first = requireNotNull(TimelineTargets.target(controller.targets(), 0))
        first.bounds.setEmpty()
        assertFalse(requireNotNull(TimelineTargets.target(controller.targets(), 0)).bounds.isEmpty)
        (TimelineTargets.ids(controller.targets()) as MutableSet<Int>).clear()
        assertEquals(3, TimelineTargets.ids(controller.targets()).size)
        val bounds = requireNotNull(TimelineTargets.target(controller.targets(), 0)).bounds
        assertEquals(null, controller.targetAt(bounds.centerX(), bounds.centerY()))
        var clicked = ""
        controller.setOnStepClickListener { index, step -> clicked = "$index:${step.id}" }
        assertTrue(requireNotNull(TimelineTargets.target(controller.targets(), 0)).clickable)
        assertEquals(0, controller.targetAt(bounds.centerX(), bounds.centerY()))
        controller.replaceSteps(steps.reversed())
        controller.measure(320)
        assertTrue(controller.clickTarget(0))
        assertEquals("2:0", clicked)
        controller.setOnStepClickListener(null)
        assertFalse(requireNotNull(TimelineTargets.target(controller.targets(), 0)).clickable)
        assertFalse(controller.clickTarget(0))
        controller.replaceSteps(steps.drop(1))
        controller.measure(320)
        assertEquals(null, TimelineTargets.target(controller.targets(), 0))
        assertFalse(controller.clickTarget(0))
    }

    @Test
    fun `full declarative config replaces direct selection and rolls back on failure`() {
        val engine = LinearTimelineMath(math())
        val controller = controller()
        controller.setMathEngine(engine)
        val replacement = TimelineConfig(math(), TimelineUiConfig(), TimelineMathStrategy.LinearHorizontal, TimelineUiStrategy.Linear)
        controller.setConfig(replacement)
        assertEquals(replacement, controller.getConfig())
        controller.measure(320)
        val before = controller.targets()
        assertThrows(IllegalArgumentException::class.java) { controller.setConfig(replacement.copy(mathStrategy = TimelineMathStrategy.TimeScaled)) }
        assertEquals(replacement, controller.getConfig())
        assertEquals(before, controller.targets())
        controller.replaceSteps(listOf(TimelineStepData(id = "replacement", progress = 0)))
        assertEquals("a", engine.getSteps().single().id)
        assertEquals("replacement", controller.getConfig().math.steps.single().id)
    }

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
