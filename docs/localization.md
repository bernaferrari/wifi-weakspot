# Localization

The shared Android/web UI uses `core/i18n/Strings.kt`. English lives in `core/i18n/en/`; screens read the `strings` composition local, while services, exports, and view models use `appStrings`. Android supplies the current configuration locale at the app root so a per-app language change refreshes the catalog.

The target languages are English (`en`), Spanish (`es`), Brazilian Portuguese (`pt-BR`), French (`fr`), German (`de`), Italian (`it`), Russian (`ru`), Japanese (`ja`), Korean (`ko`), and Simplified Chinese (`zh-Hans`). All ten catalogs are implemented and registered in Android’s per-app language settings. The nine new catalogs began as machine-translation drafts and received a substantial editorial and technical review, including terminology, sentence meaning, placeholders, date order, room names, grammatical agreement, and Russian plurals. All ten languages have browser rendering checks at normal and enlarged text sizes. Independent native-speaker validation remains outstanding. Follow-up English UI checks now also cover a physical Android phone and an emulator; this is not a native-device review of all ten translations. No translation service is called at runtime.

## Adding a catalog

1. Implement all seven string interfaces in a language directory and assemble them into a `Strings` object. Do not delegate missing entries to English; direct implementation makes missing keys compile errors.
2. Reuse `CommonStrings` for shared actions and labels; do not add a new key for an existing meaning. Translate entire messages, including interpolated values, in the catalog functions. Each language owns its word order and plural rules. Check zero, one, two, five, eleven, twenty-one, and one hundred and one; do not copy English's singular/other rule into Russian.
3. Keep calendar arrays in Monday–Sunday and January–December order. Keep the twelve room suggestions in their documented order, which identifies their icons. Picked room names and user-entered device names stay as saved when the language changes.
4. Register the completed object in `AppLanguage`, then add the same tag to `androidApp/src/main/res/xml/locales_config.xml`. Regional and script tags are matched before language fallbacks; Traditional Chinese requests do not resolve to a Simplified Chinese catalog.
5. Run `./gradlew :composeApp:testDebugUnitTest :webDemo:compileKotlinWasmJs :androidApp:assembleDebug`.
6. Review every screen on a device in the language, including long labels, dialogs, charts, large font sizes, screen-reader descriptions, notifications, generated names, and shared reports. Change the per-app language with history and saved rooms already present. Automated tests check catalog structure and locale resolution, not translation accuracy or rendered layout.

## Text versus data

Translate actions, accessibility descriptions, tool verdicts, notifications, permission explanations, share-sheet titles, and human-readable reports. Keep SSIDs, addresses, user names, brands, protocol names, and units intact.

Database keys, enum names, CSV/JSON field names, file timestamps, and diagnostic wire formats are stable data. In particular, the legacy ping/DNS history format contains `Reply`, `time=… ms`, and `Error:` markers that parsers depend on. Do not translate those stored records. The UI renders localized summaries from them, and failed DNS history uses a catalog message instead of displaying a saved English exception. CSV headers remain stable for existing consumers.

Do not trim or inspect translated sentences to make product decisions. Match stable values or enums instead. Never show arbitrary platform exception messages directly; map errors to catalog messages at the presentation boundary.

## Catalog scope

The audits reduced the catalog from 745 to 551 entries. The first pass removed 29 unused keys and consolidated 32 duplicates. The second removed another 72 entries by sharing equivalent actions, Wi-Fi prompts, status labels, and context-specific variants of save/delete errors; keeping internal sampling diagnostics outside translation catalogs; and formatting protocol names and value-only lists directly. Unused legacy permission/export components and an unused access-point text property were removed with their strings. The remaining entries include UI copy, accessibility, setup guidance, generated reports, diagnostic errors, and notifications. Similar English words with different grammatical contexts remain separate.

Share complete messages when their meaning and recovery action match. Keep specific permission, measurement, data-loss, and network-scope guidance. Do not assemble translated sentences from generic verbs and nouns to reduce a key count: agreement and word order differ across languages. Internal sampling exception messages stay diagnostic; their callers present localized recovery messages.

The third pass removed another 61 entries. Chart help now uses one explanatory paragraph per diagram, retaining the +1 grouping and color meanings. Settings omit subtitles that restate their labels. Saved-room toasts confirm the save; the room list retains the comparison. Equivalent scan/privacy prompts and share/status labels are reused. Ping/DNS/port model diagnostics remain stable internal data; their result screens use localized summaries and recovery messages. All ten catalogs have 551 entries each.

## Quality verification — 2026-10-25

The quality pass keeps 551 entries per language. It corrects false friends and ambiguous source wording rather than reducing the catalog further. Signal-change notifications use the same quality labels as the UI; formatted channel readings reuse the localized channel abbreviation. Port-count units now receive the count for grammatical agreement. Date phrases retain their capitalization.

