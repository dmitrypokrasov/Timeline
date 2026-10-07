package com.dmitrypokrasov.timelineview.model

import androidx.annotation.ColorInt

/**
 * Contains default values for timeline configuration.
 */
internal object TimelineConstants {
    const val DEFAULT_STEP_Y_SIZE = 80f
    const val DEFAULT_RADIUS_SIZE = 0f
    const val DEFAULT_STEP_Y_FIRST_SIZE = 20f

    @ColorInt val DEFAULT_PROGRESS_COLOR = 0xFF00695C.toInt()

    @ColorInt val DEFAULT_STROKE_COLOR = 0xFFB0BEC5.toInt()

    @ColorInt val DEFAULT_TITLE_COLOR = 0xFF212121.toInt()

    @ColorInt val DEFAULT_DESCRIPTION_COLOR = 0xFF616161.toInt()
    const val DEFAULT_MARGIN_TOP_DESCRIPTION = 8f
    const val DEFAULT_MARGIN_TOP_TITLE = 20f
    const val DEFAULT_MARGIN_TOP_PROGRESS_ICON = 0f
    const val DEFAULT_MARGIN_HORIZONTAL_IMAGE = 16f
    const val DEFAULT_MARGIN_HORIZONTAL_TEXT = 64f
    const val DEFAULT_MARGIN_HORIZONTAL_STROKE = 28f
    const val DEFAULT_DESCRIPTION_SIZE = 14f
    const val DEFAULT_TITLE_SIZE = 16f
    const val DEFAULT_STROKE_SIZE = 2f
    const val DEFAULT_IMAGE_LVL_SIZE = 32f
    const val DEFAULT_ICON_PROGRESS_SIZE = 24f
}
