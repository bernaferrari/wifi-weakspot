"""Make side-by-side previews from the unmodified native light/dark captures."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageOps

root = Path(__file__).resolve().parents[1] / 'store-listing/en-US'
out = root / 'comparisons'
out.mkdir(exist_ok=True)
try:
    font = ImageFont.truetype('/System/Library/Fonts/Supplemental/Arial.ttf', 28)
except OSError:
    font = ImageFont.load_default(size=28)
for name, source in [('channels', '04-channels.jpg'), ('rooms', '02-rooms.jpg'),
                     ('appearance', '08-appearance.jpg'), ('tablet-channels', 'tablet/10-02-channels.jpg')]:
    landscape = name == 'tablet-channels'
    panel = (960, 540) if landscape else (540, 960)
    canvas = Image.new('RGB', (panel[0] * 2, panel[1] + 64), '#eeeeef')
    draw = ImageDraw.Draw(canvas)
    for index, (theme, folder) in enumerate([('Dark', 'raw'), ('Light', 'raw/light')]):
        draw.text((panel[0] * index + 24, 16), theme, font=font, fill='#202024')
        image = ImageOps.contain(Image.open(root / folder / source), panel)
        canvas.paste(image, (panel[0] * index + (panel[0] - image.width) // 2, 64))
    canvas.save(out / f'{name}.jpg', quality=94, subsampling=0)
    print(out / f'{name}.jpg')
