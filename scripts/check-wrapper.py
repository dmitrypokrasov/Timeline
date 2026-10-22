#!/usr/bin/env python3
"""Check that both Gradle distributions match their reviewed URL and SHA-256 pins."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
props = dict(line.split('=', 1) for line in (root / 'gradle/wrapper/gradle-wrapper.properties').read_text().splitlines() if '=' in line and not line.startswith('#'))
assert props['distributionUrl'].replace('\\:', ':') == 'https://services.gradle.org/distributions/gradle-9.8.1-bin.zip'
assert props['distributionSha256Sum'] == 'dce76f55f8e251a3a1f130eb120f30b3d271de2b76c9b0729d316b5a1b6dc01f'
print('Gradle distribution URL and SHA-256 match the reviewed pin.')

consumer = root / 'integration/consumer/gradle/wrapper/gradle-wrapper.properties'
props = dict(line.split('=', 1) for line in consumer.read_text().splitlines() if '=' in line and not line.startswith('#'))
assert props['distributionUrl'].replace('\\:', ':') == 'https://services.gradle.org/distributions/gradle-9.8.1-bin.zip'
assert props['distributionSha256Sum'] == 'dce76f55f8e251a3a1f130eb120f30b3d271de2b76c9b0729d316b5a1b6dc01f'
print('Consumer Gradle distribution URL and SHA-256 match the reviewed pin.')
