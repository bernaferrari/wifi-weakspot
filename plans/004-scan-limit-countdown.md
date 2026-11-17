# Plan 004: The scan-limit countdown ticks calmly, and its tests exercise the real rules

> **Executor instructions**: Follow this plan step by step. Run every
> verification command and confirm the expected result before moving to the
> next step. If anything in the "STOP conditions" section occurs, stop and
> report — do not improvise. When done, update the status row for this plan
> in `plans/README.md` — unless a reviewer dispatched you and told you they
> maintain the index.
>
> **Drift check (run first)**:
> `git diff --stat 918ea41c..HEAD -- composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/core/wifi/AndroidScanThrottleManager.kt composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/wifi/ScanThrottleManager.kt composeApp/src/test/kotlin/com/bernaferrari/wifiweakspot/core/wifi/ScanThrottleManagerTest.kt`
> If any in-scope file changed since this plan was written, compare the
> "Current state" excerpts against the live code before proceeding; on a
> mismatch, treat it as a STOP condition.

## Status

- **Priority**: P2
- **Effort**: S
- **Risk**: LOW
- **Depends on**: none
- **Category**: bug
- **Planned at**: commit `918ea41c` plus the owner's uncommitted working tree, 2026-11-10

## Why this matters

Android lets a foreground app start four Wi-Fi scans in any two minutes. The
app tracks that quota and shows a countdown when it runs out. In the final
second of every countdown the code computes "0 seconds left" by rounding down,
tries to free the oldest scan, finds it has not quite expired, sees the quota
is still used up, and restarts the countdown with no pause. For up to a second
it loops as fast as the CPU allows; every pass rewrites a preferences file and
makes two system calls. It happens each time the quota runs out, which for a
user on the default settings is routine. Separately, the eight unit tests named
after this class test a private copy of the logic written inside the test
file, so they would stay green if the real class broke. After this plan the
rules live in one pure function used by both the Android class and the tests,
the countdown can never report zero while still blocked, and it always pauses
between ticks.

## Current state

Path shorthand used below (always type the full path in commands):

- `SHARED` = `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot`
- `ANDROID` = `composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot`
- `TESTS` = `composeApp/src/test/kotlin/com/bernaferrari/wifiweakspot`

About the repo: `SHARED` is platform-free code, also compiled to Kotlin/Wasm
for a web demo, so it may not use `android.*` or `java.*`. `ANDROID` holds the
Android implementations. Unit tests are plain JUnit 4 on the JVM.

Files:

- `ANDROID/core/wifi/AndroidScanThrottleManager.kt` — the real quota tracker (239 lines).
- `SHARED/core/wifi/ScanThrottleManager.kt` — the interface and the `ThrottleState` types.
- `SHARED/core/wifi/WifiRadio.kt` — exemplar of a file of small public pure functions with KDoc (`freshScanResults`).
- `TESTS/core/wifi/ScanThrottleManagerTest.kt` — eight tests against a private copy.

`ThrottleState`, `ScanThrottleManager.kt:49-67`:

```kotlin
sealed interface ThrottleState {
    data class Ready(val scansRemaining: Int, val maxScans: Int = 4) : ThrottleState
    data class LowQuota(val scansRemaining: Int, val maxScans: Int = 4) : ThrottleState
    data class Exhausted(
        val secondsUntilNextScan: Long,
        val cooldownDurationSeconds: Long = 120
    ) : ThrottleState
    data object Unlimited : ThrottleState
    data object Unknown : ThrottleState
}
```

Constants, `AndroidScanThrottleManager.kt:35-42`:

```kotlin
    companion object {
        private const val MAX_SCANS = 4
        private const val WINDOW_MS = 2 * 60 * 1000L // 2 minutes
        private const val WIFI_SCAN_THROTTLE_ENABLED = "wifi_scan_throttle_enabled"
        /** Held only by privileged system apps; those scans are never throttled. */
        private const val PREFS_NAME = "wifi_throttle_prefs"
        private const val KEY_SCAN_TIMESTAMPS = "scan_timestamps"
    }
```

Pruning, `:93-96`. Note the strict `>`: a scan exactly two minutes old is kept.

```kotlin
    private fun pruneOldTimestampsInternal(now: Long) {
        // Internal method - must be called while holding mutex
        scanTimestamps.removeAll { now - it > WINDOW_MS }
    }
```

Two small helpers, `:98-108`, used only by `updateState`:

