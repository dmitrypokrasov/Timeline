package com.dmitrypokrasov.timelineview.domain

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.dmitrypokrasov.timelineview.data.TimelineStep
import com.dmitrypokrasov.timelineview.domain.data.TimelineMathConfig

/**
 * Интерфейс рендерера временной шкалы.
 *
 * Отвечает за подготовку инструментов рисования и вывод элементов на [Canvas].
 */
interface TimelineUiRenderer {
    /** Путь для пройденных шагов. */
    val pathEnable: Path

    /** Путь для непройденных шагов. */
    val pathDisable: Path
    /**
     * Инициализирует визуальные инструменты.
     *
     * Загружает и подготавливает ресурсы (иконки, эффекты линий) на основе
     * параметров математики и контекста приложения.
     *
     * @param timelineMathConfig конфигурация размеров и отступов
     * @param context Android контекст для доступа к ресурсам
     */
    fun initTools(timelineMathConfig: TimelineMathConfig, context: Context)

    /**
     * Сбрасывает и настраивает кисть для рисования линий.
     */
    fun resetFromPaintTools()

    /**
     * Сбрасывает и настраивает кисть для рисования текста.
     */
    fun resetFromTextTools()

    /**
      * Сбрасывает и настраивает кисть для рисования иконок.
      */
    fun resetFromIconTools()

    /**
     * Рисует bitmap текущего прогресса.
     *
     * @param canvas холст, на котором ведётся рисование
     * @param leftCoordinates левая координата вывода
     * @param topCoordinates верхняя координата вывода
     */
    fun drawProgressBitmap(canvas: Canvas, leftCoordinates: Float, topCoordinates: Float)

    /**
     * Рисует путь пройденных шагов.
     */
    fun drawProgressPath(canvas: Canvas)

    /**
     * Рисует путь непройденных шагов.
     */
    fun drawDisablePath(canvas: Canvas)

    /**
     * Печатает заголовок шага.
     *
     * @param canvas холст для рисования
     * @param title текст заголовка
     * @param x X-координата текста
     * @param y Y-координата текста
     * @param align выравнивание текста
     */
    fun printTitle(canvas: Canvas, title: String, x: Float, y: Float, align: Paint.Align)

    /**
     * Печатает описание шага.
     *
     * @param canvas холст для рисования
     * @param description текст описания
     * @param x X-координата текста
     * @param y Y-координата текста
     * @param align выравнивание текста
     */
    fun printDescription(canvas: Canvas, description: String, x: Float, y: Float, align: Paint.Align)

    /**
     * Рисует иконку шага.
     *
     * В зависимости от состояния шага выбирает соответствующий ресурс.
     *
     * @param lvl данные шага
     * @param canvas холст для рисования
     * @param align выравнивание иконки
     * @param context Android контекст для получения ресурсов
     * @param x X-координата иконки
     * @param y Y-координата иконки
     */
    fun printIcon(lvl: TimelineStep, canvas: Canvas, align: Paint.Align, context: Context, x: Float, y: Float)

    /**
     * Возвращает текущее выравнивание текста, используемое кистью.
     */
    fun getTextAlign(): Paint.Align
}
