package com.dmitrypokrasov.timelineview.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import com.dmitrypokrasov.timelineview.R
import com.dmitrypokrasov.timelineview.data.TimelineConstants
import com.dmitrypokrasov.timelineview.data.TimelineStep
import com.dmitrypokrasov.timelineview.domain.TimelineMath
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

    /** Математическая и визуальная конфигурация таймлайна. */
    private var timelineMath: TimelineMath

    /** Битмап неактивного шага. */
    private var iconDisableStep: Bitmap? = null

    /** Битмап текущей иконки прогресса. */
    private var iconProgress: Bitmap? = null

    /** Путь для пройденных шагов. */
    private val pathEnable = Path()

    /** Путь для непройденных шагов. */
    private val pathDisable = Path()

    /** Основная кисть для рисования линий и текста. */
    private val pLine = Paint()

    /** Скругление углов линии пути. */
    private var pathEffect: CornerPathEffect? = null

    init {
        timelineMath = TimelineMath(initMathConfig(attrs), initUiConfig(attrs))
        initTools(timelineMath.mathConfig, timelineMath.uiConfig)
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
        if (timelineMath.mathConfig == timelineMathConfig && timelineMath.uiConfig == timelineUiConfig) return

        timelineMath = TimelineMath(timelineMathConfig, timelineUiConfig)
        initTools(timelineMathConfig, timelineUiConfig)

        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        timelineMath.setMeasuredWidth(measuredWidth)
        timelineMath.buildPath(pathEnable, pathDisable)
        setMeasuredDimension(widthMeasureSpec, timelineMath.mathConfig.measuredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        pLine.reset()
        pLine.style = Paint.Style.STROKE
        pLine.strokeWidth = timelineMath.mathConfig.sizeStroke
        pLine.pathEffect = pathEffect

        canvas.translate(timelineMath.getStartPositionX(), 0f)
        pLine.color = timelineMath.uiConfig.colorProgress
        canvas.drawPath(pathEnable, pLine)
        pLine.color = timelineMath.uiConfig.colorStroke
        canvas.drawPath(pathDisable, pLine)

        // Отрисовка шагов: иконки, заголовки, описания
        pLine.reset()
        pLine.isAntiAlias = true

        var printProgressIcon = false

        timelineMath.mathConfig.steps.forEachIndexed { i, lvl ->
            val horizontalOffset = timelineMath.getHorizontalIconOffset(i)
            val verticalOffset = timelineMath.getVerticalOffset(i)
            val topCoordinates = timelineMath.getTopCoordinates(lvl)
            val leftCoordinates = timelineMath.getLeftCoordinates(lvl)

            if (i == 0) {
                printTitle(pLine, canvas, resources.getString(lvl.title), i, Paint.Align.RIGHT)
                printDescription(
                    pLine, canvas, resources.getString(lvl.description), i, Paint.Align.RIGHT
                )
                printIcon(pLine, lvl, canvas, i, Paint.Align.RIGHT)

                if (lvl.percents != 100) {
                    this@TimelineView.iconProgress?.let {
                        canvas.drawBitmap(it, leftCoordinates, topCoordinates, pLine)
                        printProgressIcon = true
                    }
                }
            } else {

                if (lvl.percents != 100 && !printProgressIcon) {
                    this@TimelineView.iconProgress?.let {
                        canvas.drawBitmap(it, horizontalOffset, verticalOffset, pLine)
                        printProgressIcon = true
                    }
                }

                val align =
                    if (pLine.textAlign == Paint.Align.LEFT) Paint.Align.RIGHT else Paint.Align.LEFT
                printTitle(pLine, canvas, resources.getString(lvl.title), i, align)
                printDescription(pLine, canvas, resources.getString(lvl.description), i, align)
                printIcon(pLine, lvl, canvas, i, align)
            }
        }
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

        builderMathConfig.setRadius(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_radius_size, TimelineConstants.DEFAULT_RADIUS_SIZE
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

        builderMathConfig.setSizeDescription(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_description_size,
                TimelineConstants.DEFAULT_DESCRIPTION_SIZE
            )
        )

        builderMathConfig.setSizeTitle(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_title_size, TimelineConstants.DEFAULT_TITLE_SIZE
            )
        )

        builderMathConfig.setSizeStroke(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_stroke_size, TimelineConstants.DEFAULT_STROKE_SIZE
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

        pathEffect = CornerPathEffect(timelineMathConfig.radius)

        getBitmap(timelineUiConfig.iconDisableLvl)?.let { bitmap ->
            iconDisableStep = bitmap.scale(
                timelineMathConfig.sizeImageLvl.toInt(),
                timelineMathConfig.sizeImageLvl.toInt(),
                false
            )
        }

        getBitmap(timelineUiConfig.iconProgress)?.let { bitmap ->
            iconProgress = bitmap.scale(
                timelineMathConfig.sizeIconProgress.toInt(),
                timelineMathConfig.sizeIconProgress.toInt(),
                false
            )
        }
    }

    /**
     * Отрисовка иконки шага.
     */
    private fun printIcon(
        pLine: Paint,
        lvl: TimelineStep,
        canvas: Canvas,
        i: Int,
        align: Paint.Align
    ) {
        val bm: Bitmap? = when {
            lvl.count == lvl.maxCount && lvl.icon != 0 -> getBitmap(lvl.icon)
            else -> iconDisableStep
        }
        bm?.let {
            val x = timelineMath.getIconXCoordinates(align)
            val y = timelineMath.getIconYCoordinates(i)

            pLine.textAlign = align
            canvas.drawBitmap(it, x, y, pLine)

            Log.d(TAG, "printIcon i: $i; align: $align; x: $x; y: $y")
        }
    }

    /**
     * Отрисовка заголовка шага.
     */
    private fun printTitle(
        pLine: Paint,
        canvas: Canvas,
        title: String,
        i: Int,
        align: Paint.Align
    ) {
        pLine.apply {
            textSize = timelineMath.mathConfig.sizeTitle
            typeface = Typeface.DEFAULT_BOLD
            color = timelineMath.uiConfig.colorTitle
        }

        val x = timelineMath.getTitleXCoordinates(align)
        val y = timelineMath.getTitleYCoordinates(i)

        canvas.drawText(title, x, y, pLine)
        Log.d(TAG, "printTitle i: $i; align: $align; x: $x; y: $y")
    }

    /**
     * Отрисовка описания шага.
     */
    private fun printDescription(
        pLine: Paint,
        canvas: Canvas,
        description: String,
        i: Int,
        align: Paint.Align
    ) {
        pLine.apply {
            textSize = timelineMath.mathConfig.sizeDescription
            typeface = Typeface.DEFAULT
            color = timelineMath.uiConfig.colorDescription
        }

        val x = timelineMath.getTitleXCoordinates(align)
        val y = timelineMath.getDescriptionYCoordinates(i)

        canvas.drawText(description, x, y, pLine)
        Log.d(TAG, "printDescription i: $i; align: $align; x: $x; y: $y")
    }

    /**
     * Получение Bitmap из ресурса, включая поддержку VectorDrawable.
     */
    private fun getBitmap(drawableId: Int): Bitmap? {
        if (drawableId == 0) return null

        return when (val drawable = ContextCompat.getDrawable(context, drawableId)) {
            is BitmapDrawable -> BitmapFactory.decodeResource(context.resources, drawableId)
            is VectorDrawable -> {
                val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }

            else -> throw IllegalArgumentException("Unsupported drawable type")
        }
    }
}
