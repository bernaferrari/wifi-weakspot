# Plan 006: A picked floor-plan photo always arrives, and arrives upright

> **Executor instructions**: Follow this plan step by step. Run every
> verification command and confirm the expected result before moving to the
> next step. If anything in the "STOP conditions" section occurs, stop and
> report — do not improvise. When done, update the status row for this plan
> in `plans/README.md` — unless a reviewer dispatched you and told you they
> maintain the index.
>
> **Drift check (run first)**:
> `git diff --stat 918ea41c..HEAD -- composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/platform/Platform.kt composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/core/permissions/AndroidPermissionLaunchers.kt composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/survey/SurveyScreen.kt`
> Plan 002 is expected to have changed `Platform.kt` and
> `AndroidPermissionLaunchers.kt`: it removes everything named
> `notifications` from them. That difference is fine and is called out in
> "Current state". For any other difference, compare the excerpts against the
> live code before proceeding; on a mismatch, treat it as a STOP condition.

## Status

- **Priority**: P2
- **Effort**: M
- **Risk**: MED (touches the activity-result path and rewrites image files; needs device checks)
- **Depends on**: none strictly. Run after plan 002 if 002 is going to run (both edit the same two files).
- **Category**: bug
- **Planned at**: commit `918ea41c` plus the owner's uncommitted working tree, 2026-11-10

## Why this matters

Two things go wrong when a person adds a photo of their floor plan to the map.

First, the photo can silently never arrive. While the system photo picker is
open, Android is free to destroy the app's screen (it does so routinely on
phones short of memory, and always with the developer option "Don't keep
activities"). The app remembers "what to do with the picked photo" in a plain
in-memory slot, which is lost when the screen is rebuilt. Android still
delivers the picked photo, the app finds no one waiting, and drops it. The
person picks a photo and nothing happens.

Second, photos taken with the phone's camera show up lying on their side.
Cameras store the pixels as the sensor saw them and add a note saying "turn
this 90°". Gallery apps honour the note; this app's image loader does not.

After this plan, the picked photo is held until the map screen takes it, even
if the screen was rebuilt in between, and a newly picked JPEG is stored
upright.

## Current state

Path shorthand used below (always type the full path in commands):

- `SHARED` = `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot`
- `ANDROID` = `composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot`
- `TESTS` = `composeApp/src/test/kotlin/com/bernaferrari/wifiweakspot`

About the repo: `SHARED` is platform-free Compose code, also compiled to
Kotlin/Wasm for a web demo, so it may not use `android.*` or `java.*`
(`kotlinx.coroutines` is fine). `ANDROID` holds Android implementations and is
compiled into the same Gradle module, so unit tests in `TESTS` can call
`internal` functions from both.

Files:

- `SHARED/core/platform/Platform.kt` — `PermissionLaunchers`, the bridge the shared UI uses to start Android activity-result flows.
- `ANDROID/core/permissions/AndroidPermissionLaunchers.kt` — its Android implementation, including the photo picker (94 lines).
- `SHARED/feature/survey/SurveyScreen.kt` — the Map screen; starts the picker and receives the result.
- `ANDROID/core/platform/AndroidPlatformActions.kt` — `loadImage`, shown for reference only.

`Platform.kt:51-59` at the time of writing. **After plan 002 the
`notifications` line is gone; leave it as you find it.**

```kotlin
/** Launchers that need the host screen (Android activity results). */
class PermissionLaunchers(
    val scan: ((granted: Boolean) -> Unit) -> Unit = { it(true) },
    val notifications: ((granted: Boolean) -> Unit) -> Unit = { it(true) },
    /** Opens an image picker; the callback receives an app-owned path or null. */
    val pickFloorplan: (destinationId: Long, (path: String?) -> Unit) -> Unit = { _, done -> done(null) }
)

val LocalPermissionLaunchers = staticCompositionLocalOf { PermissionLaunchers() }
```

`AndroidPermissionLaunchers.kt:28-31`, the in-memory slots:

