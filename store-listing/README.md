# Google Play listing sources

Edit `en-US/listing.json` for the store copy, then run `python3 store-listing/prepare.py` with a Python environment that has Pillow. It refreshes the matching paste-ready text files, checks the image dimensions and size limits, and records the upload order in `en-US/assets.json`. The gallery order is editable near the top of the script; channels lead every gallery.

## Replace screenshots without rebuilding the artwork

1. Capture the current app and replace the relevant JPEG in `en-US/raw/`. These are real shared Compose UI captures with simulated example networks, not AI-rendered UI.
2. Edit headlines, captions, background colors, device sizes, and crops in `en-US/preview.html` (phones) or `en-US/wide-preview.html` (tablets and desktop). Each page has a small slide table near its end; CSS controls the framing. No external fonts, design-service account, or generated-image edit is needed.
3. Serve the repository locally, for example `python3 -m http.server 8776 --bind 127.0.0.1`, then open the desired preview URL in a browser.
4. Export through the Codex browser capture tools with `fullPage: true`. Phone and portrait-tablet outputs are 1080 × 1920. Landscape-tablet and desktop outputs are 1920 × 1080. Wait for the `<img>` to load before capture.
5. Upload the exported files one at a time in the intended order. Parallel uploads in Play Console can finish in a different order or fail to attach every image. Verify the thumbnails, then save the draft.

Preview routes:

- Phones: `/store-listing/en-US/preview.html?slide=0` through `7`.
- Portrait tablets: `/store-listing/en-US/wide-preview.html?kind=tablet7&slide=0` through `3`.
- Landscape tablets: `/store-listing/en-US/wide-preview.html?kind=tablet10&slide=0` through `1`.
- Desktop: `/store-listing/en-US/wide-preview.html?kind=desktop&slide=0` through `3`.

## Capture settings

Build the browser demo with `./gradlew :webDemo:wasmJsBrowserDevelopmentExecutableDistribution` and serve its distribution folder. Its optional `captureDensity` query sets the real Compose render density, rather than enlarging a low-resolution bitmap. Ordinary demo visits are unchanged.

| Layout | Viewport | `captureDensity` | Logical size |
| --- | --- | --- | --- |
| Phone | 1080 × 1920 | 2.5 | 432 × 768 dp |
| Portrait tablet | 1080 × 1920 | 1.5 | 720 × 1280 dp |
| Landscape tablet | 1920 × 1080 | 2 | 960 × 540 dp |
| Desktop | 1920 × 1080 | 1.5 | 1280 × 720 dp |

After a viewport or navigation change, wait for the new layout to settle before capturing. For raw app captures, prefer the regular `getScreenshot()` path when the full viewport fits. The browser's full-page snapshot path can omit the first rows of a Compose lazy list: this was reproduced on the landscape tablet's expanded channel view, while a regular screenshot of the same settled screen retained every row. Full-page capture is necessary when the host's physical window is shorter than a portrait viewport; visually check its lists before using the result. For the static HTML artwork pages, full-page export is reliable.

In capture mode, use screenshot-based clicks; Compose's accessibility hit rectangles may use logical coordinates instead of capture pixels.

## Branding

`en-US/graphics/feature-source.png` is the generated master. `feature-graphic.png` is the 1024 × 500 RGB export, and its exact prompt is saved beside it. The 512 × 512 RGBA store icon comes from `design/branding/weak-spot-icon-final.png`. Label the generated icon and feature artwork accurately in Play Console. The screenshot UI is captured from the app, with deterministic HTML/CSS captions and framing.

Saved drafts are not publication. Sending changes for review happens separately in Publishing overview.

## Keep the public README current

After updating the captures and exporting the Play artwork, run `python3 store-listing/prepare.py`, then `python3 scripts/refresh_readme_assets.py` from the repository root. The second command copies the current gallery assets into the six README images, without rescaling or repainting the interface. It resolves filenames from `en-US/assets.json`, so new asset versions can replace old ones without editing the README.

Run `python3 scripts/refresh_readme_assets.py --check` to check that every README image still matches the selected Play asset. Source paths and content hashes are recorded in `docs/screenshots/readme-assets.json`.
