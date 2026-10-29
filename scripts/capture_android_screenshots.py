"""Capture the isolated Android demo on the capture emulator (requires Pillow and adb).

Build and install the screenshots build first, or run `node store-listing/studio/recapture.cjs`, which
builds, captures every locale and exports. The emulator plumbing (display size per profile, demo-mode
status bar, tap maps, sidecars, cleanup) is the studio's `capture.py`; this file is the scene tour.
"""
import re
import sys
import time
from pathlib import Path

repo = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(repo / 'store-listing/studio'))
from capture import Device, parser  # noqa: E402

parser = parser(__doc__)
parser.add_argument('--store-only', action='store_true', help='Capture just the current gallery sources in the selected theme')
parser.add_argument('--settings-only', action='store_true', help='Refresh Appearance and style-picker captures only')
parser.add_argument('--campaign-only', action='store_true', help='Capture the five store campaign scenes for each device')
parser.add_argument('--networks-only', action='store_true', help='Refresh only Networks captures after a chart/control change')
parser.add_argument('--home-tools-only', action='store_true', help='Refresh only the phone Home, Router check, and Devices captures')
parser.add_argument('--theme', choices=['dark', 'light'], default='dark')
args = parser.parse_args()
mode = ('store' if args.store_only else 'campaign' if args.campaign_only else 'home-tools' if args.home_tools_only
        else 'settings' if args.settings_only else 'networks' if args.networks_only else 'full')
# Holds the connected sample at 84%; the app needs no network, and the fixture never scans.
d = Device(args, 'com.bernaferrari.wifiweakspot.screenshots', 'com.bernaferrari.wifiweakspot.android.MainActivity',
           battery=84, offline=False)
raw = d.raw / 'light' if args.theme == 'light' else d.raw
# README previews stay in the reference language.
docs = repo / 'docs/screenshots' if d.locale == d.reference else Path('/tmp/wifi-weakspot-docs-unused')


def click(label):
    d.tap(label, wait=1, attempts=20, pause=1)


def scroll(up=False):
    d.scroll(.5, up=up, x=.8, wait=.4)


def capture(*paths):
    # A full battery reading would mean the fixture lost its 84% sample.
    d.shot(*paths, settle=0, check=lambda nodes: not any('100%' in n.get('text', '') + n.get('desc', '') for n in nodes))


def back():
    d.back(.5)


with d:
    for profile in d.profiles():
        # Taps are recorded per profile, theme and mode in the reference locale and replayed in the others.
        d.scope = f'{profile}|{args.theme}|{mode}'
        d.launch(wait=2)
        if args.theme == 'light':
            click('Settings')
            click('Light')
            click('Home')
        phone = profile == 'phone'
        prefix = '' if phone else 'tablet/' + ('7-' if profile == 'tablet7' else '10-')
        if args.store_only:
            # Every capture gallery.json uses in this theme; the moving history comes from capture_history_example.py.
            if phone:
                capture(raw / '01-signal.jpg')
            click('Networks')
            click(r'2\.4 GHz, 18 networks')
            capture(raw / (prefix + ('04-channels.jpg' if phone else '02-channels.jpg')))
            if phone:
                click(re.compile(r'Suggested channel.*'))
                capture(raw / '09-channel-advice.jpg')
                back()
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
                click('Devices')
                time.sleep(4)
                capture(raw / '07-devices.jpg')
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
            # The README's dark settings card uses the dark phone capture.
            capture(raw / appearance, docs / preview, *([d.raw / 'readme-dark-settings.jpg'] if phone and args.theme == 'dark' else []))
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
        capture(*([raw / '09-channel-advice.jpg'] if phone else []), docs / (f'{args.theme}-channel-advice.png' if phone else
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
