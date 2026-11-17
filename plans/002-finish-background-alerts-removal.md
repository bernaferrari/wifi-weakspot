# Plan 002: Background alerts are fully gone, in code, strings and the privacy policy

> **Executor instructions**: Follow this plan step by step. Run every
> verification command and confirm the expected result before moving to the
> next step. If anything in the "STOP conditions" section occurs, stop and
> report — do not improvise. When done, update the status row for this plan
> in `plans/README.md` — unless a reviewer dispatched you and told you they
> maintain the index.
>
> **Precondition check (run first, before the drift check)**:
> `rg -n "BackgroundMonitor" androidApp/src/main/AndroidManifest.xml composeApp/src/main/AndroidManifest.xml composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/di/AppModule.kt composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/settings/SettingsScreen.kt`
> must return **no matches**. If it returns any, this checkout does not contain
> the owner's first half of the removal (it was uncommitted when this plan was
> written). STOP and report; do not do that half yourself.
>
> **Drift check**:
> `git diff --stat 918ea41c..HEAD -- composeApp/src demo-fixtures/src webDemo/src PRIVACY.md docs/localization.md`
> Changes to the four files named in the precondition check are expected (that
> is the owner's first half). For any other in-scope file that changed, compare
> the "Current state" excerpts against the live code before proceeding; on a
> mismatch, treat it as a STOP condition.

## Status

- **Priority**: P1
- **Effort**: M
- **Risk**: LOW
- **Depends on**: the owner's uncommitted removal being committed (see precondition check). No other plan.
- **Category**: tech-debt
- **Planned at**: commit `918ea41c` plus the owner's uncommitted working tree, 2026-11-10

## Why this matters

The app used to offer "Background alerts": a foreground service that scanned
Wi-Fi in the background and posted notifications. The owner has started
removing it: the service and its permissions are out of the manifest, the
dependency-injection binding is gone, and the Settings section is deleted.
Everything else is still there. The published privacy policy still tells users
the app can run a location foreground service and show notifications. About
440 lines of unreachable service code still compile into the app. Twenty-two
strings are still carried, translated, in each of ten languages. A permission
the app no longer declares is still checked. Phones that ran the old version
keep two empty notification categories and a leftover preferences file. This
plan removes all of it so that the code, the translations and the privacy
policy describe the same app.

## Current state

Path shorthand used below (always type the full path in commands):

- `SHARED` = `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot`
- `ANDROID` = `composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot`
- `TESTS` = `composeApp/src/test/kotlin/com/bernaferrari/wifiweakspot`
- `DEMO` = `demo-fixtures/src/main/kotlin/com/bernaferrari/wifiweakspot/demo`
- `WEB` = `webDemo/src/wasmJsMain/kotlin/com/bernaferrari/wifiweakspot/demo`

About the repo: `SHARED` is platform-free code compiled for Android and for a
Kotlin/Wasm web demo. `ANDROID` holds Android implementations. `DEMO` and `WEB`
implement the same interfaces with simulated data, so every interface change in
`SHARED` must be mirrored there or the web demo and the `screenshots` build
type stop compiling.

Translations: seven string interfaces live in `SHARED/core/i18n/` (for example
`SettingsStrings.kt`, `DomainStrings.kt`). Each of ten languages implements all
of them in its own folder: `en`, `es`, `ptbr`, `fr`, `de`, `it`, `ru`, `ja`,
`ko`, `zhhans`, with files named like `en/EnSettingsStrings.kt`,
`ptbr/PtBrDomainStrings.kt`, `zhhans/ZhHansDomainStrings.kt`. There is no
fallback to English: a key removed from an interface must be removed from all
ten implementations or the build fails. That makes the compiler your checklist.

### What is left, by file

**Dead service code (delete the files):**

- `SHARED/core/monitoring/BackgroundMonitor.kt` (28 lines) — interface `BackgroundMonitor`, data class `MonitorSettings`, enum `MonitorSignalLevel`.
- `ANDROID/core/monitoring/BackgroundMonitorManager.kt` (73 lines).
- `ANDROID/core/monitoring/BackgroundMonitorService.kt` (341 lines). Its companion object holds the identifiers needed in Step 5:
  ```kotlin
          const val CHANNEL_ID_MONITORING = "wifiweakspot_monitoring"
          const val CHANNEL_ID_ALERTS = "wifiweakspot_alerts"
          const val PREF_KEY = "monitoring_prefs"
  ```

**Demo fixtures that implement the dead interface:**

- `DEMO/SimulatedServices.kt:16-17` — imports of `BackgroundMonitor` and `MonitorSettings`.
- `DEMO/SimulatedServices.kt:270-279` — `class SimulatedBackgroundMonitor : BackgroundMonitor { ... }`.
- `DEMO/SimulationModule.kt:6` — import; `:38` — `single<BackgroundMonitor> { SimulatedBackgroundMonitor() }`.

**Domain strings (12 keys), `SHARED/core/i18n/DomainStrings.kt:141-180`:**

```kotlin
    // region Background monitor (Android notifications)

    /** Notification channel name shown in Android settings. */
    val monitorChannelName: String
    val monitorChannelDescription: String
    val alertsChannelName: String
    val alertsChannelDescription: String
    val monitorStartFailed: String
    val monitorWaitingForScanLimited: String
    val monitorWaitingForScan: String
    fun monitorStatus(networkCount: Int, lastScanTime: String): String
    fun newNetworksTitle(count: Int): String
    fun newNetworksText(names: List<String>): String
    val signalChangeTitle: String
    fun signalChangeText(networkName: String, level: MonitorSignalLevel, rssi: Int): String

    // endregion
```

(KDoc comments between them are omitted here.) `DomainStrings.kt` and all ten
`<Lang>DomainStrings.kt` files import
`com.bernaferrari.wifiweakspot.core.monitoring.MonitorSignalLevel`. In
`en/EnDomainStrings.kt` the twelve overrides run from line 103 to about line
130; several are multi-line (`monitorStatus`, `newNetworksTitle`,
`newNetworksText` with a `when`, `signalChangeText` with a `when` over
`MonitorSignalLevel`).

**Settings strings (10 keys), `SHARED/core/i18n/SettingsStrings.kt:57-85`:**

```kotlin
    val sectionAlerts: String
    val backgroundAlertsTitle: String
    val backgroundAlertsSubtitle: String
    val notificationsDenied: String
    val checkEvery: String
    fun intervalSeconds(seconds: Int): String
    fun intervalMinutes(minutes: Int): String
    val newNetworkAlertsTitle: String
    val signalChangeAlertsTitle: String
    val signalChangeAlertsSubtitle: String
```

In `en/EnSettingsStrings.kt` these are lines 32-41, one line each.

**A test of the dead strings**, `TESTS/core/i18n/TranslationMeaningTest.kt`:
line 3 imports `MonitorSignalLevel`; lines 20-35 are the test
`signalAlertsUseTheSameScaleAsTheRestOfTheApp`.

**Notification-permission plumbing:**

- `SHARED/core/platform/Platform.kt:25-26`:
  ```kotlin
      /** Android 13+ asks before an app may post notifications. */
      fun needsNotificationPermission(): Boolean
  ```
- `SHARED/core/platform/Platform.kt:54` (inside `class PermissionLaunchers`):
  ```kotlin
      val notifications: ((granted: Boolean) -> Unit) -> Unit = { it(true) },
  ```
- `ANDROID/core/platform/AndroidPlatformActions.kt:55-57` — `override fun needsNotificationPermission()`. It is the only user of the imports `android.content.pm.PackageManager`, `android.os.Build` and `androidx.core.content.ContextCompat` in that file.
- `WEB/WebPlatform.kt:20` — `override fun needsNotificationPermission() = false`.
- `ANDROID/core/permissions/AndroidPermissionLaunchers.kt:39`:
  ```kotlin
      val notifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { pending[1]?.invoke(it) }
  ```
  and `:74-84`:
  ```kotlin
      return remember(scan, notifications, picker) {
          PermissionLaunchers(
              scan = { onResult -> /* ... */ },
              notifications = { onResult ->
                  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) onResult(true)
                  else { pending[1] = onResult; notifications.launch(Manifest.permission.POST_NOTIFICATIONS) }
              },
  ```
- `SHARED/core/permissions/PermissionGate.kt:13-18` and `:29`:
  ```kotlin
  data class PermissionState(
      val wifiScan: PermissionStatus,
      val location: PermissionStatus,
      val notifications: PermissionStatus,
      val nearbyDevices: PermissionStatus
  )
  ```
  ```kotlin
      suspend fun requestNotificationPermission(): PermissionStatus
  ```
- `ANDROID/core/permissions/AndroidPermissionGate.kt` — `notifications = PermissionStatus.NOT_REQUESTED,` (line 23); the `val notifications = if (Build.VERSION.SDK_INT >= 33) { check(Manifest.permission.POST_NOTIFICATIONS) } else { PermissionStatus.GRANTED }` block (lines 42-46); `notifications = notifications,` (line 51); `override suspend fun requestNotificationPermission()` (lines 71-74).
- `DEMO/SimulatedServices.kt:153` — `override suspend fun requestNotificationPermission() = PermissionStatus.GRANTED`; `:156-157`:
  ```kotlin
      private fun stateFor(location: PermissionStatus) =
          PermissionState(location, location, PermissionStatus.GRANTED, PermissionStatus.GRANTED)
  ```
- `TESTS/core/permissions/PermissionStateTest.kt:12` and `:22` — `notifications = PermissionStatus.DENIED,` / `notifications = PermissionStatus.GRANTED,`.

**Small leftovers:**

- `ANDROID/di/AppModule.kt:65-66` — the comment `// Background Monitoring` followed by a blank line, with nothing under it.
- `SHARED/feature/settings/SettingsScreen.kt` — the parameter `onRequestScanPermission: () -> Unit = {},` in `fun SettingsScreen(`; nothing in the function reads it any more.
- `SHARED/App.kt:206-210`:
  ```kotlin
                          SettingsScreen(
                              onNavigateToScanSetup = { open(com.bernaferrari.wifiweakspot.navigation.ScanSetupDestination) },
                              onNavigateToAbout = { open(com.bernaferrari.wifiweakspot.navigation.AboutDestination) },
                              onRequestScanPermission = onRequestScanPermission
                          )
  ```
- `ANDROID/di/AppModule.kt:73-86` — `initKoin`, where the one-time clean-up of Step 5 is called:
  ```kotlin
  fun initKoin(application: Application) {
      val koin = startKoin {
          androidContext(application)
          modules(sharedModule, appModule)
      }.koin
      kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO).launch {
          // Resume the durable Room -> DataStore handoff after an interrupted restore.
          try {
  ```

**Documents:**

- `PRIVACY.md:3` — `**Effective date: October 24, 2026**` followed by two trailing spaces (a Markdown line break).
- `PRIVACY.md:22` — `- **Preferences:** appearance, favorite networks, signal-display preferences, and background-alert settings.`
- `PRIVACY.md:24` — contains `and provide the settings or alerts you request.`
- `PRIVACY.md:26` — `## Permissions and background alerts`
- `PRIVACY.md:32` — the whole paragraph starting `If you enable background alerts, the app can continue checking nearby Wi-Fi networks through an Android foreground service and show notifications.`
- `docs/localization.md:14` — list containing `screen-reader descriptions, notifications, generated names`.
- `docs/localization.md:18` — list containing `tool verdicts, notifications, permission explanations`.
- `docs/localization.md`, last paragraph — ends `while removal of the Busy strip removed two obsolete entries. Catalogs remain complete and symmetric.`

Conventions that apply: imports at the top of the file; do not rewrite
unrelated lines; comments say why, in plain words.

## Commands you will need

Documented in `docs/DEVELOPMENT.md`; **not run** while this plan was written.

| Purpose | Command | Expected on success |
|---|---|---|
| Unit tests | `./gradlew :composeApp:testDebugUnitTest` | `BUILD SUCCESSFUL` |
| Shared code + demo fixtures compile for web | `./gradlew :webDemo:compileKotlinWasmJs` | `BUILD SUCCESSFUL` |
| Android build | `./gradlew :androidApp:assembleDebug` | `BUILD SUCCESSFUL` |
| Demo fixtures compile for Android | `./gradlew :androidApp:assembleScreenshots` | `BUILD SUCCESSFUL` |
| Lint | `./gradlew :composeApp:lintDebug :androidApp:lintDebug` | `BUILD SUCCESSFUL` |
| Text-direction check on translations | `python3 scripts/check_bidi.py` | exit 0 |

## Scope

**In scope** (the only files you should modify, create or delete):

- Delete: `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/monitoring/BackgroundMonitor.kt`
- Delete: `composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/core/monitoring/BackgroundMonitorManager.kt`
- Delete: `composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/core/monitoring/BackgroundMonitorService.kt`
- Create: `composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/core/platform/RetiredBackgroundAlerts.kt`
- `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/i18n/DomainStrings.kt` and the ten `<Lang>DomainStrings.kt` files
- `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/i18n/SettingsStrings.kt` and the ten `<Lang>SettingsStrings.kt` files
- `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/platform/Platform.kt`
- `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/permissions/PermissionGate.kt`
- `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/settings/SettingsScreen.kt`
- `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/App.kt`
- `composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/core/platform/AndroidPlatformActions.kt`
- `composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/core/permissions/AndroidPermissionLaunchers.kt`
- `composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/core/permissions/AndroidPermissionGate.kt`
- `composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/di/AppModule.kt`
- `composeApp/src/test/kotlin/com/bernaferrari/wifiweakspot/core/i18n/TranslationMeaningTest.kt`
- `composeApp/src/test/kotlin/com/bernaferrari/wifiweakspot/core/permissions/PermissionStateTest.kt`
- `demo-fixtures/src/main/kotlin/com/bernaferrari/wifiweakspot/demo/SimulatedServices.kt`
- `demo-fixtures/src/main/kotlin/com/bernaferrari/wifiweakspot/demo/SimulationModule.kt`
- `webDemo/src/wasmJsMain/kotlin/com/bernaferrari/wifiweakspot/demo/WebPlatform.kt`
- `PRIVACY.md`
- `docs/localization.md`

**Out of scope** (do NOT touch, even though they look related):

- Both `AndroidManifest.xml` files, `androidApp/build.gradle.kts`, anything under `gradle/`, `fastlane/`, `store-listing/` — the owner is editing these.
- `PlatformActions.showMessage` and its implementations — unused today, but a general-purpose helper, not part of alerts.
- `PermissionState.nearbyDevices`, `PermissionGate.requestWifiScanPermission` and the `NEARBY_WIFI_DEVICES` check — also dead-looking, but unrelated to alerts and covered by tests whose intent should be reviewed separately.
- The floor-plan picker in `AndroidPermissionLaunchers.kt` (plan 006 changes it).
- `docs/localization.md` lines 26, 34 and 60 — they record past audits; history is not rewritten.
- Any other unused code you notice. Report it; do not delete it.

## Git workflow

- Branch: `advisor/002-finish-background-alerts-removal`
- One commit per step is ideal (each step leaves the build green). This repo
  writes commit subjects as one plain sentence describing the result, for
  example (from `git log`):
  `Store captures come from capture.tour.mjs, Screenshot Studio's TypeScript tour: one run per language, captures at <profile>/<scene>.png; the two Python tours are gone`.
  Suggested subject for Step 1: `The background alert service and its twelve strings are gone from all ten languages`.
- Do NOT push or open a PR unless the operator instructed it.

## Steps

### Step 1: Delete the service, its interface, and the strings and fixtures that depend on them

These must go together: `DomainStrings` refers to `MonitorSignalLevel`, which
lives in one of the deleted files.

First confirm nothing live uses the strings:

`rg -l "monitorChannelName|monitorChannelDescription|alertsChannelName|alertsChannelDescription|monitorStartFailed|monitorWaitingForScan|monitorStatus|newNetworksTitle|newNetworksText|signalChangeTitle|signalChangeText" composeApp/src androidApp/src demo-fixtures webDemo -g '*.kt' -g '!**/core/i18n/**'`

Expected: exactly two paths —
`.../core/monitoring/BackgroundMonitorService.kt` and
`.../core/monitoring/BackgroundMonitorManager.kt`. Any other path is a STOP
condition. (The command skips every folder named `core/i18n`, which also hides
`TranslationMeaningTest.kt`; that test uses `signalChangeText` and is handled
in item 4 below.)

Then:

1. Delete the three files listed under "Dead service code".
2. In `DomainStrings.kt`, delete everything from the line
   `// region Background monitor (Android notifications)` through the matching
   `// endregion` (lines 141-180), plus one of the two blank lines left behind,
   and delete the `MonitorSignalLevel` import.
3. In each of the ten `<Lang>DomainStrings.kt` files, delete the twelve
   overrides in full (including multi-line bodies) and the `MonitorSignalLevel`
   import. If a private helper in that file was used only by the deleted
   overrides, delete the helper too.
4. In `TranslationMeaningTest.kt`, delete the import on line 3 and the whole
   test `signalAlertsUseTheSameScaleAsTheRestOfTheApp` (lines 20-35).
5. In `DEMO/SimulatedServices.kt`, delete the two imports and the whole
   `SimulatedBackgroundMonitor` class. In `DEMO/SimulationModule.kt`, delete
   the import on line 6 and the binding on line 38 (and the blank line above it).

**Verify**:
`rg -n "BackgroundMonitor|MonitorSettings|MonitorSignalLevel" composeApp androidApp demo-fixtures webDemo -g '*.kt' -g '*.xml'` → no matches; then
`./gradlew :composeApp:testDebugUnitTest :webDemo:compileKotlinWasmJs :androidApp:assembleDebug` → `BUILD SUCCESSFUL`.

### Step 2: Delete the ten Settings strings

Confirm nothing reads them (strings are always read as `….settings.<key>`):

`rg -n "settings\.(sectionAlerts|backgroundAlertsTitle|backgroundAlertsSubtitle|notificationsDenied|checkEvery|intervalSeconds|intervalMinutes|newNetworkAlertsTitle|signalChangeAlertsTitle|signalChangeAlertsSubtitle)\b" composeApp/src androidApp/src demo-fixtures webDemo -g '*.kt'`

Expected: no output. Any match is a STOP condition.

Delete the ten declarations and their KDoc comments from `SettingsStrings.kt`
(lines 57-85), and the ten overrides from each `<Lang>SettingsStrings.kt`.

Careful: `intervalSeconds` is also the name of an unrelated parameter of
`RouterProbe.ping` (in `core/router/`, its Android implementation, and the
demo fixtures). Leave those alone; only the string key in `core/i18n` goes.

**Verify**:
`rg -n "sectionAlerts|backgroundAlertsTitle|backgroundAlertsSubtitle|notificationsDenied|checkEvery|intervalSeconds|intervalMinutes|newNetworkAlertsTitle|signalChangeAlertsTitle|signalChangeAlertsSubtitle" composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/i18n`
→ no matches; then
`./gradlew :composeApp:testDebugUnitTest :webDemo:compileKotlinWasmJs` → `BUILD SUCCESSFUL`.

### Step 3: Remove the notification-permission plumbing

1. `Platform.kt`: delete `needsNotificationPermission` and its KDoc (lines 25-26); delete the `notifications` property of `PermissionLaunchers` (line 54).
2. `AndroidPlatformActions.kt`: delete `needsNotificationPermission` (lines 55-57). Then run `rg -n "PackageManager|Build\.|ContextCompat" composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/core/platform/AndroidPlatformActions.kt`; for each of the three names that no longer appears in the body, delete its import.
3. `WebPlatform.kt`: delete line 20.
4. `AndroidPermissionLaunchers.kt`: delete the `val notifications = rememberLauncherForActivityResult(...)` line; delete the `notifications = { onResult -> ... },` argument (four lines); change `remember(scan, notifications, picker)` to `remember(scan, picker)`; delete `import android.os.Build` if `Build.` no longer appears in the file. Leave `val pending = remember { arrayOfNulls<(Boolean) -> Unit>(3) }` and everything about `scan` and `picker` exactly as it is.
5. `PermissionGate.kt`: delete the `notifications` property of `PermissionState` and the `requestNotificationPermission` function of `PermissionGate`.
6. `AndroidPermissionGate.kt`: delete the four pieces listed in "Current state" (lines 23, 42-46, 51, 71-74). The `nearby` block and `requestWifiScanPermission` stay.
7. `DEMO/SimulatedServices.kt`: delete line 153; change line 157 to `PermissionState(location, location, PermissionStatus.GRANTED)`.
8. `PermissionStateTest.kt`: delete lines 12 and 22.

**Verify**:
`rg -n "POST_NOTIFICATIONS|needsNotificationPermission|requestNotificationPermission|launchers\.notifications" composeApp androidApp demo-fixtures webDemo -g '*.kt' -g '*.xml'` → no matches; then
`./gradlew :composeApp:testDebugUnitTest :webDemo:compileKotlinWasmJs :androidApp:assembleDebug` → `BUILD SUCCESSFUL`.

### Step 4: Remove the two small leftovers

1. `SettingsScreen.kt`: delete the parameter `onRequestScanPermission: () -> Unit = {},`.
2. `App.kt`: in the `SettingsScreen(` call shown in "Current state", delete the argument `onRequestScanPermission = onRequestScanPermission` and the trailing comma on the line above it. Do **not** touch the other uses of `onRequestScanPermission` in `App.kt` (the `OverviewScreen` call and `PermissionRequiredScreen`).
3. `AppModule.kt`: delete the comment line `// Background Monitoring` and the blank line after it.

**Verify**: `rg -n "onRequestScanPermission" composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/settings/SettingsScreen.kt` → no matches; `./gradlew :androidApp:assembleDebug` → `BUILD SUCCESSFUL`.

### Step 5: Clean up what the old version left on phones

Create `composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/core/platform/RetiredBackgroundAlerts.kt`:

```kotlin
package com.bernaferrari.wifiweakspot.core.platform

import android.app.NotificationManager
import android.content.Context

/**
 * Version 1 could watch Wi-Fi in the background and post alerts. The feature is gone; this removes
 * what it left on phones that had it: two notification categories in Android's settings and its
 * saved options. Safe to run every start: both calls do nothing when there is nothing to remove.
 */
fun removeRetiredBackgroundAlerts(context: Context) {
    runCatching {
        val notifications = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifications.deleteNotificationChannel("wifiweakspot_monitoring")
        notifications.deleteNotificationChannel("wifiweakspot_alerts")
        context.deleteSharedPreferences("monitoring_prefs")
    }
}
```

(The app's minimum Android version is 8.0, API 26, so both APIs are always available.)

In `AppModule.kt`, add the import
`import com.bernaferrari.wifiweakspot.core.platform.removeRetiredBackgroundAlerts`
and call it as the first statement inside the existing `launch { ... }` block of
`initKoin`, before the comment `// Resume the durable Room -> DataStore handoff ...`:

```kotlin
        removeRetiredBackgroundAlerts(application)
```

**Verify**: `rg -n "removeRetiredBackgroundAlerts" composeApp/src/androidMain` → three matches (definition, import, call); `./gradlew :androidApp:assembleDebug` → `BUILD SUCCESSFUL`.

### Step 6: Bring the documents in line

`PRIVACY.md` — make exactly these edits:

1. Line 3: replace the date with the date of the day you commit this step, in
   the same format (`**Effective date: Month D, YYYY**`), keeping the two
   trailing spaces.
2. Line 22 becomes:
   `- **Preferences:** appearance, favorite networks, and signal-display preferences.`
3. Line 24: replace `and provide the settings or alerts you request.` with
   `and provide the settings you request.`
4. Line 26 becomes: `## Permissions`
5. Delete the paragraph on line 32 (the one starting `If you enable background alerts`) and one of the blank lines around it, so exactly one blank line separates the paragraph before it from the heading `## Network requests and external services`.

`docs/localization.md`:

1. Line 14: change `screen-reader descriptions, notifications, generated names` to `screen-reader descriptions, generated names`.
2. Line 18: change `tool verdicts, notifications, permission explanations` to `tool verdicts, permission explanations`.
3. Last paragraph: replace
   `while removal of the Busy strip removed two obsolete entries. Catalogs remain complete and symmetric.`
   with
   `while removal of the Busy strip removed two obsolete entries and removal of background alerts removed twenty-two. Catalogs remain complete and symmetric.`
   (Keep the file's existing line wrapping style; lines there wrap at about 100 characters.)

**Verify**: `rg -n -i "background.alert|foreground service|alerts you request" PRIVACY.md` → no matches; `rg -n "notifications" docs/localization.md` → matches only on the two historical lines (the ones starting `The quality pass keeps 551 entries` and `Audited shared UI and accessibility text`).

### Step 7: Full verification

**Verify**:
`python3 scripts/check_bidi.py` → exit 0, and
`./gradlew :composeApp:testDebugUnitTest :composeApp:lintDebug :androidApp:lintDebug :androidApp:assembleDebug :androidApp:assembleScreenshots :webDemo:compileKotlinWasmJs` → `BUILD SUCCESSFUL`.

## Test plan

- No new tests: this plan only removes unreachable code and one test of it.
- `TranslationMeaningTest` loses one test (it tested deleted strings); its other tests must pass unchanged.
- `PermissionStateTest` keeps both tests, each with one fewer constructor argument.
- `StringsTest` walks every string of every language by reflection; it must still pass, which proves the ten catalogs still implement the trimmed interfaces completely.
- Verification: `./gradlew :composeApp:testDebugUnitTest` → all pass.

## Done criteria

ALL must hold:

- [ ] `rg -n "BackgroundMonitor|MonitorSettings|MonitorSignalLevel" composeApp androidApp demo-fixtures webDemo -g '*.kt' -g '*.xml'` returns no matches
- [ ] `rg -n "sectionAlerts|backgroundAlerts|notificationsDenied|checkEvery|intervalSeconds|intervalMinutes|newNetworkAlertsTitle|signalChangeAlerts|monitorChannel|alertsChannel|monitorStartFailed|monitorWaitingForScan|monitorStatus|newNetworksT|signalChangeT" composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/i18n` returns no matches (before this plan it matched 22 files: ten keys in each Settings file, twelve in each Domain file)
- [ ] `rg -n "intervalSeconds" composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/router/RouterProbe.kt` still returns one match (the unrelated ping parameter was not touched)
- [ ] `rg -n "POST_NOTIFICATIONS|needsNotificationPermission|requestNotificationPermission" composeApp androidApp demo-fixtures webDemo -g '*.kt' -g '*.xml'` returns no matches
- [ ] `rg -n -i "background.alert|foreground service" PRIVACY.md` returns no matches
- [ ] `test ! -d composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/monitoring && test ! -d composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/core/monitoring` exits 0
- [ ] The Step 7 commands exit 0
- [ ] `git status --short` shows no file outside the in-scope list (plus `plans/README.md`)
- [ ] `plans/README.md` status row updated

## STOP conditions

Stop and report back (do not improvise) if:

- The precondition check finds `BackgroundMonitor` in a manifest, in
  `AppModule.kt` or in `SettingsScreen.kt`.
- Either usage check (Step 1 or Step 2) lists a file other than the ones named.
  A string you were about to delete is in use.
- A translation file does not contain one of the overrides you were told to
  delete, or contains it under a different name.
- After Step 1, a compile error points at a file not in the in-scope list.
- A step's verification fails twice after a reasonable fix attempt.
- You are unsure which date to write into `PRIVACY.md`. Leave line 3
  unchanged, finish the rest, and say so in your report.

## Maintenance notes

- **Owner review needed before release:** `PRIVACY.md` is a published legal
  document. Read the diff, confirm the effective date matches the release that
  ships this change, and check that the Google Play Data safety form and store
  listing no longer mention background alerts or notifications.
- `removeRetiredBackgroundAlerts` can be deleted once no installs of version 1
  remain (for example after two or three releases).
- Left for a separate decision: `PermissionState.nearbyDevices`,
  `PermissionGate.requestWifiScanPermission`, `PlatformActions.showMessage`.
  All three look unused.
- Plan 006 edits `AndroidPermissionLaunchers.kt` and `Platform.kt` too. Run it
  after this plan.
- Reviewer: check each translation file's diff removes exactly the same set of
  keys (22 per language across two files), and nothing else.
