#!/usr/bin/env python3
"""UI/state checks on a disposable, English-language Android emulator (API 26+).
Never accepts a physical device. Install the debug APK first to enable run-as.

Seed a synthetic v1 database before upgrading the APK to verify actual migration.
Example: python3 scripts/android_smoke.py --serial emulator-5554 --seed-legacy
         adb -s emulator-5554 install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
         python3 scripts/android_smoke.py --serial emulator-5554
Artifacts and a backup of the emulator test database are saved under build/qa/.
The non-seeding run expects the legacy fixture and verifies migration to the current schema.
"""
import argparse
import json
from pathlib import Path
import re
import sqlite3
import subprocess
import time
import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--serial', required=True)
parser.add_argument('--seed-legacy', action='store_true')
args = parser.parse_args()
if not re.fullmatch(r'emulator-\d+', args.serial):
    parser.error('These checks modify test permissions and fixtures; use a disposable emulator only.')
root = Path(__file__).resolve().parents[1]
out = root / 'build/qa'
out.mkdir(parents=True, exist_ok=True)
package = 'com.bernaferrari.wifiweakspot'

def adb(*arguments, binary=False, check=True, data=None):
    result = subprocess.run(['adb', '-s', args.serial, *arguments], input=data,
                            capture_output=True, timeout=40, check=check)
    return result.stdout if binary else result.stdout.decode(errors='replace').strip()

def start():
    adb('shell', 'am', 'force-stop', package)
    adb('shell', 'am', 'start', '-n', package + '/.android.MainActivity')

def screenshot(name):
    (out / (name + '.png')).write_bytes(adb('exec-out', 'screencap', '-p', binary=True))

def tree():
    # UIAutomator can exit without replacing the file when the UI never becomes idle.
    adb('shell', 'rm', '-f', '/sdcard/wifiweakspot-smoke.xml')
    adb('shell', 'uiautomator', 'dump', '--compressed', '/sdcard/wifiweakspot-smoke.xml')
    return ET.fromstring(adb('shell', 'cat', '/sdcard/wifiweakspot-smoke.xml'))

def normalized(value):
    return ' '.join(value.split())

def find(label, contains=False, timeout=15, required=True):
    labels = (label,) if isinstance(label, str) else label
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        try:
            nodes = list(tree().iter('node'))
            for node in nodes:
                if node.get('package') != package:
                    continue
                values = (normalized(node.get('text', '')), normalized(node.get('content-desc', '')))
                if any((normalized(candidate) in v if contains else normalized(candidate) == v) for candidate in labels for v in values):
                    return node
        except (ET.ParseError, subprocess.CalledProcessError):
            pass
        time.sleep(.3)
    if not required:
        return None
    screenshot('failure')
    (out / 'failure.xml').write_text(adb('shell', 'cat', '/sdcard/wifiweakspot-smoke.xml', check=False))
    raise AssertionError('Missing visible control/content: ' + str(label))

