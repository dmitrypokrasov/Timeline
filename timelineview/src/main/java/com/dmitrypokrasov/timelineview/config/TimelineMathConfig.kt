package com.dmitrypokrasov.timelineview.config

import com.dmitrypokrasov.timelineview.model.TimelineConstants
import com.dmitrypokrasov.timelineview.model.TimelineStepData

/**
 * Конфигурация параметров позиционирования и размеров таймлайна.
 *
 * @property startPosition начальная позиция таймлайна (слева, по центру или справа)
 * @property steps список шагов, образующих таймлайн
 * @property spacing группа отступов таймлайна
 * @property sizes группа размеров таймлайна
 *
 * Хранит только данные без дополнительных вычислений. Вся логика расчётов
 * вынесена в реализации [com.dmitrypokrasov.timelineview.math.TimelineMathEngine].
 */
data class TimelineMathConfig(
    val startPosition: StartPosition = StartPosition.CENTER,
    val steps: List<TimelineStepData> = listOf(),
    var spacing: Spacing = Spacing(),
    var sizes: Sizes = Sizes()
) {
    /** Положение первого шага таймлайна относительно контейнера. */
    enum class StartPosition { START, CENTER, END }

    data class Spacing(
        var stepY: Float = TimelineConstants.DEFAULT_STEP_Y_SIZE,
        var stepYFirst: Float = TimelineConstants.DEFAULT_STEP_Y_FIRST_SIZE,
        var marginTopDescription: Float = TimelineConstants.DEFAULT_MARGIN_TOP_DESCRIPTION,
        var marginTopTitle: Float = TimelineConstants.DEFAULT_MARGIN_TOP_TITLE,
        var marginTopProgressIcon: Float = TimelineConstants.DEFAULT_MARGIN_TOP_PROGRESS_ICON,
        var marginHorizontalImage: Float = TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_IMAGE,
        var marginHorizontalText: Float = TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_TEXT,
        var marginHorizontalStroke: Float = TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_STROKE
    ) {
        init {
            stepY = stepY.coerceAtLeast(0f)
            stepYFirst = stepYFirst.coerceAtLeast(0f)
            marginTopDescription = marginTopDescription.coerceAtLeast(0f)
            marginTopTitle = marginTopTitle.coerceAtLeast(0f)
            marginTopProgressIcon = marginTopProgressIcon.coerceAtLeast(0f)
            marginHorizontalImage = marginHorizontalImage.coerceAtLeast(0f)
            marginHorizontalText = marginHorizontalText.coerceAtLeast(0f)
            marginHorizontalStroke = marginHorizontalStroke.coerceAtLeast(0f)
        }
    }

    data class Sizes(
        var sizeIconProgress: Float = TimelineConstants.DEFAULT_ICON_PROGRESS_SIZE,
        var sizeImageLvl: Float = TimelineConstants.DEFAULT_IMAGE_LVL_SIZE
    ) {
        init {
            sizeIconProgress = sizeIconProgress.coerceAtLeast(0f)
            sizeImageLvl = sizeImageLvl.coerceAtLeast(0f)
        }
    }
}