```kotlin
    private suspend fun getTimestampCount(): Int {
        return mutex.withLock {
            scanTimestamps.size
        }
    }

    private suspend fun getOldestTimestamp(): Long? {
        return mutex.withLock {
            scanTimestamps.minOrNull()
        }
    }
```

`updateState`, `:110-152`:

```kotlin
    private suspend fun updateState() {
        _setupStatus.value = readSetupStatus()
        // Check if throttling is disabled first
        if (checkThrottlingDisabled()) {
            _throttleState.value = ThrottleState.Unlimited
            countdownJob?.cancel()
            return
        }

        val timestampCount = getTimestampCount()
        val remaining = MAX_SCANS - timestampCount

        when {
            remaining >= 3 -> {
                _throttleState.value = ThrottleState.Ready(remaining, MAX_SCANS)
                countdownJob?.cancel()
            }
            remaining in 1..2 -> {
                _throttleState.value = ThrottleState.LowQuota(remaining, MAX_SCANS)
                countdownJob?.cancel()
            }
            remaining <= 0 -> {
                // Calculate when the oldest scan will expire
                val oldestScan = getOldestTimestamp()
                if (oldestScan == null) {
                    _throttleState.value = ThrottleState.Ready(MAX_SCANS, MAX_SCANS)
                    return
                }

                val expiresAt = oldestScan + WINDOW_MS
                val now = System.currentTimeMillis()
                val secondsRemaining = ((expiresAt - now) / 1000).coerceAtLeast(0)

                _throttleState.value = ThrottleState.Exhausted(
                    secondsUntilNextScan = secondsRemaining,
                    cooldownDurationSeconds = WINDOW_MS / 1000
                )

                // Start countdown
                startCountdown(expiresAt)
            }
        }
    }
```

`startCountdown`, `:154-179`. This is the spin: when `expiresAt - now` is
between 1 and 999 ms, `secondsRemaining` is 0, the prune removes nothing,
`updateState()` sees four scans and calls `startCountdown` again immediately.

```kotlin
    private fun startCountdown(expiresAt: Long) {
        countdownJob?.cancel()
        countdownJob = scope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val secondsRemaining = ((expiresAt - now) / 1000).coerceAtLeast(0)

                if (secondsRemaining <= 0) {
                    // Quota restored!
                    mutex.withLock {
                        pruneOldTimestampsInternal(System.currentTimeMillis())
                        persistTimestamps()
                    }
                    updateState()
                    break
                }

                _throttleState.value = ThrottleState.Exhausted(
                    secondsUntilNextScan = secondsRemaining,
                    cooldownDurationSeconds = WINDOW_MS / 1000
                )

                delay(1000) // Update every second
            }
        }
    }
```

Restoring saved timestamps, `:229-238`:

```kotlin
    private fun restoreTimestamps() {
        val timestampString = prefs.getString(KEY_SCAN_TIMESTAMPS, "") ?: ""
        if (timestampString.isNotEmpty()) {
            val now = System.currentTimeMillis()
            timestampString.split(",")
                .mapNotNull { it.toLongOrNull() }
                .filter { now - it <= WINDOW_MS } // Only restore valid timestamps
                .forEach { scanTimestamps.add(it) }
        }
    }
```

Other members you will rely on and must not change: `scanTimestamps`
(`CopyOnWriteArrayList<Long>`), `mutex`, `scope`, `countdownJob`,
`_throttleState`, `_setupStatus`, `readSetupStatus()`,
`checkThrottlingDisabled()`, `persistTimestamps()`, `recordScan()`, `refresh()`.

The test's private copy, `ScanThrottleManagerTest.kt:13-55`:

```kotlin
    // Test implementation without Android dependencies
    private class TestScanThrottleManager(
        private val maxScans: Int = 4,
        private val windowMs: Long = 120_000L, // 2 minutes
        private val isThrottlingDisabled: () -> Boolean = { false }
    ) {
        private val scanTimestamps = mutableListOf<Long>()
        private var currentTimeMs: Long = 0

        fun setTime(timeMs: Long) { currentTimeMs = timeMs }

        fun recordScan() {
            scanTimestamps.add(currentTimeMs)
            pruneOldTimestamps()
        }

        fun getThrottleState(): ThrottleState {
            if (isThrottlingDisabled()) {
                return ThrottleState.Unlimited
            }

            pruneOldTimestamps()
            val remaining = maxScans - scanTimestamps.size

            return when {
                remaining >= 3 -> ThrottleState.Ready(remaining, maxScans)
                remaining in 1..2 -> ThrottleState.LowQuota(remaining, maxScans)
                remaining <= 0 -> { /* same maths as the Android class */ }
                else -> ThrottleState.Unknown
            }
        }

        private fun pruneOldTimestamps() {
            scanTimestamps.removeAll { currentTimeMs - it > windowMs }
        }
    }
```

