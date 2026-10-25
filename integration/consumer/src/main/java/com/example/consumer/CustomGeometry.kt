package com.example.consumer

import android.content.Context
import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryBounds
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryInput
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryIntrinsicWidth
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryLayout
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryResult
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryStep
import com.dmitrypokrasov.timelineview.geometry.TimelineGeometryStrategy
import com.dmitrypokrasov.timelineview.geometry.TimelinePathCommand
import com.dmitrypokrasov.timelineview.geometry.TimelineTextAlignment
import com.dmitrypokrasov.timelineview.math.TimelineGeometryMathAdapter
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.render.BaseTimelineUi
import com.dmitrypokrasov.timelineview.strategy.TimelineCachePolicy
import com.dmitrypokrasov.timelineview.strategy.TimelineStrategyRegistry
import com.dmitrypokrasov.timelineview.strategy.registerMath
import com.dmitrypokrasov.timelineview.strategy.registerUi
import com.dmitrypokrasov.timelineview.ui.TimelineView

/** Геометрия приложения: высота каждой ячейки зависит от измеренного текста. */
class CustomGeometry : TimelineGeometryStrategy, TimelineGeometryIntrinsicWidth {
    override fun desiredWidth(config: TimelineMathConfig): Int = maxOf(480, (config.sizes.sizeImageLvl + 48f).toInt())

    override fun calculate(input: TimelineGeometryInput): TimelineGeometryResult {
        var bottom = 0f
        val steps = input.config.steps.mapIndexed { index, step ->
            val top = bottom
            val metrics = input.textMetrics.getOrNull(index)
            val height = maxOf(80f, input.config.sizes.sizeImageLvl, (metrics?.titleHeight ?: 0) + (metrics?.descriptionHeight ?: 0) + 40f)
            bottom += height
            TimelineGeometryStep(step, 40f, top + 20f, (input.width - 48).coerceAtLeast(1),
                40f, top + 28f + (metrics?.titleHeight ?: 0), (input.width - 48).coerceAtLeast(1),
                0f, top, TimelineTextAlignment.LEFT)
        }
        val path = if (steps.isEmpty()) emptyList() else listOf(TimelinePathCommand.Move(12f, 0f), TimelinePathCommand.Line(12f, bottom))
        return TimelineGeometryResult(TimelineGeometryLayout(steps, null, null), emptyList(), path,
            TimelineGeometryBounds(0f, 0f, maxOf(input.width.toFloat(), if (steps.isEmpty()) 0f else maxOf(41f, input.config.sizes.sizeImageLvl)), bottom))
    }
}

/** Собственный renderer приложения получает ресурсы и lifecycle через BaseTimelineUi. */
class CustomRenderer(config: TimelineUiConfig) : BaseTimelineUi(config)

/** Компилируется и запускается только против артефакта из временного Maven repository. */
fun customTimeline(context: Context): TimelineView {
    val mathKey = StrategyKey("consumer.geometry")
    val uiKey = StrategyKey("consumer.renderer")
    val registry = TimelineStrategyRegistry.createLocalRegistry().apply {
        registerMath(mathKey, TimelineCachePolicy.CONFIGURATION_WITH_PROGRESS_REUSE) { TimelineGeometryMathAdapter(it, CustomGeometry()) }
        registerUi(uiKey, TimelineCachePolicy.CONFIGURATION_WITH_PROGRESS_REUSE) { CustomRenderer(it) }
    }
    return TimelineView(context).apply {
        setOnClickListener {
            setStrategy(TimelineMathStrategy.LinearVertical, TimelineUiStrategy.Linear)
            tag = "custom-switched"
        }
        setStrategyRegistry(registry, strict = true)
        setConfig(TimelineConfig(math = TimelineMathConfig(steps = listOf(TimelineStepData(title = "External geometry", progress = 40))),
            ui = TimelineUiConfig(), mathStrategyKey = mathKey, uiStrategyKey = uiKey))
    }
}