```kotlin
    val pending = remember { arrayOfNulls<(Boolean) -> Unit>(3) }
    val startedAt = remember { longArrayOf(0L) }
    val floorplanTarget = remember { longArrayOf(0L) }
    val floorplanCallback = remember { arrayOfNulls<(String?) -> Unit>(1) }
```

`AndroidPermissionLaunchers.kt:40-72`, the picker. Line 41 is where a result
is dropped after the screen was rebuilt (`floorplanCallback[0]` is null again):

```kotlin
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val done = floorplanCallback[0] ?: return@rememberLauncherForActivityResult
        if (uri == null) { done(null); return@rememberLauncherForActivityResult }
        scope.launch {
            // Copy into private storage; the temporary picker grant need not survive a restart.
            val path = withContext(Dispatchers.IO) {
                runCatching {
                    val folder = File(context.filesDir, "survey_floorplans").apply { mkdirs() }
                    val file = File(folder, "floorplan_${floorplanTarget[0]}_${System.currentTimeMillis()}")
                    try {
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            file.outputStream().use { output ->
                                val buffer = ByteArray(8192)
                                var total = 0L
                                while (true) {
                                    val read = input.read(buffer)
                                    if (read < 0) break
                                    total += read
                                    require(total <= 20L * 1024 * 1024) { "Image too large" }
                                    output.write(buffer, 0, read)
                                }
                            }
                        } ?: error("Image unavailable")
                        val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        android.graphics.BitmapFactory.decodeFile(file.absolutePath, bounds)
                        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Image unreadable" }
                        file.absolutePath
                    } catch (error: Exception) { file.delete(); throw error }
                }.getOrNull()
            }
            done(path)
        }
    }
```

The end of the same function (`:74-91` at the time of writing; after plan 002
there is no `notifications` argument and the first line reads
`remember(scan, picker)`):

```kotlin
    return remember(scan, notifications, picker) {
        PermissionLaunchers(
            scan = { onResult -> /* ... */ },
            notifications = { onResult -> /* ... */ },
            pickFloorplan = { target, onPicked ->
                floorplanTarget[0] = target
                floorplanCallback[0] = onPicked
                picker.launch("image/*")
            }
        )
    }
```

`SurveyScreen.kt:73-80` and `:105-113`:

```kotlin
    val platform: com.bernaferrari.wifiweakspot.core.platform.PlatformActions = org.koin.compose.koinInject()
    val launchers = com.bernaferrari.wifiweakspot.core.platform.LocalPermissionLaunchers.current
    val scope = rememberCoroutineScope()
    val haptics = com.bernaferrari.wifiweakspot.core.designsystem.rememberWifiWeakspotHapticFeedback()
    val mapLayer = androidx.compose.ui.graphics.rememberGraphicsLayer()
    fun releaseOwnedFloorplan(path: String?) {
        if (path != null) platform.deleteOwnedFile(path)
    }
```

```kotlin
    fun importFloorplan(destinationProjectId: Long?) {
        launchers.pickFloorplan(destinationProjectId ?: 0L) { path ->
            when {
                path == null -> Unit
                viewModel.currentProject.value?.id == destinationProjectId -> viewModel.setFloorplan(path, ::releaseOwnedFloorplan)
                else -> platform.deleteOwnedFile(path)
            }
        }
    }
```

`SurveyScreen.kt` imports `androidx.compose.runtime.*` and
`kotlinx.coroutines.launch`. `viewModel.currentProject` is a
`StateFlow<SurveyProjectEntity?>` that is `null` for a moment after the screen
is created, until the list of maps has loaded. Maps are listed most recently
used first, and the view model opens the first one, so after a rebuild the
current map is the one the person was on.

How photos are shown, `AndroidPlatformActions.kt:59-67` (reference; do not
change). It decodes pixels as stored and ignores the rotation note:

