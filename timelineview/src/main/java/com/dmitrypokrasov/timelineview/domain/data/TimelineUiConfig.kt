package com.dmitrypokrasov.timelineview.domain.data

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.dmitrypokrasov.timelineview.data.TimelineConstants

/**
 * Конфигурация визуального оформления элементов таймлайна.
 *
 * Используется для настройки иконок и цветов, отвечающих за внешний вид шагов прогресса.
 *
 * @property iconDisableLvl Иконка, отображаемая для неактивных (заблокированных) шагов.
 * @property iconProgress Иконка, отображаемая для текущего активного шага прогресса.
 * @property colorProgress Цвет линии или маркера, обозначающего прогресс.
 * @property colorStroke Цвет обводки шагов/иконок.
 * @property colorTitle Цвет заголовка шага.
 * @property colorDescription Цвет описания шага.
 * @property radius Радиус точки/индикатора для каждого шага.
 * @property sizeDescription Размер текста описания.
 * @property sizeTitle Размер текста заголовка.
 * @property sizeStroke Толщина вертикальной линии.
 *
 */
data class TimelineUiConfig(
    @DrawableRes val iconDisableLvl: Int,
    @DrawableRes val iconProgress: Int,
    @ColorInt var colorProgress: Int,
    @ColorInt var colorStroke: Int,
    @ColorInt var colorTitle: Int,
    @ColorInt var colorDescription: Int,
    val sizeDescription: Float,
    val sizeTitle: Float,
    val radius: Float,
    val sizeStroke: Float
) {


    /**
     * Билдер для создания экземпляра [TimelineUiConfig] с пошаговой настройкой параметров.
     */
    class Builder {

        @DrawableRes
        private var iconDisableLvl: Int = 0

        @DrawableRes
        private var iconProgress: Int = 0

        @ColorInt
        private var colorProgress: Int = 0

        @ColorInt
        private var colorStroke: Int = 0

        @ColorInt
        private var colorTitle: Int = 0

        @ColorInt
        private var colorDescription: Int = 0

        private var radius: Float = TimelineConstants.DEFAULT_RADIUS_SIZE
        private var sizeDescription: Float = TimelineConstants.DEFAULT_DESCRIPTION_SIZE
        private var sizeTitle: Float = TimelineConstants.DEFAULT_TITLE_SIZE
        private var sizeStroke: Float = TimelineConstants.DEFAULT_STROKE_SIZE

        /**
         * Устанавливает иконку для неактивных уровней.
         */
        fun setIconDisableLvl(value: Int) = apply { iconDisableLvl = value }

        /**
         * Устанавливает иконку прогресса для текущего шага.
         */
        fun setIconProgress(value: Int) = apply { iconProgress = value }

        /**
         * Устанавливает цвет линии или индикатора прогресса.
         */
        fun setColorProgress(value: Int) = apply { colorProgress = value }

        /**
         * Устанавливает цвет обводки шагов или иконок.
         */
        fun setColorStroke(value: Int) = apply { colorStroke = value }

        /**
         * Устанавливает цвет заголовков шагов.
         */
        fun setColorTitle(value: Int) = apply { colorTitle = value }

        /**
         * Устанавливает цвет описаний шагов.
         */
        fun setColorDescription(value: Int) = apply { colorDescription = value }

        /** Устанавливает радиус индикатора. */
        fun setRadius(value: Float) = apply { radius = value }

        /** Устанавливает размер текста описания. */
        fun setSizeDescription(value: Float) = apply { sizeDescription = value }

        /** Устанавливает размер текста заголовка. */
        fun setSizeTitle(value: Float) = apply { sizeTitle = value }

        /** Устанавливает толщину вертикальной линии. */
        fun setSizeStroke(value: Float) = apply { sizeStroke = value }


        /**
         * Создаёт экземпляр [TimelineUiConfig] на основе установленных параметров.
         */
        fun build(): TimelineUiConfig {
            val config = TimelineUiConfig(
                iconDisableLvl = iconDisableLvl,
                iconProgress = iconProgress,
                colorDescription = colorDescription,
                colorProgress = colorProgress,
                colorTitle = colorTitle,
                colorStroke = colorStroke,
                radius = radius,
                sizeDescription = sizeDescription,
                sizeTitle = sizeTitle,
                sizeStroke = sizeStroke
            )
            return config
        }
    }
}