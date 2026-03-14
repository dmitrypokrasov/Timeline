package com.dmitrypokrasov.timelineview.strategy

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer
import org.junit.Assert.assertEquals
import org.junit.Test

class TimelineStrategyResolverTest {
    @Test
    fun `built in math strategies resolve through registry providers`() {
        val registry = TimelineStrategyRegistryImpl(registerDefaults = false)
        registry.registerMath(
            ResolverFakeMathProvider(TimelineMathStrategy.Snake.key, "registry_snake"),
        )

        val engine =
            TimelineStrategyResolver(registry).resolveMath(
                TimelineMathStrategy.Snake,
                TimelineMathConfig(),
            )

        assertEquals("registry_snake", (engine as ResolverFakeMathEngine).id)
    }

    @Test
    fun `missing custom key falls back to built in registry provider`() {
        val registry =
            TimelineStrategyRegistryImpl(registerDefaults = false).apply {
                registerMath(
                    ResolverFakeMathProvider(TimelineMathStrategy.LinearVertical.key, "registry_linear"),
                )
            }

        val engine =
            TimelineStrategyResolver(registry).resolveMath(
                strategyKey = StrategyKey("missing_math"),
                fallbackStrategy = TimelineMathStrategy.LinearVertical,
                config = TimelineMathConfig(),
            )

        assertEquals("registry_linear", (engine as ResolverFakeMathEngine).id)
    }

    @Test
    fun `built in ui strategies resolve through registry providers`() {
        val registry = TimelineStrategyRegistryImpl(registerDefaults = false)
        registry.registerUi(
            ResolverFakeUiProvider(TimelineUiStrategy.Linear.key, "registry_linear_ui"),
        )

        val renderer =
            TimelineStrategyResolver(registry).resolveUi(
                TimelineUiStrategy.Linear,
                TimelineUiConfig(),
            )

        assertEquals("registry_linear_ui", (renderer as ResolverFakeUiRenderer).id)
    }
}

private class ResolverFakeMathProvider(
    override val key: StrategyKey,
    private val id: String,
) : TimelineMathProvider {
    override fun create(config: TimelineMathConfig): TimelineMathEngine =
        ResolverFakeMathEngine(id, config)
}

private class ResolverFakeUiProvider(
    override val key: StrategyKey,
    private val id: String,
) : TimelineUiProvider {
    override fun create(config: TimelineUiConfig): TimelineUiRenderer =
        ResolverFakeUiRenderer(id, config)
}

private class ResolverFakeMathEngine(
    val id: String,
    private var config: TimelineMathConfig,
) : TimelineMathEngine {
    override fun setConfig(config: TimelineMathConfig) {
        this.config = config
    }

    override fun getConfig(): TimelineMathConfig = config

    override fun replaceSteps(steps: List<TimelineStepData>) {
        config = config.copy(steps = steps)
    }

    override fun buildPath(
        pathEnable: Path,
        pathDisable: Path,
    ) = Unit

    override fun getStartPosition(): Float = 0f

    override fun setMeasuredWidth(measuredWidth: Int) = Unit

    override fun getHorizontalIconOffset(i: Int): Float = 0f

    override fun getVerticalOffset(i: Int): Float = 0f

    override fun getSteps(): List<TimelineStepData> = config.steps

    override fun getLeftCoordinates(step: TimelineStepData): Float = 0f

    override fun getTopCoordinates(step: TimelineStepData): Float = 0f

    override fun getIconYCoordinates(i: Int): Float = 0f

    override fun getTitleXCoordinates(align: Paint.Align): Float = 0f

    override fun getIconXCoordinates(align: Paint.Align): Float = 0f

    override fun getTitleYCoordinates(i: Int): Float = 0f

    override fun getDescriptionYCoordinates(i: Int): Float = 0f

    override fun buildLayout(): TimelineLayout = TimelineLayout(emptyList(), null, null)
}

private class ResolverFakeUiRenderer(
    val id: String,
    private var config: TimelineUiConfig,
) : TimelineUiRenderer {
    override fun setConfig(config: TimelineUiConfig) {
        this.config = config
    }

    override fun getConfig(): TimelineUiConfig = config

    override fun initTools(
        timelineMathConfig: TimelineMathConfig,
        context: Context,
    ) = Unit

    override fun prepareStrokePaint() = Unit

    override fun prepareTextPaint() = Unit

    override fun prepareIconPaint() = Unit

    override fun drawProgressIcon(
        canvas: Canvas,
        leftCoordinates: Float,
        topCoordinates: Float,
    ) = Unit

    override fun drawCompletedPath(canvas: Canvas) = Unit

    override fun drawRemainingPath(canvas: Canvas) = Unit

    override fun getCompletedPath(): Path = Path()

    override fun getRemainingPath(): Path = Path()

    override fun drawTitle(
        canvas: Canvas,
        title: CharSequence,
        x: Float,
        y: Float,
        align: Paint.Align,
        maxWidth: Int,
    ) = Unit

    override fun drawDescription(
        canvas: Canvas,
        description: CharSequence,
        x: Float,
        y: Float,
        align: Paint.Align,
        maxWidth: Int,
    ) = Unit

    override fun measureTitleHeight(
        title: CharSequence,
        maxWidth: Int,
        align: Paint.Align,
    ): Int = 0

    override fun measureDescriptionHeight(
        description: CharSequence,
        maxWidth: Int,
        align: Paint.Align,
    ): Int = 0

    override fun getTitleBaselineOffset(): Float = 0f

    override fun getDescriptionBaselineOffset(): Float = 0f

    override fun drawStepIcon(
        step: TimelineStepData,
        canvas: Canvas,
        align: Paint.Align,
        context: Context,
        x: Float,
        y: Float,
    ) = Unit

    override fun getTextAlignment(): Paint.Align = Paint.Align.LEFT
}
