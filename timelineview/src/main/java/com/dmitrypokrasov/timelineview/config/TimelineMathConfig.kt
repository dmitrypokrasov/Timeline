package com.dmitrypokrasov.timelineview.config

import com.dmitrypokrasov.timelineview.model.TimelineConstants
import com.dmitrypokrasov.timelineview.model.TimelineStepData

/**
 * Конфигурация параметров позиционирования и размеров таймлайна.
 *
 * @property startPosition начальная позиция таймлайна (слева, по центру или справа)
 * @property steps список шагов, образующих таймлайн
 * @property spacing группа отступов таймлайна
 * @property sizes группа размеров таймлайна
 *
 * Хранит только данные без дополнительных вычислений. Вся логика расчётов
 * вынесена в реализации [com.dmitrypokrasov.timelineview.math.TimelineMathEngine].
 */
data class TimelineMathConfig(
    val startPosition: StartPosition = StartPosition.CENTER,
    val steps: List<TimelineStepData> = listOf(),
    var spacing: Spacing = Spacing(),
    var sizes: Sizes = Sizes()
) {
    @Deprecated(
        message = "Use grouped configuration: spacing.stepY",
        replaceWith = ReplaceWith("spacing.stepY")
    )
    var stepY: Float
        get() = spacing.stepY
        set(value) {
            spacing.stepY = value
        }

    @Deprecated(
        message = "Use grouped configuration: spacing.stepYFirst",
        replaceWith = ReplaceWith("spacing.stepYFirst")
    )
    var stepYFirst: Float
        get() = spacing.stepYFirst
        set(value) {
            spacing.stepYFirst = value
        }

    @Deprecated(
        message = "Use grouped configuration: spacing.marginTopDescription",
        replaceWith = ReplaceWith("spacing.marginTopDescription")
    )
    var marginTopDescription: Float
        get() = spacing.marginTopDescription
        set(value) {
            spacing.marginTopDescription = value
        }

    @Deprecated(
        message = "Use grouped configuration: spacing.marginTopTitle",
        replaceWith = ReplaceWith("spacing.marginTopTitle")
    )
    var marginTopTitle: Float
        get() = spacing.marginTopTitle
        set(value) {
            spacing.marginTopTitle = value
        }

    @Deprecated(
        message = "Use grouped configuration: spacing.marginTopProgressIcon",
        replaceWith = ReplaceWith("spacing.marginTopProgressIcon")
    )
    var marginTopProgressIcon: Float
        get() = spacing.marginTopProgressIcon
        set(value) {
            spacing.marginTopProgressIcon = value
        }

    @Deprecated(
        message = "Use grouped configuration: spacing.marginHorizontalImage",
        replaceWith = ReplaceWith("spacing.marginHorizontalImage")
    )
    var marginHorizontalImage: Float
        get() = spacing.marginHorizontalImage
        set(value) {
            spacing.marginHorizontalImage = value
        }

    @Deprecated(
        message = "Use grouped configuration: spacing.marginHorizontalText",
        replaceWith = ReplaceWith("spacing.marginHorizontalText")
    )
    var marginHorizontalText: Float
        get() = spacing.marginHorizontalText
        set(value) {
            spacing.marginHorizontalText = value
        }

    @Deprecated(
        message = "Use grouped configuration: spacing.marginHorizontalStroke",
        replaceWith = ReplaceWith("spacing.marginHorizontalStroke")
    )
    var marginHorizontalStroke: Float
        get() = spacing.marginHorizontalStroke
        set(value) {
            spacing.marginHorizontalStroke = value
        }

    @Deprecated(
        message = "Use grouped configuration: sizes.sizeIconProgress",
        replaceWith = ReplaceWith("sizes.sizeIconProgress")
    )
    var sizeIconProgress: Float
        get() = sizes.sizeIconProgress
        set(value) {
            sizes.sizeIconProgress = value
        }

    @Deprecated(
        message = "Use grouped configuration: sizes.sizeImageLvl",
        replaceWith = ReplaceWith("sizes.sizeImageLvl")
    )
    var sizeImageLvl: Float
        get() = sizes.sizeImageLvl
        set(value) {
            sizes.sizeImageLvl = value
        }

    /** Положение первого шага таймлайна относительно контейнера. */
    enum class StartPosition { START, CENTER, END }

    data class Spacing(
        var stepY: Float = TimelineConstants.DEFAULT_STEP_Y_SIZE,
        var stepYFirst: Float = TimelineConstants.DEFAULT_STEP_Y_FIRST_SIZE,
        var marginTopDescription: Float = TimelineConstants.DEFAULT_MARGIN_TOP_DESCRIPTION,
        var marginTopTitle: Float = TimelineConstants.DEFAULT_MARGIN_TOP_TITLE,
        var marginTopProgressIcon: Float = TimelineConstants.DEFAULT_MARGIN_TOP_PROGRESS_ICON,
        var marginHorizontalImage: Float = TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_IMAGE,
        var marginHorizontalText: Float = TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_TEXT,
        var marginHorizontalStroke: Float = TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_STROKE
    ) {
        init {
            stepY = stepY.coerceAtLeast(0f)
            stepYFirst = stepYFirst.coerceAtLeast(0f)
            marginTopDescription = marginTopDescription.coerceAtLeast(0f)
            marginTopTitle = marginTopTitle.coerceAtLeast(0f)
            marginTopProgressIcon = marginTopProgressIcon.coerceAtLeast(0f)
            marginHorizontalImage = marginHorizontalImage.coerceAtLeast(0f)
            marginHorizontalText = marginHorizontalText.coerceAtLeast(0f)
            marginHorizontalStroke = marginHorizontalStroke.coerceAtLeast(0f)
        }
    }

    data class Sizes(
        var sizeIconProgress: Float = TimelineConstants.DEFAULT_ICON_PROGRESS_SIZE,
        var sizeImageLvl: Float = TimelineConstants.DEFAULT_IMAGE_LVL_SIZE
    ) {
        init {
            sizeIconProgress = sizeIconProgress.coerceAtLeast(0f)
            sizeImageLvl = sizeImageLvl.coerceAtLeast(0f)
        }
    }

    @Deprecated(
        message = "Use grouped configuration via TimelineMathConfig(startPosition, steps, spacing, sizes).",
        replaceWith = ReplaceWith(
            "TimelineMathConfig(startPosition, steps, spacing = Spacing(stepY, stepYFirst, " +
                "marginTopDescription, marginTopTitle, marginTopProgressIcon, " +
                "marginHorizontalImage, marginHorizontalText, marginHorizontalStroke), " +
                "sizes = Sizes(sizeIconProgress, sizeImageLvl))"
        )
    )
    constructor(
        startPosition: StartPosition = StartPosition.CENTER,
        steps: List<TimelineStepData> = listOf(),
        stepY: Float = TimelineConstants.DEFAULT_STEP_Y_SIZE,
        stepYFirst: Float = TimelineConstants.DEFAULT_STEP_Y_FIRST_SIZE,
        marginTopDescription: Float = TimelineConstants.DEFAULT_MARGIN_TOP_DESCRIPTION,
        marginTopTitle: Float = TimelineConstants.DEFAULT_MARGIN_TOP_TITLE,
        marginTopProgressIcon: Float = TimelineConstants.DEFAULT_MARGIN_TOP_PROGRESS_ICON,
        marginHorizontalImage: Float = TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_IMAGE,
        marginHorizontalText: Float = TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_TEXT,
        marginHorizontalStroke: Float = TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_STROKE,
        sizeIconProgress: Float = TimelineConstants.DEFAULT_ICON_PROGRESS_SIZE,
        sizeImageLvl: Float = TimelineConstants.DEFAULT_IMAGE_LVL_SIZE
    ) : this(
        startPosition = startPosition,
        steps = steps,
        spacing = Spacing(
            stepY = stepY,
            stepYFirst = stepYFirst,
            marginTopDescription = marginTopDescription,
            marginTopTitle = marginTopTitle,
            marginTopProgressIcon = marginTopProgressIcon,
            marginHorizontalImage = marginHorizontalImage,
            marginHorizontalText = marginHorizontalText,
            marginHorizontalStroke = marginHorizontalStroke
        ),
        sizes = Sizes(
            sizeIconProgress = sizeIconProgress,
            sizeImageLvl = sizeImageLvl
        )
    )

    class Builder {
        private var startPosition: StartPosition = StartPosition.CENTER
        private var steps: List<TimelineStepData> = listOf()
        private var spacing: Spacing = Spacing()
        private var sizes: Sizes = Sizes()

        fun startPosition(value: StartPosition) = apply { startPosition = value }
        fun steps(value: List<TimelineStepData>) = apply { steps = value }
        fun spacing(value: Spacing) = apply { spacing = value }
        fun sizes(value: Sizes) = apply { sizes = value }

        @Deprecated(
            message = "Use spacing.stepY",
            replaceWith = ReplaceWith("spacing.stepY = value")
        )
        fun stepY(value: Float) = apply { spacing.stepY = value }

        @Deprecated(
            message = "Use spacing.stepYFirst",
            replaceWith = ReplaceWith("spacing.stepYFirst = value")
        )
        fun stepYFirst(value: Float) = apply { spacing.stepYFirst = value }

        @Deprecated(
            message = "Use spacing.marginTopDescription",
            replaceWith = ReplaceWith("spacing.marginTopDescription = value")
        )
        fun marginTopDescription(value: Float) = apply { spacing.marginTopDescription = value }

        @Deprecated(
            message = "Use spacing.marginTopTitle",
            replaceWith = ReplaceWith("spacing.marginTopTitle = value")
        )
        fun marginTopTitle(value: Float) = apply { spacing.marginTopTitle = value }

        @Deprecated(
            message = "Use spacing.marginTopProgressIcon",
            replaceWith = ReplaceWith("spacing.marginTopProgressIcon = value")
        )
        fun marginTopProgressIcon(value: Float) = apply { spacing.marginTopProgressIcon = value }

        @Deprecated(
            message = "Use spacing.marginHorizontalImage",
            replaceWith = ReplaceWith("spacing.marginHorizontalImage = value")
        )
        fun marginHorizontalImage(value: Float) = apply { spacing.marginHorizontalImage = value }

        @Deprecated(
            message = "Use spacing.marginHorizontalText",
            replaceWith = ReplaceWith("spacing.marginHorizontalText = value")
        )
        fun marginHorizontalText(value: Float) = apply { spacing.marginHorizontalText = value }

        @Deprecated(
            message = "Use spacing.marginHorizontalStroke",
            replaceWith = ReplaceWith("spacing.marginHorizontalStroke = value")
        )
        fun marginHorizontalStroke(value: Float) = apply { spacing.marginHorizontalStroke = value }

        @Deprecated(
            message = "Use sizes.sizeIconProgress",
            replaceWith = ReplaceWith("sizes.sizeIconProgress = value")
        )
        fun sizeIconProgress(value: Float) = apply { sizes.sizeIconProgress = value }

        @Deprecated(
            message = "Use sizes.sizeImageLvl",
            replaceWith = ReplaceWith("sizes.sizeImageLvl = value")
        )
        fun sizeImageLvl(value: Float) = apply { sizes.sizeImageLvl = value }

        fun build(): TimelineMathConfig = TimelineMathConfig(
            startPosition = startPosition,
            steps = steps,
            spacing = spacing,
            sizes = sizes
        )
    }

    companion object {
        fun builder(): Builder = Builder()
    }
}
