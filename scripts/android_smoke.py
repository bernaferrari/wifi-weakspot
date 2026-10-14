#!/usr/bin/env python3
"""UI/state checks on a disposable Android emulator; never accepts a physical device.

Seed a synthetic v1 database before upgrading the APK to verify actual migration.
Example: python3 scripts/android_smoke.py --serial emulator-5554 --seed-legacy
         adb -s emulator-5554 install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
         python3 scripts/android_smoke.py --serial emulator-5554
Artifacts and a backup of the emulator test database are saved under build/qa/.
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
package = 'com.pulsewave.app'

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
    adb('shell', 'uiautomator', 'dump', '--compressed', '/sdcard/pulsewave-smoke.xml')
    return ET.fromstring(adb('shell', 'cat', '/sdcard/pulsewave-smoke.xml'))

def find(label, contains=False, timeout=15):
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        try:
            nodes = list(tree().iter('node'))
            for node in nodes:
                values = (node.get('text', ''), node.get('content-desc', ''))
                if any((label in v if contains else label == v) for v in values):
                    return node
        except (ET.ParseError, subprocess.CalledProcessError):
            pass
        time.sleep(.3)
    screenshot('failure')
    raise AssertionError('Missing visible control/content: ' + label)

def click(label, contains=False):
    node = find(label, contains)
    positions = list(map(int, re.findall(r'\d+', node.attrib['bounds'])))
    adb('shell', 'input', 'tap', str((positions[0] + positions[2]) // 2), str((positions[1] + positions[3]) // 2))

def scroll_to(label):
    for _ in range(6):
        try:
            return find(label, timeout=1)
        except AssertionError:
            adb('shell', 'input', 'swipe', '360', '990', '360', '320', '300')
    raise AssertionError('Could not scroll to ' + label)

if args.seed_legacy:
    adb('shell', 'am', 'force-stop', package)
    old = adb('exec-out', 'run-as', package, 'cat', 'databases/pulsewave.db', binary=True, check=False)
    if old.startswith(b'SQLite format 3'):
        (out / 'emulator-before-fixtures.db').write_bytes(old)
    fixture = out / 'legacy-fixture.db'
    fixture.unlink(missing_ok=True)
    schema = json.loads((root / 'composeApp/schemas/com.pulsewave.app.core.database.PulseWaveDatabase/1.json').read_text())['database']
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
    adb('shell', 'run-as', package, 'rm', '-f', 'databases/pulsewave.db-wal', 'databases/pulsewave.db-shm')
    adb('exec-in', 'run-as', package, 'tee', 'databases/pulsewave.db', binary=True, data=fixture.read_bytes())
    print('Seeded synthetic v1 fixture in disposable emulator; original test DB backed up when present.')
    raise SystemExit(0)

# Denial must leave independent tools available. Revocation may stop the process.
for permission in ['ACCESS_FINE_LOCATION', 'ACCESS_COARSE_LOCATION']:
    adb('shell', 'pm', 'revoke', package, 'android.permission.' + permission, check=False)
start()
find('PulseWave')
click('Tools')
find('Ping')
screenshot('tools-without-scan-permission')
# Open saved history after upgrading from the fixture, exercising Room validation.
click('History')
find('History')
click('Tools')
click('DNS localhost')
find('A: 127.0.0.1')
click('Done')
click('Speed')
click('12.3 Mbps download')
find('HTTP latency:', contains=True)
screenshot('saved-speed-result')
click('Done')
click('Back')
click('Home')
click('Settings')
find('Theme')
click('Dark')
screenshot('settings-dark')
scroll_to('Reduce motion')
click('Reduce motion')
scroll_to('About PulseWave')
click('About PulseWave')
find('Understand your connection')
click('Back')
click('Back')
click('Networks')
find('See the Wi-Fi around you', timeout=20)
# Simulate a grant in Settings while Activity is paused; resume must refresh the gate.
adb('shell', 'am', 'start', '-a', 'android.settings.APPLICATION_DETAILS_SETTINGS', '-d', 'package:' + package)
for permission in ['ACCESS_COARSE_LOCATION', 'ACCESS_FINE_LOCATION']:
    adb('shell', 'pm', 'grant', package, 'android.permission.' + permission)
adb('shell', 'settings', 'put', 'secure', 'location_mode', '3')
adb('shell', 'svc', 'wifi', 'enable')
adb('shell', 'input', 'keyevent', 'KEYCODE_BACK')
find('Nearby Wi-Fi', timeout=20)
screenshot('analyze-after-settings-grant')
click('Walk')
scroll_to('Floor plan heatmap')
click('Floor plan heatmap')
find('QA legacy survey')
find('1 points')
screenshot('restored-survey')
click('1 points')
find('-66', contains=True)
click('Close')
click('Back')
# Verify on-device migration preserved the original measurement and added calibration.
adb('shell', 'am', 'force-stop', package)
fixture = out / 'migrated-fixture.db'
fixture.write_bytes(adb('exec-out', 'run-as', package, 'cat', 'databases/pulsewave.db', binary=True))
with sqlite3.connect(fixture) as db:
    assert db.execute('PRAGMA user_version').fetchone()[0] == 2
    assert db.execute('SELECT x,y,rssi,timestamp FROM survey_points WHERE id=9010').fetchone() == (.3,.7,-66,3000)
    assert db.execute('SELECT widthMeters,heightMeters,mapTopBearingDegrees FROM survey_projects WHERE id=9000').fetchone() == (10.,10.,0.)
print('PASS: permission-independent tools, history details, theme/settings, permission recovery, saved survey and real v1→v2 migration.')
