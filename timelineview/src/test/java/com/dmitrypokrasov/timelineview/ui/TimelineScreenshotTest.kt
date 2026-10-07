package com.dmitrypokrasov.timelineview.ui

import android.content.pm.ApplicationInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import com.dmitrypokrasov.timelineview.R
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "en-rUS-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TimelineScreenshotTest {
    @Test
    @Config(sdk = [34])
    fun `multilingual text respects system font scale and explicit night palettes`() {
        val cases =
            TimelineMathStrategy.entries.flatMap { strategy ->
                listOf(1f, 2f).flatMap { scale -> listOf(false, true).map { night -> Triple(strategy, scale, night) } }
            }
        cases.forEach { (strategy, scale, night) ->
            val application = RuntimeEnvironment.getApplication()
            application.applicationInfo.flags = application.applicationInfo.flags or ApplicationInfo.FLAG_SUPPORTS_RTL
            val configuration =
                Configuration(application.resources.configuration).apply {
                    fontScale = scale
                    uiMode = if (night) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
                }
            val context = application.createConfigurationContext(configuration)
            val view = TimelineView(context)
            val initial = view.getConfig()
            val steps =
                List(3) { index ->
                    TimelineStepData(
                        id = "$index",
                        timestampMillis = index * 60_000L,
                        title = if (night) "الطلب جاهز $index" else "Delivery 🚚 配送 $index",
                        description = if (night) "تحديث حالة الطلب والتوصيل" else "LongUnbrokenTrackingIdentifier1234567890",
                        progress = if (index == 0) 100 else 25,
                    )
                }
            view.setConfig(
                initial.math.copy(steps = steps, horizontalLayout = TimelineMathConfig.HorizontalLayout.WRAP),
                initial.ui.copy(
                    colors = if (night) TimelineUiConfig.Colors(Color.CYAN, Color.GRAY, Color.WHITE, Color.LTGRAY) else initial.ui.colors,
                ),
            )
            view.setStrategy(strategy, TimelineUiStrategy.Linear)
            view.layoutDirection = if (night) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
            view.measure(View.MeasureSpec.makeMeasureSpec(320, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
            view.layout(0, 0, 320, view.measuredHeight)
            assertEquals(scale, context.resources.configuration.fontScale)
            val bitmap = Bitmap.createBitmap(320, view.height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(if (night) Color.BLACK else Color.WHITE)
            view.draw(Canvas(bitmap))
            compare("accessible-${strategy.key.value}-${scale.toInt()}-$night", bitmap)
            bitmap.recycle()
        }
    }

    @Test
    fun `strategies match reviewed images at narrow and wide sizes in both directions`() {
        val context = RuntimeEnvironment.getApplication()
        context.applicationInfo.flags = context.applicationInfo.flags or ApplicationInfo.FLAG_SUPPORTS_RTL
        val cases =
            TimelineMathStrategy.entries.flatMap { strategy ->
                listOf(280, 480).flatMap { width -> listOf(View.LAYOUT_DIRECTION_LTR, View.LAYOUT_DIRECTION_RTL).map { direction -> Triple(strategy, width, direction) } }
            }
        cases.forEach { (strategy, width, direction) ->
            val view = TimelineView(context)
            val scale = if (width == 280) 1.4f else 1f
            val steps =
                List(4) { index ->
                    TimelineStepData(
                        id = "step-$index",
                        timestampMillis = index * index * 60_000L,
                        title = "Step ${index + 1}",
                        description = "A useful description that wraps on narrow screens.",
                        progress =
                            if (index == 0) {
                                100
                            } else if (index == 1) {
                                50
                            } else {
                                0
                            },
                    )
                }
            view.setConfig(
                TimelineMathConfig(steps = steps, minCellWidth = 140f, horizontalLayout = TimelineMathConfig.HorizontalLayout.WRAP, spacing = TimelineMathConfig.Spacing(stepY = 80f, stepYFirst = 20f, marginTopTitle = 20f, marginHorizontalStroke = 24f, marginHorizontalText = 48f, marginHorizontalImage = 12f), sizes = TimelineMathConfig.Sizes(12f, 24f)),
                TimelineUiConfig(stroke = TimelineUiConfig.Stroke(radius = 16f, sizeStroke = 2f), icons = TimelineUiConfig.Icons(iconDisableLvl = R.drawable.timeline_test_shape, iconProgress = R.drawable.timeline_test_shape), colors = TimelineUiConfig.Colors(Color.BLUE, Color.LTGRAY, Color.BLACK, Color.DKGRAY), textSizes = TimelineUiConfig.TextSizes(sizeTitle = 16f * scale, sizeDescription = 14f * scale)),
            )
            view.setStrategy(strategy, TimelineUiStrategy.Linear)
            view.layoutDirection = direction
            view.setPadding(8, 8, 8, 8)
            view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
            view.layout(0, 0, width, view.measuredHeight)
            val bitmap = Bitmap.createBitmap(width, view.height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.WHITE)
            view.draw(Canvas(bitmap))
            compare("${strategy.key.value}-$width-$direction", bitmap)
            bitmap.recycle()
        }
    }

    private fun compare(
        name: String,
        actual: Bitmap,
    ) {
        val golden = File("src/test/golden/$name.png")
        val report = File("build/reports/screenshots/$name-actual.png")
        report.parentFile!!.mkdirs()
        report.outputStream().use { actual.compress(Bitmap.CompressFormat.PNG, 100, it) }
        if (System.getProperty("timeline.updateGoldens") == "true") {
            golden.parentFile!!.mkdirs()
            report.copyTo(golden, overwrite = true)
            return
        }
        assertTrue("Missing golden $golden; generate and visually review the baseline", golden.isFile)
        val expected = requireNotNull(BitmapFactory.decodeFile(golden.path))
        assertEquals("$name width", expected.width, actual.width)
        assertEquals("$name height", expected.height, actual.height)
        val diff = Bitmap.createBitmap(actual.width, actual.height, Bitmap.Config.ARGB_8888)
        var changed = 0
        for (y in 0 until actual.height) for (x in 0 until actual.width) {
            val a = actual.getPixel(x, y)
            val b = expected.getPixel(x, y)
            val different = listOf(0, 8, 16, 24).any { shift -> abs((a shr shift and 255) - (b shr shift and 255)) > 8 }
            if (different) changed++
            diff.setPixel(x, y, if (different) Color.MAGENTA else Color.WHITE)
        }
        val ratio = changed.toDouble() / (actual.width * actual.height)
        if (ratio > 0.002) File(report.parentFile, "$name-diff.png").outputStream().use { diff.compress(Bitmap.CompressFormat.PNG, 100, it) }
        expected.recycle()
        diff.recycle()
        assertTrue("$name: $changed changed pixels ($ratio); inspect $report", ratio <= 0.002)
    }
}
