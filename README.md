# Timeline

Android timeline widget with pluggable math/UI strategies, multiline text rendering, click handling, and optional Lottie overlays for step badges and the active progress icon.

## Project quality

The project now ships with a shared quality toolchain:

- `./gradlew qualityCheck` runs `ktlint`, `detekt`, Android lint, and unit tests for both modules.
- `./gradlew qualityFormat` formats Kotlin sources with `ktlint`.
- `./gradlew qualityDocs` generates Dokka API docs for the library module.

## Development status

The latest public release is **2.0.0**, published on 2026-10-07. Version **1.1.0** remains available unchanged.
See [CHANGELOG.md](CHANGELOG.md) for behavior changes and migration instructions.

Documentation: [migration 1.1 → 2.0](MIGRATION_1_TO_2.md), [architecture](ARCHITECTURE.md),
[custom strategies](CUSTOM_STRATEGIES.md), and [release process](RELEASING.md).

Run `bash scripts/check-release.sh` to check formatting, static analysis, Android lint,
unit/native-rendering tests, API docs, the demo APK and release AAR, then compile an
independent consumer against the staged Maven artifact in `build/repository`, including
a release APK with R8 and resource shrinking. Device jobs launch that minified APK and
verify XML inflation, rendered Lottie content and accessible click callbacks.
CI checks quality, screenshots, API compatibility, documentation and migration examples on
pull requests. Tags/manual release verification also run device tests. Reports are workflow
artifacts. The separate manually dispatched release workflow publishes the tested Maven
artifact without rebuilding it; previous versions are preserved. See the release guide for
the required one-time GitHub Pages and branch-protection settings.

## Requirements

Timeline 2.0 supports **Android 8.1 / API 27 and newer**. The checked-in builds use
JDK 17, Gradle 8.6, AGP 8.4.0, Kotlin 1.9.0 and compile SDK 34; library bytecode targets
Java 8. These are the verified toolchain versions, not a claim that every older/newer
consumer toolchain is compatible. Kotlin and Java consumers are compiled against the AAR.
The library uses Android Views; a Compose host needs its own `AndroidView` integration.

## Installation

```gradle
repositories {
    maven { url "https://dmitrypokrasov.github.io/Timeline/maven" }
}

dependencies {
    implementation "com.github.dmitrypokrasov:timelineview:2.0.0"
}
```

GitHub Packages publication is still available for private/authenticated installs, but the public distribution endpoint is GitHub Pages.

## Minimal Kotlin quickstart (2.0)

The example below uses the published **2.0.0** dependency above. When upgrading an existing
1.1.0 host, follow the [migration guide](MIGRATION_1_TO_2.md); 2.0 is a breaking major release.

The following host is compiled and rendered by the independent consumer tests. Attach
`MigratedTimeline(context).view` to your layout. It draws text and a progress line without
custom drawable resources; tapping a step updates its progress.

<!-- source: integration/migration/after/MigratedTimeline.kt -->
```kotlin
package com.example.migration

import android.content.Context
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.math.LinearTimelineMath
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.render.LinearTimelineUi
import com.dmitrypokrasov.timelineview.ui.TimelineView

/** The host owns immutable input; view updates are explicit. */
class MigratedTimeline(context: Context) {
    var steps = listOf(TimelineStepData(id = "order", title = "Order", progress = 20), TimelineStepData(id = "delivery", title = "Delivery", progress = 0))
        private set
    private var math = TimelineMathConfig(steps = steps, spacing = TimelineMathConfig.Spacing(stepY = 80f), sizes = TimelineMathConfig.Sizes(sizeImageLvl = 24f))
    private val ui = TimelineUiConfig()
    val view = TimelineView(context).apply {
        setStrategies(LinearTimelineMath(math), LinearTimelineUi(ui))
        setOnStepClickListener { _, step -> updateProgress(requireNotNull(step.id), (step.progress + 10).coerceAtMost(100)) }
    }

    fun updateProgress(id: String, progress: Int) {
        steps = steps.map { if (it.id == id) it.copy(progress = progress) else it }
        math = math.copy(steps = steps)
        view.replaceSteps(steps)
    }

    fun resize(rowDistancePx: Float) {
        math = math.copy(spacing = math.spacing.copy(stepY = rowDistancePx))
        view.setConfig(math, ui)
    }

    fun reorder() {
        steps = steps.reversed()
        math = math.copy(steps = steps)
        view.replaceSteps(steps)
    }
}
```

