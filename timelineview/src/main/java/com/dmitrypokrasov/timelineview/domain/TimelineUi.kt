package com.dmitrypokrasov.timelineview.domain

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
import com.dmitrypokrasov.timelineview.data.TimelineStep
import com.dmitrypokrasov.timelineview.domain.data.TimelineMathConfig
import com.dmitrypokrasov.timelineview.domain.data.TimelineUiConfig

internal class TimelineUi(
    var uiConfig: TimelineUiConfig
) {
    companion object {
        private const val TAG = "TimelineUi"
    }

    /** Путь для пройденных шагов. */
    val pathEnable = Path()

    /** Путь для непройденных шагов. */
    val pathDisable = Path()

    /** Битмап неактивного шага. */
    private var iconDisableStep: Bitmap? = null

    /** Скругление углов линии пути. */
    private var pathEffect: CornerPathEffect? = null

    /** Битмап текущей иконки прогресса. */
    private var iconProgressBitmap: Bitmap? = null

    /** Кисть для рисования линий. */
    private val linePaint = Paint()

    /** Кисть для рисования текста и иконок. */
    private val textPaint = Paint()

    /**
     * Инициализирует визуальные элементы (битмапы, pathEffect).
     */
    fun initTools(timelineMathConfig: TimelineMathConfig, context: Context) {
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

    fun getTextAlign(): Paint.Align {
        return textPaint.textAlign
    }

    fun resetFromPaintTools() {
        linePaint.reset()
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = uiConfig.sizeStroke
        linePaint.pathEffect = pathEffect
    }

    fun resetFromTextTools() {
        textPaint.reset()
        textPaint.isAntiAlias = true
    }

    fun drawProgressBitmap(canvas: Canvas, leftCoordinates: Float, topCoordinates: Float) {
        iconProgressBitmap?.let {
            canvas.drawBitmap(
                it,
                leftCoordinates,
                topCoordinates,
                textPaint
            )
        }
    }

    fun drawProgressPath(canvas: Canvas) {
        linePaint.color = uiConfig.colorProgress
        canvas.drawPath(pathEnable, linePaint)
    }

    fun drawDisablePath(canvas: Canvas) {
        linePaint.color = uiConfig.colorStroke
        canvas.drawPath(pathDisable, linePaint)
    }

    /**
     * Отрисовка заголовка шага.
     */
    fun printTitle(
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

    /**
     * Отрисовка описания шага.
     */
    fun printDescription(
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

    /**
     * Отрисовка иконки шага.
     */
    fun printIcon(
        lvl: TimelineStep,
        canvas: Canvas,
        align: Paint.Align,
        context: Context,
        x: Float,
        y: Float
    ) {
        val bm: Bitmap? = when {
            lvl.count == lvl.maxCount && lvl.icon != 0 -> getBitmap(lvl.icon, context)
            else -> iconDisableStep
        }
        bm?.let {
            textPaint.textAlign = align
            canvas.drawBitmap(it, x, y, textPaint)
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
}