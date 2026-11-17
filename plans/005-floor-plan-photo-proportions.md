# Plan 005: A floor-plan photo keeps its own proportions on the map

> **Executor instructions**: Follow this plan step by step. Run every
> verification command and confirm the expected result before moving to the
> next step. If anything in the "STOP conditions" section occurs, stop and
> report — do not improvise. When done, update the status row for this plan
> in `plans/README.md` — unless a reviewer dispatched you and told you they
> maintain the index.
>
> **Drift check (run first)**:
> `git diff --stat 918ea41c..HEAD -- composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/survey/SurveyCanvas.kt composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/survey/CoverageField.kt composeApp/src/test/kotlin/com/bernaferrari/wifiweakspot/feature/survey/CoverageFieldTest.kt`
> If any in-scope file changed since this plan was written, compare the
> "Current state" excerpts against the live code before proceeding; on a
> mismatch, treat it as a STOP condition.

## Status

- **Priority**: P1
- **Effort**: S
- **Risk**: MED (changes how the map screen composes; needs one look on a device)
- **Depends on**: none
- **Category**: bug
- **Planned at**: commit `918ea41c` plus the owner's uncommitted working tree, 2026-11-10

## Why this matters

The Map screen lets people put a photo of their floor plan under the Wi-Fi
heat map. The map's shape on screen comes from two stored numbers, the map's
width and height in metres. Nothing in the app ever sets them, so every map is
10 × 10, a square, and the photo is stretched to fill it. A phone photo is
4:3 or 16:9, so every floor plan people add is visibly squashed: rooms look
wider or narrower than they are, and the heat map's distances are computed for
a square home. This is the app's signature feature, and the first thing a
person sees after adding their own plan is a distorted picture. After this
plan, a map with a photo takes the photo's proportions. The area in square
metres stays the same; only the shape changes. Existing maps are fixed too,
and their pins stay on the same spots of the photo, because pins are stored as
fractions of the photo's width and height.

## Current state

Path shorthand used below (always type the full path in commands):

- `SHARED` = `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot`
- `TESTS` = `composeApp/src/test/kotlin/com/bernaferrari/wifiweakspot`

About the repo: `SHARED` is platform-free Compose code, also compiled to
Kotlin/Wasm for a web demo, so it may not use `android.*` or `java.*`.

Files:

- `SHARED/feature/survey/SurveyCanvas.kt` — draws the map: photo, heat map, pins (269 lines).
- `SHARED/feature/survey/CoverageField.kt` — the heat-map maths and two sizing helpers.
- `TESTS/feature/survey/CoverageFieldTest.kt` — tests for those helpers.

Where the map's shape is decided, `SurveyCanvas.kt:101-124`. Note that the
photo is loaded on lines 114-118, *inside* the box whose size was already
fixed on line 110, so its dimensions arrive too late to matter:

```kotlin
    ) {
    // A floor-plan photo keeps its calibrated shape. Without one, the grid has no fixed shape, so it fills the
    // whole viewport, and the field uses the same area in meters spread over the viewport's proportions.
    val fillViewport = imagePath == null && maxWidth.value > 0f && maxHeight.value > 0f
    val (fieldWidthMeters, fieldHeightMeters) = if (fillViewport) {
        val area = (widthMeters * heightMeters).takeIf { it.isFinite() && it > 0f } ?: 100f
        val viewAspect = maxWidth.value / maxHeight.value
        kotlin.math.sqrt(area * viewAspect) to kotlin.math.sqrt(area / viewAspect)
    } else widthMeters to heightMeters
    val fitted = fittedMapSize(maxWidth.value, maxHeight.value, fieldWidthMeters, fieldHeightMeters)
    val mapWidth = fitted.width.dp
    val mapHeight = fitted.height.dp
    Box(Modifier.align(Alignment.Center).size(mapWidth, mapHeight).onSizeChanged { fittedPixels = it }.graphicsLayer { scaleX = zoom; scaleY = zoom; translationX = pan.x; translationY = pan.y }) {
        val platform: com.bernaferrari.wifiweakspot.core.platform.PlatformActions = org.koin.compose.koinInject()
        val floorplan by produceState<androidx.compose.ui.graphics.ImageBitmap?>(null, imagePath) {
            value = null
            value = imagePath?.let { platform.loadImage(it) }
        }
        floorplan?.let { bitmap -> androidx.compose.foundation.Image(
            bitmap = bitmap, contentDescription = strings.survey.floorplanDescription,
            // Existing normalized pins refer to the full photo bounds. Fit the whole map above,
            // not the photo alone, so changing screens never separates pins from photo locations.
            contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize()
        ) }
```

