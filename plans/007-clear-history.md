# Plan 007: History can be cleared from the History screen

> **Executor instructions**: Follow this plan step by step. Run every
> verification command and confirm the expected result before moving to the
> next step. If anything in the "STOP conditions" section occurs, stop and
> report — do not improvise. When done, update the status row for this plan
> in `plans/README.md` — unless a reviewer dispatched you and told you they
> maintain the index.
>
> **Drift check (run first)**:
> `git diff --stat 918ea41c..HEAD -- composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/database/dao/WifiWeakspotDao.kt composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/history/HistoryScreen.kt composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/i18n/HistoryStrings.kt demo-fixtures/src/main/kotlin/com/bernaferrari/wifiweakspot/demo/InMemoryStorage.kt`
> If any in-scope file changed since this plan was written, compare the
> "Current state" excerpts against the live code before proceeding; on a
> mismatch, treat it as a STOP condition.

## Status

- **Priority**: P2
- **Effort**: M
- **Risk**: LOW
- **Depends on**: none
- **Category**: direction
- **Planned at**: commit `918ea41c` plus the owner's uncommitted working tree, 2026-11-10

## Why this matters

The app keeps a history of what it measured: nearby networks seen, devices
found on the home network, ping / DNS / port-scan results, and speed tests.
The privacy policy (`PRIVACY.md`, section "Data retention") says this stays on
the phone "until you delete them using the available in-app controls". For
this history there is no such control: the only way to remove it is to clear
the app's storage in Android settings, which also wipes maps and preferences.
A person who lends their phone, sells it, or just scanned a network they would
rather not keep a record of has no way out. After this plan the History screen
has a "Clear history" action that, after one confirmation, deletes all four
kinds of saved history and leaves maps, rooms and settings alone.

## Current state

Path shorthand used below (always type the full path in commands):

- `SHARED` = `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot`
- `DEMO` = `demo-fixtures/src/main/kotlin/com/bernaferrari/wifiweakspot/demo`

About the repo: `SHARED` is platform-free Compose code, also compiled to
Kotlin/Wasm for a web demo. The database is Room; its one DAO interface lives
in `SHARED` and has two implementations: the one Room generates, and a
hand-written in-memory one in `DEMO` used by the web demo and by the
`screenshots` build type. Adding an abstract function to the DAO therefore
requires an override in `DEMO`.

Translations: string interfaces live in `SHARED/core/i18n/` (here
`HistoryStrings.kt`). Each of ten languages implements them in its own folder:
`en/EnHistoryStrings.kt`, `es/EsHistoryStrings.kt`, `ptbr/PtBrHistoryStrings.kt`,
`fr/FrHistoryStrings.kt`, `de/DeHistoryStrings.kt`, `it/ItHistoryStrings.kt`,
`ru/RuHistoryStrings.kt`, `ja/JaHistoryStrings.kt`, `ko/KoHistoryStrings.kt`,
`zhhans/ZhHansHistoryStrings.kt`. There is no fallback to English: a new key
must be implemented in all ten or the build fails.

### The DAO

`SHARED/core/database/dao/WifiWeakspotDao.kt` — the four history tables are
read at lines 38-39, 45-46, 61-62 and 71-72:

```kotlin
    @Query("SELECT * FROM wifi_scan_results ORDER BY timestamp DESC LIMIT 3000")
    fun getRecentWifiScans(): Flow<List<WifiScanResultEntity>>
```
```kotlin
    @Query("SELECT * FROM lan_devices ORDER BY lastSeen DESC")
    fun getAllLanDevices(): Flow<List<LanDeviceEntity>>
```
```kotlin
    @Query("SELECT * FROM tool_results ORDER BY timestamp DESC")
    fun getToolHistory(): Flow<List<ToolResultEntity>>
```
```kotlin
    @Query("SELECT * FROM speed_test_results ORDER BY timestamp DESC")
    fun getSpeedTestHistory(): Flow<List<SpeedTestResultEntity>>
```

The precedent for a multi-statement transaction written as an interface
method with a body, `WifiWeakspotDao.kt:94-98`:

```kotlin
    @Transaction
    suspend fun replaceSurveyTarget(project: SurveyProjectEntity) {
        deletePointsForProject(project.id)
        updateSurveyProject(project)
    }
```

