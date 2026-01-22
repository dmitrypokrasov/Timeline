package com.dmitrypokrasov.timelineview.ui

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.math.LinearTimelineMath
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import kotlin.math.max

class TimelineHeightCalculator {

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    fun calculateHeight(mathEngine: TimelineMathEngine, uiConfig: TimelineUiConfig): Int {
        val mathConfig = mathEngine.getConfig()
        val stepsCount = mathConfig.steps.size
        val horizontal = mathEngine is LinearTimelineMath &&
            mathEngine.orientation == LinearTimelineMath.Orientation.HORIZONTAL
        val baseHeight = if (horizontal) {
            max(mathConfig.sizeImageLvl, mathConfig.sizeIconProgress)
        } else {
            (mathConfig.stepY * stepsCount) + mathConfig.stepYFirst + mathConfig.sizeIconProgress / 2f
        }
        val textHeight = if (stepsCount == 0) {
            0f
        } else {
            val textBlockHeight = getTextBlockHeight(mathConfig, uiConfig)
            if (horizontal) {
                max(mathConfig.sizeImageLvl, mathConfig.sizeIconProgress) / 2f + textBlockHeight
            } else {
                (mathConfig.stepY * (stepsCount - 1)) + textBlockHeight
            }
        }

        return max(baseHeight, textHeight).toInt()
    }

    private fun getTextBlockHeight(
        mathConfig: TimelineMathConfig,
        uiConfig: TimelineUiConfig
    ): Float {
        val titleHeight = measureTextHeight(uiConfig.sizeTitle, Typeface.DEFAULT_BOLD)
        val descriptionHeight = measureTextHeight(uiConfig.sizeDescription, Typeface.DEFAULT)
        return mathConfig.marginTopTitle + titleHeight +
            mathConfig.marginTopDescription + descriptionHeight
    }

    private fun measureTextHeight(textSize: Float, typeface: Typeface): Float {
        textPaint.textSize = textSize
        textPaint.typeface = typeface
        val metrics = textPaint.fontMetrics
        return metrics.descent - metrics.ascent
    }
}
