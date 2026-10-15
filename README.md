# PulseWave

A Material 3 Expressive Wi-Fi analyzer for finding bad Wi-Fi around the home. Scan and spot history stay on the device. Diagnostic tools contact the selected network targets; the optional internet speed test uses fast.com.

## Workflows

- **First run:** a short welcome and the one permission it needs (skipped if already granted; if Android has stopped asking, Allow opens the app's settings instead). Settings → About can replay it. Faster scans are offered in context instead: Networks shows a hint only when Android actually pauses scanning, and Settings keeps a row for it.
- **Home:** your Wi-Fi and every tool in one place. The connected network at a glance: a shape that morphs with signal quality, a plain-language verdict, band, channel, link rate, and internet status. A nearby summary says how crowded your channel is and whether a clearer one exists. Setup cards appear only when something is missing (scan permission or Location). The primary Find weak spots action leads to walking and rechecks. Supporting tools (Router check, Devices, Diagnose, History) sit in an equal-height grid.
- **Walk (signal finder):** tap **Start walking** and move around. The connected link's signal is read every second (Android does not throttle it) and the router is pinged once a second. The shape grows and shrinks with signal, haptics mark weak stretches, and a timeline retains measured, disconnected, and unavailable intervals. Percentages are weighted by elapsed time; router replies expire after two seconds and are counted only once. **Finish** shows the strongest and weakest readings, time spent in weak signal, and typical router response. **Pin spot** saves a named place in one tap; saved spots are ranked weakest first, with fixes.
- **Networks:** nearby access points by band. The channel graph draws each network as a hill exactly as wide as its channel, names the peaks, emphasises your network and channels 1, 6 and 11, and tints signal zones; signal over time shows how steady each network is. Colors match the list. Also a channel suggestion that mentions your own network, and a segmented list with your network marked. It refreshes about every 4 s when scan throttling is off, otherwise every 30 s to stay within Android's 4-scans-per-2-minutes limit.
- **Router check:** measures median response time, variation between consecutive replies, and missed replies from 25 local pings. It does not claim delivered throughput or predict streaming performance. Transfer-speed measurement would require a cooperating receiver. Old unverified sender-rate estimates remain stored but are hidden in history and trends. Internet speed is optional and opens fast.com.
- **Saved spots and rechecks:** each save collects four fresh connected-link readings while you stand still. Readings are preserved, grouped by network identity and room name, with a tappable history and Recheck action. Before/after comparisons show RSSI change and call out access-point or band changes. Named networks are scoped by exact SSID (identically named networks cannot be distinguished automatically); hidden networks fall back to BSSID. You can browse saved networks offline, but rechecks require connecting to the chosen network. No existing readings are discarded during upgrade.
- **Faster scans:** detects Developer options and Wi-Fi scan throttling (`WifiManager.isScanThrottleEnabled` on Android 11+) and walks through turning throttling off, re-checking when you return. An ADB command is offered as an alternative.
- **Root and system installs (optional):** on rooted phones, a collapsed "Rooted phone?" option at the bottom of Faster scans offers "Use root". After the root manager grants it, scans are started with `cmd wifi start-scan` from the root shell, which Android does not throttle, and results arrive through the normal scan broadcast. On Android 9 and 10 root switches the global throttle setting off instead. Root is never requested without a tap, can be turned off, and falls back to normal scanning if access is revoked. Unrooted phones never see the option. When installed as a privileged system app (holding `NETWORK_SETTINGS`), Android already exempts its scans, and the app reports unlimited scanning.
- **Tabs:** Home, Networks and Walk. Phones get a bottom bar; tablets, foldables and desktop windows get a navigation rail, a two-column Home and Networks, and readable widths elsewhere. The floor-plan heatmap survey is available from Walk.

Signal levels use one scale everywhere: Excellent ≥ -55 dBm, Good ≥ -67, Fair ≥ -75, Weak ≥ -82, otherwise Dead zone. Signal colors are toned for light and dark themes and harmonized with dynamic color.

## Measurement limits

RSSI-based distance is approximate. Negotiated link rates are radio metadata, not measured throughput. Router ICMP responsiveness is a separate measure from internet performance. Some routers ignore pings; the test reports that rather than guessing. LAN discovery can miss isolated devices. Android may delay scans; unavailable readings are shown as unavailable.

The previous Cloudflare internet speed test is kept, unbuilt, in `archive/cloudflare-speedtest/`.

`composeApp` holds the app. Its `src/main/kotlin` is platform-free: every screen, view model, chart and repository, plus interfaces for device services (Wi-Fi scanning, router probe, sensors, storage, `PlatformActions`, `PlatformTheme`). `src/androidMain/kotlin` implements those services for Android and hosts the app through `AndroidApp()`. `androidApp` is the application shell.

`webDemo` compiles the same `src/main/kotlin` for Kotlin/Wasm and swaps in simulated services: a block of ~30 drifting networks, a router whose replies and speed follow the signal, in-memory storage. There is no demo-specific UI; the browser runs the real app, onboarding included.

## Web demo

```
./gradlew :webDemo:wasmJsBrowserDevelopmentRun
```

Or build static files with `./gradlew :webDemo:wasmJsBrowserDistribution` and serve `webDemo/build/dist/wasmJs/productionExecutable/`. In the browser, system Settings shortcuts do nothing, shared reports go to the clipboard, floor plan images can't be imported, and the internet speed test opens fast.com in a new tab.

## Build

```
./gradlew :composeApp:testDebugUnitTest :androidApp:lintDebug :androidApp:assembleDebug
```

The build needs JDK 17 and Android SDK platform **37.2**. `minSdk` is 24 and `targetSdk` is 36. Android CI runs tests, lint, and the debug build on pushes to main and pull requests.

Unit tests cover the signal-quality scale, walk and spot summaries, router latency verdicts, channel geometry (including 80+80 MHz), freshness and history, survey recovery/sampling/calibration, database migration preservation, LAN discovery, measured port history, DNS contracts, and tool target handling. The disposable-emulator smoke script in `scripts/android_smoke.py` exercises navigation and state recovery. This environment’s emulator system services failed during the UI run, so that smoke check remains unverified. Physical-device checks are still required for RF accuracy, sensor drift, throughput calibration, and battery use.

## License

Apache License 2.0. See [LICENSE](LICENSE).
