"""Catalog the native files emitted by the emulator capture scripts, without editing pixels."""
import argparse
from datetime import datetime, timezone
import hashlib
import json
from pathlib import Path
import subprocess
import shutil
from PIL import Image

parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--serial', required=True)
parser.add_argument('--locale', default='en-US', help='Play locale whose captures to record')
parser.add_argument('--settings-only', action='store_true', help='Update only Appearance capture records, preserving other scenes')
args=parser.parse_args()
assert args.serial.startswith('emulator-')
repo=Path(__file__).resolve().parents[1]
root=repo/'store-listing'/args.locale
profiles=json.loads((root.parent/'profiles.json').read_text())
galleries=json.loads((root.parent/'gallery.json').read_text())
records=[]
for kind in ['phone','tablet7','tablet10']:
    profile=profiles[kind]
    for slide in galleries[kind]:
        if args.settings_only and slide['feature'] != 'appearance':
            continue
        file=root/'raw'/slide['raw']
        with Image.open(file) as image:
            assert image.size==(profile['captureWidth'],profile['captureHeight']), (file,image.size)
        sidecar=file.with_suffix('.nodes.json')
        assert sidecar.exists(), f'Missing layout file: {sidecar}'
        records.append(dict(file=str(file.relative_to(root)),profile=kind,theme=slide.get('theme','dark'),
            width=profile['captureWidth'],height=profile['captureHeight'],densityDpi=profile['captureDensity'],
            capturedAt=datetime.fromtimestamp(file.stat().st_mtime,timezone.utc).isoformat(),
            sha256=hashlib.sha256(file.read_bytes()).hexdigest(),
            layout=str(sidecar.relative_to(root)),layoutSha256=hashlib.sha256(sidecar.read_bytes()).hexdigest()))
adb=shutil.which('adb') or str(Path.home()/'Library/Android/sdk/platform-tools/adb')
package='com.bernaferrari.wifiweakspot.screenshots'
installed_path=subprocess.check_output([adb,'-s',args.serial,'shell','pm','path',package],text=True).strip().removeprefix('package:')
assert installed_path.endswith('/base.apk'), 'Expected the isolated single-APK screenshot build'
installed_apk=subprocess.check_output([adb,'-s',args.serial,'exec-out','cat',installed_path])
installed_hash=hashlib.sha256(installed_apk).hexdigest()
prior_file=root/'raw/capture-manifest.json'
prior=json.loads(prior_file.read_text()) if prior_file.exists() else {}
prior_records={record['file']: record for record in prior.get('captures', [])}
for record in records:
    old=prior_records.get(record['file'], {})
    record['apkSha256']=(old.get('apkSha256', prior.get('apkSha256', installed_hash))
        if old.get('sha256')==record['sha256'] else installed_hash)
if args.settings_only:
    updated={record['file'] for record in records}
    records=[record for record in prior.get('captures',[]) if record['file'] not in updated]+records
manifest=dict(source='Android Resizable_Experimental emulator; adb exec-out screencap -p',
    serial=args.serial,locale=args.locale,package='com.bernaferrari.wifiweakspot.screenshots',
    commit=subprocess.check_output(['git','rev-parse','HEAD'],cwd=repo,text=True).strip(),
    latestInstalledApkSha256=installed_hash,
    systemUi='Real Android status bar at 09:41, Wi-Fi, 84% battery, and gesture navigation',
    sampleData=True,phoneDisplay='1080×2340; Galaxy S25 19.5:9 aspect ratio, not Samsung hardware',
    captureScripts=[f'scripts/capture_android_screenshots.py --store-only --theme dark --locale {args.locale}',
        f'scripts/capture_android_screenshots.py --store-only --theme light --locale {args.locale}',
        f'scripts/capture_history_example.py --profile all --locale {args.locale}'],captures=records)
(root/'raw/capture-manifest.json').write_text(json.dumps(manifest,indent=2,ensure_ascii=False)+'\n')
print(f'Cataloged {len(records)} native Android captures with pixel dimensions, APK and image hashes.')
