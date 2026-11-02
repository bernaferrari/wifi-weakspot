# Google Play listing sources

Edit `en-US/listing.json` for store copy. The active artwork is defined in `gallery.json`: seven phone images, five 7-inch tablet images, five 10-inch tablet images, and four desktop images.

## The design

The set is built as one panorama, like the strongest Play listings: one canvas gradient runs under every card, and giant Material 3 Expressive shapes (four-leaf clover, cookie, flower, sunny) about one and a half cards wide sit across the seams. `theme.js` lays both out in card widths across the whole gallery and each card renders its own window, so colors and edges meet exactly at every seam. Devices take about two thirds of the width, leaving room for the shapes, and run off the bottom edge.

Each card makes one point: a bold, rounded Google Sans Flex headline with an indigo highlighted phrase (`*phrase*` in `<locale>/copy.json`) and one short supporting line, in the same ink on every card.

Phone cards may lift one real region of the capture out of the screen (`callouts` in `gallery.json`: capture-pixel `x`, `y`, `w`, `h`, plus `mag`, `dx`, `dy`, and `radius`). The callout is cut from the same native capture and centered where it lives, so it never invents UI.

The artwork runs on [Screenshot Studio](https://github.com/bernaferrari/screenshot-studio) (`npx screenshot-studio`, run from the repository root), shared by all of these apps. This app provides:

- `studio.json`: app name, locales, contact-sheet names.
- `profiles.json`: export canvases, native capture sizes, Android densities, and device frame per gallery.
- `theme.css`: typography, colors, framing, and phone/tablet/desktop compositions.
- `theme.js`: the panorama gradient and Material 3 shapes.
- `gallery.json`: shared story order, capture paths (relative to each locale’s `raw/`), callouts, and export filenames.
- `<locale>/copy.json`: headlines and supporting copy; the reference is `en-US/copy.json`.
- `en-US/gallery-contact-sheet.jpg`, `gallery-tablet7.jpg`, `gallery-tablet10.jpg`, `gallery-desktop.jpg`: joined overviews.

Artwork uses a bundled Latin subset of [Google Sans Flex](https://github.com/google/fonts/tree/main/ofl/googlesansflex), with its [OFL license](fonts/OFL.txt). There are no font or design-service requests when rendering. Native app screenshots remain unpainted, with their real Android status bar and gesture-navigation pill.

## Edit and export

1. Replace the relevant JPEG in `en-US/raw/` with a current native capture. All app data in these captures is simulated example data.
2. Edit `<locale>/copy.json` for words, `gallery.json` for layout and order, and `theme.css` or `theme.js` for composition. Each image must stand on its own as well as belonging to the set.
3. Run `npx screenshot-studio studio --open` (it opens `http://127.0.0.1:8790/store-listing/studio/`) to review every gallery live, joined edge to edge or spaced like Play.
4. Run `npx screenshot-studio export`. It serves the repository itself, waits for fonts and images, and fails if a capture has the wrong aspect ratio, copy or callouts leave the canvas, a headline overflows, copy overlaps the app, or any request fails. Device frames intentionally run off the canvas. It also writes the contact sheets.
5. Run `python3 store-listing/prepare.py` with Pillow, then `python3 scripts/refresh_readme_assets.py` and `python3 scripts/refresh_readme_assets.py --check`. This validates the 21 active images and updates README provenance.

Phone artwork is 1080 × 1920, 7-inch artwork is 1200 × 1920, 10-inch artwork is 1920 × 1200, and desktop artwork is 1920 × 1080. Native phone screens inside the artwork are 1080 × 2340, matching the [Galaxy S25’s 19.5:9 ratio](https://www.samsung.com/my/smartphones/galaxy-s25/specs/). Canvas size never determines a screen’s proportions. Use a regular screenshot at that viewport, never a full-page export. App captures keep their native aspect ratio; the frame bleeds off the canvas so the app reads large.

Upload only the images listed in `en-US/assets.json`, in that order. Earlier v2/v3 artwork remains as historical source material and is not part of the active gallery. Uploads in Play Console should be verified individually before saving a draft.

## Theme comparison

Native phone and tablet captures are available in both themes, with Expressive as the default color style. Users choose Expressive, Vibrant, Tonal, or CMF through a single unlabeled row of abstract theme previews of buttons and tonal surfaces below the color picker. Names remain available to accessibility services. Preview tiles use the actual generated primary, secondary-container, and tertiary-container roles. The connected network follows the theme primary; other chart series keep their separate, distinguishable hues. The choice is saved and also applies when wallpaper colors provide the seed. Add `&theme=light` or `&theme=dark` to a phone/tablet preview URL to compare identical framing. The five phone images use dark mode for Channels and History, and light mode for Rooms, coverage, and Appearance. Portrait-tablet coverage and Appearance also use light mode. This shows both themes while keeping the colorful graphs on a dark canvas.

After capturing both themes, run `python3 scripts/compare_screenshot_themes.py` (requires Pillow) to create side-by-side previews in `en-US/comparisons/`.

## Native Android captures

Phone and tablet images are captured from the real Android UI on the shared `Resizable_Experimental` emulator (`emulator-5578`). Android renders the status-bar clock, Wi-Fi and battery icons, window insets, and gesture-navigation pill. The `screenshots` build uses a separate application ID (`com.bernaferrari.wifiweakspot.screenshots`), in-memory settings, sample networks, and saved example room readings. It does not scan nearby networks. Ordinary still captures hold the connected sample at 84%. The same fixtures also serve the browser demo; production builds do not include them.

```sh
npx screenshot-studio recapture                      # build, install, capture every language and size, export, refresh README images
npx screenshot-studio recapture de-DE --no-build
python3 scripts/record_android_capture_provenance.py --serial emulator-5578 --locale en-US
```

The scene tour is `capture.tour.mjs`, run by Screenshot Studio for every language, profile and theme: in light, every capture `gallery.json` uses (`<profile>/<scene>.png`: signal, channels, channel advice, rooms, coverage, router, devices, appearance on the phone; channels, rooms, coverage and appearance on tablets) and the moving history; in dark, the phone settings for the README (English only). The studio sets the emulator up (display size and density, demo-mode status bar at 9:41 and 84% battery, gesture navigation, the app language), saves each screen with its `.nodes.json` layout map, and restores everything afterwards, even after a failure. Run it alone with `npx screenshot-studio tour [--profile phone] [--locale de-DE] [--only channels,rooms]`. It only runs on an emulator, never a physical phone.

| Layout | Capture pixels | Android density | Logical size |
| --- | --- | --- | --- |
| Phone (S25 ratio) | 1080 × 2340 | 400 dpi | 432 × 936 dp |
| 7-inch class portrait tablet | 1200 × 1920 | 320 dpi | 600 × 960 dp |
| 10-inch class landscape tablet | 1920 × 1200 | 240 dpi | 1280 × 800 dp |

These are Android window-size classes, not a measurement of the host display. The landscape tablet shows the fixed graph and independently scrolling network list with no dialog open. Raw captures go into `<locale>/raw/<profile>/`. Keep the entire screenshot visible in artwork: device frames must fit within the export, and images use `object-fit: contain` so neither system bar is cropped.

Desktop assets remain shared Compose browser captures, since that gallery represents the desktop layout. Build the demo with `./gradlew :webDemo:wasmJsBrowserDevelopmentExecutableDistribution`. Its `captureDensity` query can set Compose render density for high-resolution captures.

The History capture comes from a capture-only activity that records 45 seconds of an example walk through a weak spot for each profile. Its changing readings are generated by the same simulated services; no real network is scanned. The activity and fixtures are absent from the normal app.

The provenance catalog `en-US/raw/capture-manifest.json` records the 17 native sources used by the active gallery, with their layout sidecars with dimensions, Android density, timestamp, source APK hash, and per-image hashes. It describes emulator captures at S25 proportions, not a physical Samsung device. The composition approach also draws on the local Calcula campaign: independent capture and export profiles, intrinsic device ratios, scaled corner radii, and complete, undistorted app views.

## Design references

The campaign uses its own artwork and copy. Research references: [Google Fit on Play](https://play.google.com/store/apps/details?id=com.google.android.apps.fitness), [Daylio on Play](https://play.google.com/store/apps/details?id=net.daylio), and [Google Play's preview-asset guidance](https://support.google.com/googleplay/android-developer/answer/9866151).

## Branding

`en-US/graphics/feature-source.png` is the generated master. `feature-graphic.png` is the 1024 × 500 RGB export, and its exact prompt is saved beside it. The 512 × 512 store icon is `store-listing/brand/icon.png`, written with every other icon by `npx screenshot-studio icons` from the layers in `store-listing/brand` (the painted `foreground.png` and `background.png`). Label the generated icon and feature artwork accurately in Play Console. The screenshot UI is captured from the app, with deterministic HTML/CSS captions and framing.

Saved drafts are not publication. Sending changes for review happens separately in Publishing overview.

## Keep the public README current

After updating the captures and exporting the Play artwork, run `python3 store-listing/prepare.py`, then `python3 scripts/refresh_readme_assets.py` from the repository root. The second command copies the current gallery assets into the six README images (the wide README image uses the native landscape tablet), without rescaling or repainting the interface. It resolves filenames from `en-US/assets.json`, so new asset versions can replace old ones without editing the README.

Run `python3 scripts/refresh_readme_assets.py --check` to check that every README image still matches the selected Play asset. Source paths and content hashes are recorded in `docs/screenshots/readme-assets.json`.

## Settings selection regression check

With the browser demo distribution served on port 8795, run `node scripts/check_settings_state.cjs` with Playwright installed (`PLAYWRIGHT_MODULE` can point to an existing installation). The script uses `capturePreferencesDelay=700` to model asynchronous Android preference reads. It verifies Dark, Light, and Auto plus a custom palette, CMF style, and dBm selection immediately on re-entering Settings. The ordinary demo has no injected delay.

Settings reads the preferences already loaded by the app-level theme and app providers, so entering the screen does not briefly fall back to Auto, wallpaper colors, or percentage display.

## Localized screenshots (Studio v4.3)

`gallery.json` is shared layout; `<locale>/copy.json` contains only words. Add or refresh a language with `npx screenshot-studio locales --init de-DE` from the repository root (existing words are preserved). `npx screenshot-studio locales` reports copy, capture and layout coverage. Other supported languages are scaffolded as drafts with empty fields.

Capture English first, then run the tour with `--locale de-DE`. It sets only the isolated capture app's language, verifies Android's language setting, writes images and `*.nodes.json` UI trees under `<locale>/raw/`, and resets the app language afterwards. Use only the dedicated `StudioCapture` emulator (`emulator-5578`); no physical phone or another session's emulator. Sample names and other fictional content remain English.

Open the editor with `npx screenshot-studio studio`. Use **Cards**, **Languages**, and **Copy**; Copy saves directly to the selected language's file (Cmd/Ctrl+Enter also saves). Pick callouts on English captures; matching localized layout nodes move them. Headline copy shrinks to fit down to 72%, with script-specific font fallbacks.

Render one completed language with `npx screenshot-studio export --locale de-DE --strict`. `--kind phone` limits the gallery. Missing words or images borrow English and are flagged. A callout that cannot follow its UI element is flagged too; `--strict` rejects those three conditions. Draft locales should fail strict checks until translated and captured. README images continue to use English assets. Nothing is uploaded or published by this workflow.

For the current gallery run `npx screenshot-studio recapture <locale>` (or `npx screenshot-studio tour --locale <locale>` alone). The reference run records `en-US/raw/tap-map.json`; localized runs replay those UI paths.

Desktop uses the production browser demo. Build it, serve `webDemo/build/dist/wasmJs/productionExecutable`, then run `PLAYWRIGHT_MODULE=/path/to/playwright node scripts/capture_desktop_screenshots.cjs --url http://127.0.0.1:8835 --locale en-US`, followed by `--locale de-DE`. This records separate desktop navigation positions and layout sidecars.

`python3 store-listing/prepare.py` checks every configured copy schema and available assets, and reports draft gaps. `--locale de-DE --strict` requires complete words, captures, layouts and exports for German. The icon and feature graphic are shared from English. Refresh README assets only from the English export.

### Multiple projects

The server also opens a shared workspace at `http://127.0.0.1:8790/studio/`. Use **Add project** to connect another app or screenshot folder, or **Find projects** to discover listings in a parent folder. The sidebar switches between projects; each overview shows its languages, captures, layout maps and exports. Plain folders open as image libraries. Registrations are shared in `~/.screenshot-studio/projects.json`; removing a project leaves its files intact. Unsaved copy survives project switches and reloads in the same browser tab. Save before ending the browser session. Complete asset counts describe file presence; run strict export and review artwork before release.

Project and view navigation now updates in place, preserving the sidebar and recently loaded previews. Browser Back/Forward works between views, and pending saves stay scoped to the app where they started.

The editor includes searchable language coverage, copy-completion filters and expandable previews. Copy supports search, missing-field filtering, card navigation and field status; English reference editing uses one column. On narrow screens, Preview opens the saved artwork in a dialog. The gallery includes Previous/Next scroll controls.

Studio v4 uses React and official shadcn/ui components throughout, including the expandable Sidebar and mobile Sheet drawer. The sidebar footer offers Light, Dark and System appearance; Cmd/Ctrl+K searches projects.

Screenshot images now open a shared shadcn gallery dialog with thumbnails, wrapping arrows, keyboard/swipe navigation, Fit and 1:1 zoom, panning and explicit original/download actions. Live cards preserve their language and device. Escape returns focus to the clicked image; browsing does not save or change copy.

Studio v4.3 refines the project overview, language screens and shared controls with a neutral shadcn theme, keyboard-accessible filter tabs, compact project cards and responsive headers.

The v4.3 project page separates Overview, Galleries and Languages, links to unfinished work, and keeps language counts and actions visible on phones. Narrow workbenches use a device-gallery select, and translation references stack above inputs when space is limited.
