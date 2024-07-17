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
import kotlin.math.abs

class TimelineView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "TimelineView"
    }

    private val steps: MutableList<TimelineStep> = ArrayList()
    private var config: TimelineConfig

    private var iconDisableStep: Bitmap? = null
    private var iconProgress: Bitmap? = null

    private val pathEnable = Path()
    private val pathDisable = Path()
    private val pLine = Paint()
    private var pathEffect: CornerPathEffect? = null

    private var startPositionX = 0f
    private var startPositionDisableStrokeX = 0f

    init {
        config = initConfig(attrs)
        initTools(config)
    }

    fun replaceSteps(steps: List<TimelineStep>) {
        this.steps.clear()
        this.steps.addAll(steps)
        requestLayout()
    }

    fun setConfig(timelineConfig: TimelineConfig) {
        if (this.config == timelineConfig) return

        this.config = timelineConfig
        this.steps.clear()
        this.steps.addAll(timelineConfig.steps)

        initTools(timelineConfig)
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        startPositionX = when (config.startPosition) {
            TimelineConfig.StartPosition.START -> 0f + config.marginHorizontalStroke
            TimelineConfig.StartPosition.CENTER -> measuredWidth / 2f
            TimelineConfig.StartPosition.END -> measuredWidth.toFloat() - config.marginHorizontalStroke
        }

        Log.d(TAG, "onMeasure startPositionX: $startPositionX")

        buildPath()
        setMeasuredDimension(
            widthMeasureSpec, ((config.stepY * steps.size) + config.stepYFirst + 50).toInt()
        )
    }

    override fun onDraw(canvas: Canvas) {
        pLine.reset()
        pLine.style = Paint.Style.STROKE
        pLine.strokeWidth = config.sizeStroke
        pLine.pathEffect = pathEffect

        canvas.translate(startPositionX, 0f)
        pLine.color = config.colorProgress
        canvas.drawPath(pathEnable, pLine)
        pLine.color = config.colorStroke
        canvas.drawPath(pathDisable, pLine)

        pLine.reset()
        pLine.isAntiAlias = true

        var printProgressIcon = false

        steps.forEachIndexed { i, lvl ->
            val horizontalOffset = if (i % 2 == 0) {
                startPositionX - startPositionDisableStrokeX - (config.marginHorizontalStroke + config.marginHorizontalStroke / 2f)
            } else {
                startPositionDisableStrokeX - startPositionX + config.marginHorizontalStroke / 2f + config.sizeIconProgress / 2f
            }
            val verticalOffset = (config.stepY * i) + config.marginTopProgressIcon

            if (i == 0) {
                printTitle(pLine, canvas, resources.getString(lvl.title), i, Paint.Align.RIGHT)
                printDescription(
                    pLine, canvas, resources.getString(lvl.description), i, Paint.Align.RIGHT
                )
                printIcon(pLine, lvl, canvas, i, Paint.Align.RIGHT)

                if (lvl.percents != 100) {
                    this@TimelineView.iconProgress?.let {
                        canvas.drawBitmap(
                            it,
                            if (lvl.count == 0) -config.sizeIconProgress / 2f else -startPositionDisableStrokeX - config.sizeIconProgress / 2f,
                            if (lvl.count == 0) -config.sizeIconProgress / 2f else config.stepYFirst - config.sizeIconProgress / 2f,
                            pLine
                        )
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
            typedArray.getDimensionPixelSize(
                R.styleable.TimelineView_timeline_step_y_size, TimelineConstants.DEFAULT_STEP_Y_SIZE
            ).toFloat()
        )

        builder.setRadius(
            typedArray.getDimensionPixelSize(
                R.styleable.TimelineView_timeline_radius_size, TimelineConstants.DEFAULT_RADIUS_SIZE
            ).toFloat()
        )

        builder.setStepYFirst(
            typedArray.getDimensionPixelSize(
                R.styleable.TimelineView_timeline_step_y_first_size,
                TimelineConstants.DEFAULT_STEP_Y_FIRST_SIZE
            ).toFloat()
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
            typedArray.getDimensionPixelSize(
                R.styleable.TimelineView_timeline_margin_top_description,
                TimelineConstants.DEFAULT_MARGIN_TOP_DESCRIPTION
            ).toFloat()
        )

        builder.setMarginTopTitle(
            typedArray.getDimensionPixelSize(
                R.styleable.TimelineView_timeline_margin_top_title,
                TimelineConstants.DEFAULT_MARGIN_TOP_TITLE
            ).toFloat()
        )

        builder.setMarginTopProgressIcon(
            typedArray.getDimensionPixelSize(
                R.styleable.TimelineView_timeline_margin_top_progress_icon,
                TimelineConstants.DEFAULT_MARGIN_TOP_PROGRESS_ICON
            ).toFloat()
        )

        builder.setMarginHorizontalImage(
            typedArray.getDimensionPixelSize(
                R.styleable.TimelineView_timeline_margin_horizontal_image,
                TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_IMAGE
            ).toFloat()
        )

        builder.setMarginHorizontalText(
            typedArray.getDimensionPixelSize(
                R.styleable.TimelineView_timeline_margin_horizontal_text,
                TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_TEXT
            ).toFloat()
        )

        builder.setMarginHorizontalStroke(
            typedArray.getDimensionPixelSize(
                R.styleable.TimelineView_timeline_margin_horizontal_stroke,
                TimelineConstants.DEFAULT_MARGIN_HORIZONTAL_STROKE
            ).toFloat()
        )

        builder.setSizeDescription(
            typedArray.getDimensionPixelSize(
                R.styleable.TimelineView_timeline_description_size,
                TimelineConstants.DEFAULT_DESCRIPTION_SIZE
            ).toFloat()
        )

        builder.setSizeTitle(
            typedArray.getDimensionPixelSize(
                R.styleable.TimelineView_timeline_title_size, TimelineConstants.DEFAULT_TITLE_SIZE
            ).toFloat()
        )

        builder.setSizeStroke(
            typedArray.getDimensionPixelSize(
                R.styleable.TimelineView_timeline_stroke_size, TimelineConstants.DEFAULT_STROKE_SIZE
            ).toFloat()
        )

        builder.setSizeImageLvl(
            typedArray.getDimensionPixelSize(
                R.styleable.TimelineView_timeline_image_lvl_size,
                TimelineConstants.DEFAULT_IMAGE_LVL_SIZE
            )
        )

        builder.setSizeIconProgress(
            typedArray.getDimensionPixelSize(
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
            iconDisableStep = Bitmap.createScaledBitmap(
                bitmap, timelineConfig.sizeImageLvl, timelineConfig.sizeImageLvl, false
            )
        }

        getBitmap(timelineConfig.iconProgress)?.let { bitmap ->
            iconProgress = Bitmap.createScaledBitmap(
                bitmap, timelineConfig.sizeIconProgress, timelineConfig.sizeIconProgress, false
            )
        }
    }

    private fun printIcon(
        pLine: Paint, lvl: TimelineStep, canvas: Canvas, i: Int, align: Paint.Align
    ) {
        val bm: Bitmap? = when {
            lvl.count == lvl.maxCount && lvl.icon != 0 -> getBitmap(lvl.icon)
            else -> iconDisableStep
        }
        bm?.let {
            val stepX = (measuredWidth - config.marginHorizontalStroke * 2)
            val x: Float = when (align) {
                Paint.Align.LEFT -> if (config.startPosition == TimelineConfig.StartPosition.CENTER) startPositionX - config.marginHorizontalImage - config.sizeImageLvl else -startPositionX + stepX + config.marginHorizontalImage
                Paint.Align.CENTER -> startPositionX
                Paint.Align.RIGHT -> -(startPositionX - config.marginHorizontalImage)
            }

            val y =
                (config.stepY * i) + config.marginTopTitle - (config.stepY - config.sizeImageLvl) / 2

            pLine.textAlign = align
            canvas.drawBitmap(it, x, y, pLine)

            Log.d(TAG, "printIcon i: $i; align: $align; x: $x; y: $y")
        }
    }

    private fun printTitle(
        pLine: Paint, canvas: Canvas, title: String, i: Int, align: Paint.Align
    ) {
        pLine.apply {
            textSize = config.sizeTitle
            typeface = Typeface.DEFAULT_BOLD
            color = config.colorTitle
        }

        val stepX = (measuredWidth - config.marginHorizontalStroke * 2)

        val x = when (align) {
            Paint.Align.LEFT -> if (config.startPosition == TimelineConfig.StartPosition.CENTER) startPositionX - config.marginHorizontalText else -startPositionX + stepX
            Paint.Align.CENTER -> startPositionX
            Paint.Align.RIGHT -> -(startPositionX - config.marginHorizontalText)
        }
        val y = (config.stepY * i) + config.marginTopTitle


        canvas.drawText(title, x, y, pLine)
        Log.d(TAG, "printTitle i: $i; align: $align; x: $x; y: $y")
    }

    private fun printDescription(
        pLine: Paint, canvas: Canvas, description: String, i: Int, align: Paint.Align
    ) {
        pLine.apply {
            textSize = config.sizeDescription
            typeface = Typeface.DEFAULT
            color = config.colorDescription
        }

        val stepX = (measuredWidth - config.marginHorizontalStroke * 2)

        val x = when (align) {
            Paint.Align.LEFT -> if (config.startPosition == TimelineConfig.StartPosition.CENTER) startPositionX - config.marginHorizontalText else -startPositionX + stepX
            Paint.Align.CENTER -> startPositionX
            Paint.Align.RIGHT -> -(startPositionX - config.marginHorizontalText)
        }
        val y =
            (config.stepY * i) + config.marginTopTitle + config.sizeTitle + config.marginTopDescription

        canvas.drawText(description, x, y, pLine)
        Log.d(TAG, "printDescription i: $i; align: $align; x: $x; y: $y")
    }

    private fun getBitmap(drawableId: Int): Bitmap? {
        if (drawableId == 0) return null

        return when (val drawable = ContextCompat.getDrawable(context, drawableId)) {
            is BitmapDrawable -> BitmapFactory.decodeResource(context.resources, drawableId)
            is VectorDrawable -> {
                val bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }

            else -> throw IllegalArgumentException("Unsupported drawable type")
        }
    }

    private fun buildPath() {
        pathDisable.reset()
        pathEnable.reset()

        val stepX = (measuredWidth - config.marginHorizontalStroke * 2)
        val stepXFirst = startPositionX - config.marginHorizontalStroke
        var enable = steps.isNotEmpty() && steps[0].count != 0
        var path: Path = if (enable) pathEnable else pathDisable

        steps.forEachIndexed { i, lvl ->
            val horizontalStep = if (i % 2 == 0) -stepX else stepX

            when {
                lvl.percents == 100 -> {
                    if (i == 0) {
                        path.rLineTo(0f, config.stepYFirst)
                        path.rLineTo(-stepXFirst, 0f)
                        path.rLineTo(0f, config.stepY)
                    } else {
                        path.rLineTo(horizontalStep, 0f)
                        path.rLineTo(
                            0f, if (i == steps.size - 1) config.stepY / 2 else config.stepY
                        )
                    }
                }

                i == 0 -> {
                    path.rLineTo(0f, config.stepYFirst)
                    if (enable) {
                        startPositionDisableStrokeX = stepXFirst / 100 * lvl.percents
                        path.rLineTo(-startPositionDisableStrokeX, 0f)
                        path = pathDisable
                        enable = false
                        path.moveTo(-startPositionDisableStrokeX, config.stepYFirst)
                    }
                    path.rLineTo(-(stepXFirst - startPositionDisableStrokeX), 0f)
                    path.rLineTo(0f, config.stepY)
                }

                enable -> {
                    startPositionDisableStrokeX = stepX / 100 * lvl.percents
                    path.rLineTo(
                        horizontalStep / abs(horizontalStep) * startPositionDisableStrokeX, 0f
                    )
                    path = pathDisable
                    enable = false
                    path.moveTo(
                        if (i % 2 == 0) startPositionX - startPositionDisableStrokeX - config.marginHorizontalStroke
                        else startPositionDisableStrokeX - startPositionX + config.marginHorizontalStroke,
                        config.stepYFirst + config.stepY * i
                    )
                    path.rLineTo(
                        if (i % 2 == 0) -(stepX - startPositionDisableStrokeX) else stepX - startPositionDisableStrokeX,
                        0f
                    )
                    path.rLineTo(
                        0f,
                        if (i == steps.size - 1) config.stepY / 2 else config.stepY
                    )
                }

                else -> {
                    path.rLineTo(horizontalStep, 0f)
                    path.rLineTo(
                        0f,
                        if (i == steps.size - 1) config.stepY / 2 else config.stepY
                    )
                }
            }
        }
    }
}
