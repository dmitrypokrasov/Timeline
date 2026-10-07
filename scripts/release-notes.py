#!/usr/bin/env python3
"""Extract the requested changelog section without inventing release notes."""
import argparse
from pathlib import Path
import re


def notes(changelog, tag, url):
    if not re.fullmatch(r'v\d+\.\d+\.\d+', tag):
        raise ValueError('Expected a stable release tag')
    match = re.search(r'^## ' + re.escape(tag[1:]) + r'(?:\s+[^\n]*)?\n(.*?)(?=^## |\Z)', changelog, re.M | re.S)
    if not match:
        raise ValueError('Missing changelog section for ' + tag)
    base = url.rstrip('/')
    return (f'Install from [{base}/maven]({base}/maven):\n\n'
            f'```kotlin\nimplementation("com.github.dmitrypokrasov:timelineview:{tag[1:]}")\n```\n\n'
            f'[API documentation]({base}/api/{tag[1:]}/) · '
            '[Migration guide](https://github.com/dmitrypokrasov/Timeline/blob/main/MIGRATION_1_TO_2.md)\n\n'
            + match.group(1).strip() + '\n\n'
            'Public Maven hashes and a clean minified consumer build were verified before publishing this release.\n')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--tag', required=True)
    parser.add_argument('--url', required=True)
    args = parser.parse_args()
    print(notes((Path(__file__).resolve().parents[1] / 'CHANGELOG.md').read_text(), args.tag, args.url))