The line `) {` at 101 closes the argument list of `BoxWithConstraints(` and
opens its content block; the lines inside that block are indented by four
spaces (not eight) in this file. Keep that as it is.

The only caller, `SHARED/feature/survey/SurveyScreen.kt:239-240`, passes the
stored numbers, which are always the defaults:

```kotlin
                    widthMeters = project?.widthMeters ?: 10f,
                    heightMeters = project?.heightMeters ?: 10f,
```

The defaults, `SHARED/core/database/entities/SurveyEntities.kt:17-18`:

```kotlin
    @androidx.room.ColumnInfo(defaultValue = "10.0") val widthMeters: Float = 10f,
    @androidx.room.ColumnInfo(defaultValue = "10.0") val heightMeters: Float = 10f,
```

The sizing helpers, `CoverageField.kt:141-153`:

```kotlin
/** A map keeps its calibrated aspect on every viewport. Invalid imported sizes fall back to a square. */
internal fun mapAspectRatio(widthMeters: Float, heightMeters: Float): Float =
    if (widthMeters.isFinite() && heightMeters.isFinite() && widthMeters > 0f && heightMeters > 0f)
        (widthMeters / heightMeters).takeIf { it.isFinite() && it > 0f } ?: 1f
    else 1f

internal data class FittedMapSize(val width: Float, val height: Float)

internal fun fittedMapSize(viewportWidth: Float, viewportHeight: Float, widthMeters: Float, heightMeters: Float): FittedMapSize {
    val aspect = mapAspectRatio(widthMeters, heightMeters)
    val width = minOf(viewportWidth.coerceAtLeast(0f), viewportHeight.coerceAtLeast(0f) * aspect)
    return FittedMapSize(width, width / aspect)
}
```

`CoverageField.kt` already imports `kotlin.math.sqrt`. The heat map only uses
the ratio of width to height (`coverageGridForMap` calls `mapAspectRatio`), so
giving it the right ratio is enough to make its distances right.

Test style, `CoverageFieldTest.kt:8-15`:

```kotlin
    @Test fun configuredPhysicalDistancesDetermineSignalWeights() {
        // A 20 × 10 m map: .1 across and .2 down are both two metres.
        val grid = coverageGridForMap(listOf(
            PlanReading(0.6f, 0.5f, -50), PlanReading(0.5f, 0.7f, -80)
        ), widthMeters = 20f, heightMeters = 10f)
        assertEquals(28, grid.rows)
        assertEquals(-65f, grid.at(0.5f, 0.5f).first, 0.01f)
    }
```

Conventions: put imports at the top of the file. This file has several
fully-qualified names written inline (as on lines 114-115 above); do not copy
that style into lines you write, and do not rewrite existing lines you are not
moving. Comments say why, in plain words.

## Commands you will need

Documented in `docs/DEVELOPMENT.md`; **not run** while this plan was written.

| Purpose | Command | Expected on success |
|---|---|---|
| One test class | `./gradlew :composeApp:testDebugUnitTest --tests "com.bernaferrari.wifiweakspot.feature.survey.CoverageFieldTest"` | `BUILD SUCCESSFUL` |
| All unit tests | `./gradlew :composeApp:testDebugUnitTest` | `BUILD SUCCESSFUL` |
| Shared code still compiles for web | `./gradlew :webDemo:compileKotlinWasmJs` | `BUILD SUCCESSFUL` |
| Android build | `./gradlew :androidApp:assembleDebug` | `BUILD SUCCESSFUL` |

## Scope

**In scope** (the only files you should modify):

- `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/survey/CoverageField.kt`
- `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/survey/SurveyCanvas.kt`
- `composeApp/src/test/kotlin/com/bernaferrari/wifiweakspot/feature/survey/CoverageFieldTest.kt`

**Out of scope** (do NOT touch, even though they look related):

- `SurveyEntities.kt`, the database schema, backup code — the stored width and
  height stay as they are. A screen for entering the real size of a home is a
  separate feature.
