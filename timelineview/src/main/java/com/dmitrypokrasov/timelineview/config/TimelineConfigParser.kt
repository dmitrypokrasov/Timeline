package com.dmitrypokrasov.timelineview.config

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.dmitrypokrasov.timelineview.R
import com.dmitrypokrasov.timelineview.model.TimelineConstants

/**
 * Utility class for parsing view attributes into a [TimelineConfig].
 */
class TimelineConfigParser(private val context: Context) {

    @SuppressLint("CustomViewStyleable")
    fun parse(attrs: AttributeSet?): TimelineConfig {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TimelineView)

        val mathConfig = TimelineMathConfig(
            startPosition = TimelineMathConfig.StartPosition.entries[typedArray.getInt(
                R.styleable.TimelineView_timeline_start_position,
                TimelineMathConfig.StartPosition.CENTER.ordinal
            )],
            spacing = TimelineMathConfig.Spacing(
                stepY = typedArray.getDimension(
                    R.styleable.TimelineView_timeline_step_y_size,
                    TimelineConstants.DEFAULT_STEP_Y_SIZE
                ),
                stepYFirst = typedArray.getDimension(
                    R.styleable.TimelineView_timeline_step_y_first_size,
                    TimelineConstants.DEFAULT_STEP_Y_FIRST_SIZE
                ),
                marginTopDescription = typedArray.getDimension(
                    R.styleable.TimelineView_timeline_margin_top_description,
                    TimelineConstants.DEFAULT_MARGIN_TOP_DESCRIPTION
                ),
                marginTopTitle = typedArray.getDimension(
                    R.styleable.TimelineView_timeline_margin_top_title,
                    TimelineConstants.DEFAULT_MARGIN_TOP_TITLE
                ),
                marginTopProgressIcon = typedArray.getDimension(
                    R.styleable.TimelineView_timeline_margin_top_progress_icon,
                    TimelineConstants.DEFAULT_MARGIN_TOP_PROGRESS_ICON
                ),
                marginHorizontalImage = typedArray.getDimension(
                    R.styleable.TimelineView_timeline_margin_horizontal_image,
                    TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_IMAGE
                ),
                marginHorizontalText = typedArray.getDimension(
                    R.styleable.TimelineView_timeline_margin_horizontal_text,
                    TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_TEXT
                ),
                marginHorizontalStroke = typedArray.getDimension(
                    R.styleable.TimelineView_timeline_margin_horizontal_stroke,
                    TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_STROKE
                )
            ),
            sizes = TimelineMathConfig.Sizes(
                sizeImageLvl = typedArray.getDimension(
                    R.styleable.TimelineView_timeline_image_lvl_size,
                    TimelineConstants.DEFAULT_IMAGE_LVL_SIZE
                ),
                sizeIconProgress = typedArray.getDimension(
                    R.styleable.TimelineView_timeline_icon_progress_size,
                    TimelineConstants.DEFAULT_ICON_PROGRESS_SIZE
                )
            )
        )

        val uiConfig = TimelineUiConfig(
            icons = TimelineUiConfig.Icons(
                iconDisableLvl = typedArray.getResourceId(
                    R.styleable.TimelineView_timeline_disable_icon,
                    0
                ),
                iconProgress = typedArray.getResourceId(
                    R.styleable.TimelineView_timeline_progress_icon,
                    0
                )
            ),
            colors = TimelineUiConfig.Colors(
                colorProgress = typedArray.getColor(
                    R.styleable.TimelineView_timeline_progress_color,
                    ContextCompat.getColor(context, TimelineConstants.DEFAULT_PROGRESS_COLOR)
                ),
                colorStroke = typedArray.getColor(
                    R.styleable.TimelineView_timeline_stroke_color,
                    ContextCompat.getColor(context, TimelineConstants.DEFAULT_STROKE_COLOR)
                ),
                colorTitle = typedArray.getColor(
                    R.styleable.TimelineView_timeline_title_color,
                    ContextCompat.getColor(context, TimelineConstants.DEFAULT_TITLE_COLOR)
                ),
                colorDescription = typedArray.getColor(
                    R.styleable.TimelineView_timeline_description_color,
                    ContextCompat.getColor(context, TimelineConstants.DEFAULT_DESCRIPTION_COLOR)
                )
            ),
            textSizes = TimelineUiConfig.TextSizes(
                sizeDescription = typedArray.getDimension(
                    R.styleable.TimelineView_timeline_description_size,
                    TimelineConstants.DEFAULT_DESCRIPTION_SIZE
                ),
                sizeTitle = typedArray.getDimension(
                    R.styleable.TimelineView_timeline_title_size,
                    TimelineConstants.DEFAULT_TITLE_SIZE
                )
            ),
            stroke = TimelineUiConfig.Stroke(
                radius = typedArray.getDimension(
                    R.styleable.TimelineView_timeline_radius_size,
                    TimelineConstants.DEFAULT_RADIUS_SIZE
                ),
                sizeStroke = typedArray.getDimension(
                    R.styleable.TimelineView_timeline_stroke_size,
                    TimelineConstants.DEFAULT_STROKE_SIZE
                )
            )
        )

        val mathStrategyKey = typedArray.getString(
            R.styleable.TimelineView_timeline_math_strategy_id
        )?.trim()?.takeIf { it.isNotEmpty() }?.let { StrategyKey(it) }
        val uiStrategyKey = typedArray.getString(
            R.styleable.TimelineView_timeline_ui_strategy_id
        )?.trim()?.takeIf { it.isNotEmpty() }?.let { StrategyKey(it) }

        val mathStrategy = TimelineMathStrategy.fromOrdinal(typedArray.getInt(
            R.styleable.TimelineView_timeline_math_strategy,
            TimelineMathStrategy.entries.indexOf(TimelineMathStrategy.Snake)
        ))
        val uiStrategy = TimelineUiStrategy.fromOrdinal(typedArray.getInt(
            R.styleable.TimelineView_timeline_ui_strategy,
            TimelineUiStrategy.entries.indexOf(TimelineUiStrategy.Snake)
        ))

        typedArray.recycle()

        return TimelineConfig(
            math = mathConfig,
            ui = uiConfig,
            mathStrategy = mathStrategy,
            uiStrategy = uiStrategy,
            mathStrategyKey = mathStrategyKey,
            uiStrategyKey = uiStrategyKey
        )
    }
}
