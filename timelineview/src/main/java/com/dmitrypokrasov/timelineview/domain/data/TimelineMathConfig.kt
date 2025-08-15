package com.dmitrypokrasov.timelineview.domain.data

import com.dmitrypokrasov.timelineview.data.TimelineConstants
import com.dmitrypokrasov.timelineview.data.TimelineStep

/**
 * Конфигурация параметров позиционирования и размеров таймлайна.
 *
 * Хранит только данные без дополнительных вычислений. Вся логика расчётов
 * вынесена в реализации [com.dmitrypokrasov.timelineview.domain.TimelineMathEngine].
 */
data class TimelineMathConfig(
    val startPosition: StartPosition = StartPosition.CENTER,
    val steps: List<TimelineStep> = listOf(),
    val stepY: Float = TimelineConstants.DEFAULT_STEP_Y_SIZE,
    val stepYFirst: Float = TimelineConstants.DEFAULT_STEP_Y_FIRST_SIZE,
    val marginTopDescription: Float = TimelineConstants.DEFAULT_MARGIN_TOP_DESCRIPTION,
    val marginTopTitle: Float = TimelineConstants.DEFAULT_MARGIN_TOP_TITLE,
    val marginTopProgressIcon: Float = TimelineConstants.DEFAULT_MARGIN_TOP_PROGRESS_ICON,
    val marginHorizontalImage: Float = TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_IMAGE,
    val marginHorizontalText: Float = TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_TEXT,
    val marginHorizontalStroke: Float = TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_STROKE,
    val sizeIconProgress: Float = TimelineConstants.DEFAULT_ICON_PROGRESS_SIZE,
    val sizeImageLvl: Float = TimelineConstants.DEFAULT_IMAGE_LVL_SIZE
) {
    /** Положение первого шага таймлайна относительно контейнера. */
    enum class StartPosition { START, CENTER, END }
}
