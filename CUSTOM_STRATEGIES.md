# Custom strategies in 2.0

Use `TimelineMathAdapter` when the host should install a layout-first engine. It supplies
configuration storage, width and compatibility coordinate methods. Implement `buildLayout`
and `buildPath`; clear any caches in `onGeometryChanged`.

The following complete example is compiled and exercised against the staged AAR. It renders
a badge/text column without connector lines or an active marker.

<!-- source: integration/migration/after/CompactCustomMath.kt -->
```kotlin
package com.example.migration

import android.graphics.Paint
import android.graphics.Path
import com.dmitrypokrasov.timelineview.config.TimelineMathConfig
import com.dmitrypokrasov.timelineview.math.TimelineMathAdapter
import com.dmitrypokrasov.timelineview.math.data.TimelineLayout
import com.dmitrypokrasov.timelineview.math.data.TimelineLayoutStep

/** A minimal label-and-badge column. Paths are intentionally absent in this style. */
class CompactCustomMath(config: TimelineMathConfig) : TimelineMathAdapter(config) {
    private var offsets = emptyList<Float>()
    override fun getContentRows(): List<Int> = input.steps.indices.toList()
    override fun onGeometryChanged() { offsets = emptyList() }
    override fun setStepExtents(extents: List<Float>) {
        require(extents.all { it.isFinite() && it >= 0f })
        offsets = extents.runningFold(0f) { sum, extent -> sum + extent }
    }
    override fun buildPath(pathEnable: Path, pathDisable: Path) { pathEnable.reset(); pathDisable.reset() }
    override fun buildLayout(): TimelineLayout {
        val size = input.sizes.sizeImageLvl
        val x = size + 8f
        val available = (width - x).toInt().coerceAtLeast(1)
        return TimelineLayout(input.steps.mapIndexed { index, step ->
            val y = offsets.getOrNull(index) ?: index * input.spacing.stepY
            TimelineLayoutStep(step, x, y + 18f, available, x, y + 38f, available, 0f, y, Paint.Align.LEFT)
        }, null, null)
    }
}
```

Install an instance with `timelineView.setMathEngine(CompactCustomMath(config))`. For key-based
selection, register a `TimelineMathProvider` in a local registry; its `create(config)` returns
a fresh instance. The [README registry example](README.md#custom-registries) shows registration.

## Coordinates and measurement

- Coordinates are local to `getStartPosition()`; the adapter uses an origin of zero.
- Icon coordinates are top-left positions. Title/description Y values are requested baselines;
  the text resolver computes actual block tops and keeps descriptions below titles.
- Widths are positive integer pixels. An engine must handle zero available width and no steps.
- `getContentRows` returns one row number per step, beginning at zero and staying contiguous.
  Cells in a row share its number. `setStepExtents` receives distances between row origins;
  an empty list resets custom expansion. Implement both to opt in.
- `textBelowBadge` requests vertical clearance beneath the badge.
- Engines with a connector above their labels can additionally implement `TimelineTextBoundary`.
  Return the local Y boundary per step; the text resolver adds stroke clearance and uses
  actual font metrics, so large system fonts cannot cross that connector. Existing engines
  do not need to implement this optional capability.
- Paths must be reset before rebuilding. Return `hasRoundedGeometry = true` only if the
  actual path already includes curves; accept the radius via `setCornerRadius` if applicable.

## State and compatibility

A direct instance survives registry replacement. An explicit `setStrategy` switches back to
registry/factory selection. Validate configurations before mutating and make initialization
repeatable so a failed transition can restore the previous pair.

Legacy coordinate methods remain on `TimelineMathEngine`, but callers should consume the
complete layout. `TimelineLayoutEngine` documents the core contract; the adapter bridges it
to APIs currently accepting `TimelineMathEngine`.

Test at least empty/single lists, narrow widths, long labels, RTL, 0/100% progress and repeated
configuration changes. Use the [project geometry tests](timelineview/src/test/java/com/dmitrypokrasov/timelineview/ui/TimelineStrategyPropertiesTest.kt)
as examples of invariants, and include the strategy in your host's screenshot suite.
The [shared path contract suite](timelineview/src/test/java/com/dmitrypokrasov/timelineview/ui/TimelinePathContractTest.kt)
checks all built-ins at zero/narrow widths in both progress modes: completed plus remaining
arc length must equal the full path, and the active marker must meet the drawn color boundary.
Reuse those checks when adding a custom engine; they iterate every path contour, including
independently colored segments. Time-scaled tests also cover equal/pre-epoch timestamps and
atomic rejection of unsorted or unrepresentable ranges.


## Pure geometry strategies (next release)

`TimelineGeometryStrategy` receives `TimelineGeometryInput` and returns an immutable
`TimelineGeometryResult`: layout, completed/pending `Move`/`Line`/`Quad` commands, content
bounds and horizontal origin. These models do not import Android rendering types.
`TimelineRoundedPath.commands` provides the same corner construction used by built-ins;
Android arc-length sampling stays in the existing path adapter, preserving progress behavior.

Wrap your strategy in `TimelineGeometryMathAdapter(config, strategy)`. The first calculation
receives empty `textMetrics` to determine positive text widths. The next receives measured
heights in layout order. Keep step order and widths unchanged between these passes; move rows
or enlarge bounds to fit text. Bounds must include any extra content drawn by your renderer.
All coordinates and sizes are physical pixels; Y values for labels are requested baselines,
icons use top-left coordinates. `TimelineTextAlignment` uses physical LEFT/CENTER/RIGHT;
TimelineView mirrors the complete frame for RTL. Padding and `startPosition` are applied once
by View. Pure strategies must handle empty lists and zero width and return finite coordinates.

Use local registries and lambda overloads:

```kotlin
val registry = TimelineStrategyRegistry.createLocalRegistry().apply {
    registerMath(StrategyKey("app.geometry")) { TimelineGeometryMathAdapter(it, CustomGeometry()) }
    registerUi(StrategyKey("app.renderer")) { CustomRenderer(it) }
}
timelineView.setStrategyRegistry(registry, strict = true)
```

The optional strict mode reports an unknown key. Without it, existing built-in fallback is
preserved. Duplicate keys remain errors. Providers create an engine/renderer per View;
providers must not share mutable drawing tools. A failed resolution or initialization leaves
the previous working state installed. Direct instances remain supported. Calculations and
Android callbacks run on the main thread; pure strategies own no Canvas, Context or resources.
`BaseTimelineUi` manages its own bitmap and Lottie resources through the existing lifecycle.

The independent [consumer example](integration/consumer/src/main/java/com/example/consumer/CustomGeometry.kt)
implements both contracts outside the library and runs after R8 against the staged Maven AAR.
Switch back with `setStrategy(TimelineMathStrategy.LinearVertical, TimelineUiStrategy.Linear)`.
For reusable JUnit checks add `testImplementation(testFixtures("com.github.dmitrypokrasov:timelineview:<next-version>"))`
and subclass `com.dmitrypokrasov.timelineview.testing.TimelineGeometryContractTest` with your
strategy. The suite checks repeatability, empty/narrow/populated inputs, text passes and finite
coordinates. Add your own progress semantics, path continuity and screenshot assertions.
