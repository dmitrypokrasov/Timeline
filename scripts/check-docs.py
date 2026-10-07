#!/usr/bin/env python3
"""Validate local Markdown links, heading anchors, and compiled source inclusions."""
from pathlib import Path
import re
from urllib.parse import unquote, urlsplit

ROOT = Path(__file__).resolve().parents[1]
DOCS = [ROOT / name for name in ['README.md', 'CHANGELOG.md', 'ARCHITECTURE.md', 'MIGRATION_1_TO_2.md', 'CUSTOM_STRATEGIES.md', 'RELEASING.md', 'AGENTS.md']]

def headings(path):
    result = set()
    for title in re.findall(r'^#{1,6}\s+(.+)$', path.read_text(), re.M):
        result.add(re.sub(r'[^\w\- ]', '', title.lower().replace('`', '')).replace(' ', '-'))
    return result

def check():
    errors = []
    for path in DOCS:
        if not path.is_file():
            errors.append(f'Missing {path.name}')
            continue
        text = path.read_text()
        without_code = re.sub(r'```.*?```', '', text, flags=re.S)
        for link in re.findall(r'\[[^\]]*\]\(([^)]+)\)', without_code):
            link = link.strip().strip('<>')
            parsed = urlsplit(link)
            if parsed.scheme or parsed.netloc:
                continue  # Remote availability is not a deterministic PR gate.
            target = (path.parent / unquote(parsed.path)).resolve() if parsed.path else path
            if not target.exists():
                errors.append(f'{path.name}: missing link target {link}')
            elif parsed.fragment and target.suffix == '.md' and unquote(parsed.fragment) not in headings(target):
                errors.append(f'{path.name}: missing heading {link}')
        for source, content in re.findall(r'<!-- source: (.*?) -->\s*```\w+\n(.*?)```', text, re.S):
            source_path = ROOT / source
            if not source_path.is_file() or source_path.read_text().strip() != content.strip():
                errors.append(f'{path.name}: source inclusion differs from compiled {source}')
    if errors:
        raise SystemExit('\n'.join(errors))
    print(f'{len(DOCS)} documents: local links, anchors and source inclusions passed.')

if __name__ == '__main__':
    check()
