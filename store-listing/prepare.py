"""Validate locale copy, available captures and exported Play assets (Python + Pillow).

Every configured locale is checked by default; --locale limits the check. Draft locales may have
empty fields and missing assets, reported as coverage gaps. --strict fails on those gaps.
"""
import argparse
import json
from pathlib import Path
from PIL import Image

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--locale', help='Check one configured locale')
parser.add_argument('--strict', action='store_true', help='Reject untranslated fields and missing captures/exports')
args = parser.parse_args()
here = Path(__file__).resolve().parent
read = lambda path: json.loads(path.read_text())
config = read(here / 'studio.json')
reference = config.get('defaultLocale', 'en-US')
locales = [args.locale] if args.locale else config['locales']
assert set(locales) <= set(config['locales']), 'Unknown locale; initialize it with studio/locales.cjs --init'
reference_copy = read(here / reference / 'copy.json')
galleries = read(here / 'gallery.json')
profiles = read(here / 'profiles.json')


def cards(kind):
    gallery = galleries[kind]
    if isinstance(gallery, list):
        return gallery
    inherited = cards(gallery['inherits'])
    assert len(inherited) == len(gallery['cards']), kind
    return [dict(base, **override) for base, override in zip(inherited, gallery['cards'])]


incomplete = []
for locale in locales:
    root = here / locale
    copy = read(root / 'copy.json')
    assert set(copy) == set(reference_copy), f'{locale}: copy keys differ from the reference'
    empty = 0
    for key, fields in reference_copy.items():
        assert set(copy[key]) == set(fields), f'{locale}/{key}: copy fields differ from the reference'
        assert all(isinstance(value, str) for value in copy[key].values()), f'{locale}/{key}: copy must be text'
        empty += sum(not value.strip() for value in copy[key].values())
    listing_file = root / 'listing.json'
    if listing_file.exists():
        listing = read(listing_file)
        for key, filename, limit in [('appName', 'app-name.txt', 30), ('shortDescription', 'short-description.txt', 80), ('fullDescription', 'full-description.txt', 4000)]:
            assert 0 < len(listing[key]) <= limit, f'{locale}: {key} exceeds its Play limit'
            (root / filename).write_text(listing[key] + '\n')
    shared = '' if locale == reference else f'../{reference}/'
    manifest = {'galleries': {}, 'exampleReadings': True, 'sharedAssetsLocale': reference,
                'featureGraphic': shared + 'graphics/feature-graphic.png',
                'featurePrompt': shared + 'graphics/feature-graphic.prompt.txt',
                'featureGenerator': 'Built-in ImageGen', 'appIcon': shared + 'graphics/app-icon.png'}
    raws = set()
    missing_exports = 0
    for kind in galleries:
        manifest['galleries'][kind] = []
        for index, slide in enumerate(cards(kind)):
            relative = slide.get('file', f'{kind}/{index + 1:02}-{slide["feature"]}.jpg')
            devices = slide.get('devices', [{'raw': slide['raw']}] if slide.get('raw') else [])
            raws.update(device['raw'] for device in devices)
            path = root / relative
            if not path.exists():
                missing_exports += 1
                continue
            with Image.open(path) as image:
                assert image.size == (profiles[kind]['width'], profiles[kind]['height']), (path, image.size)
                assert path.stat().st_size < 8 * 1024 * 1024, path
                manifest['galleries'][kind].append({'file': relative, 'feature': slide['feature'], 'slide': index,
                    'width': image.width, 'height': image.height, 'bytes': path.stat().st_size,
                    'sources': ['raw/' + device['raw'] for device in devices]})
    missing_captures = missing_layouts = 0
    ratios = [p['captureWidth'] / p['captureHeight'] for p in profiles.values() if 'captureWidth' in p]
    for raw in sorted(raws):
        path = root / 'raw' / raw
        if not path.exists():
            missing_captures += 1
            continue
        with Image.open(path) as image:
            width, height = image.size
            assert any(abs(width / height - r) < .001 or abs(height / width - r) < .001 for r in ratios), path
        sidecar = path.with_suffix('.nodes.json')
        if not sidecar.exists():
            missing_layouts += 1
            continue
        tree = read(sidecar)
        assert tree['nodes'], f'{sidecar}: empty UI tree'
        assert (tree['width'], tree['height']) == (width, height), sidecar
        assert len({node['path'] for node in tree['nodes']}) == len(tree['nodes']), sidecar
        assert all(len(node['b']) == 4 and node['b'][2] >= node['b'][0] and node['b'][3] >= node['b'][1] for node in tree['nodes']), sidecar
    for asset, size, mode in [('featureGraphic', (1024, 500), 'RGB'), ('appIcon', (512, 512), 'RGBA')]:
        with Image.open(root / manifest[asset]) as image:
            assert image.size == size and image.mode == mode
    if not missing_exports:
        (root / 'assets.json').write_text(json.dumps(manifest, indent=2) + '\n')
    gaps = empty + missing_captures + missing_exports + missing_layouts
    if gaps:
        incomplete.append(locale)
    print(f'{locale}: {empty} untranslated fields, {missing_captures} missing captures, '
          f'{missing_layouts} missing layout files, {missing_exports} missing exports')
    if locale == reference:
        assert not empty and not missing_captures and not missing_exports, 'Reference assets are incomplete'
if args.strict and incomplete:
    raise SystemExit('--strict: incomplete locales: ' + ', '.join(incomplete))
print('Validated copy schemas, present captures/layouts, export dimensions and shared artwork.')
