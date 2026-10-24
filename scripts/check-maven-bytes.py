#!/usr/bin/env python3
"""Verify published Maven files against Git blobs, without decoding or network access."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]


def check(root=ROOT):
    def git(*args):
        return subprocess.run(['git', '-C', str(root), *args], check=True, capture_output=True).stdout
    names = git('ls-tree', '-r', '--name-only', '-z', 'HEAD', '--', 'docs/maven').decode('utf-8').split('\0')
    paths = [name for name in names if name]
    if not paths:
        raise ValueError('No published Maven files in HEAD')
    for name in paths:
        path = root / name
        if not path.is_file() or path.read_bytes() != git('cat-file', 'blob', f'HEAD:{name}'):
            raise ValueError(f'Published Maven bytes differ from Git blob: {name}')
    return len(paths)


if __name__ == '__main__':
    print(f'{check()} published Maven files match Git blobs byte for byte.')
