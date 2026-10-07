# Changelog

## 2.0.0 — unreleased

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
