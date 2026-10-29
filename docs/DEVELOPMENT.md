# Developing WiFi Weakspot

A Kotlin and Compose codebase: the shared UI lives in `:composeApp`, `:androidApp` is the Android app, and `:webDemo` compiles the same UI to Kotlin/Wasm with simulated Wi-Fi.

[README](../README.md) · [Localization](localization.md) · [Store listing](../store-listing/README.md) · [Privacy](../PRIVACY.md)

## Quick start

```bash
git clone https://github.com/bernaferrari/wifi-weakspot.git
cd wifi-weakspot
./gradlew :androidApp:installDebug
```

## What you need

| Tool | Version | Notes |
| --- | --- | --- |
| JDK | 17 | What CI uses; the wrapper downloads Gradle 9.8.1 |
| Android SDK | platform `android-37.2` | `compileSdk` 37.2, `targetSdk` 36, `minSdk` 26 (Android 8.0). Set `sdk.dir` in `local.properties` or `ANDROID_HOME` |
| Python 3 | any recent | `scripts/check_bidi.py` and the emulator scripts; Pillow for screenshot tooling |
| Node.js | any recent | Only for Screenshot Studio and the browser check scripts |

## Run it

| Target | Command |
| --- | --- |
| Android (debug) | `./gradlew :androidApp:installDebug` |
| Web demo (dev server) | `./gradlew :webDemo:wasmJsBrowserDevelopmentRun` |
| Web demo (static site) | `./gradlew :webDemo:wasmJsBrowserDistribution`, output in `webDemo/build/dist/wasmJs/productionExecutable` |

## Where things live

| Path | What's there |
| --- | --- |
| `composeApp/` | Shared screens, view models, navigation, design system and services (Android library) |
| `composeApp/src/test`, `src/androidTest` | Unit tests; device tests for backup, restore and database migration |
| `androidApp/` | The Android application: manifest, icons, signing, and the `screenshots` build type |
| `webDemo/` | Browser build of `composeApp`'s shared source with simulated platform services |
| `demo-fixtures/` | Sample networks and room readings shared by the web demo and the `screenshots` build |
| `scripts/` | Bidi check, emulator smoke test, capture scene tours, README asset refresh |
| `store-listing/` | Play listing copy, artwork sources, brand icon layers, Screenshot Studio |
| `docs/` | This page, localization notes, README images |

## Before you push

```bash
python3 scripts/check_bidi.py
./gradlew :composeApp:testDebugUnitTest :composeApp:lintDebug :androidApp:lintDebug \
  :androidApp:assembleDebug :androidApp:assembleRelease :webDemo:wasmJsBrowserDistribution
./gradlew :composeApp:connectedDebugAndroidTest   # needs a running emulator
```

[`build-android.yml`](../.github/workflows/build-android.yml) runs all of this on every push and pull request, with the device tests on an API 26 emulator; [`web-demo.yml`](../.github/workflows/web-demo.yml) publishes the web demo to GitHub Pages from `main`. Run both lint tasks: app-only lint misses issues in the shared library code.

## Store screenshots and icon

Screenshot Studio lives in `store-listing/studio/`. It is synced from `~/Documents/screenshot-studio`, so never edit it here. App-specific config is `store-listing/studio.json`; details are in [store-listing/README.md](../store-listing/README.md).

- The `screenshots` build type (`./gradlew :androidApp:assembleScreenshots`, package `com.bernaferrari.wifiweakspot.screenshots`) is a release-configured build with in-memory settings, sample networks and saved example room readings. It never scans real networks. The capture runs are the scene tours `scripts/capture_android_screenshots.py` and `scripts/capture_history_example.py`.
- `node store-listing/studio/recapture.cjs` builds, installs and captures on the dedicated emulator `CAPTURE_SERIAL` (default `emulator-5578`, never a personal phone), exports every language, then refreshes the README images. Useful flags: a locale such as `en-US`, `--profile phone`, `--no-build`, `--no-capture`.
- Export only: `PLAYWRIGHT_MODULE=/path/to/playwright node store-listing/studio/export.cjs --locale en-US`. Live editor: `node store-listing/studio/server.cjs 8790`, then open `http://127.0.0.1:8790/store-listing/studio/`.
- Desktop artwork comes from the web demo: `scripts/capture_desktop_screenshots.cjs` (see the store-listing README).
- Icon: layers in `store-listing/brand/*.svg` plus `brand/icon.json`. `node store-listing/studio/icon.cjs` regenerates the Android launcher icons, the web demo favicons and the 512 px store icon.

## Good to know

- Release signing reads `keystore.properties` (`storeFile`, `storePassword`, `keyAlias`, `keyPassword`). Without it, release builds use the debug key and are for local checks only. Never commit `keystore.properties` or `*.keystore`; both are gitignored.
- `scripts/android_smoke.py --serial emulator-NNNN` checks permissions, history, settings and migration from a synthetic v1 database. It only accepts emulators: never run its fixture setup on a device with personal data. See the script header for the seed, install, verify sequence.
- The sample data in `demo-fixtures/` is fictional and ships only in the web demo and the `screenshots` build, never in production builds.
- Strings live in `core/i18n/`; read [localization.md](localization.md) before adding or changing text in any of the ten languages.
