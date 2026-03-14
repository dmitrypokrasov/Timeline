package com.dmitrypokrasov.timelineview.ui

import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer
import kotlin.math.ceil
import kotlin.math.max

class TimelineHeightCalculator {
    fun calculateHeight(
        layout: TimelineLayout?,
        mathEngine: TimelineMathEngine,
        uiRenderer: TimelineUiRenderer,
    ): Int {
        if (layout == null) return 0

        val mathConfig = mathEngine.getConfig()
        val resolvedTextBlocks =
            TimelineTextBlockResolver.resolve(
                layout = layout,
                mathEngine = mathEngine,
                uiRenderer = uiRenderer,
            )
        var maxBottom = 0f

        layout.steps.forEachIndexed { index, stepLayout ->
            val textBlock = resolvedTextBlocks.getOrNull(index) ?: return@forEachIndexed
            maxBottom = max(maxBottom, stepLayout.iconY + mathConfig.sizes.sizeImageLvl)
            maxBottom = max(maxBottom, textBlock.titleTop + textBlock.titleHeight)
            maxBottom = max(maxBottom, textBlock.descriptionTop + textBlock.descriptionHeight)
        }

        layout.progressIcon?.let { progressIcon ->
            maxBottom = max(maxBottom, progressIcon.top + mathConfig.sizes.sizeIconProgress)
        }

        return ceil(maxBottom).toInt().coerceAtLeast(0)
    }
}