| Language | Measurement | Wi-Fi radio hardware | Fair signal |
| --- | --- | --- | --- |
| English | measurement | Wi-Fi radio | Okay |
| Spanish | medición | módulo Wi-Fi | Aceptable |
| Brazilian Portuguese | medição | rádio Wi-Fi | Razoável |
| French | mesure | module Wi-Fi | Correct |
| German | Messwert / Messung | WLAN-Funkmodul | Ausreichend |
| Italian | misurazione | modulo Wi-Fi | Discreto |
| Russian | измерение | радиомодуль Wi-Fi | Средний |
| Japanese | 測定値 / 測定 | Wi-Fi無線モジュール | 普通 |
| Korean | 측정값 / 측정 | Wi-Fi 무선 모듈 | 보통 |
| Simplified Chinese | 读数 / 测量 | Wi-Fi 无线模块 | 一般 |

Network responses are not social replies. DNS addresses are not postal addresses. “In the open” means an unobstructed indoor position, not outdoors. “Open” ports describe a state, not an action; “tap” describes touch input, not a faucet. A coverage survey means physical measurements, not a questionnaire. Color choices must have distinct accessible names.

The setup instructions use labels verified against [Android SettingsLib resources](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/packages/SettingsLib/res/) and [Android Settings resources](https://android.googlesource.com/platform/packages/apps/Settings/+/refs/heads/main/res/) for each locale: developer options, networking, Wi-Fi scan throttling, and build number. Device manufacturers can use different names, which the instructions acknowledge.

Browser verification uses the real shared Compose UI with simulated network services. For all ten locales, it covers onboarding, Home, Settings (top/bottom), Rooms, Networks, and chart help at 390×844. It additionally checks onboarding and Settings at 320×640 with 150% text, including scrolling to the privacy explanation and activating the primary action. No browser runtime errors were reported. The demo sets the document language for assistive technology. `?captureFontScale=1.5` reproduces the enlarged-text scenario.

Automated regression tests cover quality-label consistency, indoor router placement, measurement-history vocabulary, open-port agreement, unique color names, and channel abbreviations, alongside the catalog completeness, locale resolution, calendar-order, Unicode, and formatting checks. The Android debug APK and web demo build successfully. These checks do not constitute independent native-speaker certification or real-radio testing on Android hardware.


### Follow-up omission audit

Audited shared UI and accessibility text, Android resources and notifications, error presentation,
saved tool history, and the web demo bootstrap. Fixed saved port summaries bypassing the localized
unknown-service label, including legacy `Unknown` and empty markers. The seeded demo map now uses
the localized default map name. The HTML bootstrap title uses the brand until the localized title
loads, and the Android app-name resource uses the same brand. No catalog entries were added.
Network names, hostnames, protocol names, and stable stored/export formats remain data.


### Networks layout and channel advice

The band selector is in the `MediumFlexibleTopAppBar.actions` slot from the start, matching
the app-bar component and title placement of Home, Rooms, and Settings. Its width, GHz units, and count
badges interpolate from the Material scroll behavior’s collapse fraction, using the same controls
throughout. Buttons remain 48 dp high with a 16 dp trailing inset; counts yield first at large text sizes.
Phone bars collapse normally. In side-by-side layouts, only the left pane owns the
app bar and graph; the right list scrolls independently. The graph fills the available height.
The selection context stays above the right list so its clear action remains reachable.

Chart channel numbers use one color and weight. Network-wave colors identify networks, not
channel quality. The old Busy gradient and its help card were removed because a scan cannot
measure actual channel traffic and the second color scale obscured the recommendation.
The History heading is a single line; the mode button retains its full accessible name.
A trailing Info button opens network details; the row itself selects a network on the chart.
Both modes expose the animated clear-selection control. Clearing history also exits its frozen
inspection. Reduced motion disables the selection transition.

Channel advice matches the band buttons at a 48 dp minimum container height and opens a scrollable Material sheet.
The sheet shows a channel badge, a relative comparison, and three translated router steps.
Its immutable snapshot keeps the opened scan’s comparison stable while new scans arrive.
Scores group guest/hidden SSIDs by radio, discard invalid signal readings, and weight the shared
portion of a candidate’s 20 MHz band rather than counting any overlap as a full collision.
The score remains a relative scan heuristic; it does not measure airtime or promise throughput.
Unknown 5/6 GHz router support still prevents an invented channel recommendation.

Follow-up verification includes catalog completeness, all ten locales opening and closing the
advice sheet at 150% text, phone/narrow/tablet/desktop collapse and selection interactions,
independent right-list scrolling, and signal-history clearing after inspection. Native English
checks on a Samsung SM-S931B (Android 17) and an API 36 resizable emulator confirm the app
runs with real system bars, the tablet chart stays fixed, Info opens details without selecting,
and history selection can be cleared. Device Wi-Fi names stay in ignored `build/qa/` captures;
public gallery artwork uses the browser’s simulated networks, capped at 84% signal.

The earlier catalog-reduction and browser-verification sections record the initial 551-entry
quality pass. Channel-advice features subsequently added five entries, while removal of the
Busy strip removed two obsolete entries. Catalogs remain complete and symmetric.
