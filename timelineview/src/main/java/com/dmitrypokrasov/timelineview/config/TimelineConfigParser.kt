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

        val mathConfig = DefaultTimelineMathConfig(
            startPosition = TimelineMathConfig.StartPosition.entries[typedArray.getInt(
                R.styleable.TimelineView_timeline_start_position,
                TimelineMathConfig.StartPosition.CENTER.ordinal
            )],
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
            ),
            sizeImageLvl = typedArray.getDimension(
                R.styleable.TimelineView_timeline_image_lvl_size,
                TimelineConstants.DEFAULT_IMAGE_LVL_SIZE
            ),
            sizeIconProgress = typedArray.getDimension(
                R.styleable.TimelineView_timeline_icon_progress_size,
                TimelineConstants.DEFAULT_ICON_PROGRESS_SIZE
            ),
        )

        val uiConfig = DefaultTimelineUiConfig(
            iconDisableLvl = typedArray.getResourceId(
                R.styleable.TimelineView_timeline_disable_icon,
                0
            ),
            iconProgress = typedArray.getResourceId(
                R.styleable.TimelineView_timeline_progress_icon,
                0
            ),
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
            ),
            sizeDescription = typedArray.getDimension(
                R.styleable.TimelineView_timeline_description_size,
                TimelineConstants.DEFAULT_DESCRIPTION_SIZE
            ),
            sizeTitle = typedArray.getDimension(
                R.styleable.TimelineView_timeline_title_size,
                TimelineConstants.DEFAULT_TITLE_SIZE
            ),
            radius = typedArray.getDimension(
                R.styleable.TimelineView_timeline_radius_size,
                TimelineConstants.DEFAULT_RADIUS_SIZE
            ),
            sizeStroke = typedArray.getDimension(
                R.styleable.TimelineView_timeline_stroke_size,
                TimelineConstants.DEFAULT_STROKE_SIZE
            ),
        )

        typedArray.recycle()

        return TimelineConfig(mathConfig, uiConfig)
    }
}
