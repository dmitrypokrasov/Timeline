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
    private val textLayoutBuilder: TimelineTextLayoutBuilder = StaticTimelineTextLayoutBuilder()
    private val pathEnable = Path()
    private val pathDisable = Path()
    private var iconDisableStep: Bitmap? = null
    private var pathEffect: CornerPathEffect? = null
    private var iconProgressBitmap: Bitmap? = null
    private val stepIconCache = mutableMapOf<Int, Bitmap>()
    private var stepIconSize: Int = 0
    private val linePaint = Paint()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val baselineProbePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun initTools(
        timelineMathConfig: TimelineMathConfig,
        context: Context,
    ) {
        pathEffect = CornerPathEffect(uiConfig.stroke.radius)
        stepIconSize = timelineMathConfig.sizes.sizeImageLvl.toInt()
        stepIconCache.clear()

        getBitmap(uiConfig.icons.iconDisableLvl, context)?.let { bitmap ->
            iconDisableStep =
                bitmap.scale(
                    timelineMathConfig.sizes.sizeImageLvl.toInt(),
                    timelineMathConfig.sizes.sizeImageLvl.toInt(),
                    false,
                )
        }

        getBitmap(uiConfig.icons.iconProgress, context)?.let { bitmap ->
            iconProgressBitmap =
                bitmap.scale(
                    timelineMathConfig.sizes.sizeIconProgress.toInt(),
                    timelineMathConfig.sizes.sizeIconProgress.toInt(),
                    false,
                )
        }
    }

    override fun prepareStrokePaint() {
        linePaint.reset()
        linePaint.isAntiAlias = true
        linePaint.strokeCap = Paint.Cap.ROUND
        linePaint.strokeJoin = Paint.Join.ROUND
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = uiConfig.stroke.sizeStroke
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

    override fun drawProgressIcon(
        canvas: Canvas,
        leftCoordinates: Float,
        topCoordinates: Float,
    ) {
        iconProgressBitmap?.let {
            canvas.drawBitmap(it, leftCoordinates, topCoordinates, iconPaint)
        }
    }

    override fun drawCompletedPath(canvas: Canvas) {
        linePaint.color = uiConfig.colors.colorProgress
        canvas.drawPath(pathEnable, linePaint)
    }

    override fun drawRemainingPath(canvas: Canvas) {
        linePaint.color = uiConfig.colors.colorStroke
        canvas.drawPath(pathDisable, linePaint)
    }

    override fun getCompletedPath(): Path = pathEnable

    override fun getRemainingPath(): Path = pathDisable

    override fun drawTitle(
        canvas: Canvas,
        title: CharSequence,
        x: Float,
        y: Float,
        align: Paint.Align,
        maxWidth: Int,
    ) {
        drawTextBlock(
            canvas = canvas,
            text = title,
            x = x,
            y = y,
            align = align,
            maxWidth = maxWidth,
            textSize = uiConfig.textSizes.sizeTitle,
            typeface = Typeface.DEFAULT_BOLD,
            color = uiConfig.colors.colorTitle,
        )
    }

    override fun drawDescription(
        canvas: Canvas,
        description: CharSequence,
        x: Float,
        y: Float,
        align: Paint.Align,
        maxWidth: Int,
    ) {
        drawTextBlock(
            canvas = canvas,
            text = description,
            x = x,
            y = y,
            align = align,
            maxWidth = maxWidth,
            textSize = uiConfig.textSizes.sizeDescription,
            typeface = Typeface.DEFAULT,
            color = uiConfig.colors.colorDescription,
        )
    }

    override fun measureTitleHeight(
        title: CharSequence,
        maxWidth: Int,
        align: Paint.Align,
    ): Int {
        return measureTextBlock(
            text = title,
            maxWidth = maxWidth,
            textSize = uiConfig.textSizes.sizeTitle,
            typeface = Typeface.DEFAULT_BOLD,
            color = uiConfig.colors.colorTitle,
            align = align,
        )
    }

    override fun measureDescriptionHeight(
        description: CharSequence,
        maxWidth: Int,
        align: Paint.Align,
    ): Int {
        return measureTextBlock(
            text = description,
            maxWidth = maxWidth,
            textSize = uiConfig.textSizes.sizeDescription,
            typeface = Typeface.DEFAULT,
            color = uiConfig.colors.colorDescription,
            align = align,
        )
    }

    override fun getTitleBaselineOffset(): Float =
        getBaselineOffset(uiConfig.textSizes.sizeTitle, Typeface.DEFAULT_BOLD)

    override fun getDescriptionBaselineOffset(): Float =
        getBaselineOffset(uiConfig.textSizes.sizeDescription, Typeface.DEFAULT)

    override fun drawStepIcon(
        step: TimelineStepData,
        canvas: Canvas,
        align: Paint.Align,
        context: Context,
        x: Float,
        y: Float,
    ) {
        val bitmap =
            when {
                step.progress == 100 && step.iconRes != null ->
                    getStepIconBitmap(
                        step.iconRes,
                        context,
                    )
                step.progress != 100 && step.iconDisabledRes != null ->
                    getStepIconBitmap(step.iconDisabledRes, context)
                else -> iconDisableStep
            }
        bitmap?.let {
            iconPaint.textAlign = align
            canvas.drawBitmap(it, x, y, iconPaint)
        }
    }

    override fun setConfig(config: TimelineUiConfig) {
        uiConfig = config
    }

    override fun getConfig(): TimelineUiConfig = uiConfig

    override fun getTextAlignment(): Paint.Align = textPaint.textAlign

    private fun drawTextBlock(
        canvas: Canvas,
        text: CharSequence,
        x: Float,
        y: Float,
        align: Paint.Align,
        maxWidth: Int,
        textSize: Float,
        typeface: Typeface,
        color: Int,
    ) {
        val value = text.toString()
        if (value.isBlank()) return

        val layout =
            textLayoutBuilder.build(
                text = value,
                textSize = textSize,
                typeface = typeface,
                color = color,
                align = align,
                width = maxWidth,
            )
        val translateX =
            when (align) {
                Paint.Align.LEFT -> x
                Paint.Align.CENTER -> x - maxWidth / 2f
                Paint.Align.RIGHT -> x - maxWidth
            }

        canvas.save()
        canvas.translate(translateX, y)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun measureTextBlock(
        text: CharSequence,
        maxWidth: Int,
        textSize: Float,
        typeface: Typeface,
        color: Int,
        align: Paint.Align,
    ): Int {
        val value = text.toString()
        if (value.isBlank()) return 0

        return textLayoutBuilder.build(
            text = value,
            textSize = textSize,
            typeface = typeface,
            color = color,
            align = align,
            width = maxWidth,
        ).height
    }

    private fun getBaselineOffset(
        textSize: Float,
        typeface: Typeface,
    ): Float {
        baselineProbePaint.textSize = textSize
        baselineProbePaint.typeface = typeface
        return -baselineProbePaint.fontMetrics.ascent
    }

    private fun getStepIconBitmap(
        drawableId: Int,
        context: Context,
    ): Bitmap? {
        if (drawableId == 0) return null
        stepIconCache[drawableId]?.let { return it }

        val bitmap =
            getBitmap(drawableId, context)?.scale(stepIconSize, stepIconSize, false)
                ?: return null
        stepIconCache[drawableId] = bitmap
        return bitmap
    }

    private fun getBitmap(
        drawableId: Int,
        context: Context,
    ): Bitmap? {
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
