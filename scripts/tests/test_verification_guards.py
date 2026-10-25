import importlib.util
from pathlib import Path
import subprocess
import tempfile
import unittest


def load(name):
    spec = importlib.util.spec_from_file_location(name, Path(__file__).resolve().parents[1] / f'{name}.py')
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


maven = load('check-maven-bytes')
contract = load('check-consumer-contract')


class VerificationGuardsTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)

    def git(self, *args):
        return subprocess.run(['git', '-C', str(self.root), *args], check=True, capture_output=True)

    def test_maven_guard_uses_blobs_and_detects_eol_conversion_and_missing_files(self):
        self.git('init')
        self.git('config', 'core.autocrlf', 'true')
        self.git('config', 'user.email', 'test@example.invalid')
        self.git('config', 'user.name', 'Test')
        (self.root / '.gitattributes').write_text('docs/maven/** -text\n')
        artifact = self.root / 'docs/maven/test.pom'
        artifact.parent.mkdir(parents=True)
        artifact.write_bytes(b'<project>\n</project>\n')
        self.git('add', '.')
        self.git('commit', '-m', 'fixture')
        artifact.unlink()
        self.git('checkout', '--', 'docs/maven/test.pom')
        self.assertEqual(1, maven.check(self.root))
        artifact.write_bytes(b'<project>\r\n</project>\r\n')
        with self.assertRaisesRegex(ValueError, 'test.pom'):
            maven.check(self.root)
        artifact.unlink()
        with self.assertRaises(ValueError):
            maven.check(self.root)

    def test_contract_guard_rejects_missing_empty_skipped_and_failed_suites(self):
        path = self.root / 'result.xml'
        with self.assertRaises(FileNotFoundError):
            contract.check(path)
        fixtures = [
            '<testsuite name="wrong" tests="1"><testcase/></testsuite>',
            f'<testsuite name="{contract.SUITE}" tests="0"/>',
            f'<testsuite name="{contract.SUITE}" tests="1"><testcase><skipped/></testcase></testsuite>',
            f'<testsuite name="{contract.SUITE}" tests="1"><testcase><failure/></testcase></testsuite>',
            f'<testsuite name="{contract.SUITE}" tests="2"><testcase/></testsuite>',
            f'<testsuite name="{contract.SUITE}" tests="1" errors="1"><testcase/></testsuite>',
        ]
        for content in fixtures:
            path.write_text(content)
            with self.assertRaises(ValueError):
                contract.check(path)
        path.write_text(f'<testsuite name="{contract.SUITE}" tests="2" skipped="1"><testcase/><testcase><skipped/></testcase></testsuite>')
        self.assertEqual(1, contract.check(path))


if __name__ == '__main__':
    unittest.main()
