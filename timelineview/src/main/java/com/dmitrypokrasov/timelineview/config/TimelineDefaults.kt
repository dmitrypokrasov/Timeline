package com.dmitrypokrasov.timelineview.config

import android.content.Context
import android.util.TypedValue

/** Density/font-aware entry points for Kotlin and Java hosts; stored configuration stays in pixels. */
object TimelineDefaults {
    /** Same dp/sp defaults as XML inflation, using the supplied context's current configuration. */
    @JvmStatic
    fun config(context: Context): TimelineConfig = TimelineConfigParser(context).parse(null)

    /** Converts a finite, non-negative dp dimension to configuration pixels. */
    @JvmStatic
    fun dp(
        context: Context,
        value: Float,
    ): Float = dimension(context, value, TypedValue.COMPLEX_UNIT_DIP)

    /** Converts sp using platform font scaling, including non-linear scaling on newer Android. */
    @JvmStatic
    fun sp(
        context: Context,
        value: Float,
    ): Float = dimension(context, value, TypedValue.COMPLEX_UNIT_SP)

    private fun dimension(
        context: Context,
        value: Float,
        unit: Int,
    ): Float {
        require(value.isFinite() && value >= 0f) { "Dimension must be finite and non-negative" }
        return TypedValue.applyDimension(unit, value, context.resources.displayMetrics).also {
            require(it.isFinite()) { "Converted dimension exceeds the supported pixel range" }
        }
    }
}
