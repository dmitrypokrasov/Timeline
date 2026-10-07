#!/usr/bin/env python3
"""Compare the release AAR's public JVM signatures with a reviewed baseline.

This conservative check also includes JVM-public Kotlin internals. It detects
signature changes; behavioral and Kotlin metadata compatibility need review.
"""
import argparse
import difflib
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import zipfile

ROOT = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--update', action='store_true', help='write a new baseline for explicit review')
args = parser.parse_args()
aar = ROOT / 'timelineview/build/outputs/aar/timelineview-release.aar'
baseline = ROOT / 'api/timelineview.api.txt'
java_home = os.environ.get('JAVA_HOME')
javap = str(Path(java_home) / 'bin/javap') if java_home else shutil.which('javap')
if not javap or not aar.is_file():
    parser.error('A JDK and :timelineview:assembleRelease are required')
with tempfile.TemporaryDirectory(prefix='timeline-api-') as directory:
    jar = Path(directory) / 'classes.jar'
    with zipfile.ZipFile(aar) as archive:
        jar.write_bytes(archive.read('classes.jar'))
    with zipfile.ZipFile(jar) as archive:
        classes = sorted(name[:-6].replace('/', '.') for name in archive.namelist()
                         if name.endswith('.class') and name.startswith('com/dmitrypokrasov/timelineview/'))
    output = subprocess.run([javap, '-classpath', str(jar), '-public', '-constants', *classes],
                            check=True, capture_output=True, text=True).stdout
    blocks = []
    current = []
    for line in output.splitlines():
        if line.startswith('Compiled from '):
            continue
        if line.startswith('public '):
            current = [line]
        elif current:
            current.append(line)
            if line == '}':
                # Kotlin may reorder generated methods between clean and incremental builds.
                # Declaration order is not JVM API; retain every signature and constant value.
                blocks.append('\n'.join([current[0], *sorted(current[1:-1]), current[-1]]))
                current = []
    actual = '\n\n'.join(blocks) + '\n'
report = ROOT / 'build/reports/api/timelineview.api.txt'
report.parent.mkdir(parents=True, exist_ok=True)
report.write_text(actual)
if args.update:
    baseline.parent.mkdir(parents=True, exist_ok=True)
    baseline.write_text(actual)
    print(f'Updated {baseline}; review the diff before accepting.')
elif not baseline.exists() or baseline.read_text() != actual:
    expected = baseline.read_text() if baseline.exists() else ''
    print(''.join(difflib.unified_diff(expected.splitlines(True), actual.splitlines(True),
                                      fromfile=str(baseline), tofile=str(report))))
    raise SystemExit('Public JVM API changed. Review compatibility and explicitly update the baseline.')
else:
    print('Public JVM API matches the reviewed baseline.')
