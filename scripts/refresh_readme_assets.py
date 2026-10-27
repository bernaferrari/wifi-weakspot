"""Copy current Play artwork into README assets; --check verifies without writing."""
import argparse
import hashlib
import json
from pathlib import Path
import shutil

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("--check", action="store_true")
args = parser.parse_args()
repo = Path(__file__).resolve().parents[1]
listing = repo / "store-listing" / "en-US"
manifest = json.loads((listing / "assets.json").read_text())
targets = [
    ("phone", "channels", "readme-channels.jpg"),
    ("phone", "history", "readme-overtime.jpg"),
    ("phone", "coverage", "readme-floorplan.jpg"),
    ("phone", "rooms", "readme-rooms.jpg"),
    ("phone", "router", "readme-router.jpg"),
    ("phone", "network", "readme-network.jpg"),
    ("phone", "appearance", "readme-appearance.jpg"),
    ("tablet10", "channels", "readme-wide.jpg"),
]
provenance = []
for gallery, feature, filename in targets:
    matches = [item["file"] for item in manifest["galleries"][gallery]
               if item.get("feature") == feature or
               ("feature" not in item and Path(item["file"]).stem.endswith("-" + feature))]
    assert len(matches) == 1, f"Expected one {gallery}/{feature} asset, found {matches}"
    source = listing / matches[0]
    destination = repo / "docs" / "screenshots" / filename
    digest = hashlib.sha256(source.read_bytes()).hexdigest()
    if args.check:
        assert destination.exists(), f"Missing README asset: {destination}"
        assert hashlib.sha256(destination.read_bytes()).hexdigest() == digest, f"Stale README asset: {destination}"
    else:
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(source, destination)
    provenance.append({"file": str(destination.relative_to(repo)),
                       "source": str(source.relative_to(repo)), "sha256": digest})
record = repo / "docs" / "screenshots" / "readme-assets.json"
if args.check:
    assert json.loads(record.read_text()) == provenance, "README asset provenance is stale"
else:
    record.write_text(json.dumps(provenance, indent=2) + "\n")
print(f"Verified {len(targets)} README images match the current Play assets." if args.check else f"Refreshed {len(targets)} README images from the current Play assets.")
