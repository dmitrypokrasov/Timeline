package com.dmitrypokrasov.timelineview.ui

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.math.LinearTimelineMath
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import kotlin.math.max

class TimelineHeightCalculator(
    private val textLayoutHelper: TimelineTextLayoutHelper = TimelineTextLayoutHelper()
) {

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    fun calculateHeight(
        mathEngine: TimelineMathEngine,
        uiConfig: TimelineUiConfig,
        layout: TimelineLayout?
    ): Int {
        val mathConfig = mathEngine.getConfig()
        val stepsCount = mathConfig.steps.size
        val horizontal = mathEngine is LinearTimelineMath &&
            mathEngine.orientation == LinearTimelineMath.Orientation.HORIZONTAL
        val baseHeight = if (horizontal) {
            max(mathConfig.sizes.sizeImageLvl, mathConfig.sizes.sizeIconProgress)
        } else {
            (mathConfig.spacing.stepY * stepsCount) + mathConfig.spacing.stepYFirst +
                mathConfig.sizes.sizeIconProgress / 2f
        }
        val textHeight = if (stepsCount == 0) {
            0f
        } else {
            val textBlockHeight = getTextBlockHeight(mathConfig, uiConfig, layout)
            if (horizontal) {
                max(mathConfig.sizes.sizeImageLvl, mathConfig.sizes.sizeIconProgress) / 2f +
                    textBlockHeight
            } else {
                (mathConfig.spacing.stepY * (stepsCount - 1)) + textBlockHeight
            }
        }

        return max(baseHeight, textHeight).toInt()
    }

    private fun getTextBlockHeight(
        mathConfig: TimelineMathConfig,
        uiConfig: TimelineUiConfig,
        layout: TimelineLayout?
    ): Float {
        val titleHeight = measureTextHeight(uiConfig.textSizes.sizeTitle, Typeface.DEFAULT_BOLD)
        val maxDescriptionHeight = layout?.steps
            ?.maxOfOrNull { step ->
                val text = step.step.description?.toString().orEmpty()
                if (text.isBlank()) {
                    0f
                } else {
                    measureDescriptionHeight(uiConfig, text, step.descriptionMaxWidth)
                }
            }
            ?: measureDescriptionHeight(
                uiConfig,
                text = "A",
                maxWidth = resolveTextMaxWidth(1, uiConfig)
            )

        return mathConfig.spacing.marginTopTitle + titleHeight +
            mathConfig.spacing.marginTopDescription + maxDescriptionHeight
    }

    private fun measureDescriptionHeight(
        uiConfig: TimelineUiConfig,
        text: String,
        maxWidth: Int
    ): Float {
        textPaint.textSize = uiConfig.textSizes.sizeDescription
        textPaint.typeface = Typeface.DEFAULT
        return textLayoutHelper.measureDescriptionHeight(
            text = text,
            paint = textPaint,
            maxWidth = resolveTextMaxWidth(maxWidth, uiConfig),
            uiConfig = uiConfig
        )
    }

    private fun resolveTextMaxWidth(layoutWidth: Int, uiConfig: TimelineUiConfig): Int {
        return when (uiConfig.textLayout.textMaxWidthMode) {
            TimelineUiConfig.TextMaxWidthMode.AUTO_FROM_LAYOUT -> layoutWidth
            TimelineUiConfig.TextMaxWidthMode.FIXED ->
                (uiConfig.textLayout.fixedTextMaxWidth ?: layoutWidth.toFloat()).toInt()
        }.coerceAtLeast(1)
    }

    private fun measureTextHeight(textSize: Float, typeface: Typeface): Float {
        textPaint.textSize = textSize
        textPaint.typeface = typeface
        val metrics = textPaint.fontMetrics
        return metrics.descent - metrics.ascent
    }
}
