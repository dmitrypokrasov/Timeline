package com.dmitrypokrasov.timelineview.math

import android.graphics.Path
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryInput
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryIntrinsicWidth
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryResult
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryStrategy
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryValidation
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
    private var provisional: TimelineGeometryResult? = null

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
        provisional = null
        metrics = emptyList()
    }

    override fun setCornerRadius(radius: Float) {
        require(radius.isFinite() && radius >= 0f) { "Corner radius must be finite and non-negative" }
        if (this.radius != radius) {
            this.radius = radius
            cached = null
            provisional = null
        }
    }

    /** Принимает метрики UI; повторные равные измерения не сбрасывают снимок. */
    override fun setTextMetrics(metrics: List<TimelineTextMetrics>) {
        require(metrics.isEmpty() || metrics.size == input.steps.size) { "Text metrics must match steps" }
        metrics.forEachIndexed { index, metric ->
            require(metric.titleHeight >= 0) { "textMetrics[$index].titleHeight must be non-negative" }
            require(metric.descriptionHeight >= 0) { "textMetrics[$index].descriptionHeight must be non-negative" }
        }
        if (this.metrics != metrics) {
            this.metrics = metrics.toList()
            cached = null
        }
    }

    /** Делегирует необязательной capability; отрицательная ширина является ошибкой стратегии. */
    override fun getDesiredWidth(): Int =
        ((geometry as? TimelineGeometryIntrinsicWidth)?.desiredWidth(input) ?: 0).also {
            require(it >= 0) { "desiredWidth must be non-negative" }
        }

    /** Возвращает только проверенный снимок; ошибочный результат никогда не кэшируется. */
    fun geometryResult(): TimelineGeometryResult {
        cached?.let { return it }
        if (metrics.isNotEmpty() && provisional == null) provisional = calculate(emptyList(), null)
        val result = calculate(metrics, if (metrics.isEmpty()) null else provisional)
        if (metrics.isEmpty()) provisional = result
        cached = result
        return result
    }

    private fun calculate(
        metrics: List<TimelineTextMetrics>,
        initial: TimelineGeometryResult?,
    ): TimelineGeometryResult {
        val request = TimelineGeometryInput(input, width, radius, metrics)
        val raw = geometry.calculate(request)
        val result =
            raw.copy(
                layout = raw.layout.copy(steps = raw.layout.steps.toList()),
                completed = raw.completed.toList(),
                remaining = raw.remaining.toList(),
            )
        TimelineGeometryValidation.validate(request, result, initial)
        return result
    }

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
