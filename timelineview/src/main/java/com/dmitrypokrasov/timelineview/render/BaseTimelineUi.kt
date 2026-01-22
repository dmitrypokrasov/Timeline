package com.dmitrypokrasov.timelineview.render

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
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.model.TimelineStepData

/**
 * Base implementation for timeline UI renderers with shared drawing logic.
 */
open class BaseTimelineUi(
    private var uiConfig: TimelineUiConfig,
) : TimelineUiRenderer {

    /** Path for completed steps. */
    private val pathEnable = Path()

    /** Path for remaining steps. */
    private val pathDisable = Path()

    /** Bitmap for disabled steps. */
    private var iconDisableStep: Bitmap? = null

    /** Corner rounding for path. */
    private var pathEffect: CornerPathEffect? = null

    /** Bitmap for progress icon. */
    private var iconProgressBitmap: Bitmap? = null

    /** Cache for step icon bitmaps. */
    private val stepIconCache = mutableMapOf<Int, Bitmap>()

    private var stepIconSize: Int = 0

    /** Paint for lines. */
    private val linePaint = Paint()

    /** Paint for text. */
    private val textPaint = Paint()

    /** Paint for icons. */
    private val iconPaint = Paint()

    override fun initTools(timelineMathConfig: TimelineMathConfig, context: Context) {
        pathEffect = CornerPathEffect(uiConfig.radius)
        stepIconSize = timelineMathConfig.sizeImageLvl.toInt()
        stepIconCache.clear()

        getBitmap(uiConfig.iconDisableLvl, context)?.let { bitmap ->
            iconDisableStep = bitmap.scale(
                timelineMathConfig.sizeImageLvl.toInt(),
                timelineMathConfig.sizeImageLvl.toInt(),
                false
            )
        }

        getBitmap(uiConfig.iconProgress, context)?.let { bitmap ->
            iconProgressBitmap = bitmap.scale(
                timelineMathConfig.sizeIconProgress.toInt(),
                timelineMathConfig.sizeIconProgress.toInt(),
                false
            )
        }
    }

    override fun prepareStrokePaint() {
        linePaint.reset()
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = uiConfig.sizeStroke
        linePaint.pathEffect = pathEffect
    }

    override fun prepareTextPaint() {
        textPaint.reset()
        textPaint.isAntiAlias = true
    }

    override fun prepareIconPaint() {
        iconPaint.reset()
        iconPaint.isAntiAlias = true
    }

    override fun drawProgressIcon(canvas: Canvas, leftCoordinates: Float, topCoordinates: Float) {
        iconProgressBitmap?.let {
            canvas.drawBitmap(
                it,
                leftCoordinates,
                topCoordinates,
                iconPaint
            )
        }
    }

    override fun drawCompletedPath(canvas: Canvas) {
        linePaint.color = uiConfig.colorProgress
        canvas.drawPath(pathEnable, linePaint)
    }

    override fun drawRemainingPath(canvas: Canvas) {
        linePaint.color = uiConfig.colorStroke
        canvas.drawPath(pathDisable, linePaint)
    }

    override fun getCompletedPath(): Path = pathEnable

    override fun getRemainingPath(): Path = pathDisable

    override fun drawTitle(
        canvas: Canvas,
        title: CharSequence,
        x: Float,
        y: Float,
        align: Paint.Align
    ) {
        textPaint.apply {
            textAlign = align
            textSize = uiConfig.sizeTitle
            typeface = Typeface.DEFAULT_BOLD
            color = uiConfig.colorTitle
        }

        canvas.drawText(title.toString(), x, y, textPaint)
    }

    override fun drawDescription(
        canvas: Canvas,
        description: CharSequence,
        x: Float,
        y: Float,
        align: Paint.Align
    ) {
        textPaint.apply {
            textAlign = align
            textSize = uiConfig.sizeDescription
            typeface = Typeface.DEFAULT
            color = uiConfig.colorDescription
        }

        canvas.drawText(description.toString(), x, y, textPaint)
    }

    override fun drawStepIcon(
        step: TimelineStepData,
        canvas: Canvas,
        align: Paint.Align,
        context: Context,
        x: Float,
        y: Float
    ) {
        val bm: Bitmap? = when {
            step.progress == 100 && step.iconRes != null -> getStepIconBitmap(step.iconRes, context)
            else -> iconDisableStep
        }
        bm?.let {
            iconPaint.textAlign = align
            canvas.drawBitmap(it, x, y, iconPaint)
        }
    }

    override fun setConfig(config: TimelineUiConfig) {
        uiConfig = config
    }

    override fun getConfig(): TimelineUiConfig = uiConfig

    override fun getTextAlignment(): Paint.Align = textPaint.textAlign

    private fun getStepIconBitmap(drawableId: Int, context: Context): Bitmap? {
        if (drawableId == 0) return null
        stepIconCache[drawableId]?.let { return it }

        val bitmap = getBitmap(drawableId, context)?.scale(stepIconSize, stepIconSize, false)
            ?: return null
        stepIconCache[drawableId] = bitmap
        return bitmap
    }

    private fun getBitmap(drawableId: Int, context: Context): Bitmap? {
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
