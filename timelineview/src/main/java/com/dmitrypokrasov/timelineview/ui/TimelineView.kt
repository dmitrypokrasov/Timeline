package com.dmitrypokrasov.timelineview.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.dmitrypokrasov.timelineview.R
import com.dmitrypokrasov.timelineview.data.TimelineConstants
import com.dmitrypokrasov.timelineview.data.TimelineStep
import com.dmitrypokrasov.timelineview.domain.TimelineMath
import com.dmitrypokrasov.timelineview.domain.TimelineUi
import com.dmitrypokrasov.timelineview.domain.data.TimelineMathConfig
import com.dmitrypokrasov.timelineview.domain.data.TimelineUiConfig

/**
 * Кастомное View для отображения вертикального таймлайна с уровнями прогресса.
 *
 * Поддерживает настройку через XML и код:
 * - визуальные параметры (цвета, размеры, иконки)
 * - математические параметры (отступы, шаги, смещения)
 * - автоматическую отрисовку линий прогресса и иконок
 *
 * Отрисовка включает:
 * - пройденные шаги (enable path)
 * - непройденные шаги (disable path)
 * - иконку текущего шага (progress icon)
 * - заголовки и описания
 *
 * Использует [TimelineMath] как движок для всех вычислений и генерации координат.
 *
 * @constructor Создаёт [TimelineView], читая параметры из XML или по умолчанию.
 */
class TimelineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "TimelineView"
    }

    /** Математическая конфигурация таймлайна. */
    private var timelineMath: TimelineMath

    /** Визуальная конфигурация таймлайна. */
    private var timelineUi: TimelineUi

    init {
        timelineMath = TimelineMath(initMathConfig(attrs))
        timelineUi = TimelineUi(initUiConfig(attrs))
        initTools(timelineMath.mathConfig, timelineUi.uiConfig)
    }

    /**
     * Обновляет список шагов и перерисовывает таймлайн.
     */
    fun replaceSteps(steps: List<TimelineStep>) {
        timelineMath.replaceSteps(steps)
        requestLayout()
    }

    /**
     * Устанавливает новую конфигурацию таймлайна.
     */
    fun setConfig(timelineMathConfig: TimelineMathConfig, timelineUiConfig: TimelineUiConfig) {
        if (timelineMath.mathConfig == timelineMathConfig && timelineUi.uiConfig == timelineUiConfig) return

        timelineMath = TimelineMath(timelineMathConfig)
        timelineUi = TimelineUi(timelineUiConfig)

        initTools(timelineMathConfig, timelineUiConfig)

        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        timelineMath.setMeasuredWidth(measuredWidth)
        timelineMath.buildPath(timelineUi.pathEnable, timelineUi.pathDisable)
        setMeasuredDimension(widthMeasureSpec, timelineMath.getMeasuredHeight())
    }

    override fun onDraw(canvas: Canvas) {
        timelineUi.resetFromPaintTools()

        canvas.translate(timelineMath.getStartPosition(), 0f)
        timelineUi.drawProgressPath(canvas)
        timelineUi.drawDisablePath(canvas)

        // Отрисовка шагов: иконки, заголовки, описания
        timelineUi.resetFromTextTools()
        timelineUi.resetFromIconTools()

        var printProgressIcon = false
        var align = Paint.Align.LEFT

        timelineMath.mathConfig.steps.forEachIndexed { i, lvl ->
            if (i == 0) {
                timelineUi.printTitle(
                    canvas,
                    resources.getString(lvl.title),
                    timelineMath.getTitleXCoordinates(align),
                    timelineMath.getTitleYCoordinates(i),
                    align
                )
                timelineUi.printDescription(
                    canvas,
                    resources.getString(lvl.description),
                    timelineMath.getTitleXCoordinates(align),
                    timelineMath.getDescriptionYCoordinates(i),
                    align
                )
                timelineUi.printIcon(
                    lvl,
                    canvas,
                    align,
                    context,
                    timelineMath.getIconXCoordinates(align),
                    timelineMath.getIconYCoordinates(i)
                )

                if (lvl.percents != 100) {
                    timelineUi.drawProgressBitmap(
                        canvas,
                        timelineMath.getLeftCoordinates(lvl),
                        timelineMath.getTopCoordinates(lvl)
                    )
                    printProgressIcon = true
                }
            } else {
                if (lvl.percents != 100 && !printProgressIcon) {
                    timelineUi.drawProgressBitmap(
                        canvas,
                        timelineMath.getHorizontalIconOffset(i),
                        timelineMath.getVerticalOffset(i)
                    )
                    printProgressIcon = true
                }

                align = if (align == Paint.Align.LEFT) Paint.Align.RIGHT else Paint.Align.LEFT

                timelineUi.printTitle(
                    canvas,
                    resources.getString(lvl.title),
                    timelineMath.getTitleXCoordinates(align),
                    timelineMath.getTitleYCoordinates(i),
                    align
                )
                timelineUi.printDescription(
                    canvas,
                    resources.getString(lvl.description),
                    timelineMath.getTitleXCoordinates(align),
                    timelineMath.getDescriptionYCoordinates(i),
                    align
                )
                timelineUi.printIcon(
                    lvl,
                    canvas,
                    align,
                    context,
                    timelineMath.getIconXCoordinates(align),
                    timelineMath.getIconYCoordinates(i)
                )
            }
        }
    }

    /**
     * Инициализирует визуальные элементы (битмапы, pathEffect).
     */
    private fun initTools(
        timelineMathConfig: TimelineMathConfig,
        timelineUiConfig: TimelineUiConfig
    ) {
        Log.d(
            TAG,
            "initTools timelineMathConfig: $timelineMathConfig, timelineUiConfig: $timelineUiConfig"
        )

        timelineUi.initTools(timelineMathConfig, context)
    }

    /**
     * Инициализация математической конфигурации из XML-атрибутов.
     */
    @SuppressLint("CustomViewStyleable")
    private fun initMathConfig(attrs: AttributeSet?): TimelineMathConfig {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TimelineView)

        val builderUiConfig = TimelineUiConfig.Builder()
        val builderMathConfig = TimelineMathConfig.Builder()

        builderMathConfig.setStartPosition(
            TimelineMathConfig.StartPosition.entries[typedArray.getInt(
                R.styleable.TimelineView_timeline_start_position,
                TimelineMathConfig.StartPosition.CENTER.ordinal
            )]
        )

        builderMathConfig.setStepY(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_step_y_size, TimelineConstants.DEFAULT_STEP_Y_SIZE
            )
        )

        builderMathConfig.setStepYFirst(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_step_y_first_size,
                TimelineConstants.DEFAULT_STEP_Y_FIRST_SIZE
            )
        )

        builderMathConfig.setMarginTopDescription(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_margin_top_description,
                TimelineConstants.DEFAULT_MARGIN_TOP_DESCRIPTION
            )
        )

        builderMathConfig.setMarginTopTitle(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_margin_top_title,
                TimelineConstants.DEFAULT_MARGIN_TOP_TITLE
            )
        )

        builderMathConfig.setMarginTopProgressIcon(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_margin_top_progress_icon,
                TimelineConstants.DEFAULT_MARGIN_TOP_PROGRESS_ICON
            )
        )

        builderMathConfig.setMarginHorizontalImage(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_margin_horizontal_image,
                TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_IMAGE
            )
        )

        builderMathConfig.setMarginHorizontalText(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_margin_horizontal_text,
                TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_TEXT
            )
        )

        builderMathConfig.setMarginHorizontalStroke(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_margin_horizontal_stroke,
                TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_STROKE
            )
        )

        builderMathConfig.setSizeImageLvl(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_image_lvl_size,
                TimelineConstants.DEFAULT_IMAGE_LVL_SIZE
            )
        )

        builderMathConfig.setSizeIconProgress(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_icon_progress_size,
                TimelineConstants.DEFAULT_ICON_PROGRESS_SIZE
            )
        )

        builderUiConfig.setColorProgress(
            typedArray.getColor(
                R.styleable.TimelineView_timeline_progress_color,
                ContextCompat.getColor(context, TimelineConstants.DEFAULT_PROGRESS_COLOR)
            )
        )

        builderUiConfig.setColorStroke(
            typedArray.getColor(
                R.styleable.TimelineView_timeline_stroke_color,
                ContextCompat.getColor(context, TimelineConstants.DEFAULT_STROKE_COLOR)
            )
        )

        builderUiConfig.setColorTitle(
            typedArray.getColor(
                R.styleable.TimelineView_timeline_title_color,
                ContextCompat.getColor(context, TimelineConstants.DEFAULT_TITLE_COLOR)
            )
        )

        builderUiConfig.setColorDescription(
            typedArray.getColor(
                R.styleable.TimelineView_timeline_description_color,
                ContextCompat.getColor(context, TimelineConstants.DEFAULT_DESCRIPTION_COLOR)
            )
        )

        builderUiConfig.setIconDisableLvl(
            typedArray.getResourceId(
                R.styleable.TimelineView_timeline_disable_icon, 0
            )
        )
        builderUiConfig.setIconProgress(
            typedArray.getResourceId(
                R.styleable.TimelineView_timeline_progress_icon, 0
            )
        )

        typedArray.recycle()

        return builderMathConfig.build()
    }

    /**
     * Инициализация UI-конфигурации из XML-атрибутов.
     */
    @SuppressLint("CustomViewStyleable")
    private fun initUiConfig(attrs: AttributeSet?): TimelineUiConfig {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TimelineView)

        val builderUiConfig = TimelineUiConfig.Builder()

        builderUiConfig.setColorProgress(
            typedArray.getColor(
                R.styleable.TimelineView_timeline_progress_color,
                ContextCompat.getColor(context, TimelineConstants.DEFAULT_PROGRESS_COLOR)
            )
        )

        builderUiConfig.setRadius(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_radius_size, TimelineConstants.DEFAULT_RADIUS_SIZE
            )
        )

        builderUiConfig.setSizeDescription(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_description_size,
                TimelineConstants.DEFAULT_DESCRIPTION_SIZE
            )
        )

        builderUiConfig.setSizeTitle(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_title_size, TimelineConstants.DEFAULT_TITLE_SIZE
            )
        )

        builderUiConfig.setSizeStroke(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_stroke_size, TimelineConstants.DEFAULT_STROKE_SIZE
            )
        )

        builderUiConfig.setColorStroke(
            typedArray.getColor(
                R.styleable.TimelineView_timeline_stroke_color,
                ContextCompat.getColor(context, TimelineConstants.DEFAULT_STROKE_COLOR)
            )
        )

        builderUiConfig.setColorTitle(
            typedArray.getColor(
                R.styleable.TimelineView_timeline_title_color,
                ContextCompat.getColor(context, TimelineConstants.DEFAULT_TITLE_COLOR)
            )
        )

        builderUiConfig.setColorDescription(
            typedArray.getColor(
                R.styleable.TimelineView_timeline_description_color,
                ContextCompat.getColor(context, TimelineConstants.DEFAULT_DESCRIPTION_COLOR)
            )
        )

        builderUiConfig.setIconDisableLvl(
            typedArray.getResourceId(
                R.styleable.TimelineView_timeline_disable_icon, 0
            )
        )
        builderUiConfig.setIconProgress(
            typedArray.getResourceId(
                R.styleable.TimelineView_timeline_progress_icon, 0
            )
        )

        typedArray.recycle()

        return builderUiConfig.build()
    }
}
