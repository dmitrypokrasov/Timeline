#!/usr/bin/env python3
"""Record/check the tested Maven artifacts and stage an append-only Pages repository."""
import argparse
from datetime import datetime, timezone
import hashlib
import json
from pathlib import Path
import re
import shutil
import subprocess
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
MODULE = Path('com/github/dmitrypokrasov/timelineview')

def version():
    match = re.search(r'^version = "(\d+\.\d+\.\d+)"$', (ROOT / 'timelineview/build.gradle.kts').read_text(), re.M)
    if not match:
        raise ValueError('Expected a stable x.y.z release version')
    return match.group(1)

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def inventory(repository, release):
    folder = repository / MODULE / release
    if not folder.is_dir():
        raise ValueError('Missing Maven version directory')
    files = {}
    for path in sorted(folder.rglob('*')):
        if path.is_symlink():
            raise ValueError('Symlinks are not release artifacts')
        if path.is_file():
            files[str(path.relative_to(repository))] = digest(path)
    for suffix in ['.aar', '.pom', '-sources.jar']:
        if str(MODULE / release / f'timelineview-{release}{suffix}') not in files:
            raise ValueError(f'Missing required artifact {suffix}')
    return files

def validate(repository, manifest, release, commit):
    if manifest['version'] != release or manifest['commit'] != commit:
        raise ValueError('Artifact version/commit differs from the checked-out release tag')
    if inventory(repository, release) != manifest['files']:
        raise ValueError('Tested artifact hashes do not match the downloaded repository')

def stage(repository, site, manifest, release, commit):
    validate(repository, manifest, release, commit)
    source = repository / MODULE / release
    destination = site / 'maven' / MODULE / release
    if destination.exists():
        existing = inventory(site / 'maven', release)
        if existing != manifest['files']:
            raise ValueError('Refusing to overwrite an existing Maven version with different bytes')
    else:
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copytree(source, destination)
    audit = site / 'releases' / f'{release}.json'
    audit.parent.mkdir(parents=True, exist_ok=True)
    if audit.exists() and json.loads(audit.read_text()) != manifest:
        raise ValueError('Release provenance differs from the existing version')
    audit.write_text(json.dumps(manifest, indent=2, sort_keys=True) + '\n')
    artifact_root = site / 'maven' / MODULE
    versions = sorted((p.name for p in artifact_root.iterdir() if p.is_dir() and re.fullmatch(r'\d+\.\d+\.\d+', p.name)), key=lambda v: tuple(map(int, v.split('.'))))
    metadata = ET.Element('metadata')
    ET.SubElement(metadata, 'groupId').text = 'com.github.dmitrypokrasov'
    ET.SubElement(metadata, 'artifactId').text = 'timelineview'
    info = ET.SubElement(metadata, 'versioning')
    for field in ['latest', 'release']:
        ET.SubElement(info, field).text = versions[-1]
    entries = ET.SubElement(info, 'versions')
    for item in versions:
        ET.SubElement(entries, 'version').text = item
    ET.SubElement(info, 'lastUpdated').text = datetime.now(timezone.utc).strftime('%Y%m%d%H%M%S')
    metadata_path = artifact_root / 'maven-metadata.xml'
    ET.ElementTree(metadata).write(metadata_path, encoding='utf-8', xml_declaration=True)
    for algorithm in ['md5', 'sha1', 'sha256', 'sha512']:
        metadata_path.with_name(metadata_path.name + '.' + algorithm).write_text(hashlib.new(algorithm, metadata_path.read_bytes()).hexdigest())
    (site / '.nojekyll').touch()
    (site / 'index.html').write_text(f'<!doctype html><html lang="en"><meta charset="utf-8"><title>Timeline {versions[-1]}</title><h1>Timeline {versions[-1]}</h1><p>Android timeline library. Immutable Maven releases.</p><pre>implementation("com.github.dmitrypokrasov:timelineview:{versions[-1]}")</pre><p><a href="api/{versions[-1]}/index.html">API documentation</a></p><p><a href="https://github.com/dmitrypokrasov/Timeline/blob/v{release}/MIGRATION_1_TO_2.md">Migration guide</a></p></html>\n')

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('operation', choices=['record', 'stage'])
    parser.add_argument('--repository', type=Path, default=ROOT / 'build/repository')
    parser.add_argument('--site', type=Path, default=ROOT / 'build/site')
    parser.add_argument('--tag')
    args = parser.parse_args()
    release = version()
    commit = subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=ROOT, text=True).strip()
    manifest_path = args.repository / 'release-manifest.json'
    if args.operation == 'record':
        manifest = {'version': release, 'commit': commit, 'files': inventory(args.repository, release)}
        manifest_path.write_text(json.dumps(manifest, indent=2, sort_keys=True) + '\n')
        print('Recorded hashes for tested release artifacts.')
    else:
        if args.tag != f'v{release}':
            raise ValueError('Release tag does not match the project version')
        if subprocess.check_output(['git', 'rev-parse', args.tag + '^{commit}'], cwd=ROOT, text=True).strip() != commit:
            raise ValueError('Checkout is not the requested release tag')
        stage(args.repository, args.site, json.loads(manifest_path.read_text()), release, commit)
        print('Staged tested artifacts without overwriting previous versions.')

if __name__ == '__main__':
    main()
