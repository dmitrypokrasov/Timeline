package com.dmitrypokrasov.timelineview.domain

import android.graphics.Paint
import android.graphics.Path
import com.dmitrypokrasov.timelineview.data.TimelineStep

/**
 * Математический движок временной шкалы.
 *
 * Выполняет расчёты для построения линий прогресса и предоставляет координаты
 * элементов интерфейса.
 */
interface TimelineMathEngine {
    /**
     * Заменяет текущий список шагов временной шкалы.
     *
     * После вызова все вычисления будут использовать новый набор шагов.
     *
     * @param steps новый список шагов
     */
    fun replaceSteps(steps: List<TimelineStep>)

    /**
     * Перестраивает пути для отображения прогресса.
     *
     * @param pathEnable путь пройденных шагов. Метод должен очистить и заполнить его заново
     * @param pathDisable путь оставшихся шагов. Метод должен очистить и заполнить его заново
     */
    fun buildPath(pathEnable: Path, pathDisable: Path)

    /**
     * Возвращает X-координату точки начала рисования линии прогресса.
     */
    fun getStartPosition(): Float

    /**
     * Сохраняет фактическую ширину View и пересчитывает начало рисования.
     *
     * @param measuredWidth измеренная ширина View
     */
    fun setMeasuredWidth(measuredWidth: Int)

    /**
     * Возвращает горизонтальное смещение иконки прогресса для шага [i].
     */
    fun getHorizontalIconOffset(i: Int): Float

    /**
     * Возвращает вертикальное смещение для шага [i].
     */
    fun getVerticalOffset(i: Int): Float

    /**
     * Вычисляет левую координату иконки для указанного шага.
     *
     * @param lvl шаг временной шкалы
     */
    fun getLeftCoordinates(lvl: TimelineStep): Float

    /**
     * Вычисляет верхнюю координату иконки для указанного шага.
     *
     * @param lvl шаг временной шкалы
     */
    fun getTopCoordinates(lvl: TimelineStep): Float

    /**
     * Возвращает X-координату заголовка уровня в зависимости от выравнивания.
     *
     * @param align используемое выравнивание текста
     */
    fun getTitleXCoordinates(align: Paint.Align): Float

    /**
     * Возвращает X-координату иконки уровня в зависимости от выравнивания.
     *
     * @param align используемое выравнивание текста
     */
    fun getIconXCoordinates(align: Paint.Align): Float

    /**
     * Возвращает Y-координату заголовка шага [i].
     */
    fun getTitleYCoordinates(i: Int): Float

    /**
     * Возвращает Y-координату описания шага [i].
     */
    fun getDescriptionYCoordinates(i: Int): Float

    /**
     * Возвращает Y-координату иконки шага [i].
     */
    fun getIconYCoordinates(i: Int): Float

    /**
     * Возвращает рассчитанную высоту View после всех вычислений.
     */
    fun getMeasuredHeight(): Int
}
