# Localization

The shared Android/web UI uses `core/i18n/Strings.kt`. English lives in `core/i18n/en/`; screens read the `strings` composition local, while services, exports, and view models use `appStrings`. Android supplies the current configuration locale at the app root so a per-app language change refreshes the catalog.

The target languages are English (`en`), Spanish (`es`), Brazilian Portuguese (`pt-BR`), French (`fr`), German (`de`), Italian (`it`), Russian (`ru`), Japanese (`ja`), Korean (`ko`), and Simplified Chinese (`zh-Hans`). All ten catalogs are implemented and registered in Android’s per-app language settings. The nine new catalogs were drafted with machine translation, then corrected for core UI terminology, technical names, placeholders, date order, room names, and Russian plurals. They have not received a complete native-speaker or device-layout review. No translation service is called at runtime.

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
