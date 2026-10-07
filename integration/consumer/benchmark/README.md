# Device performance

This opt-in Macrobenchmark module runs against the minified consumer APK and the staged
Timeline AAR. It does not add dependencies to the library. The target APK is non-debuggable
and shell-profileable; the instrumentation runs in a separate process.

On a connected physical Android 10+ device, with JDK 17 and the Android SDK configured:

```bash
./gradlew :timelineview:publishReleasePublicationToBuildRepository
./gradlew -p integration/consumer :benchmark:connectedReleaseAndroidTest \
  -PtimelineBenchmarks=true \
  -PtimelineRepository="$PWD/build/repository" -PtimelineVersion=2.1.0
```

Use the version declared in `timelineview/build.gradle.kts` when it changes. Set
`ANDROID_SERIAL` when more than one device is connected. Keep device model, OS, refresh rate,
thermal state and build identical for before/after comparisons. Avoid background workloads.

The full matrix covers all six strategies, 100 and 1,000 events, and 100 events with Lottie
badges. Each case performs vertical scrolling (horizontal strategy uses wrapping), progress
updates, data replacement and a strategy switch, with five measured iterations after full
AOT compilation. FrameTimingMetric reports frame CPU time and, on API 31+, frame overruns;
MemoryUsageMetric reports maximum sampled process memory, not allocation counts or leak
detection. `timeline-progress`, `timeline-data` and `timeline-strategy` trace sections help
locate update costs in Perfetto. This measures the complete interaction sequence; use the
trace to distinguish individual operations.

JSON results and Perfetto traces are copied under
`benchmark/build/outputs/connected_android_test_additional_output/`; instrumentation reports
are under `benchmark/build/reports/androidTests/`. The CI workflow uploads both even on failure.

For a quick emulator harness check add:

```text
-Pandroid.testInstrumentationRunnerArguments.timelineSmoke=true
-Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR
```

To validate the complete matrix quickly, keep `timelineSmoke` unset and add
`-Pandroid.testInstrumentationRunnerArguments.timelineIterations=1`. One iteration checks
the harness, not statistical stability; use the default five or more for comparisons.

The smoke run uses one 100-event animated case and one measured iteration. Only the emulator
warning is suppressed; profileability/debuggability validation remains active. CI runs this
diagnostic on benchmark changes, weekly and manually. Emulator timings are not performance
budgets: collect repeated physical-device baselines before defining regression thresholds.
Ordinary API-27 migration/instrumentation checks do not include this opt-in module.

Setup follows the Android [Macrobenchmark guide](https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview).