- `SurveyScreen.kt` — the caller does not change.
- `AndroidPlatformActions.loadImage` and photo rotation — plan 006.
- `ContentScale.FillBounds` on the photo — it stays. Once the box has the
  photo's proportions, filling the box no longer distorts anything, and pins
  keep referring to the whole photo.
- The heat map being computed during composition (a known cost) — not part of this plan.

## Git workflow

- Branch: `advisor/005-floor-plan-photo-proportions`
- One commit. This repo writes commit subjects as one plain sentence
  describing the result. Suggested: `A floor-plan photo keeps its own proportions on the map instead of being stretched to a square`.
- Do NOT push or open a PR unless the operator instructed it.

## Steps

### Step 1: Add the reshaping helper and its tests

In `CoverageField.kt`, directly above the KDoc of `mapAspectRatio`, add:

```kotlin
/**
 * The map's size in meters, reshaped to [aspect] (width / height) while keeping its area. A photo
 * or a viewport decides the shape; the stored size only says how large the home is. An unusable
 * aspect keeps the stored size.
 */
internal fun fieldMetersForAspect(widthMeters: Float, heightMeters: Float, aspect: Float): Pair<Float, Float> {
    if (!aspect.isFinite() || aspect <= 0f) return widthMeters to heightMeters
    val area = (widthMeters * heightMeters).takeIf { it.isFinite() && it > 0f } ?: 100f
    return sqrt(area * aspect) to sqrt(area / aspect)
}
```

In `CoverageFieldTest.kt`, add three tests inside the class (before the private
helper `CoverageGrid.at`), in the file's style:

1. `aPhotoTwiceAsWideAsTallMakesTheMapTwiceAsWide` — `fieldMetersForAspect(10f, 10f, 2f)`:
   width / height equals 2 (tolerance 0.001), width × height equals 100
   (tolerance 0.01).
2. `anUnusableAspectKeepsTheStoredSize` — for each of `Float.NaN`, `0f`, `-1f`,
   `Float.POSITIVE_INFINITY`: `fieldMetersForAspect(8f, 12f, it)` equals `8f to 12f`.
3. `theFittedMapHasThePhotosProportions` — take
   `val (w, h) = fieldMetersForAspect(10f, 10f, 4f / 3f)`, then
   `fittedMapSize(300f, 600f, w, h)`: width equals 300 (tolerance 0.01) and
   width / height equals 4/3 (tolerance 0.001).

**Verify**: `./gradlew :composeApp:testDebugUnitTest --tests "com.bernaferrari.wifiweakspot.feature.survey.CoverageFieldTest"` → `BUILD SUCCESSFUL`.

### Step 2: Load the photo before the map's shape is decided

All edits are in `SurveyCanvas.kt`, inside the `BoxWithConstraints` content block.

1. Add these imports at the top of the file, with the others (skip any that is already there):
   ```kotlin
   import androidx.compose.ui.graphics.ImageBitmap
   import com.bernaferrari.wifiweakspot.core.platform.PlatformActions
   import org.koin.compose.koinInject
   ```

2. Replace lines 102-109 (the two-line comment, `val fillViewport`, and the
   whole `val (fieldWidthMeters, fieldHeightMeters) = if (fillViewport) { ... } else widthMeters to heightMeters`)
   with:

   ```kotlin
       // The map takes the shape of what is under it: the photo's proportions when there is one,
       // the viewport's when there is none. Either way it covers the same area in meters.
       val platform: PlatformActions = koinInject()
       var photoResolved by remember(imagePath) { mutableStateOf(imagePath == null) }
       val floorplan by produceState<ImageBitmap?>(null, imagePath) {
           value = null
           value = imagePath?.let { platform.loadImage(it) }
           photoResolved = true
       }
       // Wait for the photo before drawing anything, so the map never appears square and then jumps.
       if (!photoResolved) return@BoxWithConstraints
       val photoAspect = floorplan?.let { it.width.toFloat() / it.height.toFloat() }
       val fillViewport = imagePath == null && maxWidth.value > 0f && maxHeight.value > 0f
       val (fieldWidthMeters, fieldHeightMeters) = when {
           fillViewport -> fieldMetersForAspect(widthMeters, heightMeters, maxWidth.value / maxHeight.value)
           photoAspect != null -> fieldMetersForAspect(widthMeters, heightMeters, photoAspect)
           // The photo could not be read: fall back to the stored size.
           else -> widthMeters to heightMeters
       }
   ```

   (The `fillViewport` branch computes exactly what the old code did.)

