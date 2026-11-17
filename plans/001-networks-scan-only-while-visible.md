# Plan 001: The Networks screen scans only while the app is visible

> **Executor instructions**: Follow this plan step by step. Run every
> verification command and confirm the expected result before moving to the
> next step. If anything in the "STOP conditions" section occurs, stop and
> report — do not improvise. When done, update the status row for this plan
> in `plans/README.md` — unless a reviewer dispatched you and told you they
> maintain the index.
>
> **Drift check (run first)**:
> `git diff --stat 918ea41c..HEAD -- composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/analyze/AnalyzeScreen.kt composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/settings/SettingsScreen.kt composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/tools/ToolsScreen.kt`
> If any in-scope file changed since this plan was written, compare the
> "Current state" excerpts against the live code before proceeding; on a
> mismatch, treat it as a STOP condition. One change is expected and is not a
> mismatch: `SettingsScreen.kt` lost about 93 lines (a removed "Alerts"
> section), so find its excerpt by content, not by line number.

## Status

- **Priority**: P1
- **Effort**: S
- **Risk**: LOW
- **Depends on**: none
- **Category**: bug
- **Planned at**: commit `918ea41c` plus the owner's uncommitted working tree, 2026-11-10

## Why this matters

The Networks screen refreshes its list in a loop. The loop is tied to the
screen being *composed*, not to the app being *visible*, so it keeps running
after the user presses Home or turns the screen off with Networks open. For
most users Android refuses those scans, but the app's own "Faster scans" guide
tells people to switch Android's scan throttling off, and rooted phones can
scan through the system shell. For those users the app asks for a real Wi-Fi
scan every 4 seconds while nobody is looking, which costs battery and is
exactly the background activity the app says it does not do. Two smaller
network-status pollers have the same problem. After this plan, all three stop
when the app is not visible and resume when it returns.

## Current state

Path shorthand used below (always type the full path in commands):

- `SHARED` = `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot`
- `TESTS` = `composeApp/src/test/kotlin/com/bernaferrari/wifiweakspot`

About the repo: `SHARED` is platform-free Kotlin + Compose code. It is compiled
for Android **and** for a Kotlin/Wasm web demo (`:webDemo`), so it may not use
`android.*` or `java.*` APIs. Lifecycle APIs already used in `SHARED` (and
therefore known to compile on both targets) are
`androidx.lifecycle.Lifecycle`, `androidx.lifecycle.LifecycleEventObserver`,
`androidx.lifecycle.compose.LocalLifecycleOwner` and
`androidx.lifecycle.compose.collectAsStateWithLifecycle`. Use only those.

Files:

- `SHARED/feature/analyze/AnalyzeScreen.kt` — the Networks screen; contains the scan loop.
- `SHARED/feature/settings/SettingsScreen.kt` — Settings; polls network status for a preview badge.
- `SHARED/feature/tools/ToolsScreen.kt` — Tools; polls network status every 10 s for a "your router" suggestion.
- `SHARED/core/wifi/RadioAvailability.kt` — exemplar of the lifecycle-observer pattern to copy.

The scan loop, `AnalyzeScreen.kt:137-142` and `:164-171`:

```kotlin
/** Unthrottled: near-live. Throttled: one scan every 30 s stays inside 4 per 2 minutes. */
private fun throttleIntervalMs(state: ThrottleState): Long = when (state) {
    ThrottleState.Unlimited -> 4_000L
    is ThrottleState.Exhausted -> (state.secondsUntilNextScan + 1) * 1_000L
    else -> 30_000L
}
```

```kotlin
        // Refresh while the screen is visible, paced to what Android allows.
        LaunchedEffect(canScan) {
            if (!canScan) return@LaunchedEffect
            while (isActive) {
                if (throttleManager.throttleState.value !is ThrottleState.Exhausted) viewModel.startScan()
                delay(throttleIntervalMs(throttleManager.throttleState.value))
            }
        }
```

`canScan` is declared two lines above the loop:
`val canScan = permissions.canScanWifi && radios.wifiEnabled && radios.locationEnabled`.

The Settings poller (one line, find it by content):

```kotlin
    val networkStatus by remember(networkStatusRepository) { networkStatusRepository.getCurrentNetworkStatus() }.collectAsState(initial = null)
```

The Tools poller, `ToolsScreen.kt:234-235`:

```kotlin
    val status by remember(statusRepository) { statusRepository.getCurrentNetworkStatus(refreshIntervalMs = 10_000) }
        .collectAsState(initial = com.bernaferrari.wifiweakspot.core.model.NetworkStatus.Disconnected)
```

The lifecycle-observer pattern already in the repo, `RadioAvailability.kt:22-34`:

```kotlin
@Composable
fun rememberRadioState(monitor: RadioMonitor = koinInject()): RadioState {
    val lifecycleOwner = LocalLifecycleOwner.current
    var state by remember(monitor) { mutableStateOf(monitor.read()) }
    DisposableEffect(lifecycleOwner, monitor) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) state = monitor.read()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return state
}
```

Conventions that apply:

- Put imports at the top of the file. Much of this codebase writes
  fully-qualified names inline; do **not** copy that habit in new lines, and do
  not rewrite existing lines to change it.
