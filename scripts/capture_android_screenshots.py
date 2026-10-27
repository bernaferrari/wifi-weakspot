"""Capture the isolated Android demo on a resizable emulator (requires Pillow and adb)."""
import argparse
import json
from io import BytesIO
from pathlib import Path
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

from PIL import Image

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--serial', required=True, help='Dedicated emulator serial; never a physical phone')
parser.add_argument('--profile', choices=['phone', 'tablet7', 'tablet10', 'all'], default='all')
parser.add_argument('--store-only', action='store_true', help='Capture just the current gallery sources in the selected theme')
parser.add_argument('--settings-only', action='store_true', help='Refresh Appearance and style-picker captures only')
parser.add_argument('--campaign-only', action='store_true', help='Capture the five store campaign scenes for each device')
parser.add_argument('--networks-only', action='store_true', help='Refresh only Networks captures after a chart/control change')
parser.add_argument('--home-tools-only', action='store_true', help='Refresh only the phone Home, Router check, and Devices captures')
parser.add_argument('--theme', choices=['dark', 'light'], default='dark')
parser.add_argument('--locale', help='Play locale to capture in, e.g. de-DE (default: the studio default locale)')
args = parser.parse_args()
assert args.serial.startswith('emulator-'), 'Use a dedicated emulator, not a personal device.'
repo = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(repo / 'store-listing/studio'))
from uitree import Navigator, set_app_locale, write_sidecar  # noqa: E402

studio = json.loads((repo / 'store-listing/studio.json').read_text())
reference = studio.get('defaultLocale', 'en-US')
locale = args.locale or reference
# The reference locale records which UI node each tap hits; every other locale replays those tree paths,
# so this flow is written once with English labels and still runs in German or Japanese.
navigator = Navigator(repo / f'store-listing/{reference}/raw/tap-map.json', recording=locale == reference)
raw = repo / f'store-listing/{locale}/raw'
if args.theme == 'light':
    raw = raw / 'light'
# README previews stay in the reference language.
docs = repo / 'docs/screenshots' if locale == reference else Path('/tmp/wifi-weakspot-docs-unused')
package = 'com.bernaferrari.wifiweakspot.screenshots'


def adb(*parts):
    return subprocess.check_output(['adb', '-s', args.serial, *parts], stderr=subprocess.STDOUT)


last_xml = None


def tree():
    global last_xml
    adb('shell', 'uiautomator', 'dump', '--compressed', '/sdcard/wifi-capture.xml')
    last_xml = adb('shell', 'cat', '/sdcard/wifi-capture.xml')
    root = ET.fromstring(last_xml)
    assert any(n.get('package') == package for n in root.iter('node')), 'Screenshot app is not foreground'
    return root


def find(label, root=None):
    return next((n for n in (root if root is not None else tree()).iter('node')
                 if label.fullmatch(n.get('text', '')) or label.fullmatch(n.get('content-desc', ''))), None)


