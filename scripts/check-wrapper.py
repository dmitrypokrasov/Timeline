#!/usr/bin/env python3
"""Check that both Gradle distributions match their reviewed URL and SHA-256 pins."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
props = dict(line.split('=', 1) for line in (root / 'gradle/wrapper/gradle-wrapper.properties').read_text().splitlines() if '=' in line and not line.startswith('#'))
assert props['distributionUrl'].replace('\\:', ':') == 'https://services.gradle.org/distributions/gradle-8.6-bin.zip'
assert props['distributionSha256Sum'] == '9631d53cf3e74bfa726893aee1f8994fee4e060c401335946dba2156f440f24c'
print('Gradle distribution URL and SHA-256 match the reviewed pin.')

consumer = root / 'integration/consumer/gradle/wrapper/gradle-wrapper.properties'
props = dict(line.split('=', 1) for line in consumer.read_text().splitlines() if '=' in line and not line.startswith('#'))
assert props['distributionUrl'].replace('\\:', ':') == 'https://services.gradle.org/distributions/gradle-9.6.0-bin.zip'
assert props['distributionSha256Sum'] == 'bbaeb2fef8710818cf0e261201dab964c572f92b942812df0c3620d62a529a01'
print('Consumer Gradle distribution URL and SHA-256 match the reviewed pin.')
