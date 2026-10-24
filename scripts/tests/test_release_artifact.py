import importlib.util
import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

SCRIPT = Path(__file__).resolve().parents[1] / 'release-artifact.py'
spec = importlib.util.spec_from_file_location('release_artifact', SCRIPT)
release = importlib.util.module_from_spec(spec)
spec.loader.exec_module(release)

class ReleaseArtifactTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.repo = self.root / 'repository'
        self.site = self.root / 'site'
        self.folder = self.repo / release.MODULE / '2.0.0'
        self.folder.mkdir(parents=True)
        for suffix in ['.aar', '.pom', '-sources.jar']:
            (self.folder / ('timelineview-2.0.0' + suffix)).write_bytes(b'tested artifact')
        self.manifest = dict(version='2.0.0', commit='tested-commit', files=release.inventory(self.repo, '2.0.0'))

    def stage(self):
        release.stage(self.repo, self.site, self.manifest, '2.0.0', 'tested-commit')

    def test_previous_versions_survive_and_identical_retry_is_allowed(self):
        old = self.site / 'maven' / release.MODULE / '1.1.0' / 'old.aar'
        old.parent.mkdir(parents=True)
        old.write_bytes(b'unchanged public release')
        self.stage()
        self.stage()
        self.assertEqual(old.read_bytes(), b'unchanged public release')
        self.assertEqual(json.loads((self.site / 'releases/2.0.0.json').read_text()), self.manifest)
        self.assertIn('1.1.0', (self.site / 'maven' / release.MODULE / 'maven-metadata.xml').read_text())

    def test_tampered_download_and_wrong_commit_are_rejected(self):
        with self.assertRaises(ValueError):
            release.validate(self.repo, self.manifest, '2.0.0', 'another-commit')
        (self.folder / 'timelineview-2.0.0.aar').write_bytes(b'changed')
        with self.assertRaises(ValueError):
            self.stage()
        self.assertFalse((self.site / 'maven').exists())

    def test_existing_version_cannot_be_overwritten(self):
        self.stage()
        target = self.site / 'maven' / release.MODULE / '2.0.0/timelineview-2.0.0.aar'
        target.write_bytes(b'already published different content')
        with self.assertRaises(ValueError):
            self.stage()
        self.assertEqual(target.read_bytes(), b'already published different content')

    def test_symlinks_and_missing_required_files_are_rejected(self):
        (self.folder / 'timelineview-2.0.0.aar').unlink()
        with self.assertRaises(ValueError):
            release.inventory(self.repo, '2.0.0')
        artifact = self.folder / 'timelineview-2.0.0.aar'
        try:
            artifact.symlink_to(self.folder / 'timelineview-2.0.0.pom')
        except OSError as error:
            if getattr(error, 'winerror', None) != 1314:
                raise
            # Windows without Developer Mode cannot create symlinks. Exercise the
            # same rejection branch with a controlled filesystem predicate; CI
            # on Linux still verifies a real symlink. No assertion is skipped.
            artifact.write_bytes(b'linked artifact')
            with patch.object(Path, 'is_symlink', lambda path: path == artifact):
                with self.assertRaisesRegex(ValueError, 'Symlinks'):
                    release.inventory(self.repo, '2.0.0')
        else:
            with self.assertRaisesRegex(ValueError, 'Symlinks'):
                release.inventory(self.repo, '2.0.0')
