# Plan 003: Scan results never list the same access point twice

> **Executor instructions**: Follow this plan step by step. Run every
> verification command and confirm the expected result before moving to the
> next step. If anything in the "STOP conditions" section occurs, stop and
> report — do not improvise. When done, update the status row for this plan
> in `plans/README.md` — unless a reviewer dispatched you and told you they
> maintain the index.
>
> **Drift check (run first)**:
> `git diff --stat 918ea41c..HEAD -- composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/wifi/WifiRadio.kt composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/core/wifi/AndroidWifiScanner.kt composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/analyze/SettledOrder.kt composeApp/src/test/kotlin/com/bernaferrari/wifiweakspot/core/wifi/ScanFreshnessTest.kt composeApp/src/test/kotlin/com/bernaferrari/wifiweakspot/feature/analyze/SettledOrderTest.kt`
> If any in-scope file changed since this plan was written, compare the
> "Current state" excerpts against the live code before proceeding; on a
> mismatch, treat it as a STOP condition.

## Status

- **Priority**: P1
- **Effort**: S
- **Risk**: LOW
- **Depends on**: none
- **Category**: bug
- **Planned at**: commit `918ea41c` plus the owner's uncommitted working tree, 2026-11-10

## Why this matters

The Networks list uses each access point's hardware address (BSSID) as its row
key. Compose throws `IllegalArgumentException: Key ... was already used` when
two rows share a key, which crashes the app's main tab, and it would crash
again on every later scan that returns the same pair. Android can return one
BSSID twice: the usual case is a hidden network the phone knows, seen once
without a name (from its beacon) and once with it (from a probe reply).
Nothing between the scanner and the list removes such pairs today. The map
screen's network picker already guards itself with `distinctBy { it.bssid }`,
which suggests the author has met this before. After this plan the scanner
hands out one entry per BSSID, and the ordering helper can no longer emit a
duplicate even if a caller passes one in.

This could not be reproduced during planning (no device was used). The fix is
cheap and safe whether or not a given phone ever returns duplicates.

## Current state

Path shorthand used below (always type the full path in commands):

- `SHARED` = `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot`
- `ANDROID` = `composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot`
- `TESTS` = `composeApp/src/test/kotlin/com/bernaferrari/wifiweakspot`

About the repo: `SHARED` is platform-free code, also compiled to Kotlin/Wasm for
a web demo, so it may not use `android.*` or `java.*`. `ANDROID` holds the
Android implementations. Unit tests are plain JUnit 4 on the JVM.

Files:

- `ANDROID/core/wifi/AndroidWifiScanner.kt` — reads Android's scan results; no de-duplication.
- `SHARED/core/wifi/WifiRadio.kt` — pure helpers for scan results (`freshScanResults` lives here). The new helper goes here.
- `SHARED/core/wifi/WifiNetwork.kt` — the `WifiNetwork` data class.
- `SHARED/feature/analyze/SettledOrder.kt` — keeps list rows in stable order; lets duplicate newcomers through.
- `SHARED/feature/analyze/AnalyzeScreen.kt:433` — `itemsIndexed(visible, key = { _, n -> n.bssid })`, the row key that crashes on a duplicate. **Read only; do not edit.**

`AndroidWifiScanner.kt:147-155`:

```kotlin
    private fun readCurrentResults(): List<WifiNetwork> {
        return try {
            wifiManager.scanResults.map { it.toWifiNetwork() }
        } catch (_: SecurityException) {
            emptyList()
        } catch (_: IllegalStateException) {
            emptyList()
        }
    }
```

`WifiNetwork` (`WifiNetwork.kt:12-26`), the fields that matter here:

```kotlin
data class WifiNetwork(
    val ssid: String,
    val bssid: String,
    val rssi: Int,
    val frequency: Int,
    val channelWidth: Int,
    val security: String,
    val capabilities: String,
    val timestamp: Long,
    val centerFreq0: Int = 0,
    val centerFreq1: Int = 0,
    val is80Plus80: Boolean = false,
    /** Platform observation time since boot, used to distinguish fresh scans from cache. */
    val observedElapsedRealtimeMicros: Long = 0
)
```

`freshScanResults` in `WifiRadio.kt:29-40` runs *after* `readCurrentResults()`
and drops entries observed before the scan was requested. That is why the
de-duplication below keeps the **most recently observed** entry of a pair:
keeping the older one could make a live access point vanish from fresh results.

`SettledOrder.kt:8-36` (whole function):

```kotlin
internal fun <T> settledOrder(
    previous: List<String>,
    items: List<T>,
    id: (T) -> String,
    strength: (T) -> Int,
    pinned: (T) -> Boolean = { false },
    margin: Int = 6
): List<T> {
    val byId = items.associateBy(id)
    val known = previous.mapNotNull { byId[it] }
    val seen = known.map(id).toSet()
    // Newcomers start in signal order behind the rows already on screen, then bubble up if strong.
    val list = (known + items.filter { id(it) !in seen }.sortedByDescending(strength)).toMutableList()
    var changed = true
    while (changed) {
        // ... adjacent swaps, unchanged by this plan ...
    }
    return list
}
```

