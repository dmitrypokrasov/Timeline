import importlib.util
import json
from pathlib import Path
import tempfile
import unittest
import xml.etree.ElementTree as ET


def load(name):
    spec = importlib.util.spec_from_file_location(name.replace('-', '_'), Path(__file__).resolve().parents[1] / (name + '.py'))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


public = load('verify-public-release')
release = load('release-artifact')
notes = load('release-notes')


class PublicReleaseTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.repo = self.root / 'repo'
        self.site = self.root / 'site'
        folder = self.repo / release.MODULE / '2.0.0'
        folder.mkdir(parents=True)
        for suffix in ['.aar', '.pom', '-sources.jar']:
            (folder / ('timelineview-2.0.0' + suffix)).write_bytes(b'verified')
        self.manifest = dict(version='2.0.0', commit='abc', files=release.inventory(self.repo, '2.0.0'))
        release.stage(self.repo, self.site, self.manifest, '2.0.0', 'abc')
        api = self.site / 'api/2.0.0/index.html'
        api.parent.mkdir(parents=True)
        api.write_text('<html>API</html>')

    def download(self, url):
        return (self.site / url.removeprefix('https://example.test/')).read_bytes()

    def test_deployed_bytes_and_provenance_are_checked(self):
        result = public.verify_site('https://example.test', self.site, '2.0.0', 'abc', self.download)
        self.assertEqual(result, self.manifest)
        def tampered(url):
            return b'tampered' if url.endswith('.aar') else self.download(url)
        with self.assertRaisesRegex(ValueError, 'artifact differs'):
            public.verify_site('https://example.test', self.site, '2.0.0', 'abc', tampered)
        with self.assertRaisesRegex(ValueError, 'provenance'):
            public.verify_site('https://example.test', self.site, '2.0.0', 'wrong', self.download)

    def test_old_versions_and_public_manifest_cannot_drift(self):
        old = self.site / 'maven' / release.MODULE / '1.1.0/old.aar'
        old.parent.mkdir(parents=True)
        old.write_bytes(b'old')
        def altered(url):
            return b'changed' if '/1.1.0/' in url else self.download(url)
        with self.assertRaisesRegex(ValueError, 'artifact differs'):
            public.verify_site('https://example.test', self.site, '2.0.0', 'abc', altered)
        def wrong_manifest(url):
            return json.dumps(dict(self.manifest, commit='different')).encode() if url.endswith('.json') else self.download(url)
        with self.assertRaisesRegex(ValueError, 'provenance'):
            public.verify_site('https://example.test', self.site, '2.0.0', 'abc', wrong_manifest)

    def test_public_consumer_has_exact_hashes_and_no_candidate_trust_or_local_fallback(self):
        consumer = public.prepare_consumer(self.root / 'consumer', self.manifest)
        tree = ET.parse(consumer / 'gradle/verification-metadata.xml')
        trusts = tree.findall('.//' + public.NS + 'trust')
        self.assertFalse(any(t.get('group') == 'com.github.dmitrypokrasov' for t in trusts))
        components = tree.findall('.//' + public.NS + 'component')
        component = next(c for c in components if c.get('group') == 'com.github.dmitrypokrasov' and c.get('version') == '2.0.0')
        self.assertEqual(len(component), 3)
        self.assertEqual({a.find(public.NS + 'sha256').get('value') for a in component}, set(self.manifest['files'].values()))
        self.assertNotIn('docs/maven', (consumer / 'settings.gradle.kts').read_text())

    def test_release_notes_select_exact_version_and_fail_when_missing(self):
        changelog = '# Changes\n\n## 2.0.0 — today\n\nNew features\n\n## 1.1.0\n\nOld features\n'
        result = notes.notes(changelog, 'v2.0.0', 'https://example.test/')
        self.assertIn('New features', result)
        self.assertNotIn('Old features', result)
        with self.assertRaises(ValueError):
            notes.notes(changelog, 'v2.0.1', 'https://example.test')
