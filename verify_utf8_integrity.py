from __future__ import annotations

import sys
from pathlib import Path


SUSPECT_TOKENS = [
    "Ã",
    "Æ",
    "á»",
    "áº",
    "Ä‘",
    "Äƒ",
    "Ä",
    "�",
]

DEFAULT_EXTENSIONS = {".kt", ".java", ".xml", ".md", ".properties"}


def iter_files(root: Path):
    for path in root.rglob("*"):
        if path.is_file() and path.suffix.lower() in DEFAULT_EXTENSIONS:
            yield path


def find_suspect_lines(path: Path):
    text = path.read_text(encoding="utf-8", errors="strict")
    findings = []
    for line_no, line in enumerate(text.splitlines(), start=1):
        if any(token in line for token in SUSPECT_TOKENS):
            findings.append((line_no, line.strip()))
    return findings


def main() -> int:
    root = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(".")
    if not root.exists():
        print(f"Path not found: {root}")
        return 2

    total_files = 0
    bad_files = 0

    for path in iter_files(root):
        total_files += 1
        findings = find_suspect_lines(path)
        if findings:
            bad_files += 1
            print(f"[SUSPECT] {path}")
            for line_no, line in findings[:10]:
                print(f"  L{line_no}: {line}")
            if len(findings) > 10:
                print(f"  ... {len(findings) - 10} more suspect lines")

    if bad_files:
        print(f"\nFAIL: {bad_files}/{total_files} files contain suspect mojibake markers.")
        return 1

    print(f"PASS: scanned {total_files} files, no suspect mojibake markers found.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
