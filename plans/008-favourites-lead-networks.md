# Plan 008: Favourite networks lead the Networks list and carry a star

> **Executor instructions**: Follow this plan step by step. Run every
> verification command and confirm the expected result before moving to the
> next step. If anything in the "STOP conditions" section occurs, stop and
> report — do not improvise. When done, update the status row for this plan
> in `plans/README.md` — unless a reviewer dispatched you and told you they
> maintain the index.
>
> **Precondition check (run first)**:
> `rg -n "previous.distinct\(\)" composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/analyze/SettledOrder.kt`
> must return one match. If it returns none, plan 003 has not been applied;
> STOP and report (this plan edits the same function and assumes 003's version).
>
> **Drift check**:
> `git diff --stat 918ea41c..HEAD -- composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/analyze composeApp/src/test/kotlin/com/bernaferrari/wifiweakspot/feature/analyze/SettledOrderTest.kt composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/i18n/AnalyzeStrings.kt`
> Plans 001 and 003 are expected to have changed `AnalyzeScreen.kt` (the scan
> loop near the top of the file), `SettledOrder.kt` and `SettledOrderTest.kt`.
> Line numbers in this plan are from before those plans; find each place by
> the quoted code, not by number. If a quoted excerpt cannot be found, treat it
> as a STOP condition.

## Status

- **Priority**: P3
- **Effort**: S
- **Risk**: LOW
- **Depends on**: plan 003 (same function in `SettledOrder.kt`). Run after plan 001 if 001 is going to run (same file, different region).
- **Category**: direction
- **Planned at**: commit `918ea41c` plus the owner's uncommitted working tree, 2026-11-10

## Why this matters

A person can mark a network as a favourite with the star in its details
sheet. Today that star does nothing outside the sheet: the Networks list shows
no sign of it and orders rows exactly as before, and in a block of flats with
forty networks a favourite with a weak signal is even folded away behind "show
weak networks". The feature is half-built: the setting is stored, but it has
no effect a person can see. After this plan, favourites sit at the top of the
list directly under the network the phone is connected to, each with a small
star beside its name, and they are never folded away. This is the usual reason
to star something: "keep the networks I care about (my office, my parents'
house, the neighbour whose channel I am fighting with) where I can find them".

## Current state

Path shorthand used below (always type the full path in commands):

- `SHARED` = `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot`
- `TESTS` = `composeApp/src/test/kotlin/com/bernaferrari/wifiweakspot`

About the repo: `SHARED` is platform-free Compose code, also compiled to
Kotlin/Wasm for a web demo.

Translations: string interfaces live in `SHARED/core/i18n/` (here
`AnalyzeStrings.kt`). Each of ten languages implements them in its own folder:
`en/EnAnalyzeStrings.kt`, `es/EsAnalyzeStrings.kt`, `ptbr/PtBrAnalyzeStrings.kt`,
`fr/FrAnalyzeStrings.kt`, `de/DeAnalyzeStrings.kt`, `it/ItAnalyzeStrings.kt`,
`ru/RuAnalyzeStrings.kt`, `ja/JaAnalyzeStrings.kt`, `ko/KoAnalyzeStrings.kt`,
`zhhans/ZhHansAnalyzeStrings.kt`. There is no fallback to English: a new key
must be implemented in all ten or the build fails.

### The ordering function

`SHARED/feature/analyze/SettledOrder.kt` **as it is after plan 003**:

```kotlin
/**
 * Orders a live list without making it jump under the finger. Items keep their previous places
 * and only pass a neighbour when clearly stronger (by [margin] or more), so a 1–2 dB wobble
 * between scans never reshuffles rows. [pinned] items (your own connection) always lead.
 */
internal fun <T> settledOrder(
    previous: List<String>,
    items: List<T>,
    id: (T) -> String,
    strength: (T) -> Int,
    pinned: (T) -> Boolean = { false },
    margin: Int = 6
): List<T> {
    val byId = items.associateBy(id)
    val known = previous.distinct().mapNotNull { byId[it] }
    val seen = known.map(id).toSet()
    // (comment lines from plan 003)
    val list = (known + byId.values.filter { id(it) !in seen }.sortedByDescending(strength)).toMutableList()
    var changed = true
    while (changed) {
        changed = false
        for (i in 0 until list.size - 1) {
            val a = list[i]
            val b = list[i + 1]
            val swap = (pinned(b) && !pinned(a)) ||
                (pinned(a) == pinned(b) && strength(b) - strength(a) >= margin)
            if (swap) {
                list[i] = b
                list[i + 1] = a
                changed = true
            }
        }
    }
    return list
}
```

