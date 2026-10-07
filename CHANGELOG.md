# Changelog

## 2.1.0 — 2026-10-07

### Additions

- Add `GroupedTimelineView` and `TimelineSection` for bounded histories grouped by dates or
  stages, with accessible headings, stable section identity and section-aware click callbacks.
  The host controls grouping, ordering, date formatting and persistence.
- Add `TimelineDefaults.config/dp/sp` for density-aware programmatic configuration and
  `@MainThread` contracts for View, math and renderer integrations.
- Add the optional `TimelineTextBoundary` capability for custom layouts to keep tall labels
  below connecting paths. Existing custom engines do not need to implement it.
- Provide a compiled Compose `AndroidView` adapter example with immutable state, refreshed
  callbacks and lifecycle cleanup. Compose remains a consumer-only dependency.

### Fixes

- Pause Lottie overlays outside the visible viewport, including fully clipped views whose
  parent skips drawing, and resume playback on re-entry without retaining lifecycle listeners.
- Keep large Snake titles clear of horizontal connectors at increased system font scales.
- Clear keyboard/accessibility focus when the focused event is removed while preserving it
  when a stable-ID event moves.

### Verification and delivery

- Extend path/progress invariants across all six strategies and both progress modes; add
  large-font, mixed-script, RTL and dark-theme rendering regressions.
- Add Android frame/memory benchmarks and Perfetto traces for 100/1,000 events, updates and
  Lottie, with an emulator harness job and physical-device comparison instructions.
- Pin CI Actions by commit, add Dependabot updates targeting `dev`, and verify public Maven
  bytes plus a fresh-cache consumer before announcing a GitHub Release.

### Upgrading from 2.0.0

This is an additive minor release: no existing public API is removed. Update the dependency
version; no configuration migration is required. Programmatic dimensions remain pixels;
use the new conversion helpers at the host boundary. Keep using individual `TimelineView`s
for custom registries and recycling containers for unbounded event feeds.

## 2.0.0 — 2026-10-07

### Migration from 1.1.0

- Configuration properties are `val`. Replace direct mutation with `copy`, then call
  `TimelineView.setConfig(math, ui)` to invalidate geometry and renderer resources.
- Invalid negative/non-finite dimensions now fail at construction instead of silently
  clamping to zero. Zero icon dimensions intentionally hide the drawable.
- Built-in engines snapshot step lists. Update data through `replaceSteps`.
- Snake paths and progress use the same measured rounded path. Progress is the fraction
  of the incoming segment ending at a badge, including its turns.
- Horizontal steps fit into equal cells within the available width. Vertical and snake
  `stepY` is the minimum row distance and expands to fit content.
- New XML defaults use density-aware dp/sp; Kotlin configuration values remain pixels.
- The release is deliberately a major version because configuration setters are removed
  and geometry changes. Existing 1.1.0 Maven artifacts remain unchanged.

### Strategies and verification

- Make strategy/registry transitions transactional and represent direct/key selections explicitly.
- Commit a measured frame shared by drawing, hit testing and accessibility; retain it on failed measurement.
- Unify built-in factories and registration in one catalog; introduce a layout-first adapter and
  deprecate legacy coordinate accessors without removing them.
- Compile migration examples against both published 1.1.0 and staged 2.0 artifacts, including
  Kotlin, Java, XML and behavioral tests. Add dedicated architecture, extension and release guides.
- Add Kotlin-aware API validation, dependency verification, a wrapper checksum, documentation PR
  checks, a required status gate and manual publication of tested artifacts with immutable history.

- Add alternating central-axis, adaptive serpentine grid and time-scaled event strategies.
- Add horizontal FIT/WRAP/SCROLL policies, minimum cell width and content-row capabilities.
- Center vertical linear badges on the path and keep labels clear of the axis for all start positions.
  `marginHorizontalImage` now applies to Snake placement; linear badges follow their path.
- Measure rounded path progress by arc length and split that same geometry into colored paths.
- Add explicit sequential/independent progress modes and unique optional stable step IDs.
- Preserve explicitly installed engines/renderers when replacing the strategy registry.
- Preserve styled text spans; bound bitmap caching and skip offscreen step drawing.
- Add seeded geometry properties, identity/reordering regression tests, 24 screenshot
  baselines, a reviewed JVM API baseline, device gesture/accessibility tests and diagnostic
  timings for 10/100/1000 steps. CI separates quality, images, packaging, docs, devices and timings.

### Fixes

- Give XML and Kotlin configurations shared visible defaults; remove the direct Material
  dependency used only for default colors. Explicit color configuration is unchanged.
- Share path progress splitting with linear strategies so an unfinished zero-length segment
  still stops sequential completion. Cache the resulting path geometry.
- Clear scaled badge animations when placing horizontal/grid labels, including wrapped rows.
- Ship a narrowly scoped consumer rule for Okio’s optional `javax.annotation.Nullable`
  metadata so R8 consumers build without suppressing other missing-class diagnostics.
- Remove obsolete text-paint state while retaining the renderer compatibility methods.
- Render the migration example in tests, build a minified/shrunk consumer and exercise it on
  devices. Include diagnostic performance in release/tag gates and document all XML inputs.

- Measure badges, descriptions, progress, paths and overlay bounds together before drawing.
- Preserve badge/text alignment for long descriptions; avoid horizontal text-cell overlap.
- Bound and reuse text layouts across measurement and animation frames.
- Render arbitrary static Android drawables, skip zero-sized bitmaps and clear removed icons.
- Load Lottie asynchronously, separate drawable instances per occurrence, evict removed
  overlays, and pause playback when hidden. Loading failures no longer crash rendering.
- Match taps to the original target, reject drags/cancelled gestures and respect disabled state.
- Expose accessible steps/actions and mirror geometry, hit testing and accessibility in RTL.
- Add regression tests, native-rendering preview artifacts, CI and an independent AAR consumer.

## 1.1.0 — 2026-03-10

Initial GitHub Pages Maven distribution with strategy-based timelines, multiline text,
click handling and local Lottie overlays.