Default colors are opaque teal/gray with dark text, intended for light surfaces. XML and
Kotlin use the same palette; provide explicit colors for dark themes. Icon resource `0`
means no static icon. XML dimensions use dp/sp defaults; Kotlin dimensions are pixels.

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

Overlays support only local `@RawRes` animations.

## Grouped events (development preview)

`GroupedTimelineView` and `TimelineSection` are additions for the next release; they are not
in the published 2.0.0 AAR. Test this example against `build/repository` after running
`bash scripts/check-release.sh`. The example is compiled by the independent consumer.

Each section has an accessible heading and an independent timeline. Section order and date
formatting belong to the host; IDs must be unique per section, while step IDs may repeat
across different sections. All built-in layouts are supported. Use individual `TimelineView`s
for custom registries. Empty sections retain their headings. This container measures all
sections and is intended for bounded histories, not an unbounded feed.

<!-- source: integration/migration/after/GroupedTimelineSample.kt -->
```kotlin
package com.example.migration

import android.content.Context
import com.dmitrypokrasov.timelineview.config.TimelineConfigParser
import com.dmitrypokrasov.timelineview.config.TimelineMathStrategy
import com.dmitrypokrasov.timelineview.config.TimelineUiStrategy
import com.dmitrypokrasov.timelineview.model.TimelineSection
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.ui.GroupedTimelineView

/** The host chooses section order, labels and the time zone used for date grouping. */
fun groupedTimeline(context: Context): GroupedTimelineView {
    val config = TimelineConfigParser(context).parse(null).copy(
        mathStrategy = TimelineMathStrategy.LinearVertical,
        uiStrategy = TimelineUiStrategy.Linear,
    )
    return GroupedTimelineView(context).apply {
        setSections(listOf(
            TimelineSection("today", "Today", listOf(TimelineStepData(id = "delivery", title = "Delivered", progress = 100))),
            TimelineSection("yesterday", "Yesterday", listOf(TimelineStepData(id = "dispatch", title = "Dispatched", progress = 100))),
        ), config)
    }
}
```

Use `replaceSections` to update data without replacing unchanged section views.
`setOnStepClickListener` receives `(sectionId, indexWithinSection, step)`; the host owns progress
and persistence. Put the container in a `ScrollView` when its sections exceed the screen.

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

`getConfig()` returns a configuration snapshot. `setConfig(math, ui)` updates the input
while retaining the selected engines/renderers, including directly installed instances.
`setConfig(TimelineConfig)` reapplies its declarative strategy keys/enums through the registry;
use it when restoring a declarative setup. `replaceSteps` changes only the step list.

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

### Strategy configuration matrix (2.0)

All strategies support the progress modes, stable IDs, text measurement and RTL mirroring.
`LinearHorizontal(WRAP)` delegates geometry to `AdaptiveGrid`. The two built-in UI strategy
names currently share the same renderer; they are retained as public configuration choices.

| Math strategy | Axis/origin | Row distance | Width controls |
| --- | --- | --- | --- |
| Snake | `startPosition` sets the path origin | `stepY` minimum, expanded for content | Stroke/image/text horizontal margins |
| LinearVertical | `startPosition` positions the vertical line | `stepY` minimum, expanded for content | Stroke margin positions axis; text margin clears it; image margin unused |
| LinearHorizontal FIT | Equal cells; `startPosition` unused | Single row; `stepY` / `stepYFirst` unused | Available width and stroke margins; `minCellWidth` unused |
| LinearHorizontal SCROLL | Same as FIT, with requested intrinsic width | Single row | `minCellWidth` and stroke margins; exact parent width wins |
| LinearHorizontal WRAP / AdaptiveGrid | Serpentine cells; `startPosition` unused | `stepY` minimum, expanded per row | `minCellWidth` and badge size; horizontal margins unused |
| Alternating | Central axis when wide; one label column when narrow; `startPosition` unused | `stepY` minimum, expanded for content | `minCellWidth` selects one/two columns; text minus stroke margin sets gap |
| TimeScaled | Same placement as Alternating | Timestamp difference × `pixelsPerMillisecond`, expanded for content; `stepY` unused | Same as Alternating |