The file ends at line 123 with:

```kotlin
    @Delete
    suspend fun deleteProject(project: SurveyProjectEntity)
}
```

### The in-memory DAO

`DEMO/InMemoryStorage.kt:44-48`:

```kotlin
    private val seed = SeedHistory()
    private val scans = MutableStateFlow(seed.scans)
    private val lan = MutableStateFlow(seed.devices)
    private val tools = MutableStateFlow(seed.tools)
    private val speed = MutableStateFlow(seed.speedTests)
```

and, at lines 76-78, the last speed-test overrides:

```kotlin
    override fun getSpeedTestHistory(): Flow<List<SpeedTestResultEntity>> = speed
    override suspend fun tagSpeedTest(id: Long, room: String?) =
        speed.update { list -> list.map { t -> if (t.id == id) t.copy(room = room) else t } }
```

### The History screen

`SHARED/feature/history/HistoryScreen.kt:106-143`:

```kotlin
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    dao: WifiWeakspotDao = koinInject(),
    onBack: (() -> Unit)? = null
) {
    val catalog = com.bernaferrari.wifiweakspot.core.i18n.strings
    val data by remember(dao, catalog) {
        combine(dao.getRecentWifiScans(), dao.getAllLanDevices(), dao.getToolHistory(), dao.getSpeedTestHistory()) { scans, devices, tools, speed ->
            HistoryData(buildHistory(scans, devices, tools, speed), routerTests(speed))
        }.flowOn(Dispatchers.Default)
    }.collectAsStateWithLifecycle(initialValue = null)

    var filterName by rememberSaveable { mutableStateOf<String?>(null) }
    val filter = filterName?.let { name -> HistoryKind.entries.firstOrNull { it.name == name } }
    var selectedKey by rememberSaveable { mutableStateOf<String?>(null) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val reduceMotion = LocalReduceMotion.current

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(strings.common.historyTitle) },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.common.back)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
```

The `Scaffold` content block, and with it the function, ends at lines 204-208:

```kotlin
        loaded.entries.firstOrNull { it.key == selectedKey }?.let { entry ->
            HistoryDetailSheet(entry, onDismiss = { selectedKey = null })
        }
    }
}
```

This file imports each symbol one by one (no wildcard imports) and takes its
icons from `androidx.compose.material.icons.rounded.*`.

The confirmation-dialog pattern used elsewhere,
`SHARED/feature/survey/SurveyScreen.kt:313-317`:

```kotlin
    deletingProject?.let { item -> AlertDialog(
        onDismissRequest = { deletingProject = null }, title = { Text(strings.common.deleteItemTitle(item.name)) }, text = { Text(strings.survey.deleteMapBody) },
        confirmButton = { TextButton(onClick = { viewModel.deleteProject(item, ::releaseOwnedFloorplan); deletingProject = null }) { Text(strings.common.delete) } },
        dismissButton = { TextButton(onClick = { deletingProject = null }) { Text(strings.common.cancel) } }
    ) }
```

`strings.common.delete` ("Delete") and `strings.common.cancel` ("Cancel")
already exist in all ten languages.

### The strings

`SHARED/core/i18n/HistoryStrings.kt:37-44`:

```kotlin
    // History screen

    /** Empty state title when nothing has been saved. */
    val emptyTitle: String
    /** Empty state explanation under [emptyTitle]. */
    val emptySubtitle: String
    /** Accessibility description of the router trend bar chart. [readings] is a comma list of readings like "4.2 ms" or [noReplyReading]. */
    fun trendBarsDescription(count: Int, readings: String): String
```

`SHARED/core/i18n/en/EnHistoryStrings.kt:18-20`:

```kotlin
    override val emptyTitle = "Nothing saved yet"
    override val emptySubtitle = "Router checks, network scans and tool results show up here, so you can see how your Wi-Fi changes over time."
    override fun trendBarsDescription(count: Int, readings: String) = "Last $count router tests: $readings"
```

In the other nine catalogs `emptySubtitle` is on line 21.

### The instrumented-test exemplar

`composeApp/src/androidTest/kotlin/com/bernaferrari/wifiweakspot/core/backup/AndroidBackupServiceTest.kt`
is the only instrumented test. It builds a real database like this (lines 198-200)
and closes every database in `@After`:

