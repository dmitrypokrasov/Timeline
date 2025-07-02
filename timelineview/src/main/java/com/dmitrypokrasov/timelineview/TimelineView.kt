package com.dmitrypokrasov.timelineview

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

class TimelineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "TimelineView"
    }

    private var timelineMath: TimelineMath

    private var iconDisableStep: Bitmap? = null
    private var iconProgress: Bitmap? = null
    private val pathEnable = Path()
    private val pathDisable = Path()
    private val pLine = Paint()
    private var pathEffect: CornerPathEffect? = null

    init {
        timelineMath = TimelineMath(initConfig(attrs))
        initTools(timelineMath.config)
    }

    fun replaceSteps(steps: List<TimelineStep>) {
        timelineMath.replaceSteps(steps)
        requestLayout()
    }

    fun setConfig(timelineConfig: TimelineConfig) {
        if (timelineMath.config == timelineConfig) return

        timelineMath = TimelineMath(timelineConfig)

        initTools(timelineConfig)
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        timelineMath.setMeasuredWidth(measuredWidth)
        timelineMath.buildPath(pathEnable, pathDisable)
        setMeasuredDimension(widthMeasureSpec, timelineMath.config.measuredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        pLine.reset()
        pLine.style = Paint.Style.STROKE
        pLine.strokeWidth = timelineMath.config.sizeStroke
        pLine.pathEffect = pathEffect

        canvas.translate(timelineMath.getStartPositionX(), 0f)
        pLine.color = timelineMath.config.colorProgress
        canvas.drawPath(pathEnable, pLine)
        pLine.color = timelineMath.config.colorStroke
        canvas.drawPath(pathDisable, pLine)

        pLine.reset()
        pLine.isAntiAlias = true

        var printProgressIcon = false

        timelineMath.config.steps.forEachIndexed { i, lvl ->
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

    @SuppressLint("CustomViewStyleable")
    private fun initConfig(attrs: AttributeSet?): TimelineConfig {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TimelineView)

        val builder = TimelineConfig.Builder()

        builder.setStartPosition(
            TimelineConfig.StartPosition.entries[typedArray.getInt(
                R.styleable.TimelineView_timeline_start_position,
                TimelineConfig.StartPosition.CENTER.ordinal
            )]
        )

        builder.setStepY(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_step_y_size, TimelineConstants.DEFAULT_STEP_Y_SIZE
            )
        )

        builder.setRadius(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_radius_size, TimelineConstants.DEFAULT_RADIUS_SIZE
            )
        )

        builder.setStepYFirst(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_step_y_first_size,
                TimelineConstants.DEFAULT_STEP_Y_FIRST_SIZE
            )
        )

        builder.setColorProgress(
            typedArray.getColor(
                R.styleable.TimelineView_timeline_progress_color,
                ContextCompat.getColor(context, TimelineConstants.DEFAULT_PROGRESS_COLOR)
            )
        )

        builder.setColorStroke(
            typedArray.getColor(
                R.styleable.TimelineView_timeline_stroke_color,
                ContextCompat.getColor(context, TimelineConstants.DEFAULT_STROKE_COLOR)
            )
        )

        builder.setColorTitle(
            typedArray.getColor(
                R.styleable.TimelineView_timeline_title_color,
                ContextCompat.getColor(context, TimelineConstants.DEFAULT_TITLE_COLOR)
            )
        )

        builder.setColorDescription(
            typedArray.getColor(
                R.styleable.TimelineView_timeline_description_color,
                ContextCompat.getColor(context, TimelineConstants.DEFAULT_DESCRIPTION_COLOR)
            )
        )

        builder.setMarginTopDescription(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_margin_top_description,
                TimelineConstants.DEFAULT_MARGIN_TOP_DESCRIPTION
            )
        )

        builder.setMarginTopTitle(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_margin_top_title,
                TimelineConstants.DEFAULT_MARGIN_TOP_TITLE
            )
        )

        builder.setMarginTopProgressIcon(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_margin_top_progress_icon,
                TimelineConstants.DEFAULT_MARGIN_TOP_PROGRESS_ICON
            )
        )

        builder.setMarginHorizontalImage(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_margin_horizontal_image,
                TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_IMAGE
            )
        )

        builder.setMarginHorizontalText(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_margin_horizontal_text,
                TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_TEXT
            )
        )

        builder.setMarginHorizontalStroke(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_margin_horizontal_stroke,
                TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_STROKE
            )
        )

        builder.setSizeDescription(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_description_size,
                TimelineConstants.DEFAULT_DESCRIPTION_SIZE
            )
        )

        builder.setSizeTitle(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_title_size, TimelineConstants.DEFAULT_TITLE_SIZE
            )
        )

        builder.setSizeStroke(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_stroke_size, TimelineConstants.DEFAULT_STROKE_SIZE
            )
        )

        builder.setSizeImageLvl(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_image_lvl_size,
                TimelineConstants.DEFAULT_IMAGE_LVL_SIZE
            )
        )

        builder.setSizeIconProgress(
            typedArray.getDimension(
                R.styleable.TimelineView_timeline_icon_progress_size,
                TimelineConstants.DEFAULT_ICON_PROGRESS_SIZE
            )
        )

        builder.setIconDisableLvl(
            typedArray.getResourceId(
                R.styleable.TimelineView_timeline_disable_icon, 0
            )
        )
        builder.setIconProgress(
            typedArray.getResourceId(
                R.styleable.TimelineView_timeline_progress_icon, 0
            )
        )

        typedArray.recycle()

        return builder.build()
    }

    private fun initTools(timelineConfig: TimelineConfig) {
        Log.d(TAG, "initTools timelineConfig: $timelineConfig")

        pathEffect = CornerPathEffect(timelineConfig.radius)

        getBitmap(timelineConfig.iconDisableLvl)?.let { bitmap ->
            iconDisableStep = bitmap.scale(
                timelineConfig.sizeImageLvl.toInt(),
                timelineConfig.sizeImageLvl.toInt(),
                false
            )
        }

        getBitmap(timelineConfig.iconProgress)?.let { bitmap ->
            iconProgress = bitmap.scale(
                timelineConfig.sizeIconProgress.toInt(),
                timelineConfig.sizeIconProgress.toInt(),
                false
            )
        }
    }

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

    private fun printTitle(
        pLine: Paint,
        canvas: Canvas,
        title: String,
        i: Int,
        align: Paint.Align
    ) {
        pLine.apply {
            textSize = timelineMath.config.sizeTitle
            typeface = Typeface.DEFAULT_BOLD
            color = timelineMath.config.colorTitle
        }

        val x = timelineMath.getTitleXCoordinates(align)
        val y = timelineMath.getTitleYCoordinates(i)

        canvas.drawText(title, x, y, pLine)
        Log.d(TAG, "printTitle i: $i; align: $align; x: $x; y: $y")
    }

    private fun printDescription(
        pLine: Paint,
        canvas: Canvas,
        description: String,
        i: Int,
        align: Paint.Align
    ) {
        pLine.apply {
            textSize = timelineMath.config.sizeDescription
            typeface = Typeface.DEFAULT
            color = timelineMath.config.colorDescription
        }

        val x = timelineMath.getTitleXCoordinates(align)
        val y = timelineMath.getDescriptionYCoordinates(i)

        canvas.drawText(description, x, y, pLine)
        Log.d(TAG, "printDescription i: $i; align: $align; x: $x; y: $y")
    }

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
