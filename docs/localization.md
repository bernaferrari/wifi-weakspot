# Localization

The shared Android/web UI uses `core/i18n/Strings.kt`. English lives in `core/i18n/en/`; screens read the `strings` composition local, while services, exports, and view models use `appStrings`. Android supplies the current configuration locale at the app root so a per-app language change refreshes the catalog.

The target languages are English (`en`), Spanish (`es`), Brazilian Portuguese (`pt-BR`), French (`fr`), German (`de`), Italian (`it`), Russian (`ru`), Japanese (`ja`), Korean (`ko`), and Simplified Chinese (`zh-Hans`). All ten catalogs are implemented and registered in Android’s per-app language settings. The nine new catalogs began as machine-translation drafts and received a substantial editorial and technical review, including terminology, sentence meaning, placeholders, date order, room names, grammatical agreement, and Russian plurals. All ten languages have browser rendering checks at normal and enlarged text sizes. Independent native-speaker and physical Android-device validation have not been performed. No translation service is called at runtime.

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


### Tablet channel layout

Wide Networks layouts place the channel context above the network list in the right column,
leaving the graph its own column. Narrow layouts keep context below the graph. The same
available-width breakpoint controls both the columns and context placement. Recommendation
copy wraps, graph height follows available height and font scale, and the channel graph measures
localized axis labels to reserve enough room for them. Graph hit testing uses that same gutter.
The Channels toggle reuses the Networks navigation icon.

Verified the shared Compose browser layout at 390 × 844, 720 × 1280, 960 × 540, and
1280 × 720 logical pixels, plus all ten languages at 150% text on the landscape tablet.
No browser runtime errors occurred. Android assembly and all 214 unit tests pass. Home and
Networks artwork was recaptured for phone, portrait tablet, landscape tablet, and desktop;
README assets pass their provenance check. Simulated signals now cap at 84%, with real
Android signal mapping unchanged. These are browser layout checks, not physical-device checks.

Band buttons display the frequency with a numeric count badge, avoiding repeated visible
“networks” labels. Their localized accessible descriptions retain the full count phrase;
checked-state semantics remain on the toggle. The count wraps below the frequency when
needed, verified at 320 dp with 150% text as well as tablet and desktop widths.


Channel advice uses a 56 dp minimum container height, with larger text allowed to grow.
The number badge accompanies “Suggested channel”; comparisons use “Less crowded than
channel …”, and ties never imply that changing channels will improve performance. All ten
catalogs carry the updated messages, without adding entries.


The channel-advice pill is now an accessible button that opens a dismissible, scrollable
Material bottom sheet. It explains the scan comparison and gives router instructions using
the selected channel and band. The sheet preserves the advice shown at opening while
scans continue. Two new catalog entries provide the complete explanation and instructions
in every language; no fragments are stitched into translated sentences.


The advice sheet presents a channel badge, measured overlap bars, and three numbered router
steps. Bars compare channels 1, 6, and 11 (plus the inspected channel if different), normalized
to the largest observed overlap score in that snapshot. They are not invented performance
percentages. The suggested channel has both a check mark and an accessible state description.
The snapshot includes scores, so live scans cannot change the comparison while it is open.
