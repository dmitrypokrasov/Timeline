# Timeline

## Quick Setup

### 1. Include library

**Using Gradle**

```gradle
dependencies {
    implementation "com.github.dmitrypokrasov:timelineview:x.x.x"
}
```

### 2. Usage

#### In XML layout

```xml
<com.dmitrypokrasov.timelineview.core.TimelineView
    android:id="@+id/timeline"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

#### In code
You can also provide custom math and UI engines. The example below shows a linear timeline in vertical orientation.

```kotlin
import androidx.core.content.ContextCompat
import com.dmitrypokrasov.timelineview.core.TimelineMathConfig
import com.dmitrypokrasov.timelineview.core.TimelineStep
import com.dmitrypokrasov.timelineview.core.TimelineView
import com.dmitrypokrasov.timelineview.linear.LinearMathConfig
import com.dmitrypokrasov.timelineview.linear.LinearTimelineMath
import com.dmitrypokrasov.timelineview.linear.LinearTimelineUi
import com.dmitrypokrasov.timelineview.linear.LinearUiConfig

val timelineView = findViewById<TimelineView>(R.id.timeline)

val steps = listOf(
    TimelineStep(
        title = R.string.title_1_lvl,
        description = R.string.description_1_9_steps,
        icon = R.drawable.ic_tobacco_active,
        count = 9,
        maxCount = 9
    ),
    TimelineStep(
        title = R.string.title_2_lvl,
        description = R.string.description_10_99_steps,
        icon = R.drawable.ic_tobacco_active,
        count = 50,
        maxCount = 99
    )
)

// build configs first
val mathConfig = LinearMathConfig(
    steps = steps,
    startPosition = TimelineMathConfig.StartPosition.CENTER,
    stepY = 80f,
)

val uiConfig = LinearUiConfig(
    iconProgress = R.drawable.ic_progress_time_line,
    iconDisableLvl = R.drawable.ic_tobacco_unactive,
    colorProgress = ContextCompat.getColor(this, R.color.purple_700),
    colorStroke = ContextCompat.getColor(this, R.color.purple_200),
)

// configure custom linear engines in vertical orientation
val mathEngine = LinearTimelineMath(mathConfig, LinearTimelineMath.Orientation.VERTICAL)
val uiRenderer = LinearTimelineUi(uiConfig)

timelineView.setMathEngine(mathEngine)
timelineView.setUiRenderer(uiRenderer)
```