- The repo keeps small remembered scratch values in arrays, for example
  `remember { longArrayOf(0L) }`. Match that.
- Comments explain *why*, in plain words. Match the tone of the comment above
  the loop.
- Wall-clock time in shared code comes from
  `com.bernaferrari.wifiweakspot.core.util.nowMillis()`.

## Commands you will need

These are the repo's documented commands (`docs/DEVELOPMENT.md`). They were
**not run** while this plan was written (planning was read-only).

| Purpose | Command | Expected on success |
|---|---|---|
| One test class | `./gradlew :composeApp:testDebugUnitTest --tests "com.bernaferrari.wifiweakspot.feature.analyze.ScanPacingTest"` | `BUILD SUCCESSFUL`, tests pass |
| All unit tests | `./gradlew :composeApp:testDebugUnitTest` | `BUILD SUCCESSFUL` |
| Shared code still compiles for web | `./gradlew :webDemo:compileKotlinWasmJs` | `BUILD SUCCESSFUL` |
| Android build | `./gradlew :androidApp:assembleDebug` | `BUILD SUCCESSFUL` |
| Lint | `./gradlew :composeApp:lintDebug :androidApp:lintDebug` | `BUILD SUCCESSFUL` |

## Scope

**In scope** (the only files you should modify):

- `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/analyze/AnalyzeScreen.kt`
- `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/settings/SettingsScreen.kt`
- `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/tools/ToolsScreen.kt`
- `composeApp/src/test/kotlin/com/bernaferrari/wifiweakspot/feature/analyze/ScanPacingTest.kt` (create)

**Out of scope** (do NOT touch, even though they look related):

- `feature/lan/LanViewModel.kt` and `feature/lan/LanDeviceDetailViewModel.kt` —
  both keep a status poller alive with `SharingStarted.Eagerly`. Their own
  `init` blocks collect that flow, so switching to `WhileSubscribed` would not
  stop it; fixing them needs a redesign that is not part of this plan.
- `AnalyzeViewModel.startScan()` and anything under `core/wifi/` — the pacing
  rules and the scanner are correct; only *when* the loop runs is wrong.
- The other `collectAsState` calls in `ToolsScreen.kt` (lines 85 and 239). They
  read a view-model state and a database flow, not a poller.
- Any reformatting or import clean-up of existing lines.

## Git workflow

- Branch: `advisor/001-networks-scan-only-while-visible`
- One commit is enough. This repo writes commit subjects as one plain sentence
  describing the result, for example (from `git log`):
  `Networks charts name their axes, Signal up the side, Channel or Now along the bottom; the card keeps only its tools and the list is Nearby networks`.
  Suggested subject: `Networks stops scanning when the app is not visible, and picks up its pace on return`.
- Do NOT push or open a PR unless the operator instructed it.

## Steps

### Step 1: Add the pacing helper and its test

In `AnalyzeScreen.kt`, directly below `throttleIntervalMs`, add:

```kotlin
/** How long to wait before the first scan after coming back, so a quick return doesn't spend an extra scan. */
internal fun resumeDelayMs(intervalMs: Long, lastScanAt: Long, now: Long): Long {
    // Never scanned from this screen, or the clock moved backwards: scan right away.
    if (lastScanAt <= 0L || now < lastScanAt) return 0L
    return (intervalMs - (now - lastScanAt)).coerceIn(0L, intervalMs)
}
```

Create `composeApp/src/test/kotlin/com/bernaferrari/wifiweakspot/feature/analyze/ScanPacingTest.kt`,
modelled on `SettledOrderTest.kt` in the same folder (plain JUnit 4,
`org.junit.Test`, `org.junit.Assert.assertEquals`, expression-bodied tests with
sentence-style names). Cover exactly these cases:

| Test name | Call | Expected |
|---|---|---|
| `firstScanIsImmediate` | `resumeDelayMs(30_000, 0, 500_000)` | `0` |
| `quickReturnWaitsOutTheRestOfTheInterval` | `resumeDelayMs(30_000, 100_000, 110_000)` | `20_000` |
| `longAbsenceScansRightAway` | `resumeDelayMs(30_000, 100_000, 400_000)` | `0` |
| `clockSetBackScansRightAway` | `resumeDelayMs(30_000, 100_000, 90_000)` | `0` |

**Verify**: `./gradlew :composeApp:testDebugUnitTest --tests "com.bernaferrari.wifiweakspot.feature.analyze.ScanPacingTest"` → `BUILD SUCCESSFUL`, 4 tests pass.

### Step 2: Add a "screen is visible" value to `AnalyzeScreen.kt`

Add these imports to the import block at the top of `AnalyzeScreen.kt` (skip any
that already exist):

```kotlin
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bernaferrari.wifiweakspot.core.util.nowMillis
```

Add this private composable directly above `fun AnalyzeScreen(`:

```kotlin
/** True while the app is on screen. Composed-but-stopped (Home pressed, screen off) counts as not visible. */
@Composable
private fun rememberScreenVisible(): Boolean {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var visible by remember(lifecycle) { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> visible = true
                Lifecycle.Event.ON_STOP -> visible = false
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    return visible
}
```

