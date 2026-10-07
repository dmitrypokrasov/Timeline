package com.dmitrypokrasov.timelineview.math

import android.graphics.Paint
import android.graphics.Path
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.math.data.TimelineLayoutStep
import com.dmitrypokrasov.timelineview.math.data.TimelineProgressIcon
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.model.indexOfStep

/** Measured row strategies sharing curved paths and stable, index-based coordinates. */
abstract class AdaptiveTimelineMath(config: TimelineMathConfig) : TimelineMathEngine {
    protected var input: TimelineMathConfig = config.copy(steps = config.steps.toList())
        private set
    protected var width: Int = 0
        private set
    private var offsets = floatArrayOf()
    private var radius = 0f
    private var cachedLayout: TimelineLayout? = null
    private var cachedPaths: List<TimelinePathGeometry>? = null

    protected abstract fun columns(): Int

    protected abstract fun createSteps(): List<TimelineLayoutStep>

    protected open fun rowDistance(row: Int): Float = input.spacing.stepY * row

    protected fun rowTop(row: Int): Float = input.spacing.stepYFirst + (offsets.getOrNull(row) ?: rowDistance(row))

    override val hasRoundedGeometry: Boolean get() = true

    private fun invalidate() {
        cachedLayout = null
        cachedPaths = null
    }

    override fun setConfig(config: TimelineMathConfig) {
        input = config.copy(steps = config.steps.toList())
        offsets = floatArrayOf()
        invalidate()
    }

    override fun getConfig(): TimelineMathConfig = input

    override fun replaceSteps(steps: List<TimelineStepData>) = setConfig(input.copy(steps = steps))

    override fun getSteps(): List<TimelineStepData> = input.steps

    override fun getStartPosition(): Float = 0f

    override fun setMeasuredWidth(measuredWidth: Int) {
        width = measuredWidth.coerceAtLeast(0)
        invalidate()
    }

    override fun setCornerRadius(radius: Float) {
        require(radius.isFinite() && radius >= 0f)
        this.radius = radius
        invalidate()
    }

    override fun getContentRows(): List<Int> = input.steps.indices.map { it / columns() }

    override fun setStepExtents(extents: List<Float>) {
        require(extents.all { it.isFinite() && it >= 0f })
        offsets = if (extents.isEmpty()) floatArrayOf() else FloatArray(extents.size + 1)
        extents.forEachIndexed { index, extent ->
            val minimum = rowDistance(index + 1) - rowDistance(index)
            offsets[index + 1] = offsets[index] + maxOf(extent, minimum)
        }
        invalidate()
    }

    private fun paths(steps: List<TimelineLayoutStep>): List<TimelinePathGeometry> =
        cachedPaths ?: steps.mapIndexed { index, step ->
            val half = input.sizes.sizeImageLvl / 2f
            val end = TimelinePoint(step.iconX + half, step.iconY + half)
            val previous = steps.getOrNull(index - 1)
            val start = previous?.let { TimelinePoint(it.iconX + half, it.iconY + half) } ?: TimelinePoint(end.x, 0f)
            val points =
                if (textBelowBadge && previous != null && index / columns() != (index - 1) / columns()) {
                    val edge = if (((index - 1) / columns()) % 2 == 0) width.toFloat() else 0f
                    listOf(start, TimelinePoint(edge, start.y), TimelinePoint(edge, end.y), end)
                } else {
                    listOf(start, end)
                }
            TimelinePathGeometry(points, radius)
        }.also { cachedPaths = it }

    override fun buildLayout(): TimelineLayout =
        cachedLayout ?: createSteps().let { steps ->
            val active = input.steps.indexOfFirst { it.progress < 100 }.takeIf { it >= 0 }
            val point = active?.let { paths(steps)[it].point(input.steps[it].progress / 100f) }
            TimelineLayout(steps, point?.let { TimelineProgressIcon(it.x - input.sizes.sizeIconProgress / 2f, it.y - input.sizes.sizeIconProgress / 2f) }, active)
        }.also { cachedLayout = it }

    override fun buildPath(
        pathEnable: Path,
        pathDisable: Path,
    ) = drawTimelineSegments(paths(buildLayout().steps), input.steps, input.progressMode, pathEnable, pathDisable)

    private fun point(index: Int): TimelinePoint = paths(buildLayout().steps)[index].point(input.steps[index].progress / 100f)

    override fun getHorizontalIconOffset(i: Int): Float = point(i).x - input.sizes.sizeIconProgress / 2f

    override fun getVerticalOffset(i: Int): Float = point(i).y - input.sizes.sizeIconProgress / 2f

