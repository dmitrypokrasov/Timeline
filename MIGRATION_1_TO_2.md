# Migrating from 1.1.0 to 2.0

**2.0.0 is not published yet.** Keep the public dependency at 1.1.0 until a release is
announced. To try this checkout, run `bash scripts/check-release.sh` and use the Maven
repository under `build/repository`. [Release instructions](RELEASING.md) describe publication.

## Compatibility policy

This is a breaking major release. Recompile applications and custom engine/renderer modules
together. Binary compatibility with 1.1.0 is not promised. The 1.1.0 Maven directory remains
unchanged and remains available for rollback. Check [CHANGELOG.md](CHANGELOG.md) for details.

## Configuration: mutate → copy and apply

The following old source is compiled against the actual published 1.1.0 AAR in CI.

### Before: published 1.1.0

<!-- source: integration/migration/before/LegacyTimeline.kt -->
```kotlin
package com.example.migration.legacy

import android.content.Context
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.config.TimelineUiConfig
import com.dmitrypokrasov.timelineview.math.LinearTimelineMath
import com.dmitrypokrasov.timelineview.model.TimelineStepData
import com.dmitrypokrasov.timelineview.render.LinearTimelineUi
import com.dmitrypokrasov.timelineview.ui.TimelineView

/** This source is compiled against the unchanged published 1.1.0 AAR. */
fun legacyTimeline(context: Context): TimelineView {
    val math = TimelineMathConfig(steps = listOf(TimelineStepData(title = "Order", progress = 20)))
    math.spacing.stepY = 80f
    math.sizes.sizeImageLvl = 24f
    return TimelineView(context).apply {
        setMathEngine(LinearTimelineMath(math))
        setUiRenderer(LinearTimelineUi(TimelineUiConfig()))
    }
}
```

### After: staged 2.0

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


## Behavioral changes to review

| Area | 1.1 behavior | 2.0 migration |
|---|---|---|
| Configuration | Mutable fields | Use `copy`, then `setConfig(math, ui)` |
| Invalid sizes | Negative values could be clamped | Validate input; negative/non-finite dimensions throw; zero hides an icon |
| Kotlin dimensions | Pixels | Still pixels; explicitly convert dp/sp |
| XML defaults | Older raw defaults | Density-aware dp/sp; review screenshots at multiple font sizes |
| Long text | Could collide with following steps | Rows expand; `stepY` is a minimum distance |
| Linear badges | Could be offset from the path | Badges follow the axis; `marginHorizontalImage` applies to Snake |
| Snake progress | Older corner interpolation | Arc length of the actual rounded incoming path |
| Horizontal content | Fixed layout behavior | Default `FIT`; opt into `WRAP` or `SCROLL` with `minCellWidth` |
| Custom row expansion | No declared row contract | Implement both `getContentRows` and `setStepExtents` |
| Mutable text | Unspecified update behavior | Submit changed text through `replaceSteps`; do not mutate installed spans |

## Identity, progress and persistence

Provide a unique stable `id` for each logical step. `copy` keeps the ID; give duplicated
steps new IDs. IDs preserve accessibility nodes and unchanged Lottie overlays on reorder.
Without an ID, the compatibility fallback is the list position. Click callbacks still report
the current index. Duplicate or blank explicit IDs are rejected.

`SEQUENTIAL` is the default. In `[0, 100, 50]`, later badges reflect their own completion,
but the completed line stops at the first unfinished step. Choose `INDEPENDENT` when each
incoming segment should show its own completion. The single active marker still belongs
to the first unfinished step.

Persist host data in saved state or a ViewModel. The view does not save the business model.
Animations pause when hidden; detaching releases them and reattachment creates fresh instances.

For horizontal scrolling, put a `wrap_content` width TimelineView inside a
HorizontalScrollView and select `SCROLL`. An exact parent width takes precedence. `WRAP`
uses serpentine rows. The [compiled XML example](integration/migration/res/layout/migrated_timeline.xml)
shows the new attributes.

## Custom implementations

Existing implementations can still implement `TimelineMathEngine`. Coordinate accessors
are deprecated; prefer `buildLayout` and the [new adapter example](CUSTOM_STRATEGIES.md).
The adapter's legacy progress getters only support the active marker. Built-in engines
retain their existing indexed progress implementations.

If a renderer applies `CornerPathEffect`, honor `setGeometryRounded(true)` to avoid rounding
already-curved engine paths a second time. Treat preparation as repeatable and restoreable;
see [architecture and failure handling](ARCHITECTURE.md#strategy-selection-and-failure-handling).

## Verify the migration

`bash scripts/check-release.sh` compiles the old Kotlin fixture against local Maven 1.1.0,
then compiles the migrated Kotlin, Java and XML examples against the staged 2.0 AAR. Its
Robolectric tests exercise progress updates, stable identity after reorder, configuration
updates, the adapter and XML inflation. No source-project dependency is used.

For an application migration, also compare your actual themes, custom drawables, large text,
RTL, animations and saved-state behavior before releasing to users. To roll back, restore
both the 1.1.0 dependency and the pre-migration application/custom-engine source together.
