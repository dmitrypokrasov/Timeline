package com.dmitrypokrasov.timelineview.ui

import android.graphics.Paint
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.view.View
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.math.LinearTimelineMath
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.render.CachingTimelineTextLayoutBuilder
import com.dmitrypokrasov.timelineview.render.LinearTimelineUi
import com.dmitrypokrasov.timelineview.strategy.TimelineStrategyRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
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
class TimelineStrategyRegressionTest {
    @Test
    fun `registry preserves direct engines until an explicit strategy selection`() {
        val context = RuntimeEnvironment.getApplication()
        val engine = LinearTimelineMath(TimelineMathConfig(steps = listOf(TimelineStepData(progress = 40))))
        val renderer = LinearTimelineUi(TimelineUiConfig())
        val state = TimelineRuntimeState.from(com.dmitrypokrasov.timelineview.config.TimelineConfig(math = TimelineMathConfig(), ui = TimelineUiConfig()))
        val resolved = state.withMathEngine(engine).withUiRenderer(renderer).resolve(com.dmitrypokrasov.timelineview.strategy.TimelineViewStrategyController(TimelineStrategyRegistry.createLocalRegistry(false)))
        assertSame(engine, resolved.math)
        assertSame(renderer, resolved.ui)
        val controller = TimelineViewController(View(context), context, null)
        controller.setStrategies(engine, renderer)
        controller.setStrategyRegistry(TimelineStrategyRegistry.createLocalRegistry(false))
        controller.replaceSteps(listOf(TimelineStepData(id = "retained", progress = 70)))
        assertEquals("retained", engine.getSteps().single().id)
        controller.setStrategy(TimelineMathStrategy.Snake, TimelineUiStrategy.Linear)
        controller.replaceSteps(emptyList())
        assertEquals(1, engine.getSteps().size)
    }

    @Test
    fun `virtual identity follows steps through reorder and clicks use current index`() {
        val context = RuntimeEnvironment.getApplication()
        val a = TimelineStepData(id = "a", title = "Same", progress = 100)
        val b = a.copy(id = "b")
        val controller = TimelineViewController(View(context), context, null)
        controller.setMathEngine(LinearTimelineMath(TimelineMathConfig(steps = listOf(a, b))))
        var clicked = -1
        controller.setOnStepClickListener { index, _ -> clicked = index }
        controller.measure(320)
        val id = controller.targets()[0].id
        controller.replaceSteps(listOf(b, a.copy(progress = 50)))
        controller.measure(320)
        assertEquals(id, controller.targets()[1].id)
        assertTrue(controller.clickTarget(id))
        assertEquals(1, clicked)
        controller.replaceSteps(listOf(b))
        controller.measure(320)
        assertFalse(controller.clickTarget(id))
    }

    @Test
    fun `styled text retains spans in measurement and cache invalidation`() {
        val cache = CachingTimelineTextLayoutBuilder()
        val styled = SpannableString("Large styled text")

        fun measure(text: CharSequence) = cache.build(text, 16f, Typeface.DEFAULT, 0, Paint.Align.LEFT, 120).height
        val plain = measure(styled)
        styled.setSpan(RelativeSizeSpan(3f), 0, styled.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        assertTrue(measure(styled) > plain)
        styled.removeSpan(styled.getSpans(0, styled.length, RelativeSizeSpan::class.java).single())
        assertEquals(plain, measure(styled))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `duplicate stable identities are rejected`() {
        TimelineMathConfig(steps = List(2) { TimelineStepData(id = "same", progress = 0) })
    }

    @Test
    fun `XML parses adaptive policies and density aware cell width`() {
        val context = RuntimeEnvironment.getApplication()
        val attrs =
            org.robolectric.Robolectric.buildAttributeSet()
                .addAttribute(com.dmitrypokrasov.timelineview.R.attr.timeline_horizontal_layout, "1")
                .addAttribute(com.dmitrypokrasov.timelineview.R.attr.timeline_progress_mode, "1")
                .addAttribute(com.dmitrypokrasov.timelineview.R.attr.timeline_min_cell_width, "120dp")
                .build()
        val config = com.dmitrypokrasov.timelineview.config.TimelineConfigParser(context).parse(attrs).math
        assertEquals(TimelineMathConfig.HorizontalLayout.WRAP, config.horizontalLayout)
        assertEquals(TimelineMathConfig.ProgressMode.INDEPENDENT, config.progressMode)
        assertEquals(120f * context.resources.displayMetrics.density, config.minCellWidth, 0.01f)
    }

    @Test
    fun `scroll mode exposes intrinsic width to an unconstrained parent`() {
        val context = RuntimeEnvironment.getApplication()
        val view = TimelineView(context)
        view.setMathEngine(LinearTimelineMath(TimelineMathConfig(steps = List(10) { TimelineStepData(progress = 0) }, horizontalLayout = TimelineMathConfig.HorizontalLayout.SCROLL, minCellWidth = 160f), LinearTimelineMath.Orientation.HORIZONTAL))
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        assertTrue(view.measuredWidth >= 1600)
    }
}
