# Developing WiFi Weakspot


Use JDK 17, the Android SDK platform specified by the Gradle build, and the included Gradle wrapper. The minimum supported Android version is 8.0 (API 26). Set the SDK location in your untracked `local.properties` or `ANDROID_HOME`.

```sh
python3 scripts/check_bidi.py
./gradlew :composeApp:testDebugUnitTest :composeApp:lintDebug :androidApp:lintDebug :androidApp:assembleDebug :androidApp:assembleRelease :webDemo:compileKotlinWasmJs
```

The library lint task is part of the verification gate: app-only lint does not report all issues in shared Android code. `scripts/android_smoke.py` exercises permissions, history, settings, and migration from a synthetic v1 database on a **disposable emulator**; see the script’s usage instructions. Never use its fixture setup on a device with personal data. Without `keystore.properties`, release builds use the debug signing key and are for local verification only.

`./gradlew :composeApp:connectedDebugAndroidTest` runs isolated backup roundtrip, interrupted-restore, malformed-file, and database-migration tests on a connected emulator. CI runs these against the minimum supported API 26 using [Android Emulator Runner](https://github.com/ReactiveCircus/android-emulator-runner).


Localized store artwork is maintained in [Screenshot Studio](../store-listing/README.md): shared layout in `store-listing/gallery.json`, words in `store-listing/<locale>/copy.json`, and native captures plus UI layout files in `store-listing/<locale>/raw/`. The editor compares languages and saves copy; strict export rejects English fallbacks and unresolved callouts. English remains the README image source.
