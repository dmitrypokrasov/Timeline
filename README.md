# Timeline

Android timeline widget with pluggable math/UI strategies, multiline text rendering, click handling, and optional Lottie overlays for step badges and the active progress icon.

## Project quality

The project now ships with a shared quality toolchain:

- `./gradlew qualityCheck` runs `ktlint`, `detekt`, Android lint, and unit tests for both modules.
- `./gradlew qualityFormat` formats Kotlin sources with `ktlint`.
- `./gradlew qualityDocs` generates Dokka API docs for the library module.

## Development status

The working tree targets **2.0.0** (not yet published). The last public artifact is **1.1.0**.
See [CHANGELOG.md](CHANGELOG.md) for behavior changes and migration instructions.

Documentation: [migration 1.1 → 2.0](MIGRATION_1_TO_2.md), [architecture](ARCHITECTURE.md),
[custom strategies](CUSTOM_STRATEGIES.md), and [release process](RELEASING.md).

Run `bash scripts/check-release.sh` to check formatting, static analysis, Android lint,
unit/native-rendering tests, API docs, the demo APK and release AAR, then compile an
independent consumer against the staged Maven artifact in `build/repository`.
CI checks quality, screenshots, API compatibility, documentation and migration examples on
pull requests. Tags/manual release verification also run device tests. Reports are workflow
artifacts. The separate manually dispatched release workflow publishes the tested Maven
artifact without rebuilding it; previous versions are preserved. See the release guide for
the required one-time GitHub Pages and branch-protection settings.

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
- `TimelineMathStrategy.Alternating` (2.0): central axis; uses one text column when narrow
- `TimelineMathStrategy.AdaptiveGrid` (2.0): responsive serpentine rows
- `TimelineMathStrategy.TimeScaled` (2.0): chronological events with measured collision avoidance

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

## Configuration and lifecycle (2.0)

Configuration properties are immutable. Use `copy` and `timelineView.setConfig(math, ui)`
to apply changes, or replace the math engine/renderer. Kotlin dimensions are pixels;
convert dp/sp using the host resources. Negative or non-finite dimensions are rejected;
a zero icon size hides that icon. Built-in engines snapshot the supplied step list.

The host owns step state: restore it from a ViewModel or saved state after recreation.
The sample demonstrates saved progress. Animations pause while hidden and are released
on detach. Reattaching starts fresh overlay instances; replacing steps retains unchanged
animations with the same stable step ID (or at the same index when no ID is supplied). Invalid optional Lottie resources leave the static icon visible.
Change the animation settings or remove and re-add it to replay a completed one-shot overlay.

Each step is a virtual accessibility child with its own click action. English and Russian
fallback labels are provided; applications can override the `timeline_*` string resources.
With `android:supportsRtl="true"` in the host manifest, RTL mirrors positions and paths while preserving readable text and matching touch bounds.

Vertical and snake row spacing expands for multiline text and badge overlays.
Horizontal timelines expose three `TimelineMathConfig.HorizontalLayout` policies:
`FIT` preserves equal cells, `WRAP` uses measured serpentine rows, and `SCROLL` requests
an intrinsic width based on `minCellWidth`. For `SCROLL`, place a `wrap_content` width
TimelineView inside a HorizontalScrollView. An exact parent width still wins. The default
remains `FIT` for compatibility; the demo uses `WRAP`. `minCellWidth` is in pixels.
XML exposes `timeline_horizontal_layout`, `timeline_min_cell_width` (a dimension),
and `timeline_progress_mode`; the XML minimum cell width defaults to 160dp.

New custom engines can extend `TimelineMathAdapter`; the core contract is `TimelineLayoutEngine`.
The legacy coordinate getters on `TimelineMathEngine` are deprecated in favor of complete layouts.
Custom math engines opt into measured row expansion with `getContentRows()` and
`setStepExtents()`. Return one contiguous, ordered row index per step, with shared indices
for cells in the same row. Extents describe distances between row origins. Returning null
leaves measurement under the custom engine's control. `textBelowBadge` controls label
clearance. The resolver does not inspect concrete engine classes.

Built-in curved strategies construct their rounded paths before splitting progress by arc
length. `hasRoundedGeometry` prevents a second paint-only corner effect; custom renderers
should honor `setGeometryRounded`. The corner radius is clamped to adjacent segment lengths.

### Identity and progress (2.0)

