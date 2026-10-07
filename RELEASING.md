# Releasing Timeline

The checkout targets **2.0.0, unreleased**. Local verification never publishes. Publishing
requires explicitly dispatching **Publish tested release** for an existing stable tag.
The workflow builds/tests that tag and publishes the exact staged Maven bytes it tested.

## One-time repository setup

1. Enable GitHub Pages with **GitHub Actions** as its source. The publishing job uses the
   `github-pages` environment and Pages OIDC deployment. Add environment reviewers if your
   project needs release approval.
2. Require the **Required checks** status for merges to the protected development branch.
   Select the actual check after the first successful GitHub run. Do not require the
   conditionally skipped device/performance jobs independently on every PR.
3. Permit the release workflow's `GITHUB_TOKEN` to update the `gh-pages` branch. That branch
   is the persistent Maven/site history; do not force-push it or erase version directories.
4. Run the quality workflow remotely before the first release. Local macOS checks do not
   establish Linux screenshot equivalence or GitHub runner/emulator availability.

No personal access token is required by the prepared workflow. These repository settings
must be applied by a maintainer; changing local YAML does not change repository settings.

## Prepare a version

- Commit the intended library, tests, docs and API baselines together. Use a stable `x.y.z`
  version in `timelineview/build.gradle.kts`; the release workflow accepts `vx.y.z` tags.
- Update [CHANGELOG.md](CHANGELOG.md), the [migration guide](MIGRATION_1_TO_2.md), and the
  installation examples when the version is actually published. Keep 1.1.0 artifacts unchanged.
- Run `bash scripts/check-release.sh`. It runs lint/tests/docs, stages the AAR/sources/POM,
  compiles old and migrated consumers, exercises migration scenarios, checks JVM signatures
  and records SHA-256 hashes with the source commit in `build/repository/release-manifest.json`.
- Run the instrumented tests on a connected emulator. CI checks API 27 and 34 for releases;
  diagnostic performance runs are informational timings rather than frame-rate thresholds.

Create and push the release tag only after reviewing the change. For example, after committing
version 2.0.0: `git tag -a v2.0.0 -m "Timeline 2.0.0"`, then `git push origin v2.0.0`.
A tag push verifies the release but does not publish it. Dispatch **Publish tested release**
from a trusted branch containing this workflow and provide `v2.0.0`.

## Publication flow

The reusable quality workflow runs quality/API checks, screenshots, packaging/migration,
documentation and device tests. Its aggregate gate rejects failed/cancelled required jobs.
Only then does `publish` download the tested Maven repository and generated documentation.
It performs **no Gradle rebuild**.

The staging script checks version, commit and every artifact hash. On first publication it
seeds history from the committed `docs` directory, preserving the public 1.1.0 repository.
Later publications start from `gh-pages`. An existing version can only be replayed with exactly
the same bytes and provenance. Different bytes under the same version are rejected.

New files and refreshed Maven metadata are committed to `gh-pages` without a force push.
Pages deploys the staged site, while API documentation is stored under `api/<version>/`.
Releases are serialized and are not cancelled by a newer dispatch.

## Verification after publication

Check the workflow's Pages URL, version POM/AAR/sources and versioned API docs. Build a clean
application against the public Maven URL rather than a local repository or Gradle cache.
Confirm the old 1.1.0 files remain available. Update the public README status only after this
check; the workflow generates the public site's version banner from published metadata.

## Failure and recovery

- A validation/test failure cannot reach the publishing job. Fix it and verify a new commit.
- If Pages deployment fails after the history commit, **rerun failed jobs** so the same
  artifact is used. An identical replay is allowed. Rebuilding a modified artifact under
  the same version is not a recovery procedure.
- If an artifact has expired, inspect the saved `gh-pages` release manifest and retained
  artifacts before attempting recovery. Do not remove checksums or overwrite the version.
- For a bad published library, release a new patch version. Applications can pin the prior
  version; reverting the entire Pages site would remove releases and is not a library rollback.

## API and dependency maintenance

`./gradlew :timelineview:apiCheck` checks Kotlin-visible binary API through Kotlin's validator.
The reviewed baseline is `timelineview/api/timelineview.api`. `python3 scripts/check-api.py`
also checks the release AAR's JVM surface, including JVM-public internals. Source compatibility
is additionally exercised by Kotlin/Java migration examples; no binary API checker proves
all source or behavioral compatibility.

For an intentional API change, review compatibility, run `:timelineview:apiDump`, rebuild
`:timelineview:assembleRelease`, and run `python3 scripts/check-api.py --update`. Review both
diffs and the migration documentation before accepting baselines. CI never updates them.

The wrapper distribution has a pinned SHA-256. Gradle dependency verification records hashes
for externally resolved build/test dependencies. To update them after a deliberate dependency
change, use `--write-verification-metadata sha256` on the affected root/integration tasks,
review the resulting metadata against trusted upstream artifacts, then rerun without that flag.
Include Linux AAPT2 when refreshing hashes on macOS with
`./gradlew -I scripts/verification.init.gradle --write-verification-metadata sha256 verificationDependencies`;
run the equivalent task in `integration/consumer` with its repository/version properties.
Instrumented test tasks also resolve Android Test Platform dependencies and must be included.
SDK/emulator downloads and Robolectric's own runtime downloads are outside Gradle verification.
Do not use lenient/off verification modes in CI. The integration build trusts only the locally
staged candidate version, restricted to file repositories; the release manifest records its bytes.
Preserved 1.1.0 artifacts remain checksum-verified. Update the exact candidate version in the
consumer verification metadata when preparing the next release.

See [Gradle verification guidance](https://docs.gradle.org/8.6/userguide/dependency_verification.html)
and [Kotlin binary compatibility validator](https://github.com/Kotlin/binary-compatibility-validator/tree/0.16.3)
for the tools' scope and limitations.