The eight existing tests (`initialState_shouldBeReadyWithFullQuota`,
`afterOneScan_shouldStillBeReady`, `afterThreeScans_shouldShowLowQuota`,
`afterFourScans_shouldBeExhausted`, `whenThrottlingDisabled_shouldReturnUnlimited`,
`oldTimestamps_shouldBePruned`, `exhaustedState_shouldCalculateCorrectRemainingTime`,
`partialQuotaRestore_afterOldestScanExpires`) drive it through `setTime`,
`recordScan` and `getThrottleState`. They all remain valid under the new rules
and must pass **without editing their bodies**.

Conventions: imports at the top of the file; KDoc in plain words that says why;
new shared helpers are top-level functions without a visibility modifier, like
`freshScanResults` in `WifiRadio.kt`.

## Commands you will need

Documented in `docs/DEVELOPMENT.md`; **not run** while this plan was written.

| Purpose | Command | Expected on success |
|---|---|---|
| One test class | `./gradlew :composeApp:testDebugUnitTest --tests "com.bernaferrari.wifiweakspot.core.wifi.ScanThrottleManagerTest"` | `BUILD SUCCESSFUL` |
| All unit tests | `./gradlew :composeApp:testDebugUnitTest` | `BUILD SUCCESSFUL` |
| Shared code still compiles for web | `./gradlew :webDemo:compileKotlinWasmJs` | `BUILD SUCCESSFUL` |
| Android build | `./gradlew :androidApp:assembleDebug` | `BUILD SUCCESSFUL` |

## Scope

**In scope** (the only files you should modify):

- `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/wifi/ScanQuota.kt` (create)
- `composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/core/wifi/AndroidScanThrottleManager.kt`
- `composeApp/src/test/kotlin/com/bernaferrari/wifiweakspot/core/wifi/ScanThrottleManagerTest.kt`

**Out of scope** (do NOT touch, even though they look related):

- `ScanThrottleManager.kt` (the interface and `ThrottleState`) — screens depend on its shape.
- `readSetupStatus()`, `checkThrottlingDisabled()`, root handling, and the `isThrottlingDisabled` flow in the Android class.
- Switching from wall-clock time (`System.currentTimeMillis()`) to time since
  boot. Saved timestamps must survive an app restart, which wall-clock time
  does and time-since-boot does not across a reboot. This plan instead ignores
  timestamps that lie in the future.
- `demo-fixtures/` (`SimulatedScanThrottle`) and any screen that shows the countdown.

## Git workflow

- Branch: `advisor/004-scan-limit-countdown`
- One or two commits. This repo writes commit subjects as one plain sentence
  describing the result. Suggested: `The scan-limit countdown never shows zero while still blocked and always pauses between ticks; its tests now run the real rules`.
- Do NOT push or open a PR unless the operator instructed it.

## Steps

### Step 1: Create the shared rules

Create `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/wifi/ScanQuota.kt`:

```kotlin
package com.bernaferrari.wifiweakspot.core.wifi

/**
 * Whether a scan made at [timestamp] still counts against the quota at [now]. It stops counting
 * the moment its window ends. A timestamp in the future means the clock was set back; it is
 * ignored rather than blocking scans for however far the clock moved.
 */
fun scanStillCounts(timestamp: Long, now: Long, windowMs: Long): Boolean =
    timestamp <= now && now - timestamp < windowMs

/**
 * The quota state for scans made at [timestamps], seen at [now]. All times are in milliseconds
 * on the same clock. While blocked, the seconds left are rounded up, so the state never says
 * "0 seconds" before a scan is actually allowed.
 */
fun scanQuotaState(timestamps: List<Long>, now: Long, maxScans: Int = 4, windowMs: Long = 120_000L): ThrottleState {
    val counted = timestamps.filter { scanStillCounts(it, now, windowMs) }.sorted()
    val remaining = maxScans - counted.size
    return when {
        remaining >= 3 -> ThrottleState.Ready(remaining, maxScans)
        remaining >= 1 -> ThrottleState.LowQuota(remaining, maxScans)
        else -> {
            // The next scan is allowed once enough of the oldest ones have left the window.
            val waitMs = counted[counted.size - maxScans] + windowMs - now
            ThrottleState.Exhausted(
                secondsUntilNextScan = (waitMs + 999) / 1000,
                cooldownDurationSeconds = windowMs / 1000
            )
        }
    }
}
```

