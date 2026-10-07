package com.dmitrypokrasov.timelineview.ui

import android.view.View
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieTask
import com.dmitrypokrasov.timelineview.R
import com.dmitrypokrasov.timelineview.model.TimelineLottieSpec
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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
            assertEquals(1, manager.runningAnimationCount)
            manager.setActive(false)
            assertEquals(0, manager.runningAnimationCount)
            manager.setActive(true)
            assertEquals(1, manager.runningAnimationCount)
            manager.submit(emptyList())
            assertEquals(0, manager.runningAnimationCount)
            manager.submit(listOf(TimelineStepData(progress = 20, badgeAnimation = TimelineLottieSpec(android.R.string.ok))))
            assertEquals(0, manager.runningAnimationCount)
        } finally {
            manager.clear()
        }
    }
}
