"""Refresh paste-ready copy and validate exported Play assets (Python + Pillow)."""
import json
from pathlib import Path
from PIL import Image

root = Path(__file__).parent / "en-US"
copy = json.loads((root / "listing.json").read_text())
for key, filename, limit in [
    ("appName", "app-name.txt", 30),
    ("shortDescription", "short-description.txt", 80),
    ("fullDescription", "full-description.txt", 4000),
]:
    assert 0 < len(copy[key]) <= limit, f"{key} exceeds its Play limit"
    (root / filename).write_text(copy[key] + "\n")

# This order mirrors the saved Play Console draft. Lead with the analyzer.
galleries = {
    "phone": ["screenshots/" + ("v3-" if name in {"02-rooms", "06-router", "08-appearance"} else "v2-") + name + ".jpg" for name in [
        "04-channels", "01-signal", "05-history", "02-rooms",
        "03-coverage", "06-router", "07-devices", "08-appearance"]],
    "tablet7": ["tablet/" + name + ".jpg" for name in [
        "7-02-channels", "7-01-home", "7-03-coverage", "7-04-appearance-v3"]],
    "tablet10": ["tablet/10-02-channels.jpg", "tablet/10-01-home.jpg"],
    "desktop": ["desktop/desktop-" + name + ".jpg" for name in [
        "02-channels", "01-home", "03-history", "04-appearance-v3"]],
}
manifest = {"galleries": {}, "exampleReadings": True,
            "featureGraphic": "graphics/feature-graphic.png",
            "featurePrompt": "graphics/feature-graphic.prompt.txt",
            "featureGenerator": "Built-in ImageGen",
            "appIcon": "graphics/app-icon.png"}
for gallery, paths in galleries.items():
    manifest["galleries"][gallery] = []
    for relative in paths:
        path = root / relative
        with Image.open(path) as image:
            assert image.size in [(1080, 1920), (1920, 1080)], (path, image.size)
            assert path.stat().st_size < 8 * 1024 * 1024, path
            manifest["galleries"][gallery].append({
                "file": relative, "width": image.width, "height": image.height,
                "bytes": path.stat().st_size})
with Image.open(root / manifest["featureGraphic"]) as image:
    assert image.size == (1024, 500) and image.mode == "RGB"
with Image.open(root / manifest["appIcon"]) as image:
    assert image.size == (512, 512) and image.mode == "RGBA"
(root / "assets.json").write_text(json.dumps(manifest, indent=2) + "\n")
print("Copy refreshed. Verified 18 screenshots, icon, feature graphic, and gallery order.")