**Verify**: `./gradlew :webDemo:compileKotlinWasmJs` → `BUILD SUCCESSFUL`.

### Step 2: Point the tests at the real rules, then add the missing cases

In `ScanThrottleManagerTest.kt`, replace the body of the private class
`TestScanThrottleManager` so it only holds a clock and a list and asks the real
function. Keep its constructor and its three public functions so the existing
tests compile unchanged:

```kotlin
    // A clock and a list of scans; the rules themselves are the app's own scanQuotaState.
    private class TestScanThrottleManager(
        private val maxScans: Int = 4,
        private val windowMs: Long = 120_000L, // 2 minutes
        private val isThrottlingDisabled: () -> Boolean = { false }
    ) {
        private val scanTimestamps = mutableListOf<Long>()
        private var currentTimeMs: Long = 0

        fun setTime(timeMs: Long) {
            currentTimeMs = timeMs
        }

        fun recordScan() {
            scanTimestamps.add(currentTimeMs)
        }

        fun getThrottleState(): ThrottleState =
            if (isThrottlingDisabled()) ThrottleState.Unlimited
            else scanQuotaState(scanTimestamps, currentTimeMs, maxScans, windowMs)
    }
```

Also update the class KDoc above `class ScanThrottleManagerTest` to say it
tests `scanQuotaState`, the rules `AndroidScanThrottleManager` uses.

Run the class now; the eight existing tests must pass untouched.

Then add these tests to the same class, in the same style
(`fun name_shouldDoSomething()` with `assertEquals` / `assertTrue`):

| Test name | Setup | Expected |
|---|---|---|
| `lastSecond_shouldStillShowOneSecond` | four scans at time 0; time 119_500 | `Exhausted` with `secondsUntilNextScan == 1` |
| `exhausted_shouldNeverReportZeroSeconds` | four scans at time 0; for every time from 0 to 119_999 in steps of 250 | state is `Exhausted` and `secondsUntilNextScan >= 1` |
| `windowEnd_shouldFreeTheScanExactlyOnTime` | scans at 0, 1_000, 2_000, 3_000; time 120_000 | `LowQuota` with `scansRemaining == 1` |
| `futureTimestamps_shouldBeIgnored` | four scans recorded at time 500_000; then time 1_000 | `Ready` with `scansRemaining == 4` |
| `moreScansThanTheQuota_shouldWaitForEnoughToExpire` | scans at 0, 10_000, 20_000, 30_000, 40_000; time 50_000 | `Exhausted` with `secondsUntilNextScan == 80` |

(For the last one: five scans count, so two must expire; the second-oldest
leaves the window at 130_000, which is 80 seconds after 50_000.)

**Verify**: `./gradlew :composeApp:testDebugUnitTest --tests "com.bernaferrari.wifiweakspot.core.wifi.ScanThrottleManagerTest"` → `BUILD SUCCESSFUL`; 8 existing + 5 new tests pass.

### Step 3: Use the shared rules in the Android class

All edits are in `AndroidScanThrottleManager.kt`.

1. In the companion object add:
   ```kotlin
           /** How often the countdown looks again. Always waiting this long is what keeps it from spinning. */
           private const val COUNTDOWN_TICK_MS = 500L
   ```

2. Replace the body of `pruneOldTimestampsInternal` so it uses the shared rule
   (keep the "must be called while holding mutex" comment):
   ```kotlin
           scanTimestamps.removeAll { !scanStillCounts(it, now, WINDOW_MS) }
   ```

3. In `restoreTimestamps`, change the filter line to:
   ```kotlin
                   .filter { scanStillCounts(it, now, WINDOW_MS) } // Only restore scans that still count
   ```

4. Replace everything in `updateState` after the `if (checkThrottlingDisabled()) { ... }`
   block (that is, from `val timestampCount = getTimestampCount()` to the end of
   the `when`) with:
   ```kotlin
           val now = System.currentTimeMillis()
           val state = mutex.withLock { scanQuotaState(scanTimestamps.toList(), now, MAX_SCANS, WINDOW_MS) }
           _throttleState.value = state
           if (state is ThrottleState.Exhausted) startCountdown() else countdownJob?.cancel()
   ```

