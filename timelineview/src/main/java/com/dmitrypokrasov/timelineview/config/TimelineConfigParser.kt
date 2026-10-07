package com.dmitrypokrasov.timelineview.config

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import com.dmitrypokrasov.timelineview.R
import com.dmitrypokrasov.timelineview.model.TimelineConstants

/**
 * Utility class for parsing view attributes into a [TimelineConfig].
 */
class TimelineConfigParser(private val context: Context) {
    /** Parses `TimelineView` XML attributes into a strongly typed config object. */
    @SuppressLint("CustomViewStyleable")
    fun parse(
        attrs: AttributeSet?,
        defStyleAttr: Int = 0,
    ): TimelineConfig {
        return context.obtainStyledAttributes(attrs, R.styleable.TimelineView, defStyleAttr, 0).use { typedArray ->

            val mathConfig =
                TimelineMathConfig(
                    minCellWidth = typedArray.getDimension(R.styleable.TimelineView_timeline_min_cell_width, 160f * context.resources.displayMetrics.density),
                    horizontalLayout = TimelineMathConfig.HorizontalLayout.entries.getOrElse(typedArray.getInt(R.styleable.TimelineView_timeline_horizontal_layout, 0)) { TimelineMathConfig.HorizontalLayout.FIT },
                    progressMode = TimelineMathConfig.ProgressMode.entries.getOrElse(typedArray.getInt(R.styleable.TimelineView_timeline_progress_mode, 0)) { TimelineMathConfig.ProgressMode.SEQUENTIAL },
                    startPosition =
                        TimelineMathConfig.StartPosition.entries.getOrElse(
                            typedArray.getInt(
                                R.styleable.TimelineView_timeline_start_position,
                                TimelineMathConfig.StartPosition.CENTER.ordinal,
                            ),
                        ) { TimelineMathConfig.StartPosition.CENTER },
                    spacing =
                        TimelineMathConfig.Spacing(
                            stepY =
                                typedArray.getDimension(
                                    R.styleable.TimelineView_timeline_step_y_size,
                                    TimelineConstants.DEFAULT_STEP_Y_SIZE * context.resources.displayMetrics.density,
                                ),
                            stepYFirst =
                                typedArray.getDimension(
                                    R.styleable.TimelineView_timeline_step_y_first_size,
                                    TimelineConstants.DEFAULT_STEP_Y_FIRST_SIZE * context.resources.displayMetrics.density,
                                ),
                            marginTopDescription =
                                typedArray.getDimension(
                                    R.styleable.TimelineView_timeline_margin_top_description,
                                    TimelineConstants.DEFAULT_MARGIN_TOP_DESCRIPTION * context.resources.displayMetrics.density,
                                ),
                            marginTopTitle =
                                typedArray.getDimension(
                                    R.styleable.TimelineView_timeline_margin_top_title,
                                    TimelineConstants.DEFAULT_MARGIN_TOP_TITLE * context.resources.displayMetrics.density,
                                ),
                            marginTopProgressIcon =
                                typedArray.getDimension(
                                    R.styleable.TimelineView_timeline_margin_top_progress_icon,
                                    TimelineConstants.DEFAULT_MARGIN_TOP_PROGRESS_ICON * context.resources.displayMetrics.density,
                                ),
                            marginHorizontalImage =
                                typedArray.getDimension(
                                    R.styleable.TimelineView_timeline_margin_horizontal_image,
                                    TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_IMAGE * context.resources.displayMetrics.density,
                                ),
                            marginHorizontalText =
                                typedArray.getDimension(
                                    R.styleable.TimelineView_timeline_margin_horizontal_text,
                                    TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_TEXT * context.resources.displayMetrics.density,
                                ),
                            marginHorizontalStroke =
                                typedArray.getDimension(
                                    R.styleable.TimelineView_timeline_margin_horizontal_stroke,
                                    TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_STROKE * context.resources.displayMetrics.density,
                                ),
                        ),
                    sizes =
                        TimelineMathConfig.Sizes(
                            sizeImageLvl =
                                typedArray.getDimension(
                                    R.styleable.TimelineView_timeline_image_lvl_size,
                                    TimelineConstants.DEFAULT_IMAGE_LVL_SIZE * context.resources.displayMetrics.density,
                                ),
                            sizeIconProgress =
                                typedArray.getDimension(
                                    R.styleable.TimelineView_timeline_icon_progress_size,
                                    TimelineConstants.DEFAULT_ICON_PROGRESS_SIZE * context.resources.displayMetrics.density,
                                ),
                        ),
                )

            val uiConfig =
                TimelineUiConfig(
                    icons =
                        TimelineUiConfig.Icons(
                            iconDisableLvl =
                                typedArray.getResourceId(
                                    R.styleable.TimelineView_timeline_disable_icon,
                                    0,
                                ),
                            iconProgress =
                                typedArray.getResourceId(
                                    R.styleable.TimelineView_timeline_progress_icon,
                                    0,
                                ),
                        ),
                    colors =
                        TimelineUiConfig.Colors(
                            colorProgress =
                                typedArray.getColor(
                                    R.styleable.TimelineView_timeline_progress_color,
                                    ContextCompat.getColor(
                                        context,
                                        TimelineConstants.DEFAULT_PROGRESS_COLOR,
                                    ),
                                ),
                            colorStroke =
                                typedArray.getColor(
                                    R.styleable.TimelineView_timeline_stroke_color,
                                    ContextCompat.getColor(
                                        context,
                                        TimelineConstants.DEFAULT_STROKE_COLOR,
                                    ),
                                ),
                            colorTitle =
                                typedArray.getColor(
                                    R.styleable.TimelineView_timeline_title_color,
                                    ContextCompat.getColor(
                                        context,
                                        TimelineConstants.DEFAULT_TITLE_COLOR,
                                    ),
                                ),
                            colorDescription =
                                typedArray.getColor(
                                    R.styleable.TimelineView_timeline_description_color,
                                    ContextCompat.getColor(
                                        context,
                                        TimelineConstants.DEFAULT_DESCRIPTION_COLOR,
                                    ),
                                ),
                        ),
                    textSizes =
                        TimelineUiConfig.TextSizes(
                            sizeDescription =
                                typedArray.getDimension(
                                    R.styleable.TimelineView_timeline_description_size,
                                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, TimelineConstants.DEFAULT_DESCRIPTION_SIZE, context.resources.displayMetrics),
                                ),
                            sizeTitle =
                                typedArray.getDimension(
                                    R.styleable.TimelineView_timeline_title_size,
                                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, TimelineConstants.DEFAULT_TITLE_SIZE, context.resources.displayMetrics),
                                ),
                        ),
                    stroke =
                        TimelineUiConfig.Stroke(
                            radius =
                                typedArray.getDimension(
                                    R.styleable.TimelineView_timeline_radius_size,
                                    TimelineConstants.DEFAULT_RADIUS_SIZE * context.resources.displayMetrics.density,
                                ),
                            sizeStroke =
                                typedArray.getDimension(
                                    R.styleable.TimelineView_timeline_stroke_size,
                                    TimelineConstants.DEFAULT_STROKE_SIZE * context.resources.displayMetrics.density,
                                ),
                        ),
                )

