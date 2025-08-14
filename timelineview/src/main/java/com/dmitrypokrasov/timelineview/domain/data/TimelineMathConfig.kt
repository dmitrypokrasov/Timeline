package com.dmitrypokrasov.timelineview.domain.data

import android.graphics.Paint
import android.util.Log
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
 * @property sizeIconProgress Размер иконки прогресса.
 * @property sizeImageLvl Размер изображения уровня.
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
    val sizeIconProgress: Float,
    val sizeImageLvl: Float
) {

    companion object {
        private const val TAG = "TimelineMathConfig"
    }

    private var startPositionX = 0f
    private var startPositionDisableStrokeX = 0f
    private var measuredWidth = 0

    fun setMeasuredWidth(measuredWidth: Int) {
        this.measuredWidth = measuredWidth

        startPositionX = when (startPosition) {
            StartPosition.START -> marginHorizontalStroke
            StartPosition.CENTER -> measuredWidth / 2f
            StartPosition.END -> measuredWidth.toFloat() - marginHorizontalStroke
        }

        Log.d(TAG, "setMeasuredWidth startPositionX: $startPositionX")
    }

    fun getStartPosition(): Float {
        return startPositionX
    }

    /**
     * Возвращает стандартное вертикальное смещение между шагами.
     * Если последний шаг, то высота шага делится на 2.
     */
    fun getStandardDyMove(i: Int): Float {
        return if (i == steps.size - 1) stepY / 2 else stepY
    }

    /** Возвращает Y-координату для иконки уровня. */
    fun getIconYCoordinates(i: Int): Float {
        return getTitleYCoordinates(i) - (stepY - sizeImageLvl) / 2
    }

    /** Возвращает Y-координату заголовка уровня. */
    fun getTitleYCoordinates(i: Int): Float {
        return (stepY * i) + marginTopTitle
    }

    /** Возвращает Y-координату описания уровня. */
    fun getDescriptionYCoordinates(i: Int): Float {
        return getTitleYCoordinates(i) + marginTopDescription
    }

    /** Возвращает X-координату левой границы иконки прогресса. */
    fun getLeftCoordinates(lvl: TimelineStep): Float {
        return if (lvl.count == 0) -sizeIconProgress / 2f else -startPositionDisableStrokeX - sizeIconProgress / 2f
    }

    /** Возвращает Y-координату верхней границы иконки прогресса. */
    fun getTopCoordinates(lvl: TimelineStep): Float {
        return if (lvl.count == 0) -sizeIconProgress / 2f else stepYFirst - sizeIconProgress / 2f
    }

    /** Инициализирует значение [startPositionDisableStrokeX] на основе прогресса шага. */
    fun getStartPositionDisableStrokeX(lvl: TimelineStep, i: Int): Float {
        val startPosition =
            if (i == 0) getStepXFirst() / 100 * lvl.percents else getStepX() / 100 * lvl.percents

        startPositionDisableStrokeX = startPosition

        Log.d(
            TAG,
            "initStartPositionDisableStrokeX i: $i, startPositionDisableStrokeX: $startPosition"
        )

        return startPosition
    }

    /** Возвращает X-координату заголовка уровня. */
    fun getTitleXCoordinates(align: Paint.Align): Float {
        val stepX = getStepX()
        return when (align) {
            Paint.Align.LEFT -> -(startPositionX - marginHorizontalText)
            Paint.Align.CENTER -> startPositionX
            Paint.Align.RIGHT -> if (startPosition == StartPosition.CENTER) startPositionX - marginHorizontalText else -startPositionX + stepX
        }
    }

    /** Возвращает X-координату для иконки уровня. */
    fun getIconXCoordinates(align: Paint.Align): Float {
        val stepX = getStepX()
        return when (align) {
            Paint.Align.LEFT -> -(startPositionX - marginHorizontalImage)
            Paint.Align.CENTER -> startPositionX
            Paint.Align.RIGHT -> if (startPosition == StartPosition.CENTER) startPositionX - marginHorizontalImage - sizeImageLvl else -startPositionX + stepX + marginHorizontalImage
        }
    }

    /** Возвращает ширину горизонтального шага между элементами (без учёта отступов). */
    fun getStepX(): Float {
        return (measuredWidth - marginHorizontalStroke * 2)
    }

    /** Возвращает ширину первого шага (для расчёта первой линии). */
    fun getStepXFirst(): Float {
        return startPositionX - marginHorizontalStroke
    }

    /** Возвращает вертикальное смещение на шаге [i]. */
    fun getVerticalOffset(i: Int): Float {
        return (stepY * i) + marginTopProgressIcon
    }

    /** Вспомогательная функция для расчёта смещения X-координаты для текущего шага. */
    fun getHorizontalOffset(i: Int): Float {
        val offset = if (i % 2 == 0)
            when (startPosition) {
                StartPosition.START -> -startPositionDisableStrokeX + getStepX()
                StartPosition.CENTER -> -startPositionDisableStrokeX + startPositionX - marginHorizontalStroke
                StartPosition.END -> -startPositionDisableStrokeX
            }
        else startPositionDisableStrokeX - startPositionX + marginHorizontalStroke

        Log.d(
            TAG,
            "getHorizontalOffset i: $i, offset: $offset, startPositionDisableStrokeX: $startPositionDisableStrokeX, startPositionX: $startPositionX, marginHorizontalStroke: $marginHorizontalStroke"
        )

        return offset
    }

    fun getHorizontalIconOffset(i: Int): Float {
        val offset = getHorizontalOffset(i) - sizeIconProgress / 2f

        Log.d(
            TAG,
            "getHorizontalOffset i: $i, offset: $offset, startPositionDisableStrokeX: $startPositionDisableStrokeX, startPositionX: $startPositionX, marginHorizontalStroke: $marginHorizontalStroke"
        )

        return offset
    }

    /**
     * Возвращает рассчитанную высоту таймлайна.
     * Учитывает количество шагов, начальный отступ и половину размера иконки прогресса.
     */
    fun getMeasuredHeight(): Int {
        return ((stepY * steps.size) + stepYFirst + sizeIconProgress / 2f).toInt()
    }

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
        private var sizeImageLvl: Float = TimelineConstants.DEFAULT_IMAGE_LVL_SIZE

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

        /** Устанавливает размер изображения уровня. */
        fun setSizeImageLvl(value: Float) = apply { sizeImageLvl = value }

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
                sizeImageLvl = sizeImageLvl
            )
        }
    }
}
