package com.dmitrypokrasov.timelineview.strategy.snake

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
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.model.TimelineStep
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer

/**
 * Публичная реализация [TimelineUiRenderer], использующая алгоритм "змейки",
 * ранее находившийся в `TimelineUi`.
 */
class SnakeTimelineUi(
    private var uiConfig: TimelineUiConfig,
) : TimelineUiRenderer {
    companion object {
        private const val TAG = "SnakeTimelineUi"
    }

    /** Путь для пройденных шагов. */
    private val pathEnable = Path()

    /** Путь для непройденных шагов. */
    private val pathDisable = Path()

    /** Битмап неактивного шага. */
    private var iconDisableStep: Bitmap? = null

    /** Скругление углов линии пути. */
    private var pathEffect: CornerPathEffect? = null

    /** Битмап текущей иконки прогресса. */
    private var iconProgressBitmap: Bitmap? = null

    /** Кисть для рисования линий. */
    private val linePaint = Paint()

    /** Кисть для рисования текста. */
    private val textPaint = Paint()

    /** Кисть для рисования иконок. */
    private val iconPaint = Paint()

    override fun initTools(timelineMathConfig: TimelineMathConfig, context: Context) {
        Log.d(TAG, "initTools timelineUiConfig: $timelineMathConfig")

        pathEffect = CornerPathEffect(uiConfig.radius)

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

    override fun setConfig(config: TimelineUiConfig) {
        uiConfig = config
    }

    override fun getConfig(): TimelineUiConfig = uiConfig

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
        title: String,
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

        canvas.drawText(title, x, y, textPaint)
    }

    override fun drawDescription(
        canvas: Canvas,
        description: String,
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

        canvas.drawText(description, x, y, textPaint)
    }

    override fun drawStepIcon(
        step: TimelineStep,
        canvas: Canvas,
        align: Paint.Align,
        context: Context,
        x: Float,
        y: Float
    ) {
        val bm: Bitmap? = when {
            step.count == step.maxCount && step.icon != 0 -> getBitmap(step.icon, context)
            else -> iconDisableStep
        }
        bm?.let {
            iconPaint.textAlign = align
            canvas.drawBitmap(it, x, y, iconPaint)
        }
    }

    /**
     * Получение Bitmap из ресурса, включая поддержку VectorDrawable.
     */
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

    override fun getTextAlignment(): Paint.Align = textPaint.textAlign
}