`stepYFirst` offsets the first row in vertical/grid layouts. Title/description offsets are
requested positions; measurement moves labels to clear preceding content and, in grid and
horizontal layouts, the scaled badge overlay. Icon dimensions apply everywhere. Progress
icon top offset is a legacy LinearVertical adjustment. `horizontalLayout` affects only
LinearHorizontal; timestamps and `pixelsPerMillisecond` affect only TimeScaled. Corner
radius rounds turns in Snake/grid paths; a straight line has no turn to round.

### XML attribute reference (2.0)

The defaults below apply to XML inflation. Explicit Android dimensions are converted to
pixels. Kotlin constructors use the same numeric dimensions in **pixels**, rather than dp/sp.
Unspecified icons are absent; colors do not automatically switch with the application theme.
See the matrix above for strategy-specific applicability.

| Attribute | Default | Meaning |
| --- | --- | --- |
| `timeline_math_strategy` | `SNAKE` | `SNAKE`, `LINEAR_VERTICAL`, `LINEAR_HORIZONTAL`, `ALTERNATING`, `ADAPTIVE_GRID`, `TIME_SCALED` |
| `timeline_math_strategy_id` | absent | Registry key, takes precedence over the math enum |
| `timeline_ui_strategy` | `SNAKE` | `SNAKE` or `LINEAR` renderer |
| `timeline_ui_strategy_id` | absent | Registry key, takes precedence over the UI enum |
| `timeline_start_position` | `CENTER` | `START`, `CENTER`, `END` origin |
| `timeline_horizontal_layout` | `FIT` | `FIT`, `WRAP`, `SCROLL` |
| `timeline_min_cell_width` | `160dp` | Minimum grid/scroll cell width; alternating column threshold |
| `timeline_progress_mode` | `SEQUENTIAL` | Stop at first unfinished step, or `INDEPENDENT` segments |
| `timeline_progress_color` | `#00695C` | Completed line |
| `timeline_stroke_color` | `#B0BEC5` | Pending line |
| `timeline_title_color` | `#212121` | Title text |
| `timeline_description_color` | `#616161` | Description text |
| `timeline_progress_icon` | `0` | Active marker drawable resource |
| `timeline_disable_icon` | `0` | Fallback step drawable resource |
| `timeline_stroke_size` | `2dp` | Line width |
| `timeline_title_size` | `16sp` | Title font size |
| `timeline_description_size` | `14sp` | Description font size |
| `timeline_radius_size` | `0dp` | Corner radius |
| `timeline_step_y_size` | `80dp` | Minimum row spacing |
| `timeline_step_y_first_size` | `20dp` | First row offset |
| `timeline_margin_top_title` | `20dp` | Requested title baseline offset |
| `timeline_margin_top_description` | `8dp` | Requested description baseline offset from title |
| `timeline_margin_top_progress_icon` | `0dp` | LinearVertical marker offset |
| `timeline_margin_horizontal_image` | `16dp` | Snake badge margin |
| `timeline_margin_horizontal_text` | `64dp` | Label position/gap |
| `timeline_margin_horizontal_stroke` | `28dp` | Path inset or alternating gap component |
| `timeline_image_lvl_size` | `32dp` | Badge size; zero hides badge/overlay |
| `timeline_icon_progress_size` | `24dp` | Active marker size; zero hides marker/overlay |

Step data, IDs, timestamps, Lottie specs and the time scale are supplied in code, not XML.

### Verification (2.0)

Run `bash scripts/check-release.sh` for quality checks, documentation, release AAR,
public JVM signatures, and an independent consumer build. `--package-only` omits quality/docs.
CI runs quality, screenshots, packaging/migration, and documentation independently on PRs;
the Required checks job aggregates their status. Device tests on API 27/34 run for tags,
manual dispatch, release verification, and the weekly schedule.
Diagnostic performance also runs for release verification, tags, manual dispatch and the weekly schedule.

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
