"""Size the generated alpha artwork for Android adaptive and legacy launchers."""
from pathlib import Path
from PIL import Image, ImageDraw
import math

HERE = Path(__file__).resolve().parent
RES = HERE.parents[1] / 'androidApp/src/main/res'
source = Image.open(HERE / 'weak-spot-foreground-source.png').convert('RGBA')
# Ignore near-transparent generation fringes when measuring the actual mark.
alpha = source.getchannel('A')
bounds = alpha.point(lambda a: 255 if a >= 128 else 0).getbbox()
mark = source.crop(bounds)
# Center the mark and fit its entire bounding rectangle inside a 62dp circle,
# leaving a 2dp radial margin inside Android's 66dp guaranteed safe circle.
width, height = mark.size
scale = 248 / math.hypot(width, height)
mark = mark.resize((round(width * scale), round(height * scale)), Image.Resampling.LANCZOS)
fg = Image.new('RGBA', (432, 432))
fg.alpha_composite(mark, ((432-mark.width)//2, (432-mark.height)//2))
bg = Image.new('RGBA', (432, 432), '#031344')
fg.save(RES / 'drawable-nodpi/ic_launcher_foreground_art.png')
fg.save(HERE / 'weak-spot-android-foreground.png')
bg.save(HERE / 'weak-spot-android-background.png')
mono = Image.new('RGBA', fg.size, 'white')
mono.putalpha(fg.getchannel('A'))
mono.save(RES / 'drawable-nodpi/ic_launcher_monochrome_art.png')
combined = Image.alpha_composite(bg, fg)
# Launchers expose roughly the central 72dp of the 108dp adaptive canvas.
visible = combined.crop((72, 72, 360, 360))
for density, pixels in [('mdpi',48),('hdpi',72),('xhdpi',96),('xxhdpi',144),('xxxhdpi',192)]:
    folder = RES / f'mipmap-{density}'
    folder.mkdir(exist_ok=True)
    icon = visible.resize((pixels,pixels), Image.Resampling.LANCZOS)
    mask = Image.new('L', (pixels*4,pixels*4))
    ImageDraw.Draw(mask).rounded_rectangle((0,0,pixels*4-1,pixels*4-1), radius=pixels*.22*4, fill=255)
    square = icon.copy()
    square.putalpha(mask.resize(icon.size, Image.Resampling.LANCZOS))
    square.save(folder / 'ic_launcher.png')
    ImageDraw.Draw(mask).rectangle((0,0,pixels*4,pixels*4), fill=0)
    ImageDraw.Draw(mask).ellipse((0,0,pixels*4-1,pixels*4-1), fill=255)
    icon.putalpha(mask.resize(icon.size, Image.Resampling.LANCZOS))
    icon.save(folder / 'ic_launcher_round.png')
# Review masks at a useful size.
sheet = Image.new('RGB', (960, 360), '#e4e6eb')
for index, shape in enumerate(['circle','squircle','square']):
    tile = visible.resize((256,256), Image.Resampling.LANCZOS)
    mask = Image.new('L',(256,256))
    draw = ImageDraw.Draw(mask)
    if shape == 'circle': draw.ellipse((0,0,255,255),fill=255)
    else: draw.rounded_rectangle((0,0,255,255),radius=58 if shape=='squircle' else 8,fill=255)
    sheet.paste(tile,(index*320+32,40),mask)
    ImageDraw.Draw(sheet).text((index*320+32,310),shape,fill='#17213a')
sheet.save(HERE / 'weak-spot-android-mask-preview.png')
assert math.hypot(mark.width/8, mark.height/8) <= 31.2
print(f'432px / 108dp layers; mark {mark.width/4:.2f} × {mark.height/4:.2f}dp; safe-circle radius <31.2dp')
