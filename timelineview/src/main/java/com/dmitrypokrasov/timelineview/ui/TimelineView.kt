package com.dmitrypokrasov.timelineview.ui

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.view.ViewCompat
import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineConfig
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
        private val accessibilityHelper = TimelineAccessibilityHelper(this, controller)

        init {
            isFocusable = true
            importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
            ViewCompat.setAccessibilityDelegate(this, accessibilityHelper)
        }

        /** Returns the current declarative timeline config. */
        fun getConfig(): TimelineConfig = controller.getConfig()

        /** Replaces the full declarative timeline config. */
        fun setConfig(config: TimelineConfig) {
            mutateTimeline {
                setConfig(config)
            }
        }

        /** Replaces the current steps and triggers a relayout/redraw. */
        fun replaceSteps(steps: List<TimelineStepData>) {
            mutateTimeline {
                replaceSteps(steps)
            }
        }

        /** Replaces only the math engine. */
        fun setMathEngine(engine: TimelineMathEngine) {
            mutateTimeline {
                setMathEngine(engine)
            }
        }

        /** Replaces only the UI renderer. */
        fun setUiRenderer(renderer: TimelineUiRenderer) {
            mutateTimeline {
                setUiRenderer(renderer)
            }
        }

        /** Switches both math and UI using built-in strategy types. */
        fun setStrategy(
            mathStrategy: TimelineMathStrategy,
            uiStrategy: TimelineUiStrategy,
        ) {
            mutateTimeline {
                setStrategy(mathStrategy, uiStrategy)
            }
        }

        /** Switches both math and UI using a prebuilt composite strategy. */
        fun setStrategy(strategy: TimelineStrategy) {
            mutateTimeline {
                setStrategy(strategy)
            }
        }

        /** Switches both math and UI using strategy keys resolved from the registry. */
        fun setStrategy(
            mathStrategyKey: StrategyKey?,
            uiStrategyKey: StrategyKey?,
        ) {
            mutateTimeline {
                setStrategy(mathStrategyKey, uiStrategyKey)
            }
        }

        /** Replaces both the math engine and renderer directly. */
        fun setStrategies(
            mathEngine: TimelineMathEngine,
            uiRenderer: TimelineUiRenderer,
        ) {
            mutateTimeline {
                setStrategies(mathEngine, uiRenderer)
            }
        }

        /** Replaces the strategy registry used by this view. */
        fun setStrategyRegistry(registry: TimelineStrategyRegistryContract) {
            mutateTimeline {
                setStrategyRegistry(registry)
            }
        }

        /** Creates and installs a local strategy registry configured by [configure]. */
        fun setStrategyRegistry(configure: TimelineStrategyRegistryContract.() -> Unit) {
            val registry = TimelineStrategyRegistry.createLocalRegistry()
            configure(registry)
            setStrategyRegistry(registry)
        }

        /** Registers a click listener for badge icons. */
        fun setOnStepClickListener(listener: (index: Int, step: TimelineStepData) -> Unit) {
            mutateTimeline(requestLayout = false, redraw = false) {
                setOnStepClickListener(listener)
            }
        }

        /** Registers a click listener for the active progress icon. */
        fun setOnProgressIconClickListener(listener: () -> Unit) {
            mutateTimeline(requestLayout = false, redraw = false) {
                setOnProgressIconClickListener(listener)
            }
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
            accessibilityHelper.invalidateTimeline()
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

        override fun dispatchHoverEvent(event: MotionEvent): Boolean {
            return accessibilityHelper.dispatchHoverEvent(event) || super.dispatchHoverEvent(event)
        }

        override fun performClick(): Boolean {
            super.performClick()
            return true
        }

        override fun onDetachedFromWindow() {
            controller.release()
            super.onDetachedFromWindow()
        }

        private fun mutateTimeline(
            requestLayout: Boolean = true,
            redraw: Boolean = true,
            mutation: TimelineViewController.() -> Unit,
        ) {
            controller.mutation()
            isClickable = controller.isInteractive()
            accessibilityHelper.invalidateTimeline()
            if (requestLayout) {
                requestLayout()
            }
            if (redraw) {
                invalidate()
            }
        }
    }