Assign each step a unique, nonblank `id` when inserting or reordering events. Accessibility
node IDs and Lottie instances then follow that identity. Click callbacks still return the
current list index. Without IDs, identity remains positional. `copy` retains the ID, so assign
a new one when duplicating a step. Duplicate IDs are rejected. Prefer index-based geometry
methods over the legacy methods that accept a step object.

`ProgressMode.SEQUENTIAL` colors the path only through the first unfinished step.
`ProgressMode.INDEPENDENT` colors each incoming segment using its own progress. In both
modes the single progress marker and its animation belong to the first unfinished step;
badge completion follows each step's own progress.

`TimeScaled` requires sorted, non-null `timestampMillis` values. `pixelsPerMillisecond`
sets the vertical scale. Equal times are accepted. Label collisions expand intervals,
so the result is a readable event timeline, not a strictly proportional scientific plot.
Reduce the scale for ranges that would exceed the supported 10,000,000 pixel time span.
Text titles/descriptions preserve Android spans; submit changes through `replaceSteps`.

```kotlin
val steps = events.map { event ->
    TimelineStepData(id = event.id, timestampMillis = event.timeMillis,
        title = event.title, progress = event.progress)
}
timelineView.setConfig(
    math.copy(steps = steps, minCellWidth = 160f * resources.displayMetrics.density,
        horizontalLayout = TimelineMathConfig.HorizontalLayout.WRAP),
    ui
)
timelineView.setStrategy(TimelineMathStrategy.LinearHorizontal, TimelineUiStrategy.Linear)
```

Directly installed engines/renderers survive registry replacement. Calling `setStrategy`
explicitly returns both halves to registry/factory selection. Candidate strategies are resolved
and prepared before installation; failed transitions preserve the prior state and measured frame.

### Verification (2.0)

Run `bash scripts/check-release.sh` for quality checks, documentation, release AAR,
public JVM signatures, and an independent consumer build. `--package-only` omits quality/docs.
CI runs quality, screenshots, packaging/migration, and documentation independently on PRs;
the Required checks job aggregates their status. Device tests on API 27/34 run for tags,
manual dispatch, release verification, and the weekly schedule.
Diagnostic performance runs on manual dispatch and the weekly schedule.

- `python3 scripts/check-api.py` checks `api/timelineview.api.txt` against the release AAR.
  After reviewing an intentional API change, run it with `--update` and review the diff.
  This conservative JVM check includes JVM-public Kotlin internals; it does not validate
  Kotlin metadata or behavioral compatibility. `./gradlew :timelineview:apiCheck` additionally
  checks Kotlin-visible binary API; the standalone migration build exercises Kotlin/Java source
  compatibility. See the release guide before updating either baseline.
- `./gradlew :timelineview:testDebugUnitTest --tests '*TimelineScreenshotTest'` compares
  24 native-rendered fixtures: six strategies, two widths/text sizes, LTR and RTL on API 28.
  Use `-PupdateGoldens=true` only to generate an intentional baseline, visually review the
  images under `timelineview/src/test/golden`, then rerun without that flag. Failures save
  actual/difference images. A small pixel tolerance allows antialiasing differences.
- `./gradlew :timelineview:connectedDebugAndroidTest :app:connectedDebugAndroidTest`
  runs on a connected device/emulator, including real gestures, accessibility and reattachment.
- `./gradlew :timelineview:testDebugUnitTest --tests '*TimelinePerformanceTest' -PincludeBenchmarks`
  reports median layout and clipped-draw times for 10/100/1000 steps under
  `timelineview/build/reports/performance/timings.csv`. These are Robolectric diagnostics,
  not device frame-rate measurements. Drawing skips offscreen steps, but layout still
  measures the complete list; a recycling container remains preferable for unbounded feeds.

## Notes

- Linear timelines now start before the first badge and stop at the last badge anchor instead of drawing a trailing tail.
- Long titles and descriptions are rendered with multiline `StaticLayout` and contribute to measured height.
- `TimelineView` respects padding during drawing, hit testing, and measurement.
- Linear vertical measurement is based on the actual rendered content bounds, which keeps multi-step previews from reserving extra empty space below the last visible element.
- Snake timelines keep their corner transition stable when progress switches color at the beginning of a turn.
- Click handling stays attached to the badge/progress icon bounds even when Lottie overlays are enabled.
