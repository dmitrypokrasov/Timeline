package com.dmitrypokrasov.timelineview.ui

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer
import com.dmitrypokrasov.timelineview.strategy.TimelineStrategyRegistry
import com.dmitrypokrasov.timelineview.strategy.TimelineStrategyRegistryContract

/**
 * Custom View for rendering a timeline.
 */
class TimelineView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : View(context, attrs, defStyleAttr) {
        private val controller = TimelineViewController(this, context, attrs)

        /** Replaces the current steps and triggers a relayout/redraw. */
        fun replaceSteps(steps: List<TimelineStepData>) {
            controller.replaceSteps(steps)
            updateAccessibilityDescription()
            requestLayout()
            invalidate()
        }

        /** Replaces only the math engine. */
        fun setMathEngine(engine: TimelineMathEngine) {
            controller.setMathEngine(engine)
            requestLayout()
            invalidate()
        }

        /** Replaces only the UI renderer. */
        fun setUiRenderer(renderer: TimelineUiRenderer) {
            controller.setUiRenderer(renderer)
            requestLayout()
            invalidate()
        }

        /** Switches both math and UI using built-in strategy types. */
        fun setStrategy(
            mathStrategy: TimelineMathStrategy,
            uiStrategy: TimelineUiStrategy,
        ) {
            controller.setStrategy(mathStrategy, uiStrategy)
            requestLayout()
            invalidate()
        }

        /** Switches both math and UI using a prebuilt composite strategy. */
        fun setStrategy(strategy: TimelineStrategy) {
            controller.setStrategy(strategy)
            requestLayout()
            invalidate()
        }

        /** Switches both math and UI using strategy keys resolved from the registry. */
        fun setStrategy(
            mathStrategyKey: StrategyKey?,
            uiStrategyKey: StrategyKey?,
        ) {
            controller.setStrategy(mathStrategyKey, uiStrategyKey)
            requestLayout()
            invalidate()
        }

        /** Replaces both the math engine and renderer directly. */
        fun setStrategies(
            mathEngine: TimelineMathEngine,
            uiRenderer: TimelineUiRenderer,
        ) {
            controller.setStrategies(mathEngine, uiRenderer)
            requestLayout()
            invalidate()
        }

        /** Replaces the strategy registry used by this view. */
        fun setStrategyRegistry(registry: TimelineStrategyRegistryContract) {
            controller.setStrategyRegistry(registry)
            requestLayout()
            invalidate()
        }

        /** Creates and installs a local strategy registry configured by [configure]. */
        fun setStrategyRegistry(configure: TimelineStrategyRegistryContract.() -> Unit) {
            val registry = TimelineStrategyRegistry.createLocalRegistry()
            configure(registry)
            setStrategyRegistry(registry)
        }

        /** Registers a click listener for badge icons. */
        fun setOnStepClickListener(listener: (index: Int, step: TimelineStepData) -> Unit) {
            controller.setOnStepClickListener(listener)
            isClickable = true
            updateAccessibilityDescription()
        }

        /** Registers a click listener for the active progress icon. */
        fun setOnProgressIconClickListener(listener: () -> Unit) {
            controller.setOnProgressIconClickListener(listener)
            isClickable = true
            updateAccessibilityDescription()
        }

        override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ) {
            val resolvedWidth =
                resolveSizeAndState(
                    suggestedMinimumWidth + paddingLeft + paddingRight,
                    widthMeasureSpec,
                    0,
                )
            val contentWidth = (resolvedWidth - paddingLeft - paddingRight).coerceAtLeast(0)
            val desiredHeight = controller.measure(contentWidth) + paddingTop + paddingBottom
            val resolvedHeight = resolveSizeAndState(desiredHeight, heightMeasureSpec, 0)
            updateAccessibilityDescription()
            setMeasuredDimension(resolvedWidth, resolvedHeight)
        }

        override fun onDraw(canvas: Canvas) {
            canvas.save()
            canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
            controller.draw(canvas)
            canvas.restore()
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val adjustedX = event.x - paddingLeft
            val adjustedY = event.y - paddingTop
            return when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> true
                MotionEvent.ACTION_UP -> {
                    val handled = controller.handleClick(adjustedX, adjustedY)
                    if (handled) {
                        performClick()
                        true
                    } else {
                        super.onTouchEvent(event)
                    }
                }

                else -> super.onTouchEvent(event)
            }
        }

        override fun performClick(): Boolean {
            super.performClick()
            return true
        }

        override fun onDetachedFromWindow() {
            controller.release()
            super.onDetachedFromWindow()
        }

        private fun updateAccessibilityDescription() {
            contentDescription = controller.getAccessibilityDescription()
        }
    }
