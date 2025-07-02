package com.dmitrypokrasov.timelineview.domain.data

import com.dmitrypokrasov.timelineview.data.TimelineConstants
import com.dmitrypokrasov.timelineview.data.TimelineStep

/**
 * Конфигурация для расчёта позиционирования и геометрии элементов таймлайна.
 *
 * Содержит математические параметры, используемые для построения вертикального
 * расположения шагов, отступов, размеров элементов и общей высоты компонента.
 *
 * @property startPosition Начальная позиция таймлайна (вверху, по центру или внизу).
 * @property steps Список шагов, из которых состоит таймлайн.
 * @property stepY Расстояние по вертикали между шагами.
 * @property stepYFirst Отступ от начала компонента до первого шага.
 * @property marginTopDescription Отступ сверху для описания шага.
 * @property marginTopTitle Отступ сверху для заголовка шага.
 * @property marginTopProgressIcon Отступ сверху для иконки текущего прогресса.
 * @property marginHorizontalImage Горизонтальный отступ для изображения уровня.
 * @property marginHorizontalText Горизонтальный отступ для текста.
 * @property marginHorizontalStroke Горизонтальный отступ для вертикальной линии.
 * @property top Смещение всего контента относительно начала вьюшки (на основе первого шага).
 * @property sizeIconProgress Размер иконки прогресса.
 * @property measuredHeight Полная высота компонента, рассчитанная на основе количества шагов и отступов.
 * @property sizeDescription Размер текста описания.
 * @property sizeTitle Размер текста заголовка.
 * @property sizeImageLvl Размер изображения уровня.
 * @property sizeStroke Толщина вертикальной линии.
 * @property radius Радиус точки/индикатора для каждого шага.
 */
data class TimelineMathConfig(
    val startPosition: StartPosition,
    val steps: List<TimelineStep>,
    val stepY: Float,
    val stepYFirst: Float,
    val marginTopDescription: Float,
    val marginTopTitle: Float,
    val marginTopProgressIcon: Float,
    val marginHorizontalImage: Float,
    val marginHorizontalText: Float,
    val marginHorizontalStroke: Float,
    val top: Float,
    val sizeIconProgress: Float,
    val measuredHeight: Int,
    val sizeDescription: Float,
    val sizeTitle: Float,
    val sizeImageLvl: Float,
    val sizeStroke: Float,
    val radius: Float
) {

    /**
     * Положение первого шага таймлайна относительно контейнера.
     */
    enum class StartPosition {
        START, CENTER, END
    }

    /**
     * Билдер для создания [TimelineMathConfig] с настраиваемыми значениями.
     *
     * Позволяет поэтапно конфигурировать параметры позиционирования и расчёта размеров.
     */
    class Builder {

        private var startPosition: StartPosition = StartPosition.CENTER
        private var steps: List<TimelineStep> = listOf()
        private var stepY: Float = TimelineConstants.DEFAULT_STEP_Y_SIZE
        private var stepYFirst: Float = TimelineConstants.DEFAULT_STEP_Y_FIRST_SIZE

        private var marginHorizontalText: Float = TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_TEXT
        private var marginTopDescription: Float = TimelineConstants.DEFAULT_MARGIN_TOP_DESCRIPTION
        private var marginTopTitle: Float = TimelineConstants.DEFAULT_MARGIN_TOP_TITLE
        private var marginTopProgressIcon: Float =
            TimelineConstants.DEFAULT_MARGIN_TOP_PROGRESS_ICON
        private var marginHorizontalImage: Float = TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_IMAGE
        private var marginHorizontalStroke: Float =
            TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_STROKE

        private var sizeIconProgress: Float = TimelineConstants.DEFAULT_ICON_PROGRESS_SIZE
        private var sizeDescription: Float = TimelineConstants.DEFAULT_DESCRIPTION_SIZE
        private var sizeTitle: Float = TimelineConstants.DEFAULT_TITLE_SIZE
        private var sizeStroke: Float = TimelineConstants.DEFAULT_STROKE_SIZE
        private var sizeImageLvl: Float = TimelineConstants.DEFAULT_IMAGE_LVL_SIZE
        private var radius: Float = TimelineConstants.DEFAULT_RADIUS_SIZE

        /** Устанавливает список шагов таймлайна. */
        fun setSteps(steps: List<TimelineStep>) = apply { this.steps = steps }

        /** Устанавливает начальное положение таймлайна. */
        fun setStartPosition(value: StartPosition) = apply { startPosition = value }

        /** Устанавливает расстояние между шагами по Y. */
        fun setStepY(value: Float) = apply { stepY = value }

        /** Устанавливает отступ от начала до первого шага. */
        fun setStepYFirst(value: Float) = apply { stepYFirst = value }

        /** Устанавливает горизонтальный отступ для текста. */
        fun setMarginHorizontalText(value: Float) = apply { marginHorizontalText = value }

        /** Устанавливает отступ сверху для описания. */
        fun setMarginTopDescription(value: Float) = apply { marginTopDescription = value }

        /** Устанавливает отступ сверху для заголовка. */
        fun setMarginTopTitle(value: Float) = apply { marginTopTitle = value }

        /** Устанавливает отступ сверху для иконки прогресса. */
        fun setMarginTopProgressIcon(value: Float) = apply { marginTopProgressIcon = value }

        /** Устанавливает горизонтальный отступ для изображения уровня. */
        fun setMarginHorizontalImage(value: Float) = apply { marginHorizontalImage = value }

        /** Устанавливает горизонтальный отступ для линии. */
        fun setMarginHorizontalStroke(value: Float) = apply { marginHorizontalStroke = value }

        /** Устанавливает размер иконки прогресса. */
        fun setSizeIconProgress(value: Float) = apply { sizeIconProgress = value }

        /** Устанавливает размер текста описания. */
        fun setSizeDescription(value: Float) = apply { sizeDescription = value }

        /** Устанавливает размер текста заголовка. */
        fun setSizeTitle(value: Float) = apply { sizeTitle = value }

        /** Устанавливает толщину вертикальной линии. */
        fun setSizeStroke(value: Float) = apply { sizeStroke = value }

        /** Устанавливает размер изображения уровня. */
        fun setSizeImageLvl(value: Float) = apply { sizeImageLvl = value }

        /** Устанавливает радиус индикатора. */
        fun setRadius(value: Float) = apply { radius = value }

        /**
         * Создаёт экземпляр [TimelineMathConfig] с рассчитанной высотой и смещением.
         */
        fun build(): TimelineMathConfig {
            return TimelineMathConfig(
                startPosition = startPosition,
                steps = steps,
                stepY = stepY,
                stepYFirst = stepYFirst,
                marginTopDescription = marginTopDescription,
                marginTopTitle = marginTopTitle,
                marginTopProgressIcon = marginTopProgressIcon,
                marginHorizontalImage = marginHorizontalImage,
                marginHorizontalText = marginHorizontalText,
                marginHorizontalStroke = marginHorizontalStroke,
                sizeIconProgress = sizeIconProgress,
                top = stepYFirst - sizeIconProgress / 2f,
                measuredHeight = ((stepY * steps.size) + stepYFirst + 50).toInt(),
                sizeDescription = sizeDescription,
                sizeTitle = sizeTitle,
                sizeStroke = sizeStroke,
                sizeImageLvl = sizeImageLvl,
                radius = radius
            )
        }
    }
}
