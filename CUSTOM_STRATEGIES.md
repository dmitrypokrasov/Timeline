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