Its tests, `TESTS/feature/analyze/SettledOrderTest.kt` (plan 003 adds two more
tests below these; they use the same helper):

```kotlin
class SettledOrderTest {
    private data class N(val id: String, val rssi: Int, val mine: Boolean = false)
    private fun order(previous: List<String>, vararg items: N) =
        settledOrder(previous, items.toList(), { it.id }, { it.rssi }, { it.mine }).map { it.id }

    @Test fun firstShowingIsStrongestFirstWithYoursOnTop() =
        assertEquals(listOf("home", "b", "a"), order(emptyList(), N("a", -70), N("b", -50), N("home", -80, mine = true)))
```

`settledOrder` has one caller (`rg -n "settledOrder\(" composeApp/src/main`
shows the definition and `AnalyzeScreen.kt`).

### The Networks list

All in `SHARED/feature/analyze/AnalyzeScreen.kt`, inside the private composable
that draws the list (it starts with `BoxWithConstraints(Modifier.fillMaxSize()) {`
followed by `val reduceMotion = LocalReduceMotion.current`).

Where the order is computed (was lines 318-330):

```kotlin
        var detail by remember { mutableStateOf<WifiNetwork?>(null) }
        // Your own connection leads the list; everything else is strongest first.
        // Rows hold their places between scans (see settledOrder), so taps land where you aim.
        val lastOrder = remember(band) { arrayOf(emptyList<String>()) }
        val inBand = remember(networks, band, connected.bssid) {
            settledOrder(
                lastOrder[0],
                networks.filter { band.contains(it.frequency) },
                id = { it.bssid },
                strength = { it.rssi },
                pinned = { connected.isConnected && it.bssid.equals(connected.bssid, ignoreCase = true) }
            ).also { lastOrder[0] = it.map { n -> n.bssid } }
        }
```

Where weak networks are folded away and rows are emitted (was lines 430-445):

```kotlin
            // With dozens of neighbors, the unusable ones fold away behind one row.
            val weak = if (inBand.size > 10) inBand.filter { SignalQuality.of(it.rssi) == SignalQuality.DeadZone && !it.bssid.equals(connected.bssid, true) } else emptyList()
            val visible = if (showWeak) inBand else inBand - weak.toSet()
            itemsIndexed(visible, key = { _, n -> n.bssid }) { index, network ->
                NetworkRow(
                    network = network,
                    index = index,
                    count = visible.size,
                    isConnected = connected.isConnected && network.bssid.equals(connected.bssid, ignoreCase = true),
                    isYourRouter = connected.isConnected && network.isSiblingOf(connected.bssid, connected.frequency),
                    isSelected = selected?.bssid == network.bssid,
                    chartColor = palette.color(network.bssid),
                    onClick = { toggle(network) },
                    onDetails = { haptics.click(); detail = network },
                    modifier = if (reduceMotion) Modifier else Modifier.animateItem()
                )
            }
```

Where favourites are read today, only once the details sheet is open (was
lines 496-499). The sheet passes `isFavorite = network.bssid in favorites` and
calls `settings.toggleFavorite(...)` a few lines further down:

```kotlin
        detail?.let { network ->
            val settings: SettingsRepository = koinInject()
            val favorites by settings.favoriteBssids.collectAsState(initial = emptySet())
            val scope = rememberCoroutineScope()
```

This is the only use of `collectAsState(` in the file (the file imports both
`androidx.compose.runtime.collectAsState` and
`androidx.lifecycle.compose.collectAsStateWithLifecycle`).
`SettingsRepository.favoriteBssids` is a `Flow<Set<String>>`.

The row (was lines 611-623 and 666-685):

```kotlin
@Composable
private fun NetworkRow(
    network: WifiNetwork,
    index: Int,
    count: Int,
    isConnected: Boolean,
    isYourRouter: Boolean,
    isSelected: Boolean,
    chartColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    onDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
```

```kotlin
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Matches this network's hill and line in the chart above.
            Box(Modifier.size(10.dp).background(chartColor, androidx.compose.foundation.shape.CircleShape))
            Spacer(Modifier.width(8.dp))
            Text(
                network.ssid.ifEmpty { common.hiddenNetwork },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (isConnected) {
                Spacer(Modifier.width(8.dp))
                Surface(shape = WifiWeakspotShapes.full, color = scheme.primary, contentColor = scheme.onPrimary) {
                    Text(s.connected, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }
        }
    }
```

