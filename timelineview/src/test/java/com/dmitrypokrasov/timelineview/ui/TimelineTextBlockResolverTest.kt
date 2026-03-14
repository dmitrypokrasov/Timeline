package com.dmitrypokrasov.timelineview.ui

import android.graphics.Paint
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.math.LinearTimelineMath
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.math.data.TimelineLayoutStep
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import org.junit.Assert.assertEquals
import org.junit.Test

class TimelineTextBlockResolverTest {
    @Test
    fun `linear vertical text uses baseline offsets without artificial lift`() {
        val layout =
            TimelineLayout(
                steps =
                    listOf(
                        TimelineLayoutStep(
                            step =
                                TimelineStepData(
                                    title = "Title",
                                    description = "Description",
                                    progress = 0,
                                ),
                            titleX = 0f,
                            titleY = 40f,
                            titleWidth = 100,
                            descriptionX = 0f,
                            descriptionY = 60f,
                            descriptionWidth = 100,
                            iconX = 0f,
                            iconY = 20f,
                            textAlign = Paint.Align.LEFT,
                        ),
                    ),
                progressIcon = null,
                progressStepIndex = null,
            )
        val mathEngine =
            LinearTimelineMath(
                TimelineMathConfig(
                    spacing = TimelineMathConfig.Spacing(stepYFirst = 200f),
                ),
                LinearTimelineMath.Orientation.VERTICAL,
            )
        val renderer =
            FakeMeasuringRenderer(
                titleHeight = 12,
                descriptionHeight = 14,
                titleBaselineOffset = 10f,
                descriptionBaselineOffset = 10f,
            )

        val blocks =
            TimelineTextBlockResolver.resolve(
                layout = layout,
                mathEngine = mathEngine,
                uiRenderer = renderer,
            )

        assertEquals(30f, blocks.single().titleTop, 0.01f)
        assertEquals(50f, blocks.single().descriptionTop, 0.01f)
    }
}
