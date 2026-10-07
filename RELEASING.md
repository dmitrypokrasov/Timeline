# Releasing Timeline

The checkout targets **2.0.0**. Local verification never publishes. Publishing
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

Repository setup was verified on 2026-10-07: Pages uses **GitHub Actions** and the
`github-pages` environment permits deployments from `main`. The `dev` branch requires
pull requests, an up-to-date branch and the GitHub Actions **Required checks** gate;
force pushes and deletion are disabled, and administrators are subject to these rules.
The release workflow rejects legacy Pages configuration before modifying Maven history.

Prepare releases on `codex/release/<version>` from current `dev`, merge the release MR
into `main`, and tag that reviewed merge commit. Dispatch publication from `main` with
the release tag as input. Merge release/documentation changes back into `dev` through a
checked MR. Never retag or replace an already published version.

## Prepare a version

- Commit the intended library, tests, docs and API baselines together. Use a stable `x.y.z`
  version in `timelineview/build.gradle.kts`; the release workflow accepts `vx.y.z` tags.
- Update [CHANGELOG.md](CHANGELOG.md), the [migration guide](MIGRATION_1_TO_2.md), and the
  installation examples when the version is actually published. Keep 1.1.0 artifacts unchanged.
- Run `bash scripts/check-release.sh`. It runs lint/tests/docs, stages the AAR/sources/POM,
  compiles old and migrated consumers, builds an R8/resource-shrunk release consumer and its
  instrumentation APK, exercises migration scenarios, checks JVM signatures
  and records SHA-256 hashes with the source commit in `build/repository/release-manifest.json`.
- Run the instrumented tests on a connected emulator. CI checks API 27 and 34 for releases;
  device jobs also run the minified consumer against the staged Maven AAR. Release/tag
  verification includes diagnostic performance runs (informational timings, not frame-rate thresholds).

Create and push the release tag only after reviewing the change. For example, after committing
version 2.0.0: `git tag -a v2.0.0 -m "Timeline 2.0.0"`, then `git push origin v2.0.0`.
A tag push verifies the release but does not publish it. Dispatch **Publish tested release**
from a trusted branch containing this workflow and provide `v2.0.0`.

## Publication flow

The reusable quality workflow runs quality/API checks, screenshots, packaging/migration,
documentation, device tests and diagnostic performance. Its aggregate gate rejects failed/cancelled required jobs.
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

After deployment, the workflow runs `scripts/verify-public-release.py`. It compares the
public release manifest, all preserved Maven version files and version metadata with the
staged site, and checks versioned API docs. It then builds a copied consumer with an empty
Gradle user home, the public Maven URL as its only Timeline source and exact release hashes
in dependency verification. Candidate trust exemptions and local fallback repositories are
removed from that temporary consumer. R8/resource shrinking and migration tests must pass.

Only then does the workflow create the GitHub Release from its matching changelog section.
An existing published release is left unchanged on retry; an existing draft fails explicitly.
Consumer reports are uploaded even when the build fails. No library artifact is rebuilt or
replaced during these post-deployment steps.

For a read-only replay, run `python3 scripts/verify-public-release.py --tag v2.0.0
--url https://dmitrypokrasov.github.io/Timeline/ --site <checkout-of-gh-pages>` with the Android
SDK available. `--artifacts-only` skips the consumer and is for diagnostics, not release approval.
Update the public README status after verification; the workflow generates the public site's
version banner from published metadata.

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

External GitHub Actions are pinned to full commit SHAs, with their release tags retained
as comments. Resolve updates from the action's official repository and review its changes.
Dependabot opens weekly update PRs against `dev` for Actions, the root Gradle build and the
independent consumer. Its configuration must exist on the repository's default branch
before GitHub starts scheduling updates; bring it to `main` through the next normal release.
Updates are not automatically merged. Gradle changes still require reviewed dependency
verification metadata and green consumer/API checks; a bot PR alone does not establish
compatibility. Major toolchain upgrades should be reviewed separately from runtime libraries.

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
change, use `--refresh-dependencies --write-verification-metadata sha256` on the affected root/integration tasks,
review the resulting metadata against trusted upstream artifacts, then rerun without that flag.
Refreshing is necessary to include parent POMs and Gradle module metadata hidden by a warm
dependency cache. Validate the root and consumer builds with an empty Gradle user home before
accepting the update; never treat a cached local build as proof of clean-runner verification.
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

## Candidate rehearsal without publication

Follow [Gitflow](AGENTS.md#gitflow): ordinary fixes target `dev`; create a release branch
from `dev` only when preparing the actual release. Do not tag or publish during an audit.

1. Run `bash scripts/check-release.sh` and review the candidate manifest and consumer APKs.
2. Run the quality workflow manually for the candidate branch to include both device APIs
   and diagnostics. Require its aggregate status to succeed before approving a release.
3. For a local device run, use `./gradlew -p integration/consumer connectedReleaseAndroidTest
   -PtimelineRepository="$PWD/build/repository" -PtimelineVersion=2.0.0` after staging.
   The release consumer uses a debug signing key solely to install the test APK.
4. Confirm the repository's actual Pages source, environment rules and protected-branch
   checks using the repository settings. A successful local build cannot confirm these.
5. `check-release.sh` runs `python3 scripts/release-artifact.py rehearse`: it stages the actual
   candidate in a disposable copy of the public repository, checks identical retry and
   verifies old artifacts retain their checksums. It creates no tag and publishes nothing.

Consumer runtime tests address the minified app only through Android framework APIs;
they do not add keep-all rules that would hide missing library consumer rules.
