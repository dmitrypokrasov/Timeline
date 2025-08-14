package com.dmitrypokrasov.timelineview.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.dmitrypokrasov.timelineview.R
import com.dmitrypokrasov.timelineview.data.TimelineConstants
import com.dmitrypokrasov.timelineview.domain.data.TimelineMathConfig
import com.dmitrypokrasov.timelineview.domain.data.TimelineUiConfig
import com.dmitrypokrasov.timelineview.ui.TimelineOrientation

/**
 * Utility class for parsing view attributes into math and UI configurations.
 */
class ConfigParser(private val context: Context) {

    @SuppressLint("CustomViewStyleable")
    fun parse(attrs: AttributeSet?): Triple<TimelineMathConfig, TimelineUiConfig, TimelineOrientation> {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TimelineView)

        val mathBuilder = TimelineMathConfig.Builder()
            .setStartPosition(
                TimelineMathConfig.StartPosition.entries[typedArray.getInt(
                    R.styleable.TimelineView_timeline_start_position,
                    TimelineMathConfig.StartPosition.CENTER.ordinal
                )]
            )
            .setStepY(
                typedArray.getDimension(
                    R.styleable.TimelineView_timeline_step_y_size,
                    TimelineConstants.DEFAULT_STEP_Y_SIZE
                )
            )
            .setStepYFirst(
                typedArray.getDimension(
                    R.styleable.TimelineView_timeline_step_y_first_size,
                    TimelineConstants.DEFAULT_STEP_Y_FIRST_SIZE
                )
            )
            .setMarginTopDescription(
                typedArray.getDimension(
                    R.styleable.TimelineView_timeline_margin_top_description,
                    TimelineConstants.DEFAULT_MARGIN_TOP_DESCRIPTION
                )
            )
            .setMarginTopTitle(
                typedArray.getDimension(
                    R.styleable.TimelineView_timeline_margin_top_title,
                    TimelineConstants.DEFAULT_MARGIN_TOP_TITLE
                )
            )
            .setMarginTopProgressIcon(
                typedArray.getDimension(
                    R.styleable.TimelineView_timeline_margin_top_progress_icon,
                    TimelineConstants.DEFAULT_MARGIN_TOP_PROGRESS_ICON
                )
            )
            .setMarginHorizontalImage(
                typedArray.getDimension(
                    R.styleable.TimelineView_timeline_margin_horizontal_image,
                    TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_IMAGE
                )
            )
            .setMarginHorizontalText(
                typedArray.getDimension(
                    R.styleable.TimelineView_timeline_margin_horizontal_text,
                    TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_TEXT
                )
            )
            .setMarginHorizontalStroke(
                typedArray.getDimension(
                    R.styleable.TimelineView_timeline_margin_horizontal_stroke,
                    TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_STROKE
                )
            )
            .setSizeImageLvl(
                typedArray.getDimension(
                    R.styleable.TimelineView_timeline_image_lvl_size,
                    TimelineConstants.DEFAULT_IMAGE_LVL_SIZE
                )
            )
            .setSizeIconProgress(
                typedArray.getDimension(
                    R.styleable.TimelineView_timeline_icon_progress_size,
                    TimelineConstants.DEFAULT_ICON_PROGRESS_SIZE
                )
            )

        val uiBuilder = TimelineUiConfig.Builder()
            .setColorProgress(
                typedArray.getColor(
                    R.styleable.TimelineView_timeline_progress_color,
                    ContextCompat.getColor(context, TimelineConstants.DEFAULT_PROGRESS_COLOR)
                )
            )
            .setRadius(
                typedArray.getDimension(
                    R.styleable.TimelineView_timeline_radius_size,
                    TimelineConstants.DEFAULT_RADIUS_SIZE
                )
            )
            .setSizeDescription(
                typedArray.getDimension(
                    R.styleable.TimelineView_timeline_description_size,
                    TimelineConstants.DEFAULT_DESCRIPTION_SIZE
                )
            )
            .setSizeTitle(
                typedArray.getDimension(
                    R.styleable.TimelineView_timeline_title_size,
                    TimelineConstants.DEFAULT_TITLE_SIZE
                )
            )
            .setSizeStroke(
                typedArray.getDimension(
                    R.styleable.TimelineView_timeline_stroke_size,
                    TimelineConstants.DEFAULT_STROKE_SIZE
                )
            )
            .setColorStroke(
                typedArray.getColor(
                    R.styleable.TimelineView_timeline_stroke_color,
                    ContextCompat.getColor(context, TimelineConstants.DEFAULT_STROKE_COLOR)
                )
            )
            .setColorTitle(
                typedArray.getColor(
                    R.styleable.TimelineView_timeline_title_color,
                    ContextCompat.getColor(context, TimelineConstants.DEFAULT_TITLE_COLOR)
                )
            )
            .setColorDescription(
                typedArray.getColor(
                    R.styleable.TimelineView_timeline_description_color,
                    ContextCompat.getColor(context, TimelineConstants.DEFAULT_DESCRIPTION_COLOR)
                )
            )
            .setIconDisableLvl(
                typedArray.getResourceId(R.styleable.TimelineView_timeline_disable_icon, 0)
            )
            .setIconProgress(
                typedArray.getResourceId(R.styleable.TimelineView_timeline_progress_icon, 0)
            )

        val orientation = TimelineOrientation.entries[typedArray.getInt(
            R.styleable.TimelineView_timeline_orientation,
            TimelineOrientation.SNAKE_VERTICAL.ordinal
        )]

        typedArray.recycle()

        return Triple(mathBuilder.build(), uiBuilder.build(), orientation)
    }
}