`known` is already unique (it goes through `byId`), but
`items.filter { id(it) !in seen }` passes two newcomers with the same id
straight through.

Test exemplars:

- `TESTS/core/wifi/ScanFreshnessTest.kt` builds networks with a helper:
  ```kotlin
  private fun network(observedMicros: Long) = WifiNetwork(
      "Network", "00:11:22:33:44:55", -50, 5180, 20, "WPA2", "", 1000,
      observedElapsedRealtimeMicros = observedMicros
  )
  ```
- `TESTS/feature/analyze/SettledOrderTest.kt` uses
  `private data class N(val id: String, val rssi: Int, val mine: Boolean = false)` and
  `private fun order(previous: List<String>, vararg items: N) = settledOrder(previous, items.toList(), { it.id }, { it.rssi }, { it.mine }).map { it.id }`.

Conventions: imports at the top of the file; KDoc comments in plain words that
say why; test names are sentences in camelCase (`smallWobblesKeepRowsInPlace`).

## Commands you will need

Documented in `docs/DEVELOPMENT.md`; **not run** while this plan was written.

| Purpose | Command | Expected on success |
|---|---|---|
| Two test classes | `./gradlew :composeApp:testDebugUnitTest --tests "com.bernaferrari.wifiweakspot.core.wifi.ScanFreshnessTest" --tests "com.bernaferrari.wifiweakspot.feature.analyze.SettledOrderTest"` | `BUILD SUCCESSFUL` |
| All unit tests | `./gradlew :composeApp:testDebugUnitTest` | `BUILD SUCCESSFUL` |
| Shared code still compiles for web | `./gradlew :webDemo:compileKotlinWasmJs` | `BUILD SUCCESSFUL` |
| Android build | `./gradlew :androidApp:assembleDebug` | `BUILD SUCCESSFUL` |

## Scope

**In scope** (the only files you should modify):

- `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/wifi/WifiRadio.kt`
- `composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/core/wifi/AndroidWifiScanner.kt`
- `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/analyze/SettledOrder.kt`
- `composeApp/src/test/kotlin/com/bernaferrari/wifiweakspot/core/wifi/ScanFreshnessTest.kt`
- `composeApp/src/test/kotlin/com/bernaferrari/wifiweakspot/feature/analyze/SettledOrderTest.kt`

**Out of scope** (do NOT touch, even though they look related):

- `feature/analyze/AnalyzeScreen.kt`, `feature/analyze/ChartExplorer.kt`,
  `feature/survey/SurveyScreen.kt` — the list keys are right once the data is
  unique. Do not add `distinctBy` at each call site.
- `demo-fixtures/` — the simulated scanner never produces duplicates.
- The swap loop inside `settledOrder` and its `pinned` parameter (plan 008
  changes that parameter; leave it alone here).

## Git workflow

- Branch: `advisor/003-one-row-per-access-point`
- One commit. This repo writes commit subjects as one plain sentence describing
  the result. Suggested: `A hidden network seen twice in one scan is one row, so the Networks list can't crash on a repeated address`.
- Do NOT push or open a PR unless the operator instructed it.

## Steps

### Step 1: Add `distinctAccessPoints` to `WifiRadio.kt`

Directly below `freshScanResults`, add:

```kotlin
/**
 * One entry per access point. Android can report the same address twice, usually a hidden network
 * seen once without its name and once with it. The most recent sighting wins, so a fresh scan
 * keeps it, and it borrows the name when only the older sighting had one.
 */
fun distinctAccessPoints(results: List<WifiNetwork>): List<WifiNetwork> =
    results.groupBy { it.bssid.lowercase() }.values.map { sightings ->
        val latest = sightings.maxBy { it.observedElapsedRealtimeMicros }
        if (latest.ssid.isNotBlank()) latest
        else sightings.firstOrNull { it.ssid.isNotBlank() }?.let { latest.copy(ssid = it.ssid) } ?: latest
    }
```

`groupBy` keeps first-seen order, so a list without duplicates comes back
unchanged and in the same order.

Add these tests to `ScanFreshnessTest.kt` (reuse its `network(...)` helper and
`.copy(...)`):