Inside `NetworkRow`, `s` is `strings.analyze` and `scheme` is
`MaterialTheme.colorScheme`. The file imports icons one by one, for example
`import androidx.compose.material.icons.rounded.LockOpen`.
`WifiDetailScreen.kt` in the same folder already uses `Icons.Rounded.Star`.

### The strings

`SHARED/core/i18n/AnalyzeStrings.kt:280-281`:

```kotlin
    val addToFavorites: String
    val removeFromFavorites: String
```

`SHARED/core/i18n/en/EnAnalyzeStrings.kt:125-126`:

```kotlin
    override val addToFavorites = "Add to favorites"
    override val removeFromFavorites = "Remove from favorites"
```

In every catalog the `removeFromFavorites` override is a single line; find it
with `rg -n "removeFromFavorites" composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/i18n`.

Conventions: imports at the top of the file; comments say why, in plain
words. The code and comments use American spelling ("favorite").

## Commands you will need

Documented in `docs/DEVELOPMENT.md`; **not run** while this plan was written.

| Purpose | Command | Expected on success |
|---|---|---|
| One test class | `./gradlew :composeApp:testDebugUnitTest --tests "com.bernaferrari.wifiweakspot.feature.analyze.SettledOrderTest"` | `BUILD SUCCESSFUL` |
| All unit tests | `./gradlew :composeApp:testDebugUnitTest` | `BUILD SUCCESSFUL` |
| Shared code + demo compile for web | `./gradlew :webDemo:compileKotlinWasmJs` | `BUILD SUCCESSFUL` |
| Android build | `./gradlew :androidApp:assembleDebug` | `BUILD SUCCESSFUL` |
| Text-direction check on translations | `python3 scripts/check_bidi.py` | exit 0 |

## Scope

**In scope** (the only files you should modify):

- `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/analyze/SettledOrder.kt`
- `composeApp/src/test/kotlin/com/bernaferrari/wifiweakspot/feature/analyze/SettledOrderTest.kt`
- `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/analyze/AnalyzeScreen.kt`
- `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/i18n/AnalyzeStrings.kt`
- The ten `<Lang>AnalyzeStrings.kt` files listed above

**Out of scope** (do NOT touch, even though they look related):

