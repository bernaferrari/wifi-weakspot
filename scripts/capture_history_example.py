"""Capture a changing example signal using the isolated screenshot APK, never real networks."""
import argparse
from io import BytesIO
import json
from pathlib import Path
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET
from PIL import Image

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--serial', required=True)
parser.add_argument('--profile', choices=['phone', 'tablet7', 'tablet10', 'all'], default='all')
parser.add_argument('--theme', choices=['dark', 'light'], default='dark')
parser.add_argument('--locale', help='Play locale to capture in, e.g. de-DE (default: the studio default locale)')
args = parser.parse_args()
assert args.serial.startswith('emulator-')
package = 'com.bernaferrari.wifiweakspot.screenshots'
repo = Path(__file__).resolve().parents[1]
profiles = json.loads((repo/'store-listing/profiles.json').read_text())
sys.path.insert(0, str(repo/'store-listing/studio'))
from uitree import Navigator, set_app_locale, write_sidecar  # noqa: E402
reference = json.loads((repo/'store-listing/studio.json').read_text()).get('defaultLocale', 'en-US')
locale = args.locale or reference
navigator = Navigator(repo/f'store-listing/{reference}/raw/tap-map-history-{args.theme}.json', recording=locale == reference)
last_xml = None

def adb(*parts):
    return subprocess.check_output(['adb','-s',args.serial,*parts],stderr=subprocess.STDOUT)

def tree():
    global last_xml
    adb('shell','uiautomator','dump','--compressed','/sdcard/history-example.xml')
    last_xml=adb('shell','cat','/sdcard/history-example.xml')
    root=ET.fromstring(last_xml)
    assert any(n.get('package')==package for n in root.iter('node'))
    return root

def click(label):
    # Taps are recorded by UI tree path in the reference locale and replayed in every other language.
    for attempt in range(4):
        tree()
        try:
            node=navigator.resolve(f'{current}|{label}',last_xml,lambda n: label in (n.get('text'),n.get('desc')))
            break
        except AssertionError:
            if attempt == 3:
                raise
            time.sleep(1)
    x1,y1,x2,y2=node['b']
    adb('shell','input','tap',str((x1+x2)//2),str((y1+y2)//2));time.sleep(1)

assert 'Override' not in adb('shell','wm','size').decode()+adb('shell','wm','density').decode()
current=None
try:
    set_app_locale(lambda *w: adb(*w),package,locale)
    adb('shell','settings','put','global','sysui_demo_allowed','1')
    for name, profile in profiles.items():
        if name == 'desktop' or args.profile not in ('all', name):
            continue
        current=name
        width,height=profile['captureWidth'],profile['captureHeight']
        adb('shell','am','force-stop',package)
        adb('shell','am','broadcast','-a','com.android.systemui.demo','--es','command','exit')
        adb('shell','wm','size',f'{width}x{height}');adb('shell','wm','density',str(profile['captureDensity']));time.sleep(1)
        for options in [('enter',),('clock','--es','hhmm','0941'),('battery','--es','level','84','--es','plugged','false'),('notifications','--es','visible','false'),('network','--es','wifi','show','--es','level','4','--es','fully','true','--es','mobile','hide','--es','sims','0')]:
            adb('shell','am','broadcast','-a','com.android.systemui.demo','--es','command',*options)
        adb('shell','am','start','-W','-n',package+'/com.bernaferrari.wifiweakspot.android.ScreenshotActivity','--ez','moving','true','--es','theme',args.theme)
        time.sleep(2);click('Networks');click('2.4 GHz, 18 networks');click('Signal over time')
        print(f'{name}: recording the example walk through a weak spot…',flush=True)
        time.sleep(45)
        root=tree()
        if locale==reference:
            assert any((n.get('content-desc') or '').startswith('Signal history for') for n in root.iter('node'))
        assert not any('100%' in (n.get('text','')+n.get('content-desc','')) for n in root.iter('node'))
        png=adb('exec-out','screencap','-p');png=png[png.index(b'\x89PNG\r\n\x1a\n'):]
        image=Image.open(BytesIO(png)).convert('RGB');assert image.size==(width,height)
        filename='05-history.jpg' if name=='phone' else f'tablet/{"7" if name=="tablet7" else "10"}-05-history.jpg'
        target=repo/f'store-listing/{locale}/raw'/('light/' if args.theme=='light' else '')/filename
        target.parent.mkdir(parents=True,exist_ok=True)
        image.save(target,quality=95,subsampling=0)
        write_sidecar(target,last_xml,width,height)
        if locale==reference and args.theme=='dark':
            image.save(repo/'docs/screenshots'/('dark-overtime.png' if name=='phone' else name+'-dark-overtime.png'))
        print(f'{name}: native changing-signal history captured',flush=True)
finally:
    navigator.save()
    adb('shell','am','force-stop',package)
    set_app_locale(lambda *w: adb(*w),package,None)
    adb('shell','am','broadcast','-a','com.android.systemui.demo','--es','command','exit')
    adb('shell','wm','size','reset');adb('shell','wm','density','reset')