```kotlin
    private fun database(ctx: Context = context): WifiWeakspotDatabase =
        Room.inMemoryDatabaseBuilder(ctx, WifiWeakspotDatabase::class.java)
            .setDriver(BundledSQLiteDriver()).build().also { databases += it }
```

The DAO is reached with `database().dao()`. Tests there are written as
`@Test(timeout = 60_000) fun name() = runBlocking { ... }` with
`@RunWith(AndroidJUnit4::class)` on the class.

Conventions: imports at the top of the file; comments say why, in plain words.

## Commands you will need

Documented in `docs/DEVELOPMENT.md`; **not run** while this plan was written.

| Purpose | Command | Expected on success |
|---|---|---|
| Unit tests | `./gradlew :composeApp:testDebugUnitTest` | `BUILD SUCCESSFUL` |
| Shared code + demo compile for web | `./gradlew :webDemo:compileKotlinWasmJs` | `BUILD SUCCESSFUL` |
| Android build (runs Room's code generator) | `./gradlew :androidApp:assembleDebug` | `BUILD SUCCESSFUL` |
| Demo fixtures compile for Android | `./gradlew :androidApp:assembleScreenshots` | `BUILD SUCCESSFUL` |
| Instrumented tests compile | `./gradlew :composeApp:assembleDebugAndroidTest` | `BUILD SUCCESSFUL` |
| Instrumented tests run (needs a device or emulator) | `./gradlew :composeApp:connectedDebugAndroidTest` | `BUILD SUCCESSFUL` |
| Text-direction check on translations | `python3 scripts/check_bidi.py` | exit 0 |

## Scope

**In scope** (the only files you should modify or create):

- `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/database/dao/WifiWeakspotDao.kt`
- `demo-fixtures/src/main/kotlin/com/bernaferrari/wifiweakspot/demo/InMemoryStorage.kt`
- `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/i18n/HistoryStrings.kt`
- The ten `<Lang>HistoryStrings.kt` files listed above
- `composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/feature/history/HistoryScreen.kt`
- `composeApp/src/androidTest/kotlin/com/bernaferrari/wifiweakspot/core/database/HistoryClearTest.kt` (create)

**Out of scope** (do NOT touch, even though they look related):

- The database schema, entity classes, migrations, `composeApp/schemas/`. No
  table or column changes; if a schema JSON file changes, something went wrong.
- Survey maps and their readings (`survey_projects`, `survey_points`), room
  checks, favourites, appearance settings. "Clear history" leaves them alone.
- Deleting single entries, exporting history, automatic expiry — each is a
  separate feature.
- `PRIVACY.md` — its wording becomes true with this plan and needs no edit.
- `AnalyzeViewModel` (which saves a snapshot of nearby networks about once a
  minute while the Networks screen is in use) — see "Maintenance notes".

## Git workflow

- Branch: `advisor/007-clear-history`
- One or two commits. This repo writes commit subjects as one plain sentence
  describing the result. Suggested: `History has a Clear history action that removes saved scans, devices, tool results and speed tests after one confirmation`.
- Do NOT push or open a PR unless the operator instructed it.

## Steps

### Step 1: Add the delete queries

In `WifiWeakspotDao.kt`, directly above the comment
`// Snapshot queries retain the caller's driver transaction; Flow collection is for observers.`
(line 74), add:

```kotlin
    // Clearing history
    @Query("DELETE FROM wifi_scan_results")
    suspend fun deleteWifiScans()

    @Query("DELETE FROM lan_devices")
    suspend fun deleteLanDevices()

    @Query("DELETE FROM tool_results")
    suspend fun deleteToolResults()

    @Query("DELETE FROM speed_test_results")
    suspend fun deleteSpeedTests()

    /** Everything the History screen lists, in one step so a failure leaves nothing half-cleared. Maps and rooms stay. */
    @Transaction
    suspend fun clearHistory() {
        deleteWifiScans()
        deleteLanDevices()
        deleteToolResults()
        deleteSpeedTests()
    }

```

In `DEMO/InMemoryStorage.kt`, directly after the `tagSpeedTest` override
(line 78), add:

```kotlin
    override suspend fun deleteWifiScans() { scans.value = emptyList() }
    override suspend fun deleteLanDevices() { lan.value = emptyList() }
    override suspend fun deleteToolResults() { tools.value = emptyList() }
    override suspend fun deleteSpeedTests() { speed.value = emptyList() }
```

Do not override `clearHistory` there; the interface's body is enough.

**Verify**: `./gradlew :androidApp:assembleDebug :webDemo:compileKotlinWasmJs :composeApp:testDebugUnitTest` → `BUILD SUCCESSFUL`; `git status --short composeApp/schemas` → no output.

### Step 2: Add the three strings in all ten languages

In `HistoryStrings.kt`, directly after `val emptySubtitle: String`, add:

```kotlin
    /** Top-bar action that deletes all saved history. Also read aloud by screen readers. */
    val clearHistory: String
    /** Title of the confirmation dialog for [clearHistory]. */
    val clearHistoryTitle: String
    /** Body of that dialog: says what is deleted, including names given to devices, and that it is permanent. */
    val clearHistoryBody: String
```

In each catalog, directly after the `override val emptySubtitle = ...` line,
add three overrides (`override val clearHistory = "..."`, and so on) with
exactly these texts:

| File | `clearHistory` | `clearHistoryTitle` | `clearHistoryBody` |
|---|---|---|---|
| `en/EnHistoryStrings.kt` | Clear history | Clear history? | Everything listed here will be deleted from this device, including names you gave to devices. This can't be undone. |
| `es/EsHistoryStrings.kt` | Borrar historial | ¿Borrar el historial? | Todo lo que aparece aquí se eliminará de este dispositivo, incluidos los nombres personalizados de los dispositivos. Esta acción no se puede deshacer. |
| `ptbr/PtBrHistoryStrings.kt` | Limpar histórico | Limpar o histórico? | Tudo o que aparece aqui será apagado deste aparelho, incluindo os nomes personalizados dos dispositivos. Esta ação não pode ser desfeita. |
| `fr/FrHistoryStrings.kt` | Effacer l'historique | Effacer l'historique ? | Tout ce qui figure ici sera supprimé de cet appareil, y compris les noms personnalisés des appareils. Cette action est irréversible. |
| `de/DeHistoryStrings.kt` | Verlauf löschen | Verlauf löschen? | Alles, was hier aufgeführt ist, wird von diesem Gerät gelöscht, einschließlich selbst vergebener Gerätenamen. Das lässt sich nicht rückgängig machen. |
| `it/ItHistoryStrings.kt` | Cancella cronologia | Cancellare la cronologia? | Tutto ciò che è elencato qui verrà eliminato da questo dispositivo, compresi i nomi personalizzati dei dispositivi. L'operazione non può essere annullata. |
| `ru/RuHistoryStrings.kt` | Очистить историю | Очистить историю? | Всё, что здесь показано, будет удалено с этого устройства, включая заданные имена устройств. Это действие нельзя отменить. |
| `ja/JaHistoryStrings.kt` | 履歴を消去 | 履歴を消去しますか？ | ここに表示されている内容はすべてこのデバイスから削除されます。デバイスに付けた名前も削除されます。この操作は元に戻せません。 |
| `ko/KoHistoryStrings.kt` | 기록 지우기 | 기록을 지울까요? | 여기에 표시된 모든 항목이 이 기기에서 삭제되며, 기기에 지정한 이름도 함께 삭제됩니다. 이 작업은 되돌릴 수 없습니다. |
| `zhhans/ZhHansHistoryStrings.kt` | 清除历史记录 | 要清除历史记录吗？ | 此处列出的所有内容都将从此设备中删除，包括为设备设置的名称。此操作无法撤销。 |

Punctuation details:

- French: before the `?` use exactly the same character(s) that
  `fr/FrCommonStrings.kt` uses before the `?` in its `deleteItemTitle` line
  (copy it from there; it may be a non-breaking space). Use the same apostrophe
  character that `FrHistoryStrings.kt` already uses elsewhere in the file.
- Italian: use the same apostrophe character `ItHistoryStrings.kt` or
  `ItCommonStrings.kt` already uses.
- Japanese and Chinese: the question mark is the full-width `？`, as in
  `zhhans/ZhHansCommonStrings.kt`'s `deleteItemTitle`.

**Verify**:
`rg -c "clearHistory" composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/i18n -g '*HistoryStrings.kt'` → eleven files, each with count `3` (the interface file may show more because of KDoc references; at least 3);
`python3 scripts/check_bidi.py` → exit 0;
`./gradlew :composeApp:testDebugUnitTest :webDemo:compileKotlinWasmJs` → `BUILD SUCCESSFUL`.

### Step 3: Add the action and the confirmation to the History screen

All edits are in `HistoryScreen.kt`.

1. Add imports, each in its alphabetical neighbourhood among the existing ones:
   ```kotlin
   import androidx.compose.material.icons.rounded.DeleteOutline
   import androidx.compose.material3.AlertDialog
   import androidx.compose.material3.TextButton
   import androidx.compose.runtime.rememberCoroutineScope
   import kotlinx.coroutines.CancellationException
   import kotlinx.coroutines.launch
   ```

2. After the line `val reduceMotion = LocalReduceMotion.current`, add:
   ```kotlin
       val scope = rememberCoroutineScope()
       var confirmClear by remember { mutableStateOf(false) }
   ```

3. In the `LargeFlexibleTopAppBar(` call, between the `navigationIcon = { ... },`
   argument and `colors = ...`, add:
   ```kotlin
                   actions = {
                       // Only offered when there is something to clear.
                       if (data?.entries?.isNotEmpty() == true) {
                           IconButton(onClick = { confirmClear = true }) {
                               Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.history.clearHistory)
                           }
                       }
                   },
   ```

4. After the closing brace of the `Scaffold(...) { padding -> ... }` block and
   before the closing brace of `HistoryScreen` (that is, between lines 207 and
   208 of the excerpt), add:
   ```kotlin
       if (confirmClear) AlertDialog(
           onDismissRequest = { confirmClear = false },
           title = { Text(strings.history.clearHistoryTitle) },
           text = { Text(strings.history.clearHistoryBody) },
           confirmButton = {
               TextButton(onClick = {
                   confirmClear = false
                   selectedKey = null
                   filterName = null
                   scope.launch {
                       try {
                           dao.clearHistory()
                       } catch (cancelled: CancellationException) {
                           throw cancelled
                       } catch (_: Exception) {
                           // Nothing was removed (it is all-or-nothing); the list still shows the truth.
                       }
                   }
               }) { Text(strings.common.delete) }
           },
           dismissButton = { TextButton(onClick = { confirmClear = false }) { Text(strings.common.cancel) } }
       )
   ```

**Verify**: `./gradlew :androidApp:assembleDebug :androidApp:assembleScreenshots :webDemo:compileKotlinWasmJs` → `BUILD SUCCESSFUL`.

### Step 4: Test the queries against a real database

Create `composeApp/src/androidTest/kotlin/com/bernaferrari/wifiweakspot/core/database/HistoryClearTest.kt`,
modelled on `AndroidBackupServiceTest.kt` (same runner annotation, same way of
building and closing the in-memory database, `runBlocking` test bodies). One test:

`clearHistoryRemovesSavedHistoryAndKeepsMaps`:

1. Build the database; take `val dao = database.dao()`.
2. Insert one row into each history table:
   - `WifiScanResultEntity(timestamp = 1, ssid = "Home", bssid = "aa:bb:cc:dd:ee:01", rssi = -50, frequency = 2437, channelWidth = 20, security = "WPA2")` via `insertWifiScan`
   - `LanDeviceEntity(ipAddress = "192.168.1.10", macAddress = null, hostname = null, vendor = null, lastSeen = 1, isOnline = true, customName = "TV", networkKey = "home")` via `insertLanDevice`
   - `ToolResultEntity(timestamp = 1, toolType = "PING", target = "example.com", resultJson = "{}", isSuccess = true)` via `insertToolResult`
   - `SpeedTestResultEntity(timestamp = 1, downloadMbps = 10f, uploadMbps = 5f, pingMs = 20f, jitterMs = 2f, serverName = "test")` via `insertSpeedTest`
3. Insert one survey project the same way `AndroidBackupServiceTest` does
   (reuse its way of constructing a `SurveyProjectEntity`), via `insertSurveyProject`.
4. Call `dao.clearHistory()`.
5. Assert that `getRecentWifiScans().first()`, `getAllLanDevices().first()`,
   `getToolHistory().first()` and `getSpeedTestHistory().first()` are all empty,
   and that `snapshotSurveyProjects()` has size 1.

**Verify**: `./gradlew :composeApp:assembleDebugAndroidTest` → `BUILD SUCCESSFUL`.
If a device or emulator is connected (`adb devices` lists one), also run
`./gradlew :composeApp:connectedDebugAndroidTest` → `BUILD SUCCESSFUL`. If none
is connected, say in your report that the new test was compiled but not run.

### Step 5: Full verification

**Verify**:
`python3 scripts/check_bidi.py` → exit 0, and
`./gradlew :composeApp:testDebugUnitTest :composeApp:lintDebug :androidApp:assembleDebug :androidApp:assembleScreenshots :webDemo:compileKotlinWasmJs :composeApp:assembleDebugAndroidTest` → `BUILD SUCCESSFUL`.

If a device is available, manual check: open History with some entries → the
bin icon shows in the top bar → tap it → dialog → Cancel leaves everything →
tap again → Delete → the list is replaced by the "Nothing saved yet" state and
the bin icon disappears. Open Map: maps and readings are still there.

## Test plan

- New instrumented test `HistoryClearTest` (Step 4): all four tables are
  emptied in one call and survey maps survive.
- Existing unit test `StringsTest` checks by reflection that every language
  provides non-blank text for every key; it covers the three new strings.
- Existing unit tests mock the DAO, so they are unaffected by the new functions.
- The screen itself has no UI-test harness in this repo; it is covered by
  compilation on all targets and the manual check.
- Verification: `./gradlew :composeApp:testDebugUnitTest` → all pass;
  `./gradlew :composeApp:assembleDebugAndroidTest` → compiles.

## Done criteria

ALL must hold:

- [ ] The Step 5 commands exit 0
- [ ] `rg -n "fun clearHistory|fun deleteWifiScans|fun deleteLanDevices|fun deleteToolResults|fun deleteSpeedTests" composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/database/dao/WifiWeakspotDao.kt` returns five matches
- [ ] `rg -l "clearHistoryBody" composeApp/src/main/kotlin/com/bernaferrari/wifiweakspot/core/i18n` lists eleven files
- [ ] `git status --short composeApp/schemas` prints nothing
- [ ] `HistoryClearTest.kt` exists and compiles
- [ ] `git status --short` lists only in-scope files (plus `plans/README.md`)
- [ ] `plans/README.md` status row updated, with a note saying whether the instrumented test and the manual check were run

## STOP conditions

Stop and report back (do not improvise) if:

- Room's code generator rejects the `@Transaction` method with a body, or any
  of the four `@Query("DELETE ...")` functions.
- A file under `composeApp/schemas/` changes.
- Another class implements `WifiWeakspotDao` besides the generated one and
  `InMemoryDao` (`rg -n ": WifiWeakspotDao" composeApp demo-fixtures webDemo androidApp -g '*.kt'`
  should list only `InMemoryStorage.kt`).
- `LargeFlexibleTopAppBar` has no `actions` parameter in this project's
  Material 3 version.
- `StringsTest` fails on one of the new strings.
- A step's verification fails twice after a reasonable fix attempt.

## Maintenance notes

- **Decisions made in this plan that the owner may want to revisit:**
  - "Clear history" removes saved devices too, and with them the names a
    person gave their devices on the Devices screen. The dialog says so. The alternative (keep named devices) would leave rows in a
    list the person just asked to clear.
  - The same tables feed other screens: the "latest test" on Home, the trend on
    the router test, recent targets in Tools, known devices on Devices. They
    all become empty too. That is intended; it is the same data.
  - The action is an icon in the top bar rather than an entry in Settings,
    because it sits next to the thing it deletes.
- If the Networks screen is in use, a new snapshot of nearby networks is saved
  about a minute later, so History will not stay empty for long on a phone
  that keeps scanning. That is expected behaviour, not a failed delete.
- The translations were written with the plan, not by native speakers. They
  follow each catalog's existing terms for "history" and "device"; have them
  reviewed with the next translation pass (`docs/localization.md`).
- Natural follow-ups, deliberately left out: deleting a single entry from its
  detail sheet; an export of history before clearing.
- Reviewer: check the dialog on a small phone in German and Russian (longest
  texts), and that TalkBack reads the bin icon as "Clear history".