- `WifiDetailScreen.kt` (the star button) and `SettingsRepository.kt` (how favourites are stored).
- `ChartExplorer.kt` (the full-screen chart has its own list) and
  `OverviewScreen.kt` (Home's short list of nearby networks). Favourites could
  lead there too; that is a separate, later decision.
- The chart colours (`rememberNetworkPalette`) and the scan loop.
- Matching favourites case-insensitively. They are stored and compared exactly
  as the scanner reports them, here and in the details sheet; keep it the same.

## Git workflow

- Branch: `advisor/008-favourites-lead-networks`
- One or two commits. This repo writes commit subjects as one plain sentence
  describing the result. Suggested: `Favorite networks lead the Networks list under your own connection, wear a star, and are never folded away as weak`.
- Do NOT push or open a PR unless the operator instructed it.

## Steps

### Step 1: Let the ordering take a rank instead of a yes/no

In `SettledOrder.kt` change exactly three things:

1. The last sentence of the KDoc, `[pinned] items (your own connection) always lead.`, becomes:
   `Items with a higher [rank] always lead (your own connection, then favorites); the rules above apply within a rank.`
2. The parameter `pinned: (T) -> Boolean = { false },` becomes `rank: (T) -> Int = { 0 },`
3. The two-line `val swap = ...` becomes:
   ```kotlin
            val swap = rank(b) > rank(a) ||
                (rank(a) == rank(b) && strength(b) - strength(a) >= margin)
   ```

In `SettledOrderTest.kt`:

1. Replace the helper's data class and function with:
   ```kotlin
       private data class N(val id: String, val rssi: Int, val mine: Boolean = false, val favorite: Boolean = false)
       private fun order(previous: List<String>, vararg items: N) =
           settledOrder(previous, items.toList(), { it.id }, { it.rssi }, { if (it.mine) 2 else if (it.favorite) 1 else 0 }).map { it.id }
   ```
   Existing tests stay as they are.
2. Add these tests at the end of the class:
   ```kotlin
       @Test fun aFavoriteLeadsStrongerStrangers() =
           assertEquals(listOf("fav", "a"), order(emptyList(), N("a", -40), N("fav", -80, favorite = true)))

       @Test fun yourConnectionStaysAboveAStrongerFavorite() =
           assertEquals(listOf("home", "fav", "a"), order(emptyList(), N("fav", -40, favorite = true), N("home", -80, mine = true), N("a", -50)))

       @Test fun aNewFavoriteMovesUpFromWhereItWas() =
           assertEquals(listOf("c", "a", "b"), order(listOf("a", "b", "c"), N("a", -40), N("b", -50), N("c", -80, favorite = true)))

       @Test fun removingAFavoriteLetsItSinkBySignal() =
           assertEquals(listOf("a", "b", "c"), order(listOf("c", "a", "b"), N("a", -40), N("b", -50), N("c", -80)))

       @Test fun favoritesHoldTheirPlacesThroughSmallWobbles() =
           assertEquals(listOf("f1", "f2", "a"), order(listOf("f1", "f2", "a"), N("f1", -62, favorite = true), N("f2", -59, favorite = true), N("a", -30)))
   ```

`AnalyzeScreen.kt` does not compile at this point (it still passes `pinned =`).
That is fixed in Step 3; do not verify the build until then.

**Verify**: nothing to run yet. Continue to Step 2.

### Step 2: Add the star's spoken label in all ten languages

In `AnalyzeStrings.kt`, directly after `val removeFromFavorites: String`, add:

```kotlin
    /** Read aloud for the star beside a favorite network in the list. One or two words. */
    val favoriteNetwork: String
```

In each catalog, directly after its `override val removeFromFavorites = ...`
line, add `override val favoriteNetwork = "..."` with:

| File | Text |
|---|---|
| `en/EnAnalyzeStrings.kt` | Favorite |
| `es/EsAnalyzeStrings.kt` | Favorita |
| `ptbr/PtBrAnalyzeStrings.kt` | Favorita |
| `fr/FrAnalyzeStrings.kt` | Favori |
| `de/DeAnalyzeStrings.kt` | Favorit |
| `it/ItAnalyzeStrings.kt` | Preferita |
| `ru/RuAnalyzeStrings.kt` | В избранном |
| `ja/JaAnalyzeStrings.kt` | お気に入り |
| `ko/KoAnalyzeStrings.kt` | 즐겨찾기 |
| `zhhans/ZhHansAnalyzeStrings.kt` | 已收藏 |

(Spanish, Portuguese and Italian use the feminine form because "network" is
feminine in those languages.)

**Verify**: `rg -l "favoriteNetwork" composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/i18n` → eleven files.

### Step 3: Use favourites in the Networks list

All edits are in `AnalyzeScreen.kt`.

1. **Imports.** Add `import androidx.compose.material.icons.rounded.Star`.
   Remove `import androidx.compose.runtime.collectAsState` (edit 2 removes its
   only use; confirm afterwards with
   `rg -n "collectAsState\(" composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/analyze/AnalyzeScreen.kt`
   → no matches).

2. **Read favourites once, for the whole list.** Directly after
   `var detail by remember { mutableStateOf<WifiNetwork?>(null) }`, add:
   ```kotlin
           // Starred in a network's details; here they lead the list, right under your own connection.
           val settings: SettingsRepository = koinInject()
           val favorites by settings.favoriteBssids.collectAsStateWithLifecycle(initialValue = emptySet())
   ```
   Then, inside `detail?.let { network ->`, delete the two lines
   `val settings: SettingsRepository = koinInject()` and
   `val favorites by settings.favoriteBssids.collectAsState(initial = emptySet())`.
   The sheet keeps using `settings` and `favorites`; they now come from the
   lines you just added.

3. **Order.** Replace the comment line
   `// Your own connection leads the list; everything else is strongest first.` with
   `// Your own connection leads the list, then favorites; everything else is strongest first.`
   Change `remember(networks, band, connected.bssid) {` to
   `remember(networks, band, connected.bssid, favorites) {`, and replace the
   `pinned = { ... }` argument with:
   ```kotlin
                   rank = {
                       when {
                           connected.isConnected && it.bssid.equals(connected.bssid, ignoreCase = true) -> 2
                           it.bssid in favorites -> 1
                           else -> 0
                       }
                   }
   ```

4. **Never fold a favourite away.** In the `val weak = ...` line, change
   `&& !it.bssid.equals(connected.bssid, true) }` to
   `&& !it.bssid.equals(connected.bssid, true) && it.bssid !in favorites }`.

5. **Pass it to the row.** In the `NetworkRow(` call, after the `isYourRouter = ...` line, add:
   ```kotlin
                       isFavorite = network.bssid in favorites,
   ```

6. **Show it.** In `private fun NetworkRow(`, add the parameter
   `isFavorite: Boolean,` directly after `isYourRouter: Boolean,`. Then, in the
   title `Row`, between the closing `)` of the SSID `Text(...)` and
   `if (isConnected) {`, add:
   ```kotlin
               if (isFavorite) {
                   Spacer(Modifier.width(6.dp))
                   Icon(Icons.Rounded.Star, s.favoriteNetwork, Modifier.size(16.dp), tint = scheme.primary)
               }
   ```

**Verify**:
`rg -n "pinned" composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/analyze` → no matches;
`rg -c "favoriteBssids" composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/analyze/AnalyzeScreen.kt` → `1`;
`./gradlew :composeApp:testDebugUnitTest --tests "com.bernaferrari.wifiweakspot.feature.analyze.SettledOrderTest"` → `BUILD SUCCESSFUL`, with the five new tests passing.

### Step 4: Full verification

**Verify**:
`python3 scripts/check_bidi.py` → exit 0, and
`./gradlew :composeApp:testDebugUnitTest :webDemo:compileKotlinWasmJs :androidApp:assembleDebug` → `BUILD SUCCESSFUL`.

If a device is available, manual check: open Networks, open a network's
details (the "i" button), tap the star, close the sheet. The network moves to
the top of the list (under the connected one) and shows a small star. Un-star
it: the star goes and the row returns to its place by signal strength.

## Test plan

- `TESTS/feature/analyze/SettledOrderTest.kt`: five new tests (Step 1) — a
  favourite leads stronger strangers; the connected network stays above a
  stronger favourite; a newly starred row moves up; an un-starred row sinks by
  signal; favourites do not reshuffle on small signal changes. All existing
  tests (four original, two from plan 003) must pass with unchanged bodies.
- Existing `StringsTest` covers the new string in all ten languages by reflection.
- The list itself is a composable with no test harness in this repo; it is
  covered by compilation on both targets and the manual check.
- Verification: `./gradlew :composeApp:testDebugUnitTest` → all pass.

## Done criteria

ALL must hold:

- [ ] The Step 4 commands exit 0
- [ ] `rg -n "pinned" composeApp/src/main composeApp/src/test -g '*.kt'` returns no matches in `feature/analyze`
- [ ] `rg -l "favoriteNetwork" composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot` lists twelve files (interface, ten catalogs, `AnalyzeScreen.kt`)
- [ ] `rg -n "isFavorite" composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/analyze/AnalyzeScreen.kt` shows the parameter, the argument in the `NetworkRow(` call, the `if (isFavorite)` in the row, and the existing argument passed to the details sheet
- [ ] `git status --short` lists only in-scope files (plus `plans/README.md`)
- [ ] `plans/README.md` status row updated, with a note saying whether the manual check was done

## STOP conditions

Stop and report back (do not improvise) if:

- The precondition check fails (plan 003 not applied).
- `settledOrder(` is called from anywhere other than `AnalyzeScreen.kt`.
- The two `val settings` / `val favorites` lines inside `detail?.let` are not
  there, or `settings` / `favorites` are declared somewhere else in the same
  function already.
- Any existing test in `SettledOrderTest` fails. Do not change an existing
  expectation; report which test and the actual order.
- A step's verification fails twice after a reasonable fix attempt.

## Maintenance notes

- Decision made in this plan: the connected network outranks favourites. A
  person's own connection is what the screen is about; favourites come second.
- Favourites are matched by access point (BSSID), so starring one radio of a
  mesh or dual-band router does not star its siblings. If that surprises
  people, the place to change it is the `rank` lambda (use `isSiblingOf`, as
  the "also your router" label does).
- Follow-ups left out on purpose: the same ordering in the full-screen chart's
  list (`ChartExplorer.kt`) and on Home; a filter chip "Favorites only".
- The one-word translations were written with the plan, not by native
  speakers; include them in the next translation review.
- Reviewer: check the star does not push the "Connected" pill off a narrow
  screen for a long network name (the name is the element that should shrink).
