#!/usr/bin/env python3
"""Verify deployed Maven bytes and build a fresh, strictly verified public consumer."""
import argparse
import concurrent.futures
import hashlib
import importlib.util
import json
from pathlib import Path
import re
import shutil
import subprocess
import tempfile
import time
import urllib.error
import urllib.request
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
MODULE = Path('com/github/dmitrypokrasov/timelineview')
NS = '{https://schema.gradle.org/dependency-verification}'


def fetch(url):
    for attempt in range(5):
        try:
            with urllib.request.urlopen(url, timeout=30) as response:
                return response.read()
        except (urllib.error.URLError, TimeoutError):
            if attempt == 4:
                raise
            time.sleep(2 ** attempt)


def verify_site(base, site, release, commit, download=fetch):
    if not re.fullmatch(r'\d+\.\d+\.\d+', release):
        raise ValueError('Expected a stable release version')
    manifest = json.loads((site / 'releases' / f'{release}.json').read_text())
    if manifest['version'] != release or manifest['commit'] != commit:
        raise ValueError('Release provenance does not match the requested tag')
    spec = importlib.util.spec_from_file_location('release_artifact', ROOT / 'scripts/release-artifact.py')
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    module.validate(site / 'maven', manifest, release, commit)
    base = base.rstrip('/') + '/'
    if json.loads(download(base + f'releases/{release}.json')) != manifest:
        raise ValueError('Public provenance differs from tested artifacts')

    # Include every preserved version, including releases predating provenance manifests.
    files = sorted(p for p in (site / 'maven' / MODULE).glob('*/*') if p.is_file())
    def verify(path):
        relative = path.relative_to(site).as_posix()
        actual = hashlib.sha256(download(base + relative)).digest()
        if actual != hashlib.sha256(path.read_bytes()).digest():
            raise ValueError('Published artifact differs: ' + relative)
    with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool:
        list(pool.map(verify, files))
    metadata = ET.fromstring(download(base + f'maven/{MODULE}/maven-metadata.xml'))
    versions = [v.text for v in metadata.findall('versioning/versions/version')]
    expected_versions = sorted(p.name for p in (site / 'maven' / MODULE).iterdir() if p.is_dir())
    if sorted(versions) != expected_versions or metadata.findtext('versioning/release') != max(versions, key=lambda v: tuple(map(int, v.split('.')))):
        raise ValueError('Public Maven version metadata is stale or incomplete')
    if b'<html' not in download(base + f'api/{release}/index.html').lower():
        raise ValueError('Versioned API documentation is unavailable')
    print(f'Verified {len(files)} Maven files, release provenance, metadata and API documentation.', flush=True)
    return manifest


def prepare_consumer(destination, manifest):
    shutil.copytree(ROOT / 'integration', destination / 'integration', ignore=shutil.ignore_patterns('build', '.gradle', 'local.properties'))
    consumer = destination / 'integration/consumer'
    (consumer / 'settings.gradle.kts').write_text('''pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        exclusiveContent {
            forRepository { maven { url = uri(providers.gradleProperty("timelineRepository").get()) } }
            filter { includeModule("com.github.dmitrypokrasov", "timelineview") }
        }
        google()
        mavenCentral()
    }
}
rootProject.name = "TimelinePublicReleaseConsumer"
include(":legacy")
''')
    verification = consumer / 'gradle/verification-metadata.xml'
    tree = ET.parse(verification)
    trusted = tree.getroot().find(NS + 'configuration/' + NS + 'trusted-artifacts')
    if trusted is not None:
        for trust in list(trusted):
            if trust.get('group') == 'com.github.dmitrypokrasov' and trust.get('name') == 'timelineview':
                trusted.remove(trust)
    components = tree.getroot().find(NS + 'components')
    attrs = {'group': 'com.github.dmitrypokrasov', 'name': 'timelineview', 'version': manifest['version']}
    for component in list(components):
        if all(component.get(k) == v for k, v in attrs.items()):
            components.remove(component)
    component = ET.SubElement(components, NS + 'component', attrs)
    for path, digest in sorted(manifest['files'].items()):
        if path.endswith(('.aar', '.pom', '.module', '.jar')):
            artifact = ET.SubElement(component, NS + 'artifact', {'name': Path(path).name})
            ET.SubElement(artifact, NS + 'sha256', {'value': digest, 'origin': 'Tested release ' + manifest['commit']})
    ET.register_namespace('', NS[1:-1])
    tree.write(verification, encoding='utf-8', xml_declaration=True)
    return consumer


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--url', required=True)
    parser.add_argument('--site', type=Path, required=True)
    parser.add_argument('--tag', required=True)
    parser.add_argument('--artifacts-only', action='store_true', help='Skip consumer build (diagnostics only)')
    args = parser.parse_args()
    if not re.fullmatch(r'https://[A-Za-z0-9.-]+(?:/[A-Za-z0-9_-]+)*/?', args.url):
        parser.error('Expected an HTTPS Pages site URL')
    if not re.fullmatch(r'v\d+\.\d+\.\d+', args.tag):
        parser.error('Expected a stable vx.y.z tag')
    commit = subprocess.check_output(['git', 'rev-parse', args.tag + '^{commit}'], cwd=ROOT, text=True).strip()
    manifest = verify_site(args.url, args.site, args.tag[1:], commit)
    if not args.artifacts_only:
        with tempfile.TemporaryDirectory(prefix='timeline-public-') as temp:
            destination = Path(temp)
            consumer = prepare_consumer(destination, manifest)
            try:
                subprocess.run([str(ROOT / 'gradlew'), '--gradle-user-home', str(destination / 'gradle-home'),
                                '-p', str(consumer), 'assembleRelease', 'testDebugUnitTest',
                                '-PtimelineVersion=' + manifest['version'],
                                '-PtimelineRepository=' + args.url.rstrip('/') + '/maven'], check=True)
            finally:
                report = ROOT / 'build/reports/public-consumer'
                if (consumer / 'build/reports').exists():
                    shutil.copytree(consumer / 'build/reports', report, dirs_exist_ok=True)
    print('Public release verification passed.', flush=True)


if __name__ == '__main__':
    main()
