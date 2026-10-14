#!/usr/bin/env python3
"""Reject invisible bidi embedding/override/isolate controls in Kotlin/Java sources."""
from pathlib import Path
import sys

root = Path(__file__).resolve().parents[1]
unsafe = set(range(0x202A, 0x202F)) | set(range(0x2066, 0x206A))
findings = []
for module in ("composeApp", "androidApp"):
    for path in (root / module / "src").rglob("*"):
        if path.suffix not in {".kt", ".java"}:
            continue
        for line_number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
            for character in line:
                if ord(character) in unsafe:
                    findings.append(f"{path.relative_to(root)}:{line_number}: forbidden U+{ord(character):04X}")
for path in root.rglob("*.gradle.kts"):
    if "build" in path.relative_to(root).parts or ".gradle" in path.parts:
        continue
    for line_number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        for character in line:
            if ord(character) in unsafe:
                findings.append(f"{path.relative_to(root)}:{line_number}: forbidden U+{ord(character):04X}")
if findings:
    print("\n".join(findings))
    sys.exit(1)
print("Source Unicode safety check passed.")