```kotlin
    override suspend fun loadImage(path: String): ImageBitmap? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { runCatching {
        // Floor plans are often phone photos; decode near screen size, not at full resolution.
        val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        android.graphics.BitmapFactory.decodeFile(path, bounds)
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= 2048) sample *= 2
        val options = android.graphics.BitmapFactory.Options().apply { inSampleSize = sample }
        android.graphics.BitmapFactory.decodeFile(path, options)?.asImageBitmap()
    }.getOrNull() }
```

The web demo and `demo-fixtures/` use `PermissionLaunchers()` with its
defaults, so new constructor parameters need defaults and those targets then
keep compiling without edits.

Conventions: imports at the top of the file. Some files here write
fully-qualified names inline; do not copy that into lines you write, and do
not rewrite existing lines you are not changing. Comments say why, in plain
words.

## Commands you will need

Documented in `docs/DEVELOPMENT.md`; **not run** while this plan was written.

| Purpose | Command | Expected on success |
|---|---|---|
| One test class | `./gradlew :composeApp:testDebugUnitTest --tests "com.bernaferrari.wifiweakspot.core.permissions.FloorplanOrientationTest"` | `BUILD SUCCESSFUL` |
| All unit tests | `./gradlew :composeApp:testDebugUnitTest` | `BUILD SUCCESSFUL` |
| Shared code + demo compile for web | `./gradlew :webDemo:compileKotlinWasmJs` | `BUILD SUCCESSFUL` |
| Android build | `./gradlew :androidApp:assembleDebug` | `BUILD SUCCESSFUL` |
| Demo fixtures compile for Android | `./gradlew :androidApp:assembleScreenshots` | `BUILD SUCCESSFUL` |

## Scope

**In scope** (the only files you should modify or create):

- `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/platform/Platform.kt`
- `composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/core/permissions/AndroidPermissionLaunchers.kt`
- `composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/core/permissions/FloorplanOrientation.kt` (create)
- `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/survey/SurveyScreen.kt`
- `composeApp/src/test/kotlin/com/bernaferrari/wifiweakspot/core/permissions/FloorplanOrientationTest.kt` (create)

**Out of scope** (do NOT touch, even though they look related):

- `AndroidPlatformActions.loadImage`. Do **not** make it apply the rotation
  when showing a photo. Maps people already made have pins placed on the photo
  as it was drawn (sideways); turning the photo under them at display time
  would move every pin to the wrong room. Only newly picked photos are fixed.
- The `scan` launcher and its `pending` / `startedAt` slots. Its callback has
  the same weakness, but the permission screen re-reads the permission state
  on its own, so nothing is lost there.
- Anything named `notifications` — plan 002 owns it.
- `SurveyViewModel.kt`, `SurveyCanvas.kt` (plan 005), the database.
- Adding a library (for example `androidx.exifinterface`). The platform class
  `android.media.ExifInterface` is enough for JPEG files read by path.

## Git workflow

- Branch: `advisor/006-floor-plan-pick-survives-and-upright`
- Two commits, one per part. This repo writes commit subjects as one plain
  sentence describing the result. Suggested:
  `A picked floor-plan photo is kept until the map takes it, even if Android rebuilt the screen while the picker was open` and
  `A newly picked camera photo is stored upright`.
- Do NOT push or open a PR unless the operator instructed it.

## Steps

### Step 1: Let the picked photo wait for the screen

**1a. `Platform.kt`.** Add imports
`kotlinx.coroutines.flow.MutableStateFlow` and `kotlinx.coroutines.flow.StateFlow`.
Directly above the KDoc of `class PermissionLaunchers`, add:

```kotlin
/** A photo picked for the map with id [destinationId], already copied to the app-owned [path]. */
data class FloorplanPick(val destinationId: Long, val path: String)
```

In `PermissionLaunchers`, replace the `pickFloorplan` parameter and its KDoc
with these three parameters (leave every other parameter exactly as found):

