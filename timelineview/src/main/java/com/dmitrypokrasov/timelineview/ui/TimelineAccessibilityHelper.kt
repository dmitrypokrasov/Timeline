package com.dmitrypokrasov.timelineview.ui

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.customview.widget.ExploreByTouchHelper

internal class TimelineAccessibilityHelper(
    private val ownerView: View,
    private val controller: TimelineViewController,
) : ExploreByTouchHelper(ownerView) {
    override fun getVirtualViewAt(
        x: Float,
        y: Float,
    ): Int =
        controller
            .buildAccessibilitySnapshot(ownerView.paddingLeft, ownerView.paddingTop)
            .findAt(x, y)
            ?.virtualId
            ?: INVALID_ID

    override fun getVisibleVirtualViews(virtualViewIds: MutableList<Int>) {
        controller
            .buildAccessibilitySnapshot(ownerView.paddingLeft, ownerView.paddingTop)
            .nodes
            .mapTo(virtualViewIds) { it.virtualId }
    }

    override fun onPopulateNodeForVirtualView(
        virtualViewId: Int,
        node: AccessibilityNodeInfoCompat,
    ) {
        val item =
            controller
                .buildAccessibilitySnapshot(ownerView.paddingLeft, ownerView.paddingTop)
                .findById(virtualViewId)
                ?: return

        node.contentDescription = item.contentDescription
        node.setBoundsInParent(item.boundsInParent.toRect())
        node.className =
            if (item.isClickable) {
                Button::class.java.name
            } else {
                View::class.java.name
            }
        node.isFocusable = true
        node.isVisibleToUser = true
        node.isClickable = item.isClickable
        if (item.isClickable) {
            node.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK)
        }
    }

    override fun onPerformActionForVirtualView(
        virtualViewId: Int,
        action: Int,
        arguments: Bundle?,
    ): Boolean {
        if (action != AccessibilityNodeInfoCompat.ACTION_CLICK) return false

        val item =
            controller
                .buildAccessibilitySnapshot(ownerView.paddingLeft, ownerView.paddingTop)
                .findById(virtualViewId)
                ?: return false

        val handled =
            when (item) {
                is TimelineAccessibilityNode.ProgressIcon -> controller.performProgressIconClick()
                is TimelineAccessibilityNode.Step -> controller.performStepClick(item.index)
            }

        if (handled) {
            sendEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_CLICKED)
        }
        return handled
    }

    fun invalidateTimeline() {
        invalidateRoot()
    }
}
