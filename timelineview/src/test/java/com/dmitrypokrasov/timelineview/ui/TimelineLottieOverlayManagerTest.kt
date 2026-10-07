package com.dmitrypokrasov.timelineview.ui

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import android.widget.FrameLayout
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieTask
import com.dmitrypokrasov.timelineview.R
import com.dmitrypokrasov.timelineview.model.TimelineLottieSpec
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.concurrent.Executor

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TimelineLottieOverlayManagerTest {
    private val originalExecutor = LottieTask.EXECUTOR

    @Before
    fun prepareExecutor() {
        LottieCompositionFactory.clearCache(RuntimeEnvironment.getApplication())
        LottieTask.EXECUTOR = Executor { it.run() }
    }

    @After
    fun restoreExecutor() {
        LottieTask.EXECUTOR = originalExecutor
        LottieCompositionFactory.clearCache(RuntimeEnvironment.getApplication())
    }

    @Test
    fun `removed overlays are evicted and shared spec has independent occurrences`() {
        val manager = TimelineLottieOverlayManager(View(RuntimeEnvironment.getApplication()))
        val spec = TimelineLottieSpec(R.raw.timeline_test_animation)
        val steps = List(3) { TimelineStepData(progress = 30, badgeAnimation = spec, progressAnimation = spec) }
        manager.submit(steps)
        assertEquals(4, manager.entryCount)
        manager.submit(steps.take(1))
        assertEquals(2, manager.entryCount)
        manager.submit(listOf(steps.first().copy(progress = 100, badgeAnimation = null)))
        assertEquals(0, manager.entryCount)
        manager.submit(steps)
        manager.setActive(false)
        manager.clear()
        assertEquals(0, manager.entryCount)
    }

    @Test
    fun `visibility pauses playback and eviction stops active animations`() {
        val manager = TimelineLottieOverlayManager(View(RuntimeEnvironment.getApplication()))
        try {
            val composition = LottieCompositionFactory.fromRawResSync(RuntimeEnvironment.getApplication(), R.raw.timeline_test_animation)
            assertNotNull(composition.exception?.toString(), composition.value)
            val spec = TimelineLottieSpec(R.raw.timeline_test_animation)
            manager.submit(listOf(TimelineStepData(progress = 20, badgeAnimation = spec)))
            assertEquals(0, manager.runningAnimationCount)
            manager.setActive(true)
            drawVisible(manager, listOf(0))
            assertEquals(1, manager.runningAnimationCount)
            manager.setActive(false)
            assertEquals(0, manager.runningAnimationCount)
            manager.setActive(true)
            drawVisible(manager, listOf(0))
            assertEquals(1, manager.runningAnimationCount)
            manager.submit(emptyList())
            assertEquals(0, manager.runningAnimationCount)
            manager.submit(listOf(TimelineStepData(progress = 20, badgeAnimation = TimelineLottieSpec(android.R.string.ok))))
            assertEquals(0, manager.runningAnimationCount)
        } finally {
            manager.clear()
        }
    }

    @Test
    fun `only overlays intersecting the scrolled viewport play and reentry resumes`() {
        val manager = TimelineLottieOverlayManager(View(RuntimeEnvironment.getApplication()))
        try {
            val spec = TimelineLottieSpec(R.raw.timeline_test_animation)
            manager.submit(List(100) { TimelineStepData(progress = 100, badgeAnimation = spec) })
            manager.setActive(true)
            assertEquals(0, manager.runningAnimationCount)
            drawVisible(manager, (0 until 100).toList())
            assertEquals(2, manager.runningAnimationCount)
            drawVisible(manager, (0 until 100).toList(), scrollY = 2500f)
            assertEquals(2, manager.runningAnimationCount)
            drawVisible(manager, emptyList())
            assertEquals(0, manager.runningAnimationCount)
            drawVisible(manager, listOf(0))
            assertEquals(1, manager.runningAnimationCount)
            manager.setActive(false)
            assertEquals(0, manager.runningAnimationCount)
        } finally {
            manager.clear()
        }
    }

    @Test
    fun `fully clipped owner pauses even when drawing is skipped and reentry resumes`() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().visible()
        val parent = FrameLayout(activity.get())
        val owner = View(activity.get())
        val manager = TimelineLottieOverlayManager(owner)
        try {
            activity.get().setContentView(parent)
            parent.addView(owner, FrameLayout.LayoutParams(100, 100))
            val exact = View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.EXACTLY)
            parent.measure(exact, exact)
            parent.layout(0, 0, 200, 200)
            manager.submit(listOf(TimelineStepData(progress = 20, badgeAnimation = TimelineLottieSpec(R.raw.timeline_test_animation))))
            manager.setActive(true)
            owner.viewTreeObserver.dispatchOnPreDraw()
            assertTrue(owner.getGlobalVisibleRect(Rect()))
            drawVisible(manager, listOf(0))
            assertEquals(1, manager.runningAnimationCount)
            parent.scrollTo(0, 300)
            assertFalse(owner.getGlobalVisibleRect(Rect()))
            owner.viewTreeObserver.dispatchOnPreDraw()
            // A parent can skip onDraw entirely for a fully clipped child.
            assertEquals(0, manager.runningAnimationCount)
            parent.scrollTo(0, 0)
            owner.viewTreeObserver.dispatchOnPreDraw()
            drawVisible(manager, listOf(0))
            assertEquals(1, manager.runningAnimationCount)
            manager.clear()
            owner.viewTreeObserver.dispatchOnPreDraw()
            assertEquals(0, manager.runningAnimationCount)
        } finally {
            manager.clear()
            activity.pause().stop().destroy()
        }
    }

    private fun drawVisible(
        manager: TimelineLottieOverlayManager,
        indices: List<Int>,
        scrollY: Float = 0f,
    ) {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.translate(0f, -scrollY)
        manager.beginFrame()
        indices.forEach { manager.draw(canvas, TimelineLottieOverlayManager.Key(it), 0f, it * 50f, 24f) }
        manager.endFrame()
        bitmap.recycle()
    }
}
