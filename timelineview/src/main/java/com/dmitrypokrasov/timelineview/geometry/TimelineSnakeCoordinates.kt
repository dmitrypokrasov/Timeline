package com.dmitrypokrasov.timelineview.geometry

import com.dmitrypokrasov.timelineview.config.TimelineMathConfig

/** Чистая топология Snake: строки, якоря, текст и полилинии до native sampling. */
internal class TimelineSnakeCoordinates(
    private val config: TimelineMathConfig,
    private val width: Int,
    private val rowOffsets: FloatArray,
) {
    private val edge get() = config.spacing.marginHorizontalStroke.coerceAtMost(width / 2f)
    val origin: Float =
        when (config.startPosition) {
            TimelineMathConfig.StartPosition.START -> edge
            TimelineMathConfig.StartPosition.CENTER -> width / 2f
            TimelineMathConfig.StartPosition.END -> width - edge
        }

    fun rowTop(index: Int): Float = config.spacing.stepYFirst + (rowOffsets.getOrNull(index) ?: (config.spacing.stepY * index))

    fun anchor(index: Int): TimelineGeometryPoint =
        TimelineGeometryPoint(
            (if (index % 2 == 0) edge else width - edge) - origin,
            rowTop(index) + config.spacing.stepY / 2f,
        )

    /** Начало каждого сегмента совпадает с концом предыдущего; первый начинается в origin. */
    fun segment(index: Int): List<TimelineGeometryPoint> {
        val end = anchor(index)
        val start = if (index == 0) TimelineGeometryPoint(0f, 0f) else anchor(index - 1)
        return listOf(start, TimelineGeometryPoint(start.x, rowTop(index)), TimelineGeometryPoint(end.x, rowTop(index)), end)
    }

    fun iconX(align: TimelineTextAlignment): Float =
        when (align) {
            TimelineTextAlignment.LEFT -> config.spacing.marginHorizontalImage - origin
            TimelineTextAlignment.RIGHT -> width - config.spacing.marginHorizontalImage - config.sizes.sizeImageLvl - origin
            TimelineTextAlignment.CENTER -> width / 2f - config.sizes.sizeImageLvl / 2f - origin
        }

    fun titleX(align: TimelineTextAlignment): Float =
        when (align) {
            TimelineTextAlignment.LEFT -> config.spacing.marginHorizontalText.coerceAtMost(width.toFloat()) - origin
            TimelineTextAlignment.RIGHT -> width - config.spacing.marginHorizontalText.coerceAtMost(width.toFloat()) - origin
            TimelineTextAlignment.CENTER -> width / 2f - origin
        }

    fun iconY(index: Int): Float = anchor(index).y - config.sizes.sizeImageLvl / 2f

    fun titleY(index: Int): Float = rowTop(index) + config.spacing.marginTopTitle

    fun descriptionY(index: Int): Float = titleY(index) + config.spacing.marginTopDescription

    fun layout(): TimelineGeometryLayout =
        TimelineGeometryLayout(
            config.steps.mapIndexed { index, step ->
                val align = if (index % 2 == 0) TimelineTextAlignment.LEFT else TimelineTextAlignment.RIGHT
                val x = titleX(align)
                val textWidth = TimelineGeometryTextWidth.resolve(width, origin, x, align)
                TimelineGeometryStep(step, x, titleY(index), textWidth, x, descriptionY(index), textWidth, iconX(align), iconY(index), align)
            },
            null,
            null,
        )
}
