package com.dmitrypokrasov.timeline

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dmitrypokrasov.timelineview.model.TimelineStep
import com.dmitrypokrasov.timelineview.config.DefaultTimelineMathConfig
import com.dmitrypokrasov.timelineview.config.DefaultTimelineUiConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.strategy.snake.SnakeTimelineMath
import com.dmitrypokrasov.timelineview.strategy.snake.SnakeTimelineUi
import com.dmitrypokrasov.timelineview.ui.TimelineView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val timeLineView = findViewById<TimelineView>(R.id.timeline)

        val configMath = DefaultTimelineMathConfig(
            steps = ArrayList(
                listOf(
                    TimelineStep(
                        title = R.string.title_1_lvl,
                        description = R.string.description_1_9_steps,
                        icon = R.drawable.ic_active,
                        count = 5,
                        maxCount = 9
                    ), TimelineStep(
                        title = R.string.title_2_lvl,
                        description = R.string.description_10_99_steps,
                        icon = R.drawable.ic_active,
                        count = 0,
                        maxCount = 99
                    ), TimelineStep(
                        title = R.string.title_3_lvl,
                        description = R.string.description_100_999_steps,
                        icon = R.drawable.ic_active,
                        count = 0,
                        maxCount = 999
                    ), TimelineStep(
                        title = R.string.title_4_lvl,
                        description = R.string.description_1000_9999_steps,
                        icon = R.drawable.ic_active,
                        count = 0,
                        maxCount = 9999
                    ), TimelineStep(
                        title = R.string.title_5_lvl,
                        description = R.string.description_10000_99999_steps,
                        icon = R.drawable.ic_unactive,
                        count = 0,
                        maxCount = 99999
                    )
                )
            ),
            stepY = resources.getDimension(R.dimen.dimen_80dp),
            startPosition = TimelineMathConfig.StartPosition.START,
            marginHorizontalStroke = resources.getDimension(R.dimen.dimen_40dp),
            marginHorizontalText = resources.getDimension(R.dimen.dimen_80dp),
            marginHorizontalImage = resources.getDimension(R.dimen.dimen_16dp),
            marginTopTitle = resources.getDimension(R.dimen.dimen_52dp),
            marginTopDescription = resources.getDimension(R.dimen.dimen_16dp),
            marginTopProgressIcon = resources.getDimension(R.dimen.dimen_6dp),
            stepYFirst = resources.getDimension(R.dimen.dimen_20dp),
            sizeImageLvl = resources.getDimension(R.dimen.dimen_48dp),
            sizeIconProgress = resources.getDimension(R.dimen.dimen_28dp),
        )

        val configUi = DefaultTimelineUiConfig(
            iconProgress = R.drawable.ic_progress_timeline,
            iconDisableLvl = R.drawable.ic_unactive,
            colorTitle = ContextCompat.getColor(baseContext, R.color.black),
            sizeTitle = resources.getDimension(R.dimen.dimen_12sp),
            sizeDescription = resources.getDimension(R.dimen.dimen_12sp),
            sizeStroke = resources.getDimension(R.dimen.dimen_6dp),
            colorDescription = ContextCompat.getColor(baseContext, R.color.black),
            radius = resources.getDimension(R.dimen.dimen_48dp),
            colorStroke = ContextCompat.getColor(baseContext, R.color.teal_700),
            colorProgress = ContextCompat.getColor(baseContext, R.color.teal_200),
        )

        val mathEngine = SnakeTimelineMath(configMath)
        val uiRenderer = SnakeTimelineUi(configUi)

        timeLineView.setMathEngine(mathEngine)
        timeLineView.setUiRenderer(uiRenderer)
    }
}