| Test name | Input | Expected |
|---|---|---|
| `listWithoutRepeatsIsUnchanged` | two networks with different `bssid` | the same list, same order |
| `repeatedAddressKeepsTheLatestSighting` | `network(100)` and `network(200).copy(rssi = -70)` (same `bssid`) | a single entry with `observedElapsedRealtimeMicros == 200` and `rssi == -70` |
| `hiddenSightingBorrowsTheNameSeenEarlier` | `network(100)` (ssid `"Network"`) and `network(200).copy(ssid = "")` | a single entry with `ssid == "Network"` and `observedElapsedRealtimeMicros == 200` |
| `addressCaseDoesNotSplitAnAccessPoint` | `network(100).copy(bssid = "AA:BB:CC:DD:EE:FF")` and `network(200).copy(bssid = "aa:bb:cc:dd:ee:ff")` | `size == 1` |

**Verify**: `./gradlew :composeApp:testDebugUnitTest --tests "com.bernaferrari.wifiweakspot.core.wifi.ScanFreshnessTest"` → `BUILD SUCCESSFUL`, 4 new tests pass.

### Step 2: Use it in the Android scanner

In `AndroidWifiScanner.kt`, change the one line inside `readCurrentResults()`:

```kotlin
            distinctAccessPoints(wifiManager.scanResults.map { it.toWifiNetwork() })
```

`distinctAccessPoints` is in the same package (`core.wifi`), so no import is needed.

**Verify**: `rg -n "distinctAccessPoints" composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/core/wifi/AndroidWifiScanner.kt` → exactly one match, inside `readCurrentResults`.

### Step 3: Make `settledOrder` unable to emit a duplicate

In `SettledOrder.kt`, change only the three lines that build `known`, `seen`
and `list`:

```kotlin
    val byId = items.associateBy(id)
    val known = previous.distinct().mapNotNull { byId[it] }
    val seen = known.map(id).toSet()
    // Newcomers start in signal order behind the rows already on screen, then bubble up if strong.
    // They come from byId, so an id that arrives twice still makes one row.
    val list = (known + byId.values.filter { id(it) !in seen }.sortedByDescending(strength)).toMutableList()
```

Add these tests to `SettledOrderTest.kt`:

```kotlin
    @Test fun aRepeatedNewcomerMakesOneRow() =
        assertEquals(listOf("a", "b"), order(emptyList(), N("a", -70), N("b", -60), N("a", -50)))

    @Test fun aRepeatedPreviousIdMakesOneRow() =
        assertEquals(listOf("a", "b"), order(listOf("a", "a", "b"), N("a", -60), N("b", -61)))
```

(In the first test the later `a` at -50 replaces the earlier one and then
outranks `b` at -60.)

**Verify**: `./gradlew :composeApp:testDebugUnitTest --tests "com.bernaferrari.wifiweakspot.feature.analyze.SettledOrderTest"` → `BUILD SUCCESSFUL`; the 4 existing tests and 2 new tests pass.

### Step 4: Full verification

**Verify**: `./gradlew :composeApp:testDebugUnitTest :webDemo:compileKotlinWasmJs :androidApp:assembleDebug` → `BUILD SUCCESSFUL`.

## Test plan

- `ScanFreshnessTest.kt`: 4 new tests for `distinctAccessPoints` (Step 1 table).
- `SettledOrderTest.kt`: 2 new tests (Step 3). The 4 existing tests must pass unchanged.
- Verification: `./gradlew :composeApp:testDebugUnitTest` → all pass.

## Done criteria

ALL must hold:

- [ ] `./gradlew :composeApp:testDebugUnitTest` exits 0, including 6 new tests
- [ ] `./gradlew :webDemo:compileKotlinWasmJs :androidApp:assembleDebug` exits 0
- [ ] `rg -n "fun distinctAccessPoints" composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/wifi/WifiRadio.kt` returns one match
- [ ] `rg -n "wifiManager.scanResults.map" composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/core/wifi/AndroidWifiScanner.kt` shows the call wrapped in `distinctAccessPoints(`
- [ ] `git status --short` lists only the five in-scope files (plus `plans/README.md`)
- [ ] `plans/README.md` status row updated

## STOP conditions

Stop and report back (do not improvise) if:

- `readCurrentResults()` or `settledOrder` do not match the excerpts.
- An existing test in `SettledOrderTest`, `ScanFreshnessTest` or
  `AndroidWifiScannerTest` fails after your change. Do not edit an existing
  assertion to make it pass.
- A step's verification fails twice after a reasonable fix attempt.
- The fix appears to need a change in a screen file.

## Maintenance notes

- The scanner is now the single place that guarantees unique BSSIDs. Any new
  source of `WifiNetwork` lists (for example a future passive-scan path) must
  go through `distinctAccessPoints` too, because several lists key rows by BSSID.
- `SurveyScreen.kt:289` still has its own `distinctBy { it.bssid }`. It is now
  redundant but harmless; removing it is optional clean-up.
- Reviewer: check that `freshScanResults` still receives de-duplicated input
  on both paths in the broadcast receiver (both call `readCurrentResults()`).
- Plan 008 changes `settledOrder`'s `pinned` parameter; it expects this plan to
  have landed first.
