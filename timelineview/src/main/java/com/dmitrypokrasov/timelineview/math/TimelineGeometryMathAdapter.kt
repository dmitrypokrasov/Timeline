package com.dmitrypokrasov.timelineview.math

import android.graphics.Path
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryInput
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryResult
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryStrategy
import com.dmitrypokrasov.timelineview.geometry.TimelineTextMetrics
import com.dmitrypokrasov.timelineview.render.setCommands
import com.dmitrypokrasov.timelineview.render.toAndroid

/**
 * Адаптирует чистую геометрию к совместимому Android API. Кэш принадлежит одному View.
 * Первый проход определяет ширины текста, второй получает измеренные высоты.
 * Стратегия сохраняет порядок шагов и ширины между проходами; координаты заданы в пикселях.
 */
class TimelineGeometryMathAdapter(
    config: TimelineMathConfig,
    private val geometry: TimelineGeometryStrategy,
) : TimelineMathAdapter(config), TimelineMeasuredGeometry {
    private var radius = 0f
    private var metrics: List<TimelineTextMetrics> = emptyList()
    private var cached: TimelineGeometryResult? = null

    /** Готовые кривые не требуют дополнительного CornerPathEffect. */
    override val hasRoundedGeometry: Boolean get() = true

    /** Равная конфигурация сохраняет рассчитанный снимок и измерения текста. */
    override fun setConfig(config: TimelineMathConfig) {
        if (input != config) super.setConfig(config)
    }

    /** Повторное измерение той же ширины не сбрасывает снимок. */
    override fun setMeasuredWidth(measuredWidth: Int) {
        if (width != measuredWidth.coerceAtLeast(0)) super.setMeasuredWidth(measuredWidth)
    }

    override fun onGeometryChanged() {
        cached = null
        metrics = emptyList()
    }

    override fun setCornerRadius(radius: Float) {
        require(radius.isFinite() && radius >= 0f) { "Corner radius must be finite and non-negative" }
        if (this.radius != radius) {
            this.radius = radius
            cached = null
        }
    }

    /** Принимает метрики UI; повторные равные измерения не сбрасывают снимок. */
    override fun setTextMetrics(metrics: List<TimelineTextMetrics>) {
        require(metrics.isEmpty() || metrics.size == input.steps.size) { "Text metrics must match steps" }
        if (this.metrics != metrics) {
            this.metrics = metrics.toList()
            cached = null
        }
    }

    /** Возвращает единый снимок для layout, путей и границ содержимого. */
    fun geometryResult(): TimelineGeometryResult =
        cached ?: geometry.calculate(TimelineGeometryInput(input, width, radius, metrics)).let { result ->
            result.copy(
                layout = result.layout.copy(steps = result.layout.steps.toList()),
                completed = result.completed.toList(),
                remaining = result.remaining.toList(),
            )
        }.also { cached = it }

    override fun buildLayout() = geometryResult().layout.toAndroid()

    override fun getStartPosition(): Float = geometryResult().startPosition

    override fun getGeometryBounds() = geometryResult().bounds

    override fun buildPath(
        pathEnable: Path,
        pathDisable: Path,
    ) {
        val result = geometryResult()
        pathEnable.setCommands(result.completed)
        pathDisable.setCommands(result.remaining)
    }
}