```kotlin
    /** Opens an image picker for the map with this id. The result arrives in [floorplanPick]. */
    val pickFloorplan: (destinationId: Long) -> Unit = {},
    /**
     * The picked photo, held until a screen takes it. Android may rebuild the screen while the
     * picker is open; a callback would be lost with the old screen, this is not.
     */
    val floorplanPick: StateFlow<FloorplanPick?> = MutableStateFlow(null),
    /** Call once the pick has been taken, so it is not applied twice. */
    val consumeFloorplanPick: () -> Unit = {}
```

**1b. `AndroidPermissionLaunchers.kt`.**

- Add imports: `androidx.compose.runtime.getValue`,
  `androidx.compose.runtime.mutableStateOf`,
  `androidx.compose.runtime.saveable.rememberSaveable`,
  `androidx.compose.runtime.setValue`,
  `com.bernaferrari.wifiweakspot.core.platform.FloorplanPick`,
  `kotlinx.coroutines.flow.MutableStateFlow`.
- Replace the two lines `val floorplanTarget = remember { longArrayOf(0L) }`
  and `val floorplanCallback = remember { arrayOfNulls<(String?) -> Unit>(1) }` with:

  ```kotlin
      // Saved with the screen: Android may rebuild it while the picker is open, and the photo must
      // still reach the map it was picked for.
      var floorplanTarget by rememberSaveable { mutableStateOf(0L) }
      val floorplanPick = remember { MutableStateFlow<FloorplanPick?>(null) }
  ```

- In the picker callback, replace its first two lines
  (`val done = floorplanCallback[0] ?: return@...` and `if (uri == null) { done(null); return@... }`) with:

  ```kotlin
          if (uri == null) return@rememberLauncherForActivityResult
          val target = floorplanTarget
  ```

- In the file name, change `floorplan_${floorplanTarget[0]}_` to `floorplan_${target}_`.
- Replace the last line of the `scope.launch { ... }` block, `done(path)`, with:

  ```kotlin
              if (path != null) floorplanPick.value = FloorplanPick(target, path)
  ```

- In the `PermissionLaunchers(` call at the end, replace the whole
  `pickFloorplan = { target, onPicked -> ... }` argument with:

  ```kotlin
              pickFloorplan = { target ->
                  floorplanTarget = target
                  picker.launch("image/*")
              },
              floorplanPick = floorplanPick,
              consumeFloorplanPick = { floorplanPick.value = null }
  ```

**1c. `SurveyScreen.kt`.**