**Verify**: `./gradlew :webDemo:compileKotlinWasmJs` → `BUILD SUCCESSFUL`.

### Step 3: Make the scan loop follow visibility

Replace the loop shown in "Current state" (`AnalyzeScreen.kt:164-171`) with:

```kotlin
        val visible = rememberScreenVisible()
        // When this screen last asked for a scan, so coming back to the app continues the pace.
        val lastScanAt = remember { longArrayOf(0L) }

        // Refresh only while the app is on screen, paced to what Android allows. With throttling off
        // or with root, a loop that outlives the screen would be real background scanning.
        LaunchedEffect(canScan, visible) {
            if (!canScan || !visible) return@LaunchedEffect
            delay(resumeDelayMs(throttleIntervalMs(throttleManager.throttleState.value), lastScanAt[0], nowMillis()))
            while (isActive) {
                if (throttleManager.throttleState.value !is ThrottleState.Exhausted) {
                    viewModel.startScan()
                    lastScanAt[0] = nowMillis()
                }
                delay(throttleIntervalMs(throttleManager.throttleState.value))
            }
        }
```

Do not change anything else in the function.

**Verify**:
`rg -n "LaunchedEffect\(canScan\)" composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/analyze/AnalyzeScreen.kt` → no matches, and
`rg -c "LaunchedEffect\(canScan, visible\)" composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/analyze/AnalyzeScreen.kt` → `1`.

### Step 4: Stop the two status pollers when the app is not visible

In `SettingsScreen.kt`, change the line shown in "Current state" so it ends with
`.collectAsStateWithLifecycle(initialValue = null)` instead of
`.collectAsState(initial = null)`, and add
`import androidx.lifecycle.compose.collectAsStateWithLifecycle` to the imports.

In `ToolsScreen.kt:234-235`, change `.collectAsState(initial = ...)` to
`.collectAsStateWithLifecycle(initialValue = ...)` (keep the same value), and add
the same import. Leave `import androidx.compose.runtime.collectAsState` in place;
lines 85 and 239 still use it.

**Verify**:
`rg -n "getCurrentNetworkStatus" composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/settings/SettingsScreen.kt composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/tools/ToolsScreen.kt`
shows two call sites, and
`rg -n "collectAsState\(initial = (null|com\.bernaferrari\.wifiweakspot\.core\.model\.NetworkStatus\.Disconnected)\)" composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/settings/SettingsScreen.kt composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/tools/ToolsScreen.kt`
→ no matches.

### Step 5: Full verification

**Verify**: `./gradlew :composeApp:testDebugUnitTest :webDemo:compileKotlinWasmJs :androidApp:assembleDebug :composeApp:lintDebug :androidApp:lintDebug` → `BUILD SUCCESSFUL`.

## Test plan

- New file `ScanPacingTest.kt` with the four cases in Step 1. Structural
  pattern: `SettledOrderTest.kt` in the same folder.
- The visibility wiring itself is Compose + lifecycle and has no unit-test
  harness in this repo (there are no Compose UI tests). It is covered by the
  compile checks and the manual check under "Maintenance notes".
- Verification: `./gradlew :composeApp:testDebugUnitTest` → all pass, including 4 new tests.

## Done criteria

ALL must hold:

- [ ] `./gradlew :composeApp:testDebugUnitTest` exits 0; `ScanPacingTest` exists with 4 passing tests
- [ ] `./gradlew :webDemo:compileKotlinWasmJs :androidApp:assembleDebug` exits 0
- [ ] `rg -n "LaunchedEffect\(canScan\)" composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/analyze/AnalyzeScreen.kt` returns no matches
- [ ] `rg -c "rememberScreenVisible" composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/analyze/AnalyzeScreen.kt` returns `2`
- [ ] `git status --short` lists only the four in-scope files (plus `plans/README.md`)
- [ ] `plans/README.md` status row updated

## STOP conditions

Stop and report back (do not improvise) if:

- The loop or either poller does not match the excerpts in "Current state".
- `lifecycle.currentState` or any of the four lifecycle imports does not
  resolve for the Wasm target in Step 2. Do **not** reach for a different
  lifecycle API or add a dependency; report the compiler error.
- A step's verification fails twice after a reasonable fix attempt.
- Making this work appears to require editing a view model or anything under
  `core/`.

## Maintenance notes

- Manual check for a reviewer with a device: turn Android's "Wi-Fi scan
  throttling" off in Developer options, open Networks, press Home, wait a
  minute, return. Before this change the list has been refreshing the whole
  time; after it, the list refreshes within 4 seconds of returning and not
  while away. With throttling on, returning within 30 seconds must not spend
  an extra scan from the quota.
- Switching tabs inside the app still triggers an immediate scan on return to
  Networks (the remembered timestamp does not outlive the tab). That matches
  today's behaviour and is intentional.
- Deferred: the always-on status pollers in `LanViewModel` and
  `LanDeviceDetailViewModel` (see "Out of scope").
- If the scan loop ever moves into `AnalyzeViewModel`, visibility must travel
  with it; a view model outlives a stopped screen.