def click(label, contains=False):
    node = find(label, contains)
    positions = list(map(int, re.findall(r'\d+', node.attrib['bounds'])))
    adb('shell', 'input', 'tap', str((positions[0] + positions[2]) // 2), str((positions[1] + positions[3]) // 2))

def scroll_to(label):
    size = list(map(int, re.findall(r'(\d+)x(\d+)', adb('shell', 'wm', 'size'))[-1]))
    for _ in range(6):
        node = find(label, timeout=1, required=False)
        if node is not None:
            bounds = list(map(int, re.findall(r'\d+', node.attrib['bounds'])))
            if bounds[3] < size[1] * .87:
                return node
        adb('shell', 'input', 'swipe', str(size[0] // 2), str(int(size[1] * .78)), str(size[0] // 2), str(int(size[1] * .28)), '300')
    screenshot('failure')
    raise AssertionError('Could not scroll to ' + label)

def back():
    adb('shell', 'input', 'keyevent', 'KEYCODE_BACK')
    time.sleep(.4)

# Fail early rather than running against an unsupported Android version.
assert int(adb('shell', 'getprop', 'ro.build.version.sdk')) >= 26, 'Requires Android 8.0/API 26 or newer.'

if args.seed_legacy:
    adb('shell', 'am', 'force-stop', package)
    old = adb('exec-out', 'run-as', package, 'cat', 'databases/wifiweakspot.db', binary=True, check=False)
    if old.startswith(b'SQLite format 3'):
        (out / 'emulator-before-fixtures.db').write_bytes(old)
        for suffix in ['-wal', '-shm']:
            sidecar = adb('exec-out', 'run-as', package, 'cat', 'databases/wifiweakspot.db' + suffix, binary=True, check=False)
            if sidecar:
                (out / ('emulator-before-fixtures.db' + suffix)).write_bytes(sidecar)
    fixture = out / 'legacy-fixture.db'
    fixture.unlink(missing_ok=True)
    schema = json.loads((root / 'composeApp/schemas/com.bernaferrari.wifiweakspot.core.database.WifiWeakspotDatabase/1.json').read_text())['database']
    with sqlite3.connect(fixture) as db:
        for entity in schema['entities']:
            db.execute(entity['createSql'].replace('${TABLE_NAME}', entity['tableName']))
            for index in entity.get('indices', []):
                db.execute(index['createSql'].replace('${TABLE_NAME}', entity['tableName']))
        for query in schema['setupQueries']:
            db.execute(query)
        db.execute('PRAGMA user_version=1')
        db.execute("INSERT INTO survey_projects VALUES (9000,'QA legacy survey',NULL,NULL,1000,2000)")
        db.execute("INSERT INTO survey_points VALUES (9010,9000,0.3,0.7,-66,'QA network','00:11:22:33:44:55',1,3000)")
        db.execute("INSERT INTO speed_test_results VALUES (9020,3000,12.3,4.5,28.0,1.2,'QA server')")
        db.execute("INSERT INTO tool_results VALUES (9030,3000,'DNS','localhost','A: 127.0.0.1',1)")
    adb('shell', 'run-as', package, 'mkdir', '-p', 'databases')
    adb('shell', 'run-as', package, 'rm', '-f', 'databases/wifiweakspot.db-wal', 'databases/wifiweakspot.db-shm')
    # exec-in does not drain tee's stdout; a binary echo can terminate the transfer early.
    staging = '/data/local/tmp/wifiweakspot-legacy-fixture.db'
    adb('push', str(fixture), staging)
    try:
        adb('shell', 'run-as', package, 'cp', staging, 'databases/wifiweakspot.db')
    finally:
        adb('shell', 'rm', '-f', staging)
    written = adb('exec-out', 'run-as', package, 'cat', 'databases/wifiweakspot.db', binary=True)
    assert written == fixture.read_bytes(), 'Legacy database transfer was incomplete'

    print('Seeded synthetic v1 fixture in disposable emulator; original test DB backed up when present.')
    raise SystemExit(0)

print('Checking permission-independent tools and saved history…', flush=True)
# Denial must leave independent tools available. Revocation may stop the process.
for permission in ['ACCESS_FINE_LOCATION', 'ACCESS_COARSE_LOCATION']:
    adb('shell', 'pm', 'revoke', package, 'android.permission.' + permission, check=False)
start()
# A clean install has onboarding; a returning install goes straight to Home.
ready = find(('Not now', 'Home'), timeout=45)
if normalized(ready.get('text', '') or ready.get('content-desc', '')) == 'Not now':
    click('Not now')
find('Home')
scroll_to('Test a website')
click('Test a website')
find('Does it answer?')
screenshot('tools-without-scan-permission')
back()
# Open saved history after upgrading from the fixture, exercising Room validation.
scroll_to('History')
click('History')
click('Look up localhost')
find('127.0.0.1', contains=True)
back()
click('12 Mbps download')
find('Latency')
screenshot('saved-speed-result')
back()
back()
print('Checking appearance and permission recovery…', flush=True)
click('Settings')
find('Appearance')
click('Dark')
screenshot('settings-dark')
scroll_to('Reduce motion')
click('Reduce motion')
scroll_to('Restore backup')
find('Save backup')
screenshot('settings-backup')
click('Restore backup')
find('Choose a WiFi Weakspot backup.', contains=True)
screenshot('backup-restore-confirmation')
click('Cancel')
back()
click('Networks')
find('Allow access to nearby Wi-Fi', timeout=20)
# Simulate a grant in Settings while Activity is paused; resume must refresh the gate.
adb('shell', 'am', 'start', '-a', 'android.settings.APPLICATION_DETAILS_SETTINGS', '-d', 'package:' + package)
for permission in ['ACCESS_COARSE_LOCATION', 'ACCESS_FINE_LOCATION']:
    adb('shell', 'pm', 'grant', package, 'android.permission.' + permission)
adb('shell', 'settings', 'put', 'secure', 'location_mode', '3')
adb('shell', 'svc', 'wifi', 'enable')
back()
find('Networks', timeout=20)
for attempt in range(20):
    if find('Allow access to nearby Wi-Fi', timeout=.2, required=False) is None:
        break
    time.sleep(.5)
else:
    raise AssertionError('Scan permission gate did not recover after the Settings grant')
screenshot('analyze-after-settings-grant')
print('Checking saved survey and database migration…', flush=True)
click('Rooms')
click('Map your home')
find('QA legacy survey')
find('1 reading', contains=True)
screenshot('restored-survey')
click('All readings')
find('Weakest first', contains=True)
screenshot('restored-readings')
click('Reading 9010')
find('Recheck here')
screenshot('selected-map-reading')
click('Reading 9010', contains=True)
find('Position: 30% across, 70% down')
find('Across the map, from left')
find('Down the map, from top')
screenshot('accessible-map-position')
click('Close')
click('Rename')
find('Name')
screenshot('rename-map-reading')
click('Cancel')
back()
# Verify on-device migration preserved the original measurement and added calibration.
adb('shell', 'am', 'force-stop', package)
fixture = out / 'migrated-fixture.db'
fixture.write_bytes(adb('exec-out', 'run-as', package, 'cat', 'databases/wifiweakspot.db', binary=True))
# Preserve the WAL when Room has not checkpointed before process termination.
wal = adb('exec-out', 'run-as', package, 'cat', 'databases/wifiweakspot.db-wal', binary=True, check=False)
wal_path = Path(str(fixture) + '-wal')
wal_path.unlink(missing_ok=True)
Path(str(fixture) + '-shm').unlink(missing_ok=True)
if wal:
    wal_path.write_bytes(wal)
database_source = (root / 'composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/core/database/WifiWeakspotDatabase.kt').read_text()
current_version = int(re.search(r'version\s*=\s*(\d+)', database_source).group(1))
with sqlite3.connect(fixture) as db:
    actual_version = db.execute('PRAGMA user_version').fetchone()[0]
    assert actual_version == current_version, (actual_version, current_version)
    assert db.execute('SELECT x,y,rssi,timestamp FROM survey_points WHERE id=9010').fetchone() == (.3,.7,-66,3000)
    assert db.execute('SELECT widthMeters,heightMeters,mapTopBearingDegrees FROM survey_projects WHERE id=9000').fetchone() == (10.,10.,0.)
    assert db.execute("SELECT resultJson FROM tool_results WHERE id=9030").fetchone() == ('A: 127.0.0.1',)
    assert db.execute("SELECT networkKey FROM tool_results WHERE id=9030").fetchone() == (None,)
    schema_path = root / f'composeApp/schemas/com.bernaferrari.wifiweakspot.core.database.WifiWeakspotDatabase/{current_version}.json'
    expected_hash = json.loads(schema_path.read_text())['database']['identityHash']
    assert db.execute('SELECT identity_hash FROM room_master_table WHERE id=42').fetchone() == (expected_hash,)
print(f'PASS: permission-independent tools, history details, theme/settings, permission recovery, saved survey and real v1→v{current_version} migration.')
