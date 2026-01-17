package com.dmitrypokrasov.timelineview.config

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
interface TimelineMathConfig {
    val startPosition: StartPosition
    val steps: List<TimelineStep>
    val stepY: Float
    val stepYFirst: Float
    val marginTopDescription: Float
    val marginTopTitle: Float
    val marginTopProgressIcon: Float
    val marginHorizontalImage: Float
    val marginHorizontalText: Float
    val marginHorizontalStroke: Float
    val sizeIconProgress: Float
    val sizeImageLvl: Float

    /** Положение первого шага таймлайна относительно контейнера. */
    enum class StartPosition { START, CENTER, END }

    /** Возвращает копию конфигурации с новым набором шагов. */
    fun withSteps(steps: List<TimelineStep>): TimelineMathConfig
}
