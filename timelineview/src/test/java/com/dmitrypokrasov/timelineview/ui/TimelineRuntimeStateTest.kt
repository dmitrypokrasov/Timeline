package com.dmitrypokrasov.timelineview.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer
import com.dmitrypokrasov.timelineview.strategy.TimelineMathProvider
import com.dmitrypokrasov.timelineview.strategy.TimelineStrategyRegistryImpl
import com.dmitrypokrasov.timelineview.strategy.TimelineUiProvider
import com.dmitrypokrasov.timelineview.strategy.TimelineViewStrategyController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimelineRuntimeStateTest {
    @Test
    fun `strategy resolution keeps replaced steps and configs`() {
        val steps = listOf(TimelineStepData(title = "Updated", progress = 25))
        val mathConfig =
            TimelineMathConfig(
                steps = emptyList(),
                spacing = TimelineMathConfig.Spacing(stepY = 123f),
            )
        val uiConfig =
            TimelineUiConfig(
                colors = TimelineUiConfig.Colors(colorTitle = 42),
            )
        val registry =
            TimelineStrategyRegistryImpl(registerDefaults = false).apply {
                registerMath(
                    object : TimelineMathProvider {
                        override val key: StrategyKey = StrategyKey("custom_math")

                        override fun create(config: TimelineMathConfig): TimelineMathEngine =
                            CaptureMathEngine(
                                config,
                            )
                    },
                )
                registerUi(
                    object : TimelineUiProvider {
                        override val key: StrategyKey = StrategyKey("custom_ui")

                        override fun create(config: TimelineUiConfig): TimelineUiRenderer =
                            CaptureUiRenderer(
                                config,
                            )
                    },
                )
            }
        val state =
            TimelineRuntimeState.from(
                TimelineConfig(
                    math = mathConfig,
                    ui = uiConfig,
                    mathStrategy = TimelineMathStrategy.Snake,
                    uiStrategy = TimelineUiStrategy.Linear,
                ),
            ).withSteps(
                steps,
            ).withStrategyKeys(StrategyKey("custom_math"), StrategyKey("custom_ui"))

        val resolved = state.resolve(TimelineViewStrategyController(registry))

        assertEquals(steps, resolved.math.getConfig().steps)
        assertEquals(123f, resolved.math.getConfig().spacing.stepY, 0.01f)
        assertEquals(42, resolved.ui.getConfig().colors.colorTitle)
    }

    @Test
    fun `setting built in strategy clears custom keys`() {
        val state =
            TimelineRuntimeState.from(
                TimelineConfig(
                    math = TimelineMathConfig(),
                    ui = TimelineUiConfig(),
                    mathStrategyKey = StrategyKey("custom_math"),
                    uiStrategyKey = StrategyKey("custom_ui"),
                ),
            )

        val updated =
            state.withStrategy(
                com.dmitrypokrasov.timelineview.config.TimelineStrategy(
                    math = TimelineMathStrategy.LinearVertical,
                    ui = TimelineUiStrategy.Snake,
                ),
            )

        assertNull(updated.mathStrategyKey)
        assertNull(updated.uiStrategyKey)
        assertEquals(TimelineMathStrategy.LinearVertical, updated.mathStrategy)
        assertEquals(TimelineUiStrategy.Snake, updated.uiStrategy)
    }

    @Test
    fun `with config replaces stored config and strategy keys`() {
        val replacement =
            TimelineConfig(
                math = TimelineMathConfig(steps = listOf(TimelineStepData(title = "A", progress = 30))),
                ui = TimelineUiConfig(colors = TimelineUiConfig.Colors(colorProgress = 9)),
                mathStrategy = TimelineMathStrategy.LinearHorizontal,
                uiStrategy = TimelineUiStrategy.Linear,
                mathStrategyKey = StrategyKey("math_key"),
                uiStrategyKey = StrategyKey("ui_key"),
            )

        val updated =
            TimelineRuntimeState.from(
                TimelineConfig(
                    math = TimelineMathConfig(),
                    ui = TimelineUiConfig(),
                ),
            ).withConfig(replacement)

        assertEquals(replacement, updated.config)
        assertEquals(TimelineMathStrategy.LinearHorizontal, updated.mathStrategy)
        assertEquals(TimelineUiStrategy.Linear, updated.uiStrategy)
        assertEquals(StrategyKey("math_key"), updated.mathStrategyKey)
        assertEquals(StrategyKey("ui_key"), updated.uiStrategyKey)
    }
}

private class CaptureMathEngine(
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

private class CaptureUiRenderer(
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