def click(label):
    pattern = re.compile(label) if isinstance(label, str) else label
    mode = 'store' if args.store_only else 'campaign' if args.campaign_only else 'home-tools' if args.home_tools_only else 'settings' if args.settings_only else 'networks' if args.networks_only else 'full'
    for attempt in range(4):
        tree()
        try:
            node = navigator.resolve(f'{current_profile}|{args.theme}|{mode}|{pattern.pattern}', last_xml,
                                     lambda n: bool(pattern.fullmatch(n.get('text', '')) or pattern.fullmatch(n.get('desc', ''))))
            break
        except AssertionError:
            if attempt == 3:
                raise
            time.sleep(1)

    x1, y1, x2, y2 = node['b']
    adb('shell', 'input', 'tap', str((x1 + x2) // 2), str((y1 + y2) // 2))
    time.sleep(1)


def scroll(up=False):
    adb('shell', 'input', 'swipe', str(width * 4 // 5), str(height * (3 if up else 8) // 10),
        str(width * 4 // 5), str(height * (8 if up else 3) // 10), '300')
    time.sleep(.4)


def capture(*paths):
    root = tree()
    assert not any('100%' in (n.get('text', '') + n.get('content-desc', '')) for n in root.iter('node'))
    png = adb('exec-out', 'screencap', '-p')
    # Some emulator versions print a multiple-display warning before the PNG stream.
    png = png[png.index(b'\x89PNG\r\n\x1a\n'):]
    im = Image.open(BytesIO(png)).convert('RGB')
    assert im.size == (width, height), im.size
    for path in paths:
        path.parent.mkdir(parents=True, exist_ok=True)
        if path.suffix == '.png':
            im.save(path)
        else:
            im.save(path, quality=95, subsampling=0)
            # The UI tree beside each store capture lets callouts follow their element in every language.
            write_sidecar(path, last_xml, width, height)
        print(path.relative_to(repo) if path.is_relative_to(repo) else path, flush=True)


def back():
    adb('shell', 'input', 'keyevent', 'KEYCODE_BACK')
    time.sleep(.5)


profiles = {name: (p['captureWidth'], p['captureHeight'], p['captureDensity'])
            for name, p in json.loads((repo / 'store-listing/profiles.json').read_text()).items()
            if name != 'desktop'}
original_size = adb('shell', 'wm', 'size').decode()
original_density = adb('shell', 'wm', 'density').decode()
assert 'Override' not in original_size + original_density, 'Start from a dedicated emulator with no display overrides.'
current_profile = None
try:
    set_app_locale(adb, package, locale)
    adb('shell', 'cmd', 'overlay', 'enable-exclusive', '--category', 'com.android.internal.systemui.navbar.gestural')
    adb('shell', 'settings', 'put', 'global', 'sysui_demo_allowed', '1')
    for profile, (width, height, density) in profiles.items():
        if args.profile not in ('all', profile):
            continue
        current_profile = profile
        adb('shell', 'am', 'broadcast', '-a', 'com.android.systemui.demo', '--es', 'command', 'exit')
        adb('shell', 'am', 'force-stop', package)
        adb('shell', 'wm', 'size', f'{width}x{height}')
        adb('shell', 'wm', 'density', str(density))
        time.sleep(1)
        for extra in [('enter',), ('clock', '--es', 'hhmm', '0941'),
                      ('battery', '--es', 'level', '84', '--es', 'plugged', 'false'),
                      ('notifications', '--es', 'visible', 'false'),
                      ('network', '--es', 'wifi', 'show', '--es', 'level', '4', '--es', 'fully', 'true', '--es', 'mobile', 'hide', '--es', 'sims', '0')]:
            adb('shell', 'am', 'broadcast', '-a', 'com.android.systemui.demo', '--es', 'command', *extra)
        adb('shell', 'am', 'start', '-W', '-n', package + '/com.bernaferrari.wifiweakspot.android.MainActivity')
        time.sleep(2)
        if args.theme == 'light':
            click('Settings')
            click('Light')
            click('Home')
        phone = profile == 'phone'
        prefix = '' if phone else 'tablet/' + ('7-' if profile == 'tablet7' else '10-')
        if args.store_only:
            if args.theme == 'dark':
                if phone:
                    capture(raw / '01-signal.jpg')
                click('Networks')
                click(r'2\.4 GHz, 18 networks')
                capture(raw / (prefix + ('04-channels.jpg' if phone else '02-channels.jpg')))
                click('Signal over time')
                time.sleep(8)
                capture(raw / ('05-history.jpg' if phone else prefix + '05-history.jpg'))
            else:
                click('Rooms')
                capture(raw / (prefix + '02-rooms.jpg'))
                click('Map your home')
                capture(raw / (prefix + '03-coverage.jpg'))
                back()
                if phone:
                    click('Home')
                    scroll()
                    click('Router check')
                    click('GO')
                    time.sleep(8)
                    capture(raw / '06-router.jpg')
                    back()
                click('Settings')
                capture(raw / ('08-appearance.jpg' if phone else prefix + '04-appearance.jpg'))
            continue
        if args.campaign_only:
            click('Networks')
            click(r'2\.4 GHz, 18 networks')
            capture(raw / (prefix + ('04-channels.jpg' if phone else '02-channels.jpg')))
            click('Signal over time')
            time.sleep(6)
            capture(raw / ('05-history.jpg' if phone else prefix + '05-history.jpg'))
            click('Rooms')
            capture(raw / ('02-rooms.jpg' if phone else prefix + '02-rooms.jpg'))
            click('Map your home')
            capture(raw / ('03-coverage.jpg' if phone else prefix + '03-coverage.jpg'))
            back()
            click('Settings')
            capture(raw / ('08-appearance.jpg' if phone else prefix + '04-appearance.jpg'),
                    docs / (f'{args.theme}-settings.png' if phone else f'{profile}-{args.theme}-settings.png'))
            continue
        if args.home_tools_only:
            if phone:
                capture(raw / '01-signal.jpg', docs / f'{args.theme}-home.png')
                scroll()
                click('Router check')
                click('GO')
                time.sleep(8)
                capture(raw / '06-router.jpg', docs / f'{args.theme}-router-result.png')
                back()
                click('Devices')
                time.sleep(4)
                capture(raw / '07-devices.jpg', docs / f'{args.theme}-devices.png')
            continue
        if args.settings_only:
            click('Settings')
            appearance = '08-appearance.jpg' if phone else prefix + '04-appearance.jpg'
            preview = f'{args.theme}-settings.png' if phone else f'{profile}-{args.theme}-settings.png'
            capture(raw / appearance, docs / preview)
            continue
        if not args.networks_only:
            capture(raw / (prefix + ('01-signal.jpg' if phone else '01-home.jpg')),
                    *([docs / f'{args.theme}-home.png'] if phone else []))
        click('Networks')
        click(r'2\.4 GHz, 18 networks')
        capture(raw / (prefix + ('04-channels.jpg' if phone else '02-channels.jpg')),
                *([docs / f'{args.theme}-networks.png'] if phone else []))
        if phone:
            click('Signal over time')
            time.sleep(8)
            capture(raw / '05-history.jpg', docs / f'{args.theme}-overtime.png')
            click(re.compile(r'(Good|Weak|Okay|Dead zone|Excellent) signal, \d+%'))
            capture(docs / f'{args.theme}-overtime-selected.png')
            click(re.compile(r'Clear selected network: .+'))
            click('Channels')
            scroll()
            click(re.compile(r'(Good|Weak|Okay|Dead zone|Excellent) signal, \d+%'))
            scroll(up=True)
            capture(docs / f'{args.theme}-channel-selection.png')
            click(re.compile(r'Clear selected network: .+'))
        click(re.compile(r'Suggested channel.*'))
        capture(docs / (f'{args.theme}-channel-advice.png' if phone else
                         ('tablet-' if profile == 'tablet7' else 'desktop-') +
                         ('light-' if args.theme == 'light' else '') + 'channel-advice.png'))
        back()
        if args.networks_only:
            continue
        click('Home')
        if phone:
            scroll()
            click('Router check')
            click('GO')
            time.sleep(8)
            capture(raw / '06-router.jpg', docs / f'{args.theme}-router-result.png')
            back()
            click('Devices')
            time.sleep(4)
            capture(raw / '07-devices.jpg', docs / f'{args.theme}-devices.png')
            back()
        if profile != 'tablet10':
            click('Rooms')
            if phone:
                capture(raw / '02-rooms.jpg', docs / f'{args.theme}-rooms.png')
            click('Map your home')
            capture(raw / ('03-coverage.jpg' if phone else prefix + '03-coverage.jpg'),
                    *([docs / f'{args.theme}-floorplan.png'] if phone else []))
            back()
            click('Settings')
            capture(raw / ('08-appearance.jpg' if phone else prefix + '04-appearance.jpg'),
                    *([docs / f'{args.theme}-settings.png'] if phone else []))
finally:
    navigator.save()
    adb('shell', 'am', 'force-stop', package)
    set_app_locale(adb, package, None)
    adb('shell', 'wm', 'size', 'reset')
    adb('shell', 'wm', 'density', 'reset')
    adb('shell', 'am', 'broadcast', '-a', 'com.android.systemui.demo', '--es', 'command', 'exit')