    override fun getLeftCoordinates(step: TimelineStepData): Float = getHorizontalIconOffset(input.steps.indexOfStep(step))

    override fun getTopCoordinates(step: TimelineStepData): Float = getVerticalOffset(input.steps.indexOfStep(step))

    override fun getIconYCoordinates(i: Int): Float = buildLayout().steps[i].iconY

    override fun getTitleYCoordinates(i: Int): Float = buildLayout().steps[i].titleY

    override fun getDescriptionYCoordinates(i: Int): Float = buildLayout().steps[i].descriptionY

    override fun getTitleXCoordinates(align: Paint.Align): Float = buildLayout().steps.firstOrNull { it.textAlign == align }?.titleX ?: width / 2f

    override fun getIconXCoordinates(align: Paint.Align): Float = buildLayout().steps.firstOrNull { it.textAlign == align }?.iconX ?: width / 2f
}

/** Alternates event labels around a central vertical axis. */
open class AlternatingTimelineMath(config: TimelineMathConfig) : AdaptiveTimelineMath(config) {
    override fun columns(): Int = 1

    override fun createSteps(): List<TimelineLayoutStep> =
        input.steps.mapIndexed { index, step ->
            val half = input.sizes.sizeImageLvl / 2f
            val gap = maxOf(4f, input.spacing.marginHorizontalText - input.spacing.marginHorizontalStroke)
            val twoColumns = width >= 2f * (input.minCellWidth + half + gap)
            val center = if (twoColumns) width / 2f else half
            val left = twoColumns && index % 2 == 0
            val textWidth = (if (twoColumns) center - half - gap else width - 2f * half - gap).toInt().coerceAtLeast(1)
            val x = if (left) (center - half - gap).coerceAtLeast(0f) else (center + half + gap).coerceAtMost(width.toFloat())
            val y = rowTop(index)
            TimelineLayoutStep(step, x, y + input.spacing.marginTopTitle, textWidth, x, y + input.spacing.marginTopTitle + input.spacing.marginTopDescription, textWidth, center - half, y, if (left) Paint.Align.RIGHT else Paint.Align.LEFT)
        }
}

/** Responsive serpentine grid; row heights expand to fit the tallest cell's measured content. */
class AdaptiveGridTimelineMath(config: TimelineMathConfig) : AdaptiveTimelineMath(config) {
    override val textBelowBadge: Boolean get() = true

    override fun columns(): Int = (width / maxOf(input.minCellWidth, input.sizes.sizeImageLvl + 8f)).toInt().coerceIn(1, input.steps.size.coerceAtLeast(1))

    override fun createSteps(): List<TimelineLayoutStep> {
        val count = columns()
        val cell = width.toFloat() / count
        val half = input.sizes.sizeImageLvl / 2f
        return input.steps.mapIndexed { index, step ->
            val row = index / count
            val col = if (row % 2 == 0) index % count else count - 1 - index % count
            val x = cell * (col + 0.5f)
            val y = rowTop(row)
            val textWidth = (cell - 8f).toInt().coerceAtLeast(1)
            TimelineLayoutStep(step, x, y + input.spacing.marginTopTitle, textWidth, x, y + input.spacing.marginTopTitle + input.spacing.marginTopDescription, textWidth, x - half, y, Paint.Align.CENTER)
        }
    }
}

/** Alternating events with a time scale. Timestamps must be present and sorted; colliding labels expand gaps. */
class TimeScaledTimelineMath(config: TimelineMathConfig) : AlternatingTimelineMath(config) {
    init {
        validate(config)
    }

    override fun setConfig(config: TimelineMathConfig) {
        validate(config)
        super.setConfig(config)
    }

    private fun validate(config: TimelineMathConfig) {
        require(config.steps.all { it.timestampMillis != null }) { "Time-scaled steps require timestamps" }
        require(config.steps.zipWithNext().all { (a, b) -> a.timestampMillis!! <= b.timestampMillis!! }) { "Timestamps must be sorted" }
        val first = config.steps.firstOrNull()?.timestampMillis ?: return
        val last = config.steps.last().timestampMillis!!
        require((last.toDouble() - first.toDouble()) * config.pixelsPerMillisecond <= 10_000_000.0) { "Time scale exceeds the supported content height; reduce pixelsPerMillisecond" }
    }

    override fun rowDistance(row: Int): Float {
        val first = input.steps.firstOrNull()?.timestampMillis ?: return 0f
        val time = input.steps.getOrNull(row)?.timestampMillis ?: return 0f
        return ((time.toDouble() - first.toDouble()) * input.pixelsPerMillisecond).toFloat()
    }
}
