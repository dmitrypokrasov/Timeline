package com.dmitrypokrasov.timeline

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dmitrypokrasov.timelineview.TimelineConfig
import com.dmitrypokrasov.timelineview.TimelineStep
import com.dmitrypokrasov.timelineview.TimelineView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val timeLineView = findViewById<TimelineView>(R.id.timeline)
        val config = TimelineConfig.Builder()
            .setSteps(
                ArrayList(
                    listOf(
                        TimelineStep(
                            title = R.string.title_1_lvl,
                            description = R.string.description_1_9_steps,
                            icon = R.drawable.ic_active,
                            count = 3,
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
                )
            )
            .setRadius(resources.getDimension(R.dimen.dimen_48dp))
            .setStepY(resources.getDimension(R.dimen.dimen_80dp))
            .setSizeTitle(resources.getDimension(R.dimen.dimen_12sp))
            .setSizeDescription(resources.getDimension(R.dimen.dimen_12sp))
            .setSizeStroke(resources.getDimension(R.dimen.dimen_6dp))
            .setStartPosition(TimelineConfig.StartPosition.CENTER)
            .setIconProgress(R.drawable.ic_progress_timeline)
            .setIconDisableLvl(R.drawable.ic_unactive)
            .setMarginHorizontalStroke(resources.getDimension(R.dimen.dimen_40dp))
            .setMarginHorizontalText(resources.getDimension(R.dimen.dimen_80dp))
            .setMarginHorizontalImage(resources.getDimension(R.dimen.dimen_16dp))
            .setMarginTopTitle(resources.getDimension(R.dimen.dimen_52dp))
            .setMarginTopDescription(resources.getDimension(R.dimen.dimen_4dp))
            .setMarginTopProgressIcon(resources.getDimension(R.dimen.dimen_6dp))
            .setStepYFirst(resources.getDimension(R.dimen.dimen_20dp))
            .setSizeImageLvl(resources.getDimension(R.dimen.dimen_48dp))
            .setSizeIconProgress(resources.getDimension(R.dimen.dimen_28dp))
            .setColorTitle(ContextCompat.getColor(baseContext, R.color.black))
            .setColorDescription(ContextCompat.getColor(baseContext, R.color.black))
            .setColorStroke(ContextCompat.getColor(baseContext, R.color.teal_700))
            .setColorProgress(ContextCompat.getColor(baseContext, R.color.teal_200))
            .build()

        timeLineView.setConfig(config)
    }
}
