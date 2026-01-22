package com.dmitrypokrasov.timelineview.strategy

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.math.TimelineLayout
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.math.TimelineLayoutStep
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TimelineStrategyRegistryIsolationTest {
    @Test
    fun `registries with same key return different math engines`() {
        val key = StrategyKey("custom_math")
        val registryA = TimelineStrategyRegistryImpl(registerDefaults = false)
        val registryB = TimelineStrategyRegistryImpl(registerDefaults = false)

        registryA.registerMath(FakeMathProvider(key, "A"))
        registryB.registerMath(FakeMathProvider(key, "B"))

        val mathA = registryA.getMathProvider(key)!!.create(TimelineMathConfig())
        val mathB = registryB.getMathProvider(key)!!.create(TimelineMathConfig())

        assertNotEquals(mathA, mathB)
        assertEquals("A", (mathA as FakeMathEngine).id)
        assertEquals("B", (mathB as FakeMathEngine).id)
    }

    @Test
    fun `registries with same key return different ui renderers`() {
        val key = StrategyKey("custom_ui")
        val registryA = TimelineStrategyRegistryImpl(registerDefaults = false)
        val registryB = TimelineStrategyRegistryImpl(registerDefaults = false)

        registryA.registerUi(FakeUiProvider(key, "A"))
        registryB.registerUi(FakeUiProvider(key, "B"))

        val uiA = registryA.getUiProvider(key)!!.create(TimelineUiConfig())
        val uiB = registryB.getUiProvider(key)!!.create(TimelineUiConfig())

        assertNotEquals(uiA, uiB)
        assertEquals("A", (uiA as FakeUiRenderer).id)
        assertEquals("B", (uiB as FakeUiRenderer).id)
    }
}

private class FakeMathProvider(
    override val key: StrategyKey,
    private val id: String
) : TimelineMathProvider {
    override fun create(config: TimelineMathConfig): TimelineMathEngine = FakeMathEngine(id, config)
}

private class FakeUiProvider(
    override val key: StrategyKey,
    private val id: String
) : TimelineUiProvider {
    override fun create(config: TimelineUiConfig): TimelineUiRenderer = FakeUiRenderer(id, config)
}

private class FakeMathEngine(
    val id: String,
    private var config: TimelineMathConfig
) : TimelineMathEngine {
    override fun setConfig(config: TimelineMathConfig) {
        this.config = config
    }

    override fun getConfig(): TimelineMathConfig = config

    override fun replaceSteps(steps: List<TimelineStepData>) {
        config = config.copy(steps = steps)
    }

    override fun buildPath(pathEnable: Path, pathDisable: Path) {
        pathEnable.reset()
        pathDisable.reset()
    }

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

    override fun buildLayout(): TimelineLayout = TimelineLayout(
        steps = emptyList<TimelineLayoutStep>(),
        progressIcon = null
    )
}

private class FakeUiRenderer(
    val id: String,
    private var config: TimelineUiConfig
) : TimelineUiRenderer {
    private val completedPath = Path()
    private val remainingPath = Path()

    override fun setConfig(config: TimelineUiConfig) {
        this.config = config
    }

    override fun getConfig(): TimelineUiConfig = config

    override fun initTools(timelineMathConfig: TimelineMathConfig, context: Context) = Unit

    override fun prepareStrokePaint() = Unit

    override fun prepareTextPaint() = Unit

    override fun prepareIconPaint() = Unit

    override fun drawProgressIcon(canvas: Canvas, leftCoordinates: Float, topCoordinates: Float) = Unit

    override fun drawCompletedPath(canvas: Canvas) = Unit

    override fun drawRemainingPath(canvas: Canvas) = Unit

    override fun getCompletedPath(): Path = completedPath

    override fun getRemainingPath(): Path = remainingPath

    override fun drawTitle(canvas: Canvas, title: CharSequence, x: Float, y: Float, align: Paint.Align) = Unit

    override fun drawDescription(
        canvas: Canvas,
        description: CharSequence,
        x: Float,
        y: Float,
        align: Paint.Align
    ) = Unit

    override fun drawStepIcon(
        step: TimelineStepData,
        canvas: Canvas,
        align: Paint.Align,
        context: Context,
        x: Float,
        y: Float
    ) = Unit

    override fun getTextAlignment(): Paint.Align = Paint.Align.LEFT
}