            val mathStrategyKey =
                typedArray.getString(
                    R.styleable.TimelineView_timeline_math_strategy_id,
                )?.trim()?.takeIf { it.isNotEmpty() }?.let { StrategyKey(it) }
            val uiStrategyKey =
                typedArray.getString(
                    R.styleable.TimelineView_timeline_ui_strategy_id,
                )?.trim()?.takeIf { it.isNotEmpty() }?.let { StrategyKey(it) }

            val mathStrategy =
                TimelineMathStrategy.fromOrdinal(
                    typedArray.getInt(
                        R.styleable.TimelineView_timeline_math_strategy,
                        TimelineMathStrategy.entries.indexOf(TimelineMathStrategy.Snake),
                    ),
                )
            val uiStrategy =
                TimelineUiStrategy.fromOrdinal(
                    typedArray.getInt(
                        R.styleable.TimelineView_timeline_ui_strategy,
                        TimelineUiStrategy.entries.indexOf(TimelineUiStrategy.Snake),
                    ),
                )

            TimelineConfig(
                math = mathConfig,
                ui = uiConfig,
                mathStrategy = mathStrategy,
                uiStrategy = uiStrategy,
                mathStrategyKey = mathStrategyKey,
                uiStrategyKey = uiStrategyKey,
            )
        }
    }
}
