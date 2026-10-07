package com.dmitrypokrasov.timelineview.ui

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.customview.widget.ExploreByTouchHelper

/** Exposes every canvas step as a readable, focusable virtual child. */
internal class TimelineAccessibilityHelper(
    private val host: TimelineView,
    private val controller: TimelineViewController,
) : ExploreByTouchHelper(host) {
    fun onLayoutChanged() {
        val ids = controller.targets().map { it.id }.toSet()
        val keyboard = keyboardFocusedVirtualViewId
        if (keyboard != INVALID_ID && keyboard !in ids) clearKeyboardFocusForVirtualView(keyboard)
        val spoken = accessibilityFocusedVirtualViewId
        if (spoken != INVALID_ID && spoken !in ids) {
            getAccessibilityNodeProvider(host)?.performAction(spoken, AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS, null)
        }
        invalidateRoot()
    }

    override fun getVirtualViewAt(
        x: Float,
        y: Float,
    ): Int =
        controller.targetAt(x - host.paddingLeft, y - host.paddingTop, false) ?: INVALID_ID

    override fun getVisibleVirtualViews(virtualViewIds: MutableList<Int>) {
        virtualViewIds += controller.targets().map { it.id }
    }

    override fun onPopulateNodeForVirtualView(
        virtualViewId: Int,
        node: AccessibilityNodeInfoCompat,
    ) {
        val target = controller.targets().firstOrNull { it.id == virtualViewId }
        node.contentDescription = target?.description.orEmpty()
        val bounds = Rect()
        target?.bounds?.roundOut(bounds)
        bounds.offset(host.paddingLeft, host.paddingTop)
        node.setBoundsInParent(bounds)
        node.className = if (target?.clickable == true) "android.widget.Button" else View::class.java.name
        node.isEnabled = host.isEnabled
        node.isFocusable = true
        node.isClickable = target?.clickable == true && host.isEnabled
        if (node.isClickable) node.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK)
    }

    override fun onPerformActionForVirtualView(
        virtualViewId: Int,
        action: Int,
        arguments: Bundle?,
    ): Boolean {
        if (action != AccessibilityNodeInfoCompat.ACTION_CLICK || !host.isEnabled) return false
        val handled = controller.clickTarget(virtualViewId)
        if (handled) {
            host.performClick()
            sendEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_CLICKED)
        }
        return handled
    }
}