5. Replace the whole `startCountdown` function with:
   ```kotlin
       /** Re-reads the quota twice a second until a scan is allowed again. It never calls [updateState], so it cannot restart itself. */
       private fun startCountdown() {
           if (countdownJob?.isActive == true) return
           countdownJob = scope.launch {
               while (true) {
                   delay(COUNTDOWN_TICK_MS)
                   val now = System.currentTimeMillis()
                   val state = mutex.withLock {
                       pruneOldTimestampsInternal(now)
                       scanQuotaState(scanTimestamps.toList(), now, MAX_SCANS, WINDOW_MS)
                   }
                   _throttleState.value = state
                   if (state !is ThrottleState.Exhausted) {
                       mutex.withLock { persistTimestamps() }
                       break
                   }
               }
           }
       }
   ```

6. Delete the two helpers `getTimestampCount` and `getOldestTimestamp`; nothing
   uses them now.

Do not change `recordScan`, `refresh`, the `init` block, or anything about
setup status and root.

**Verify**:
`rg -n "getTimestampCount|getOldestTimestamp|expiresAt|secondsRemaining" composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/core/wifi/AndroidScanThrottleManager.kt` → no matches;
`rg -c "scanQuotaState\(" composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/core/wifi/AndroidScanThrottleManager.kt` → `2`;
`./gradlew :androidApp:assembleDebug` → `BUILD SUCCESSFUL`.

### Step 4: Full verification

**Verify**: `./gradlew :composeApp:testDebugUnitTest :webDemo:compileKotlinWasmJs :androidApp:assembleDebug` → `BUILD SUCCESSFUL`.

## Test plan

- `ScanThrottleManagerTest.kt`: the eight existing tests keep their bodies and
  now run `scanQuotaState`; five new tests cover the last second, the exact
  window end, a clock set back, and more scans than the quota (Step 2 table).
- The Android class itself (coroutines, preferences, system settings) has no
  unit-test harness in this repo. Its logic is now the tested function plus a
  fixed-delay loop; it is covered by compilation and by the manual check in
  "Maintenance notes".
- Verification: `./gradlew :composeApp:testDebugUnitTest` → all pass, 13 tests in this class.

## Done criteria

ALL must hold:

- [ ] `./gradlew :composeApp:testDebugUnitTest` exits 0; `ScanThrottleManagerTest` has 13 passing tests
- [ ] `./gradlew :webDemo:compileKotlinWasmJs :androidApp:assembleDebug` exits 0
- [ ] `rg -n "remaining in 1\.\.2|removeAll \{ currentTimeMs" composeApp/src/test/kotlin/com/bernaferrari/wifiweakspot/core/wifi/ScanThrottleManagerTest.kt` returns no matches (the private copy of the rules is gone)
- [ ] `rg -n "delay\(COUNTDOWN_TICK_MS\)" composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/core/wifi/AndroidScanThrottleManager.kt` returns one match, and it is the first statement inside the `while (true)` loop
- [ ] `rg -n "updateState\(\)" composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/core/wifi/AndroidScanThrottleManager.kt` shows no call inside `startCountdown`
- [ ] `git status --short` lists only the three in-scope files (plus `plans/README.md`)
- [ ] `plans/README.md` status row updated

## STOP conditions

Stop and report back (do not improvise) if:

- `updateState`, `startCountdown`, `pruneOldTimestampsInternal` or
  `restoreTimestamps` do not match the excerpts.
- Any of the eight existing tests fails after Step 2. Do not edit an existing
  test body or expectation to make it pass; report which one and why.
- `scanStillCounts` / `scanQuotaState` are not visible from
  `AndroidScanThrottleManager.kt` (a compile error about an unresolved
  reference). Report it; do not copy the functions into the Android file.
- A step's verification fails twice after a reasonable fix attempt.

## Maintenance notes

- Manual check for a reviewer with a device on default settings (throttling
  on): open Networks, let the quota run out, and watch the countdown reach its
  end. It should step down to 1, then the list should refresh within about a
  second. With a CPU profiler attached, there should be no burst of activity in
  the last second.
- The countdown now updates twice a second and can be up to half a second late
  in noticing the quota is back. The scan loop on the Networks screen adds its
  own one-second margin, so this is not visible.
- `ThrottleState.Exhausted.secondsUntilNextScan` is now rounded up, so it is
  never 0. If a future screen formats it, "0 s" no longer needs a special case.
- Still using wall-clock time: if the user sets the clock **forward**, saved
  scans expire early and the app may ask for a scan Android refuses. That is
  harmless (the request simply fails) and was already true.
- `demo-fixtures`' `SimulatedScanThrottle` has its own simplified behaviour and
  was deliberately left alone.