- Add imports `kotlinx.coroutines.flow.first` and `kotlinx.coroutines.withTimeoutOrNull`.
- Replace the whole `importFloorplan` function with:

  ```kotlin
      fun importFloorplan(destinationProjectId: Long?) {
          launchers.pickFloorplan(destinationProjectId ?: 0L)
      }
      // The picked photo arrives here rather than in a callback, so it still arrives if Android
      // rebuilt this screen while the picker was open.
      LaunchedEffect(launchers, viewModel) {
          launchers.floorplanPick.collect { pick ->
              if (pick == null) return@collect
              launchers.consumeFloorplanPick()
              // After a rebuild the list of maps loads a moment later; give it that moment.
              val current = withTimeoutOrNull(2_000) { viewModel.currentProject.first { it != null } }
              if (current?.id == pick.destinationId) viewModel.setFloorplan(pick.path, ::releaseOwnedFloorplan)
              else platform.deleteOwnedFile(pick.path)
          }
      }
  ```

  The callers of `importFloorplan` (in the map's menu) do not change.

**Verify**:
`rg -n "floorplanCallback" composeApp/src` → no matches;
`rg -n "pickFloorplan" composeApp demo-fixtures webDemo androidApp -g '*.kt'` → three matches (the definition in `Platform.kt`, the argument in `AndroidPermissionLaunchers.kt`, the call in `SurveyScreen.kt`);
`./gradlew :webDemo:compileKotlinWasmJs :androidApp:assembleDebug :androidApp:assembleScreenshots` → `BUILD SUCCESSFUL`.

### Step 2: Store newly picked camera photos upright

**2a.** Create `composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/core/permissions/FloorplanOrientation.kt`:

```kotlin
package com.bernaferrari.wifiweakspot.core.permissions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.File

/**
 * How far to turn a photo clockwise so it is upright, from the orientation note cameras write
 * into the file (EXIF): 3 is upside down, 6 and 8 are on their side. Mirrored variants, which
 * only some front cameras write, are left alone.
 */
internal fun exifRotationDegrees(orientation: Int): Int = when (orientation) {
    3 -> 180
    6 -> 90
    8 -> 270
    else -> 0
}

/**
 * Phone cameras store a photo as the sensor saw it and note how to turn it; the map draws the
 * pixels as stored, so a photo taken upright would lie on its side. This rewrites a freshly picked
 * JPEG upright. It runs once, when the photo is picked, never when it is shown: readings on older
 * maps were placed on the photo as it was drawn then, and turning it would move them.
 */
internal fun storeUpright(file: File, mimeType: String?) {
    // Other formats are either never rotated this way or already turned by Android's decoder.
    if (mimeType != "image/jpeg") return
    val orientation = runCatching {
        ExifInterface(file.absolutePath).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
    }.getOrDefault(1)
    val degrees = exifRotationDegrees(orientation)
    if (degrees == 0) return

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    // A full camera photo and its turned copy do not both fit in memory on small phones;
    // about 2000 pixels on the long side is plenty for a floor plan.
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= 1280) sample *= 2
    val stored = BitmapFactory.decodeFile(file.absolutePath, BitmapFactory.Options().apply { inSampleSize = sample })
        ?: return
    val upright = Bitmap.createBitmap(
        stored, 0, 0, stored.width, stored.height, Matrix().apply { postRotate(degrees.toFloat()) }, true
    )
    if (upright !== stored) stored.recycle()
    try {
        file.outputStream().use { output ->
            check(upright.compress(Bitmap.CompressFormat.JPEG, 92, output)) { "Image could not be saved" }
        }
    } finally {
        upright.recycle()
    }
}
```

**2b.** In `AndroidPermissionLaunchers.kt`, inside the picker's copy block:

- Directly after the line
  `require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Image unreadable" }`, add:

  ```kotlin
                          storeUpright(file, bounds.outMimeType)
  ```

- Change `} catch (error: Exception) { file.delete(); throw error }` to
  `} catch (error: Throwable) { file.delete(); throw error }`, so that running
  out of memory while turning a large photo also removes the half-written file.

**2c.** Create `composeApp/src/test/kotlin/com/bernaferrari/wifiweakspot/core/permissions/FloorplanOrientationTest.kt`
with JUnit 4 tests (`org.junit.Test`, `org.junit.Assert.assertEquals`), modelled
on `PermissionStateTest.kt` in the same folder:

1. `sidewaysAndUpsideDownPhotosAreTurnedUpright` — `exifRotationDegrees(6) == 90`,
   `exifRotationDegrees(3) == 180`, `exifRotationDegrees(8) == 270`.
2. `uprightUnknownAndMirroredPhotosAreLeftAlone` — for each of `0, 1, 2, 4, 5, 7, 9, -1`:
   `exifRotationDegrees(it) == 0`.

Do not write a unit test that calls `storeUpright`: it needs real Android
image classes, which the JVM test environment does not have.

**Verify**: `./gradlew :composeApp:testDebugUnitTest --tests "com.bernaferrari.wifiweakspot.core.permissions.FloorplanOrientationTest"` → `BUILD SUCCESSFUL`; `./gradlew :androidApp:assembleDebug` → `BUILD SUCCESSFUL`.

### Step 3: Full verification

**Verify**: `./gradlew :composeApp:testDebugUnitTest :composeApp:lintDebug :webDemo:compileKotlinWasmJs :androidApp:assembleDebug :androidApp:assembleScreenshots` → `BUILD SUCCESSFUL`.

If a device or emulator is available, do the manual checks below and report
each result. If none is available, say so in your report; do not claim them.

Manual checks:

1. Normal pick: Map → menu → "Use a plan photo" → choose a picture. It appears on the map.
2. Rebuilt screen: enable Developer options → "Don't keep activities". Repeat
   check 1. The picture must still appear after returning from the picker.
   Disable the option afterwards.
3. Cancel: open the picker and press Back. Nothing changes, no error.
4. Upright: take a photo with the phone held upright (portrait) using the
   camera app, then pick it. It must appear upright, not on its side.
5. Replace: with a photo already set, choose "Change photo" and pick another.
   The new one appears.

## Test plan

- New `TESTS/core/permissions/FloorplanOrientationTest.kt` with the two tests of Step 2c.
- The pick path (activity results, saved state, file copy) has no unit-test
  harness in this repo; it is covered by compilation on all targets and by
  manual checks 1-3 and 5.
- `storeUpright` is covered by manual check 4.
- Verification: `./gradlew :composeApp:testDebugUnitTest` → all pass.

## Done criteria

ALL must hold:

- [ ] The Step 3 command exits 0
- [ ] `rg -n "floorplanCallback" composeApp/src` returns no matches
- [ ] `rg -n "rememberSaveable \{ mutableStateOf\(0L\) \}" composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/core/permissions/AndroidPermissionLaunchers.kt` returns one match
- [ ] `rg -n "storeUpright" composeApp/src` returns exactly two matches (definition and call)
- [ ] `rg -n "ExifInterface|exif" composeApp/src/androidMain/kotlin/com/bernaferrari/wifiweakspot/core/platform/AndroidPlatformActions.kt` returns no matches (`loadImage` was not changed)
- [ ] `FloorplanOrientationTest` exists and passes
- [ ] `git status --short` lists only the five in-scope files (plus `plans/README.md`)
- [ ] `plans/README.md` status row updated, with a note listing which manual checks were done

## STOP conditions

Stop and report back (do not improvise) if:

- The picker block or `importFloorplan` do not match the excerpts (other than
  the `notifications` differences described above).
- `rememberSaveable` cannot be resolved in `AndroidPermissionLaunchers.kt`.
  Do not add a dependency; report the error.
- `PermissionLaunchers(` is constructed with a `pickFloorplan` argument
  anywhere other than `AndroidPermissionLaunchers.kt`
  (`rg -n "pickFloorplan\s*=" composeApp demo-fixtures webDemo androidApp -g '*.kt'`).
- Manual check 2 fails (the picture does not appear after the screen was
  rebuilt). Report what you saw; do not add workarounds such as global
  variables or a singleton.
- Manual check 4 shows a photo turned the wrong way or turned twice.
- A step's verification fails twice after a reasonable fix attempt.

## Maintenance notes

- **Not verified by reading alone** (needs a device): that Android delivers
  the picker result to the rebuilt screen through
  `rememberLauncherForActivityResult` in this app's setup; that HEIC photos
  (the default on some phones) already appear upright. If HEIC photos appear
  sideways, extend `storeUpright` to `image/heif` / `image/heic` only after
  confirming Android's decoder does not already turn them, or they will be
  turned twice.
- Photos already on maps are not changed. A person whose existing plan lies on
  its side fixes it by choosing "Change photo" and picking it again; their
  pins were placed on the sideways image, so they will need re-measuring
  either way.
- A turned photo is stored at up to about 2,500 pixels on its long side;
  photos that needed no turning keep their original file.
- Known leftovers, deliberately not fixed here: if the map is busy saving when
  the photo arrives, `setFloorplan` refuses with "still saving" and the copied
  file stays in `files/survey_floorplans/` unused; if the person never returns
  to the Map screen after a pick, the copied file also stays. Both waste a few
  megabytes at worst. A sweep that deletes files in that folder not referenced
  by any map would cover both.
- The `scan` permission callback (`pending[0]`) is still an in-memory slot.
  If a future screen depends on that callback for more than refreshing state,
  give it the same treatment as the photo pick.
- Reviewer: look closely at the `LaunchedEffect` in `SurveyScreen.kt` — the
  pick must be consumed before any suspension, so a second collection can
  never apply the same photo twice.
