# Timeline

Android timeline widget with pluggable math/UI strategies, multiline text rendering, click handling, and optional Lottie overlays for step badges and the active progress icon.

## Project quality

The project now ships with a shared quality toolchain:

- `./gradlew qualityCheck` runs `ktlint`, `detekt`, Android lint, and unit tests for both modules.
- `./gradlew qualityFormat` formats Kotlin sources with `ktlint`.
- `./gradlew qualityDocs` generates Dokka API docs for the library module.

## Installation

```gradle
repositories {
    maven { url "https://dmitrypokrasov.github.io/Timeline/maven" }
}

dependencies {
    implementation "com.github.dmitrypokrasov:timelineview:1.1.0"
}
```

GitHub Packages publication is still available for private/authenticated installs, but the public distribution endpoint is GitHub Pages.

## XML usage

```xml
<com.dmitrypokrasov.timelineview.ui.TimelineView
    android:id="@+id/timeline"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:timeline_start_position="START"
    app:timeline_math_strategy="LINEAR_VERTICAL"
    app:timeline_ui_strategy="LINEAR"
    app:timeline_progress_icon="@drawable/ic_progress_timeline"
    app:timeline_disable_icon="@drawable/ic_unactive" />
```

## Kotlin usage

```kotlin
val steps = listOf(
    TimelineStepData(
        title = "Step 1",
        description = "A long description will wrap inside the view width automatically.",
        iconRes = R.drawable.ic_active,
        iconDisabledRes = R.drawable.ic_unactive,
        badgeAnimation = TimelineLottieSpec(R.raw.timeline_badge_pulse, scale = 1.2f),
        progress = 100
    ),
    TimelineStepData(
        title = "Step 2",
        description = "The active step can also provide a Lottie overlay for the progress icon.",
        iconRes = R.drawable.ic_active,
        iconDisabledRes = R.drawable.ic_unactive,
        progressAnimation = TimelineLottieSpec(R.raw.timeline_progress_orbit, scale = 1.25f),
        progress = 45
    )
)

val mathConfig = TimelineMathConfig(
    steps = steps,
    startPosition = TimelineMathConfig.StartPosition.START,
    spacing = TimelineMathConfig.Spacing(
        stepY = 80f,
        stepYFirst = 20f,
        marginTopTitle = 52f,
        marginTopDescription = 16f,
        marginTopProgressIcon = 6f,
        marginHorizontalImage = 16f,
        marginHorizontalText = 80f,
        marginHorizontalStroke = 40f
    ),
    sizes = TimelineMathConfig.Sizes(
        sizeImageLvl = 48f,
        sizeIconProgress = 28f
    )
)

val uiConfig = TimelineUiConfig(
    icons = TimelineUiConfig.Icons(
        iconProgress = R.drawable.ic_progress_timeline,
        iconDisableLvl = R.drawable.ic_unactive
    ),
    colors = TimelineUiConfig.Colors(
        colorTitle = Color.BLACK,
        colorDescription = Color.DKGRAY,
        colorStroke = Color.GRAY,
        colorProgress = Color.GREEN
    ),
    textSizes = TimelineUiConfig.TextSizes(
        sizeTitle = 12f,
        sizeDescription = 12f
    ),
    stroke = TimelineUiConfig.Stroke(
        sizeStroke = 6f,
        radius = 48f
    )
)

timelineView.replaceSteps(steps)
timelineView.setMathEngine(LinearTimelineMath(mathConfig, LinearTimelineMath.Orientation.VERTICAL))
timelineView.setUiRenderer(LinearTimelineUi(uiConfig))
```

## Lottie overlays

`TimelineStepData` supports two optional overlays:

- `badgeAnimation`: drawn above the step badge icon.
- `progressAnimation`: drawn above the active progress icon for the first step with `progress != 100`.

Version 1 supports only local `@RawRes` animations.

## Strategies

Built-in math strategies:

- `TimelineMathStrategy.Snake`
- `TimelineMathStrategy.LinearVertical`
- `TimelineMathStrategy.LinearHorizontal`

Built-in UI strategies:

- `TimelineUiStrategy.Snake`
- `TimelineUiStrategy.Linear`

Switch both together:

```kotlin
timelineView.setStrategy(
    TimelineStrategy(
        math = TimelineMathStrategy.LinearHorizontal,
        ui = TimelineUiStrategy.Linear
    )
)
```

## Custom registries

```kotlin
val registry = TimelineStrategyRegistry.createLocalRegistry()

registry.registerMath(object : TimelineMathProvider {
    override val key = StrategyKey("custom_math")
    override fun create(config: TimelineMathConfig): TimelineMathEngine = MyMathEngine(config)
})

registry.registerUi(object : TimelineUiProvider {
    override val key = StrategyKey("custom_ui")
    override fun create(config: TimelineUiConfig): TimelineUiRenderer = MyUiRenderer(config)
})

timelineView.setStrategyRegistry(registry)
timelineView.setStrategy(
    mathStrategyKey = StrategyKey("custom_math"),
    uiStrategyKey = StrategyKey("custom_ui")
)
```

## Notes

- Linear timelines now start before the first badge and stop at the last badge anchor instead of drawing a trailing tail.
- Long titles and descriptions are rendered with multiline `StaticLayout` and contribute to measured height.
- `TimelineView` respects padding during drawing, hit testing, and measurement.
- Linear vertical measurement is based on the actual rendered content bounds, which keeps multi-step previews from reserving extra empty space below the last visible element.
- Snake timelines keep their corner transition stable when progress switches color at the beginning of a turn.
- Click handling stays attached to the badge/progress icon bounds even when Lottie overlays are enabled.
