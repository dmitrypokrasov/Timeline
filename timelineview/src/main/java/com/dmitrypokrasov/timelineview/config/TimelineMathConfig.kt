package com.dmitrypokrasov.timelineview.config

import com.dmitrypokrasov.timelineview.model.TimelineConstants
import com.dmitrypokrasov.timelineview.model.TimelineStep

/**
 * Конфигурация параметров позиционирования и размеров таймлайна.
 *
 * @property startPosition начальная позиция таймлайна (слева, по центру или справа)
 * @property steps список шагов, образующих таймлайн
 * @property stepY расстояние по вертикали между шагами
 * @property stepYFirst отступ от начала компонента до первого шага
 * @property marginTopDescription отступ сверху для описания шага
 * @property marginTopTitle отступ сверху для заголовка шага
 * @property marginTopProgressIcon отступ сверху для иконки прогресса
 * @property marginHorizontalImage горизонтальный отступ для изображений шагов
 * @property marginHorizontalText горизонтальный отступ для текстовых блоков
 * @property marginHorizontalStroke горизонтальный отступ для вертикальной линии
 * @property sizeIconProgress размер иконки текущего прогресса
 * @property sizeImageLvl размер иконок шагов
 *
 * Хранит только данные без дополнительных вычислений. Вся логика расчётов
 * вынесена в реализации [com.dmitrypokrasov.timelineview.math.TimelineMathEngine].
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
