#!/usr/bin/env python3
"""Check that the wrapper distribution is pinned to the reviewed Gradle 8.7 binary."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
props = dict(line.split('=', 1) for line in (root / 'gradle/wrapper/gradle-wrapper.properties').read_text().splitlines() if '=' in line and not line.startswith('#'))
assert props['distributionUrl'].replace('\\:', ':') == 'https://services.gradle.org/distributions/gradle-8.7-bin.zip'
assert props['distributionSha256Sum'] == '544c35d6bd849ae8a5ed0bcea39ba677dc40f49df7d1835561582da2009b961d'
print('Gradle distribution URL and SHA-256 match the reviewed pin.')
