package com.dmitrypokrasov.timelineview.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.view.ViewCompat
import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
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
        private val controller = TimelineViewController(this, context, attrs, defStyleAttr = defStyleAttr)
        private val accessibility = TimelineAccessibilityHelper(this, controller)
        private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        private var downTarget: Int? = null
        private var downX = 0f
        private var downY = 0f
        private var pointerId = -1
        private var ready = false

        init {
            ViewCompat.setAccessibilityDelegate(this, accessibility)
            isFocusable = true
            ready = true
        }

        /** Returns declarative configuration; direct instances are represented by their fallback strategies. */
        fun getConfig(): TimelineConfig = controller.getConfig()

        /** Replaces the complete configuration and returns both strategies to registry selection. */
        fun setConfig(config: TimelineConfig) {
            controller.setConfig(config)
            changed()
        }

        /** Applies immutable configuration explicitly and invalidates all derived geometry. */
        fun setConfig(
            math: TimelineMathConfig,
            ui: TimelineUiConfig,
        ) {
            controller.setConfig(math, ui)
            changed()
        }

        private fun changed() {
            cancelTouch()
            accessibility.invalidateRoot()
            requestLayout()
            invalidate()
        }

        /** Replaces the current steps and triggers a relayout/redraw. */
        fun replaceSteps(steps: List<TimelineStepData>) {
            controller.replaceSteps(steps)
            changed()
        }

        /** Replaces only the math engine. */
        fun setMathEngine(engine: TimelineMathEngine) {
            controller.setMathEngine(engine)
            changed()
        }

        /** Replaces only the UI renderer. */
        fun setUiRenderer(renderer: TimelineUiRenderer) {
            controller.setUiRenderer(renderer)
            changed()
        }

        /** Switches both math and UI using built-in strategy types. */
        fun setStrategy(
            mathStrategy: TimelineMathStrategy,
            uiStrategy: TimelineUiStrategy,
        ) {
            controller.setStrategy(mathStrategy, uiStrategy)
            changed()
        }

        /** Switches both math and UI using a prebuilt composite strategy. */
        fun setStrategy(strategy: TimelineStrategy) {
            controller.setStrategy(strategy)
            changed()
        }

        /** Switches both math and UI using strategy keys resolved from the registry. */
        fun setStrategy(
            mathStrategyKey: StrategyKey?,
            uiStrategyKey: StrategyKey?,
        ) {
            controller.setStrategy(mathStrategyKey, uiStrategyKey)
            changed()
        }

        /** Replaces both the math engine and renderer directly. */
        fun setStrategies(
            mathEngine: TimelineMathEngine,
            uiRenderer: TimelineUiRenderer,
        ) {
            controller.setStrategies(mathEngine, uiRenderer)
            changed()
        }

        /** Replaces the strategy registry used by this view. */
        fun setStrategyRegistry(registry: TimelineStrategyRegistryContract) {
            controller.setStrategyRegistry(registry)
            changed()
        }

        /** Creates and installs a local strategy registry configured by [configure]. */
        fun setStrategyRegistry(configure: TimelineStrategyRegistryContract.() -> Unit) {
            val registry = TimelineStrategyRegistry.createLocalRegistry()
            configure(registry)
            setStrategyRegistry(registry)
        }

        /** Registers a click listener for badge icons. */
        fun setOnStepClickListener(listener: ((index: Int, step: TimelineStepData) -> Unit)?) {
            controller.setOnStepClickListener(listener)
            accessibility.invalidateRoot()
        }

        /** Registers a click listener for the active progress icon. */
        fun setOnProgressIconClickListener(listener: (() -> Unit)?) {
            controller.setOnProgressIconClickListener(listener)
            accessibility.invalidateRoot()
        }

        override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ) {
            val resolvedWidth =
                resolveSizeAndState(
                    if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
                        maxOf(controller.desiredWidth(), suggestedMinimumWidth, (240f * resources.displayMetrics.density).toInt()) + paddingLeft + paddingRight
                    } else {
                        MeasureSpec.getSize(widthMeasureSpec)
                    },
                    widthMeasureSpec,
                    0,
                )
            val contentWidth = (resolvedWidth - paddingLeft - paddingRight).coerceAtLeast(0)
            val desiredHeight = controller.measure(contentWidth) + paddingTop + paddingBottom
            val resolvedHeight = resolveSizeAndState(desiredHeight, heightMeasureSpec, 0)
            setMeasuredDimension(resolvedWidth, resolvedHeight)
            accessibility.onLayoutChanged()
        }

        override fun onDraw(canvas: Canvas) {
            canvas.save()
            canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
            controller.draw(canvas)
            canvas.restore()
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (!isEnabled) {
                cancelTouch()
                return false
            }
            return when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> touchDown(event)
                MotionEvent.ACTION_MOVE -> touchMove(event)
                MotionEvent.ACTION_UP -> {
                    if (touchUp(event)) {
                        performClick()
                        true
                    } else {
                        super.onTouchEvent(event)
                    }
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_POINTER_UP -> {
                    cancelTouch()
                    true
                }
                else -> super.onTouchEvent(event)
            }
        }

        private fun touchDown(event: MotionEvent): Boolean {
            downTarget = controller.targetAt(event.x - paddingLeft, event.y - paddingTop)
            if (downTarget == null) return super.onTouchEvent(event)
            pointerId = event.getPointerId(0)
            downX = event.x
            downY = event.y
            isPressed = true
            return true
        }

        private fun withinSlop(
            x: Float,
            y: Float,
        ): Boolean =
            kotlin.math.abs(x - downX) <= touchSlop && kotlin.math.abs(y - downY) <= touchSlop

        private fun touchMove(event: MotionEvent): Boolean {
            val index = event.findPointerIndex(pointerId)
            if (index < 0 || !withinSlop(event.getX(index), event.getY(index))) cancelTouch()
            return true
        }

        private fun touchUp(event: MotionEvent): Boolean {
            val target = downTarget
            val samePointer = event.getPointerId(event.actionIndex) == pointerId
            val tap = withinSlop(event.x, event.y)
            val hit = controller.targetAt(event.x - paddingLeft, event.y - paddingTop)
            cancelTouch()
            val sameTarget = target != null && target == hit
            if (!samePointer || !tap || !sameTarget) return false
            return target != null && controller.clickTarget(target)
        }

        private fun cancelTouch() {
            downTarget = null
            pointerId = -1
            isPressed = false
        }

        override fun dispatchHoverEvent(event: MotionEvent): Boolean =
            accessibility.dispatchHoverEvent(event) || super.dispatchHoverEvent(event)

        override fun dispatchKeyEvent(event: KeyEvent): Boolean =
            accessibility.dispatchKeyEvent(event) || super.dispatchKeyEvent(event)

        override fun onFocusChanged(
            gainFocus: Boolean,
            direction: Int,
            previouslyFocusedRect: Rect?,
        ) {
            super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
            if (ready) accessibility.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        }

        override fun setEnabled(enabled: Boolean) {
            super.setEnabled(enabled)
            if (ready) {
                cancelTouch()
                accessibility.invalidateRoot()
            }
        }

        override fun onRtlPropertiesChanged(layoutDirection: Int) {
            super.onRtlPropertiesChanged(layoutDirection)
            if (ready) changed()
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            controller.setActive(isShown && windowVisibility == VISIBLE)
        }

        override fun onVisibilityChanged(
            changedView: View,
            visibility: Int,
        ) {
            super.onVisibilityChanged(changedView, visibility)
            if (ready) controller.setActive(isAttachedToWindow && isShown && windowVisibility == VISIBLE)
        }

        override fun onWindowVisibilityChanged(visibility: Int) {
            super.onWindowVisibilityChanged(visibility)
            if (ready) controller.setActive(isAttachedToWindow && isShown && visibility == VISIBLE)
        }

        override fun performClick(): Boolean {
            super.performClick()
            return true
        }

        override fun onDetachedFromWindow() {
            cancelTouch()
            controller.setActive(false)
            controller.release()
            super.onDetachedFromWindow()
        }
    }
