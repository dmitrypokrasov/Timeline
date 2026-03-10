package com.dmitrypokrasov.timelineview.ui

import android.content.Context
import android.graphics.Canvas
import android.view.View
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.dmitrypokrasov.timelineview.model.TimelineLottieSpec
import kotlin.math.roundToInt

internal class TimelineLottieOverlayManager(
    private val ownerView: View
) {
    private val drawableCache = mutableMapOf<TimelineLottieSpec, LottieDrawable>()

    fun draw(
        canvas: Canvas,
        context: Context,
        spec: TimelineLottieSpec?,
        left: Float,
        top: Float,
        size: Float
    ) {
        if (spec == null) return

        val drawable = drawableCache.getOrPut(spec) {
            createDrawable(context, spec)
        }

        val scaledSize = size * spec.scale
        val inset = (size - scaledSize) / 2f
        val drawLeft = left + inset
        val drawTop = top + inset
        drawable.setBounds(
            drawLeft.roundToInt(),
            drawTop.roundToInt(),
            (drawLeft + scaledSize).roundToInt(),
            (drawTop + scaledSize).roundToInt()
        )

        if (spec.autoPlay && !drawable.isAnimating) {
            drawable.playAnimation()
        }
        if (!spec.autoPlay && drawable.isAnimating) {
            drawable.pauseAnimation()
        }

        drawable.draw(canvas)
    }

    fun clear() {
        drawableCache.values.forEach { drawable ->
            drawable.cancelAnimation()
            drawable.callback = null
        }
        drawableCache.clear()
    }

    private fun createDrawable(context: Context, spec: TimelineLottieSpec): LottieDrawable {
        val composition = LottieCompositionFactory.fromRawResSync(context, spec.rawRes).value
            ?: throw IllegalArgumentException("Unable to load Lottie animation: ${spec.rawRes}")

        return LottieDrawable().apply {
            callback = ownerView
            this.composition = composition
            repeatCount = if (spec.repeat) LottieDrawable.INFINITE else 0
        }
    }
}
