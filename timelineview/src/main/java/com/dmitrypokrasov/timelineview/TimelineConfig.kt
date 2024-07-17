package com.dmitrypokrasov.timelineview

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.dmitrypokrasov.timelineview.TimelineConstants.DEFAULT_DESCRIPTION_SIZE
import com.dmitrypokrasov.timelineview.TimelineConstants.DEFAULT_ICON_PROGRESS_SIZE
import com.dmitrypokrasov.timelineview.TimelineConstants.DEFAULT_IMAGE_LVL_SIZE
import com.dmitrypokrasov.timelineview.TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_IMAGE
import com.dmitrypokrasov.timelineview.TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_STROKE
import com.dmitrypokrasov.timelineview.TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_TEXT
import com.dmitrypokrasov.timelineview.TimelineConstants.DEFAULT_MARGIN_TOP_DESCRIPTION
import com.dmitrypokrasov.timelineview.TimelineConstants.DEFAULT_MARGIN_TOP_PROGRESS_ICON
import com.dmitrypokrasov.timelineview.TimelineConstants.DEFAULT_MARGIN_TOP_TITLE
import com.dmitrypokrasov.timelineview.TimelineConstants.DEFAULT_RADIUS_SIZE
import com.dmitrypokrasov.timelineview.TimelineConstants.DEFAULT_STEP_Y_FIRST_SIZE
import com.dmitrypokrasov.timelineview.TimelineConstants.DEFAULT_STEP_Y_SIZE
import com.dmitrypokrasov.timelineview.TimelineConstants.DEFAULT_STROKE_SIZE
import com.dmitrypokrasov.timelineview.TimelineConstants.DEFAULT_TITLE_SIZE

data class TimelineConfig(
    val startPosition: StartPosition,

    val steps: List<TimelineStep>,

    val stepY: Float,
    val radius: Float,
    val stepYFirst: Float,

    val marginTopDescription: Float,
    val marginTopTitle: Float,
    val marginTopProgressIcon: Float,
    val marginHorizontalImage: Float,
    val marginHorizontalText: Float,
    val marginHorizontalStroke: Float,

    @DrawableRes val iconDisableLvl: Int,
    @DrawableRes val iconProgress: Int,
    @ColorInt var colorProgress: Int,
    @ColorInt var colorStroke: Int,
    @ColorInt var colorTitle: Int,
    @ColorInt var colorDescription: Int,

    val sizeDescription: Float,
    val sizeTitle: Float,
    val sizeStroke: Float,
    val sizeImageLvl: Int,
    val sizeIconProgress: Int
) {
    enum class StartPosition {
        START, CENTER, END
    }

    class Builder {
        private var startPosition: StartPosition = StartPosition.CENTER

        private var steps: List<TimelineStep> = listOf()

        private var stepY: Float = DEFAULT_STEP_Y_SIZE.toFloat()
        private var radius: Float = DEFAULT_RADIUS_SIZE.toFloat()
        private var stepYFirst: Float = DEFAULT_STEP_Y_FIRST_SIZE.toFloat()

        private var marginTopDescription: Float = DEFAULT_MARGIN_TOP_DESCRIPTION.toFloat()
        private var marginTopTitle: Float = DEFAULT_MARGIN_TOP_TITLE.toFloat()
        private var marginTopProgressIcon: Float = DEFAULT_MARGIN_TOP_PROGRESS_ICON.toFloat()
        private var marginHorizontalImage: Float = DEFAULT_MARGIN_HORIZONTAL_IMAGE.toFloat()
        private var marginHorizontalText: Float = DEFAULT_MARGIN_HORIZONTAL_TEXT.toFloat()
        private var marginHorizontalStroke: Float = DEFAULT_MARGIN_HORIZONTAL_STROKE.toFloat()

        @DrawableRes
        private var iconDisableLvl: Int = 0

        @DrawableRes
        private var iconProgress: Int = 0

        @ColorInt
        private var colorProgress: Int = 0

        @ColorInt
        private var colorStroke: Int = 0

        @ColorInt
        private var colorTitle: Int = 0

        @ColorInt
        private var colorDescription: Int = 0

        private var sizeDescription: Float = DEFAULT_DESCRIPTION_SIZE.toFloat()
        private var sizeTitle: Float = DEFAULT_TITLE_SIZE.toFloat()
        private var sizeStroke: Float = DEFAULT_STROKE_SIZE.toFloat()
        private var sizeImageLvl: Int = DEFAULT_IMAGE_LVL_SIZE
        private var sizeIconProgress: Int = DEFAULT_ICON_PROGRESS_SIZE

        fun setSteps(steps: List<TimelineStep>) = apply { this.steps = steps }
        fun setStartPosition(value: StartPosition) = apply { startPosition = value }
        fun setStepY(value: Float) = apply { stepY = value }
        fun setRadius(value: Float) = apply { radius = value }
        fun setStepYFirst(value: Float) = apply { stepYFirst = value }
        fun setMarginTopDescription(value: Float) = apply { marginTopDescription = value }
        fun setMarginTopTitle(value: Float) = apply { marginTopTitle = value }
        fun setMarginTopProgressIcon(value: Float) = apply { marginTopProgressIcon = value }
        fun setMarginHorizontalImage(value: Float) = apply { marginHorizontalImage = value }
        fun setMarginHorizontalText(value: Float) = apply { marginHorizontalText = value }
        fun setMarginHorizontalStroke(value: Float) = apply { marginHorizontalStroke = value }
        fun setIconDisableLvl(value: Int) = apply { iconDisableLvl = value }
        fun setIconProgress(value: Int) = apply { iconProgress = value }
        fun setSizeDescription(value: Float) = apply { sizeDescription = value }
        fun setSizeTitle(value: Float) = apply { sizeTitle = value }
        fun setSizeStroke(value: Float) = apply { sizeStroke = value }
        fun setSizeImageLvl(value: Int) = apply { sizeImageLvl = value }
        fun setSizeIconProgress(value: Int) = apply { sizeIconProgress = value }
        fun setColorProgress(value: Int) = apply { colorProgress = value }
        fun setColorStroke(value: Int) = apply { colorStroke = value }
        fun setColorTitle(value: Int) = apply { colorTitle = value }
        fun setColorDescription(value: Int) = apply { colorDescription = value }

        fun build(): TimelineConfig {
            return TimelineConfig(
                startPosition = startPosition,
                steps = steps,
                stepY = stepY,
                radius = radius,
                stepYFirst = stepYFirst,
                marginTopDescription = marginTopDescription,
                marginTopTitle = marginTopTitle,
                marginTopProgressIcon = marginTopProgressIcon,
                marginHorizontalImage = marginHorizontalImage,
                marginHorizontalText = marginHorizontalText,
                marginHorizontalStroke = marginHorizontalStroke,
                iconDisableLvl = iconDisableLvl,
                iconProgress = iconProgress,
                sizeDescription = sizeDescription,
                sizeTitle = sizeTitle,
                sizeStroke = sizeStroke,
                sizeImageLvl = sizeImageLvl,
                sizeIconProgress = sizeIconProgress,
                colorDescription = colorDescription,
                colorProgress = colorProgress,
                colorTitle = colorTitle,
                colorStroke = colorStroke
            )
        }
    }
}