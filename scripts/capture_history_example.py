"""Capture a changing example signal using the isolated screenshot APK, never real networks.

The capture-only activity records 45 seconds of an example walk through a weak spot for each profile.
Emulator plumbing comes from the studio's capture.py; `npx screenshot-studio recapture` runs this with the
studio's folder on PYTHONPATH.
"""
import time
from pathlib import Path

from capture import Device, parser  # the studio's, on PYTHONPATH when recapture runs this tour

repo = Path(__file__).resolve().parents[1]

parser = parser(__doc__)
parser.add_argument('--theme', choices=['dark', 'light'], default='dark')
args = parser.parse_args()
d = Device(args, 'com.bernaferrari.wifiweakspot.screenshots', 'com.bernaferrari.wifiweakspot.android.ScreenshotActivity',
           tap_map=f'tap-map-history-{args.theme}.json', battery=84, offline=False,
           listing=repo / 'store-listing')


def no_full_battery(nodes):
    if d.locale == d.reference:
        assert any(n.get('desc', '').startswith('Signal history for') for n in nodes)
    return not any('100%' in n.get('text', '') + n.get('desc', '') for n in nodes)


with d:
    for profile in d.profiles():
        d.scope = profile  # taps are recorded per profile in the reference locale and replayed elsewhere
        d.launch(wait=2, moving=True, theme=args.theme)
        for label in ('Networks', '2.4 GHz, 18 networks', 'Signal over time'):
            d.tap(label, wait=1, attempts=20, pause=1)
        print(f'{profile}: recording the example walk through a weak spot…', flush=True)
        time.sleep(45)
        name = '05-history.jpg' if profile == 'phone' else f'tablet/{"7" if profile == "tablet7" else "10"}-05-history.jpg'
        targets = [('light/' if args.theme == 'light' else '') + name]
        if d.locale == d.reference and args.theme == 'dark':
            targets.append(repo / 'docs/screenshots' / ('dark-overtime.png' if profile == 'phone' else f'{profile}-dark-overtime.png'))
        d.shot(*targets, settle=0, check=no_full_battery)
        print(f'{profile}: native changing-signal history captured', flush=True)
