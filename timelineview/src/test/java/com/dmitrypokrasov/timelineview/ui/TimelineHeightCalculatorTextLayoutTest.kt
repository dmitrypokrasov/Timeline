package com.dmitrypokrasov.timelineview.ui

import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.math.LinearTimelineMath
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TimelineHeightCalculatorTextLayoutTest {

    private val calculator = TimelineHeightCalculator()

    @Test
    fun `long description on narrow width increases height in multiline mode`() {
        val math = createMathEngine("Очень длинное описание, которое обязано переноситься на несколько строк")
        val singleLineHeight = measure(math, TimelineUiConfig.TextMode.SINGLE_LINE)
        val multiLineHeight = measure(math, TimelineUiConfig.TextMode.MULTI_LINE)

        assertTrue(multiLineHeight > singleLineHeight)
    }

    @Test
    fun `maxLines limits multiline height`() {
        val math = createMathEngine("Повторяющийся текст для проверки лимита строк ".repeat(12))
        val noLimitHeight = measure(math, TimelineUiConfig.TextMode.MULTI_LINE, maxLines = null)
        val limitedHeight = measure(math, TimelineUiConfig.TextMode.MULTI_LINE, maxLines = 2)

        assertTrue(noLimitHeight > limitedHeight)
    }

    @Test
    fun `single line is lower than multiline for same text`() {
        val math = createMathEngine("Description Description Description Description Description")
        val singleLineHeight = measure(math, TimelineUiConfig.TextMode.SINGLE_LINE)
        val multiLineHeight = measure(math, TimelineUiConfig.TextMode.MULTI_LINE)

        assertTrue(singleLineHeight < multiLineHeight)
    }

    private fun measure(
        math: LinearTimelineMath,
        mode: TimelineUiConfig.TextMode,
        maxLines: Int? = null
    ): Int {
        val config = TimelineUiConfig(
            textSizes = TimelineUiConfig.TextSizes(sizeDescription = 24f, sizeTitle = 28f),
            textLayout = TimelineUiConfig.TextLayout(
                descriptionMode = mode,
                maxLinesDescription = maxLines,
                textMaxWidthMode = TimelineUiConfig.TextMaxWidthMode.AUTO_FROM_LAYOUT
            )
        )
        val layout = math.buildLayout()
        return calculator.calculateHeight(math, config, layout)
    }

    private fun createMathEngine(description: String): LinearTimelineMath {
        val math = LinearTimelineMath(
            TimelineMathConfig(
                startPosition = TimelineMathConfig.StartPosition.START,
                steps = listOf(
                    TimelineStepData(
                        title = "Step",
                        description = description,
                        progress = 100,
                        iconRes = null
                    )
                ),
                spacing = TimelineMathConfig.Spacing(
                    stepY = 120f,
                    stepYFirst = 60f,
                    marginTopTitle = 20f,
                    marginTopDescription = 20f,
                    marginHorizontalText = 24f,
                    marginHorizontalStroke = 16f
                )
            ),
            orientation = LinearTimelineMath.Orientation.VERTICAL
        )
        math.setMeasuredWidth(220)
        return math
    }
}
