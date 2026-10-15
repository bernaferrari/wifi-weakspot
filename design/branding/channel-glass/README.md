# Overlapping channel peaks — Icon Composer source

Two editable SVG foreground layers, each on the same 1024 × 1024 canvas, derived from the app's Networks icon. The tall blue peak sits behind the shorter pink peak. Their shared baseline and intersection are intentional.

Import `01-tall-blue.svg` and `02-short-pink.svg` separately into Icon Composer without changing their relative placement. Use a solid midnight-blue background, starting at `#071339`. Enable Liquid Glass on the foreground layers individually so they retain separate material surfaces; tune translucency while checking that the blue peak remains visible through the pink peak. Let Icon Composer supply highlights, refraction, and shadows. The SVGs deliberately contain no simulated lighting or baked overlap color.

Start with restrained highlights and depth. Check default, dark, clear, and tinted appearances, including small previews. In monochrome, the two peaks must remain distinguishable. If they merge, adjust the layer grouping or translucency before adding decoration. This is source artwork, not a validated `.icon` document or a final exported icon.

`preview-flat.svg` shows only placement and color; it is not a Liquid Glass render. Keep it out of the foreground layer stack.

Workflow reference: https://developer.apple.com/documentation/xcode/creating-your-app-icon-using-icon-composer
