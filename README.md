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
<com.dmitrypokrasov.timelineview.ui.TimelineView
    android:id="@+id/timeline"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    style="@style/Widget.TimelineView"
    app:timeline_start_position="CENTER"
    app:timeline_progress_icon="@drawable/ic_progress_time_line"
    app:timeline_disable_icon="@drawable/ic_tobacco_unactive" />
```

#### XML attributes

**Strategies**

| Attribute | Description |
|----------|-------------|
| `app:timeline_start_position` | Start position of the timeline (`START`, `CENTER`, `END`). |
| `app:timeline_math_strategy` | Math strategy for positioning (`SNAKE`, `LINEAR_VERTICAL`, `LINEAR_HORIZONTAL`). |
| `app:timeline_math_strategy_id` | Custom math strategy ID (overrides `timeline_math_strategy` if provided). |
| `app:timeline_ui_strategy` | UI rendering strategy (`SNAKE`, `LINEAR`). |
| `app:timeline_ui_strategy_id` | Custom UI strategy ID (overrides `timeline_ui_strategy` if provided). |

**Colors & icons**

| Attribute | Description |
|----------|-------------|
| `app:timeline_progress_color` | Color of completed part of the stroke. |
| `app:timeline_stroke_color` | Color of the remaining part of the stroke. |
| `app:timeline_title_color` | Color of step titles. |
| `app:timeline_description_color` | Color of step descriptions. |
| `app:timeline_progress_icon` | Drawable for the current progress icon. |
| `app:timeline_disable_icon` | Drawable for inactive step icons. |

**Sizes**

| Attribute | Description |
|----------|-------------|
| `app:timeline_stroke_size` | Thickness of the stroke. |
| `app:timeline_title_size` | Text size of step titles. |
| `app:timeline_description_size` | Text size of step descriptions. |
| `app:timeline_radius_size` | Corner radius of the stroke path. |
| `app:timeline_image_lvl_size` | Size of step icons. |
| `app:timeline_icon_progress_size` | Size of the progress icon. |

**Spacing**

| Attribute | Description |
|----------|-------------|
| `app:timeline_step_y_size` | Vertical distance between steps. |
| `app:timeline_step_y_first_size` | Top offset before the first step. |
| `app:timeline_margin_top_title` | Top margin for titles. |
| `app:timeline_margin_top_description` | Top margin for descriptions. |
| `app:timeline_margin_top_progress_icon` | Top margin for progress icon. |
| `app:timeline_margin_horizontal_image` | Horizontal margin for step icons. |
| `app:timeline_margin_horizontal_text` | Horizontal margin for text. |
| `app:timeline_margin_horizontal_stroke` | Horizontal margin for the vertical stroke. |

#### Simplified setup via style/theme
Prefer styles/themes for common defaults and override only what differs.

```xml
<style name="Widget.TimelineView">
    <item name="timeline_start_position">CENTER</item>
    <item name="timeline_progress_color">@color/purple_700</item>
    <item name="timeline_stroke_color">@color/purple_200</item>
    <item name="timeline_progress_icon">@drawable/ic_progress_time_line</item>
    <item name="timeline_disable_icon">@drawable/ic_tobacco_unactive</item>
</style>
```

#### In code
You can also provide custom math and UI engines. The example below shows a linear timeline in vertical orientation.

```kotlin
import androidx.core.content.ContextCompat
import com.dmitrypokrasov.timelineview.math.LinearTimelineMath
import com.dmitrypokrasov.timelineview.render.LinearTimelineUi
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.ui.TimelineView

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
val mathConfig = TimelineMathConfig(
    steps = steps,
    startPosition = TimelineMathConfig.StartPosition.CENTER,
    spacing = TimelineMathConfig.Spacing(stepY = 80f)
)

val uiConfig = TimelineUiConfig(
    icons = TimelineUiConfig.Icons(
        iconProgress = R.drawable.ic_progress_time_line,
        iconDisableLvl = R.drawable.ic_tobacco_unactive
    ),
    colors = TimelineUiConfig.Colors(
        colorProgress = ContextCompat.getColor(this, R.color.purple_700),
        colorStroke = ContextCompat.getColor(this, R.color.purple_200)
    )
)

// configure custom linear engines in vertical orientation
val mathEngine = LinearTimelineMath(mathConfig, LinearTimelineMath.Orientation.VERTICAL)
val uiRenderer = LinearTimelineUi(uiConfig)

timelineView.setMathEngine(mathEngine)
timelineView.setUiRenderer(uiRenderer)
```

#### Switching strategies in code
Use a composite strategy to swap both math and UI renderers together.

```kotlin
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy

timelineView.setStrategy(
    TimelineStrategy(
        math = TimelineMathStrategy.LinearHorizontal,
        ui = TimelineUiStrategy.Linear
    )
)
```

#### Registering custom strategies
You can register your own math/UI implementations and reference them by key.
Built-in keys are `snake`, `linear_vertical`, `linear_horizontal`, and `linear`.

```kotlin
import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer
import com.dmitrypokrasov.timelineview.strategy.TimelineMathProvider
import com.dmitrypokrasov.timelineview.strategy.TimelineStrategyRegistry
import com.dmitrypokrasov.timelineview.strategy.TimelineUiProvider

TimelineStrategyRegistry.registerMath(object : TimelineMathProvider {
    override val key: StrategyKey = StrategyKey("custom_math")
    override fun create(config: TimelineMathConfig): TimelineMathEngine {
        return MyCustomMath(config)
    }
})

TimelineStrategyRegistry.registerUi(object : TimelineUiProvider {
    override val key: StrategyKey = StrategyKey("custom_ui")
    override fun create(config: TimelineUiConfig): TimelineUiRenderer {
        return MyCustomUi(config)
    }
})

timelineView.setStrategy(
    mathStrategyKey = StrategyKey("custom_math"),
    uiStrategyKey = StrategyKey("custom_ui")
)
```

#### Local registry per TimelineView
If you want different strategy sets for different views, create a local registry and pass it to a single
`TimelineView` instance (or use the builder).

```kotlin
import com.dmitrypokrasov.timelineview.config.StrategyKey
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.math.TimelineMathEngine
import com.dmitrypokrasov.timelineview.render.TimelineUiRenderer
import com.dmitrypokrasov.timelineview.strategy.TimelineStrategyRegistry
import com.dmitrypokrasov.timelineview.strategy.TimelineMathProvider
import com.dmitrypokrasov.timelineview.strategy.TimelineUiProvider

val localRegistry = TimelineStrategyRegistry.createLocalRegistry()

localRegistry.registerMath(object : TimelineMathProvider {
    override val key: StrategyKey = StrategyKey("local_math")
    override fun create(config: TimelineMathConfig): TimelineMathEngine {
        return MyCustomMath(config)
    }
})

localRegistry.registerUi(object : TimelineUiProvider {
    override val key: StrategyKey = StrategyKey("local_ui")
    override fun create(config: TimelineUiConfig): TimelineUiRenderer {
        return MyCustomUi(config)
    }
})

timelineView.setStrategyRegistry(localRegistry)
timelineView.setStrategy(
    mathStrategyKey = StrategyKey("local_math"),
    uiStrategyKey = StrategyKey("local_ui")
)
```
