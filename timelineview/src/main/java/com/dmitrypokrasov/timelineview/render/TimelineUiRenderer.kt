package com.dmitrypokrasov.timelineview.render

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.dmitrypokrasov.timelineview.model.TimelineStep
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig

/**
 * Интерфейс рендерера временной шкалы.
 *
 * Отвечает за подготовку инструментов рисования и вывод элементов на [Canvas].
 */
interface TimelineUiRenderer {
    /** Устанавливает новую конфигурацию визуального оформления. */
    fun setConfig(config: TimelineUiConfig)

    /** Возвращает текущую конфигурацию визуального оформления. */
    fun getConfig(): TimelineUiConfig

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
    fun prepareStrokePaint()

    /**
     * Сбрасывает и настраивает кисть для рисования текста.
     */
    fun prepareTextPaint()

    /** Сбрасывает и настраивает кисть для рисования иконок. */
    fun prepareIconPaint()

    /**
     * Рисует bitmap текущего прогресса.
     *
     * @param canvas холст, на котором ведётся рисование
     * @param leftCoordinates левая координата вывода
     * @param topCoordinates верхняя координата вывода
     */
    fun drawProgressIcon(canvas: Canvas, leftCoordinates: Float, topCoordinates: Float)

    /**
     * Рисует путь пройденных шагов.
     */
    fun drawCompletedPath(canvas: Canvas)

    /**
     * Рисует путь непройденных шагов.
     */
    fun drawRemainingPath(canvas: Canvas)

    /** Возвращает путь пройденных шагов. */
    fun getCompletedPath(): Path

    /** Возвращает путь непройденных шагов. */
    fun getRemainingPath(): Path

    /**
     * Печатает заголовок шага.
     *
     * @param canvas холст для рисования
     * @param title текст заголовка
     * @param x X-координата текста
     * @param y Y-координата текста
     * @param align выравнивание текста
     */
    fun drawTitle(canvas: Canvas, title: String, x: Float, y: Float, align: Paint.Align)

    /**
     * Печатает описание шага.
     *
     * @param canvas холст для рисования
     * @param description текст описания
     * @param x X-координата текста
     * @param y Y-координата текста
     * @param align выравнивание текста
     */
    fun drawDescription(canvas: Canvas, description: String, x: Float, y: Float, align: Paint.Align)

    /**
     * Рисует иконку шага.
     *
     * В зависимости от состояния шага выбирает соответствующий ресурс.
     *
     * @param step данные шага
     * @param canvas холст для рисования
     * @param align выравнивание иконки
     * @param context Android контекст для получения ресурсов
     * @param x X-координата иконки
     * @param y Y-координата иконки
     */
    fun drawStepIcon(step: TimelineStep, canvas: Canvas, align: Paint.Align, context: Context, x: Float, y: Float)

    /**
     * Возвращает текущее выравнивание текста, используемое кистью.
     */
    fun getTextAlignment(): Paint.Align
}
