#!/usr/bin/env bash
set -euo pipefail
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT"
if [[ -z "${ANDROID_HOME:-}" && -f local.properties ]]; then
  SDK_DIRECTORY="$(sed -n 's/^sdk.dir=//p' local.properties)"
  if [[ -n "$SDK_DIRECTORY" ]]; then export ANDROID_HOME="$SDK_DIRECTORY"; fi
fi
VERSION="$(sed -n 's/^version = "\([^"]*\)"/\1/p' timelineview/build.gradle.kts)"
if [[ -n "${TIMELINE_RELEASE_TAG:-}" && "$TIMELINE_RELEASE_TAG" != "v$VERSION" ]]; then
  echo "Requested release tag does not match library version $VERSION" >&2
  exit 1
fi
if [[ "${GITHUB_REF:-}" == refs/tags/* && "${GITHUB_REF#refs/tags/}" != "v$VERSION" ]]; then
  echo "Tag does not match library version $VERSION" >&2
  exit 1
fi
if [[ "${1:-}" != "--package-only" ]]; then
  python3 scripts/check-wrapper.py
  python3 -m unittest discover -s scripts/tests -v
  ./gradlew qualityCheck qualityDocs --continue --stacktrace
fi
./gradlew :app:assembleDebug :timelineview:assembleRelease \
  :timelineview:publishReleasePublicationToBuildRepository --stacktrace
./gradlew -p integration/consumer assembleDebug assembleRelease assembleReleaseAndroidTest testDebugUnitTest \
  -PtimelineRepository="$PROJECT_ROOT/build/repository" -PtimelineVersion="$VERSION" --stacktrace

python3 scripts/check-api.py
python3 scripts/check-docs.py
python3 scripts/release-artifact.py record
python3 scripts/release-artifact.py rehearse
