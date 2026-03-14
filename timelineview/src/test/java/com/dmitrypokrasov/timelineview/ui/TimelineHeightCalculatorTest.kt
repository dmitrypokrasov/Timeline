package com.dmitrypokrasov.timelineview.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.math.LinearTimelineMath
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.math.data.TimelineLayoutStep
import com.dmitrypokrasov.timelineview.math.data.TimelineProgressIcon
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimelineHeightCalculatorTest {
    @Test
    fun `height grows with measured multiline text`() {
        val calculator = TimelineHeightCalculator()
        val mathConfig =
            TimelineMathConfig(
                sizes = TimelineMathConfig.Sizes(sizeIconProgress = 20f, sizeImageLvl = 24f),
            )
        val mathEngine = CaptureMathEngineForHeight(mathConfig)
        val layout =
            TimelineLayout(
                steps =
                    listOf(
                        TimelineLayoutStep(
                            step =
                                TimelineStepData(
                                    title = "Title",
                                    description = "Description",
                                    progress = 10,
                                ),
                            titleX = 0f,
                            titleY = 20f,
                            titleWidth = 100,
                            descriptionX = 0f,
                            descriptionY = 30f,
                            descriptionWidth = 100,
                            iconX = 0f,
                            iconY = 0f,
                            textAlign = Paint.Align.LEFT,
                        ),
                    ),
                progressIcon = TimelineProgressIcon(left = 0f, top = 5f),
                progressStepIndex = 0,
            )
        val shortRenderer = FakeMeasuringRenderer(titleHeight = 10, descriptionHeight = 12)
        val longRenderer = FakeMeasuringRenderer(titleHeight = 10, descriptionHeight = 48)

        val shortHeight = calculator.calculateHeight(layout, mathEngine, shortRenderer)
        val longHeight = calculator.calculateHeight(layout, mathEngine, longRenderer)

        assertTrue(longHeight > shortHeight)
        assertEquals(82, longHeight)
    }

    @Test
    fun `linear vertical height ends at last rendered badge when text stays above it`() {
        val calculator = TimelineHeightCalculator()
        val mathConfig =
            TimelineMathConfig(
                startPosition = TimelineMathConfig.StartPosition.START,
                steps =
                    (1..6).map { index ->
                        TimelineStepData(
                            title = "Step $index",
                            description = "Description $index",
                            progress = if (index < 6) 100 else 0,
                        )
                    },
                spacing =
                    TimelineMathConfig.Spacing(
                        stepY = 320f,
                        stepYFirst = 200f,
                        marginTopTitle = 32f,
                        marginTopDescription = 12f,
                        marginTopProgressIcon = 8f,
                        marginHorizontalImage = 16f,
                        marginHorizontalText = 80f,
                        marginHorizontalStroke = 40f,
                    ),
                sizes =
                    TimelineMathConfig.Sizes(
                        sizeIconProgress = 24f,
                        sizeImageLvl = 48f,
                    ),
            )
        val mathEngine = LinearTimelineMath(mathConfig, LinearTimelineMath.Orientation.VERTICAL)
        mathEngine.setMeasuredWidth(320)

        val height =
            calculator.calculateHeight(
                layout = mathEngine.buildLayout(),
                mathEngine = mathEngine,
                uiRenderer =
                    FakeMeasuringRenderer(
                        titleHeight = 10,
                        descriptionHeight = 10,
                        titleBaselineOffset = 10f,
                        descriptionBaselineOffset = 10f,
                    ),
            )

        assertEquals(1848, height)
    }
}

internal class FakeMeasuringRenderer(
    private val titleHeight: Int,
    private val descriptionHeight: Int,
    private val titleBaselineOffset: Float = 0f,
    private val descriptionBaselineOffset: Float = 0f,
) : TimelineUiRenderer {
    override fun setConfig(config: TimelineUiConfig) = Unit

    override fun getConfig(): TimelineUiConfig = TimelineUiConfig()

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
    ): Int = if (title.isBlank()) 0 else titleHeight

    override fun measureDescriptionHeight(
        description: CharSequence,
        maxWidth: Int,
        align: Paint.Align,
    ): Int = if (description.isBlank()) 0 else descriptionHeight

    override fun getTitleBaselineOffset(): Float = titleBaselineOffset

    override fun getDescriptionBaselineOffset(): Float = descriptionBaselineOffset

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

private class CaptureMathEngineForHeight(
    private val config: TimelineMathConfig,
) : TimelineMathEngine {
    override fun setConfig(config: TimelineMathConfig) = Unit

    override fun getConfig(): TimelineMathConfig = config

    override fun replaceSteps(steps: List<TimelineStepData>) = Unit

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