3. Inside the inner `Box(...) {`, delete the two declarations that you just
   moved up: the `val platform: ... = org.koin.compose.koinInject()` line and the
   five-line `val floorplan by produceState<...>(null, imagePath) { ... }` block.

4. Replace the two-line comment above `contentScale = ContentScale.FillBounds`
   with:

   ```kotlin
               // The box already has the photo's proportions, so filling it does not distort the photo,
               // and pins, stored as fractions of the photo's width and height, stay on their spots.
   ```

   Leave `contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize()` unchanged.

Everything else in the file stays as it is: the `fitted` / `mapWidth` /
`mapHeight` lines, the heat map, gestures, drawing.

**Verify**:
`rg -c "produceState" composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/survey/SurveyCanvas.kt` → `1`;
`rg -n "fieldMetersForAspect" composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/survey/SurveyCanvas.kt` → two matches;
`./gradlew :webDemo:compileKotlinWasmJs :androidApp:assembleDebug` → `BUILD SUCCESSFUL`.

### Step 3: Full verification

**Verify**: `./gradlew :composeApp:testDebugUnitTest :webDemo:compileKotlinWasmJs :androidApp:assembleDebug` → `BUILD SUCCESSFUL`.

If a device or emulator is available, also do the manual check below and
report the result. If none is available, say so in your report; do not claim
the visual result.

Manual check: install the debug build, open Map, choose "Use a plan photo",
pick a clearly non-square picture (a landscape screenshot works). Expected:
the picture is not stretched; circles in it stay round; it is letterboxed
inside the map area. Tap to add a reading, rotate the phone: the pin stays on
the same spot of the picture. Remove the photo: the grid fills the whole area
again.

## Test plan

- New unit tests in `TESTS/feature/survey/CoverageFieldTest.kt` (Step 1):
  shape follows the aspect and keeps the area; unusable aspects keep the stored
  size; the fitted on-screen size has the photo's proportions.
- The two existing tests in that file must pass unchanged.
- `SurveyCanvas` is a composable with no test harness in this repo; it is
  covered by compilation on both targets and the manual check.
- Verification: `./gradlew :composeApp:testDebugUnitTest` → all pass.

## Done criteria

ALL must hold:

- [ ] `./gradlew :composeApp:testDebugUnitTest :webDemo:compileKotlinWasmJs :androidApp:assembleDebug` exits 0
- [ ] `CoverageFieldTest` has three more tests than before, all passing
- [ ] `rg -n "kotlin.math.sqrt\(area" composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/survey/SurveyCanvas.kt` returns no matches (the inline maths moved into the helper)
- [ ] `rg -n "ContentScale.FillBounds" composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/survey/SurveyCanvas.kt` returns one match
- [ ] `git status --short` lists only the three in-scope files (plus `plans/README.md`)
- [ ] `plans/README.md` status row updated, with a note saying whether the manual check was done

## STOP conditions

Stop and report back (do not improvise) if:

- `SurveyCanvas.kt` lines 101-124 do not match the excerpt.
- The compiler rejects `return@BoxWithConstraints` (for example "return is not
  allowed here"). Do not restructure the function on your own; report the
  exact error.
- Any existing test in `CoverageFieldTest` or `SurveyViewModelTest` fails.
- A step's verification fails twice after a reasonable fix attempt.
- During the manual check, pins on an existing map appear at different places
  of the photo than before the change.

## Maintenance notes

- Existing maps change shape the first time they are opened after this ships
  (from a stretched square to the photo's real shape). Pins stay on the same
  photo features. The heat map between pins shifts slightly, because distances
  are now measured in the photo's true proportions; that is the fix, not a
  regression.
- A map restored from a backup, or the demo map, that has both a photo and a
  non-square stored size now takes the photo's shape. The stored area is kept.
  Nothing in the app can produce such a map today.
- If a "real size of my home" setting is added later, it should set the area
  (or one side) and let the photo keep deciding the shape; `fieldMetersForAspect`
  is the place where the two meet.
- While the photo is being decoded the map area is empty for a moment (it used
  to show the grid). If that reads as a flash on slow phones, show a
  placeholder there rather than drawing the map early.
- Plan 006 (photos stored upright) is independent of this plan but touches the
  same feature; either order works.
- Reviewer: check on a device that zoom and pan limits still feel right on a
  wide photo (`fittedPixels` now reports the letterboxed size).
