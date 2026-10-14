# PulseWave

A Material 3 Expressive Wi-Fi analyzer for finding bad Wi-Fi around the home. Everything is measured on the phone or inside the home network; nothing is uploaded.

## Workflows

- **First run:** a short welcome (what the app does), the one permission it needs (skipped if already granted; if Android has stopped asking, Allow opens the app's settings instead), and an optional "live updates" step shown only when scans are throttled and Developer options can lift the limit. Settings → About can replay it.
- **Home:** the connected network at a glance: a shape that morphs with signal quality, a plain-language verdict, band, channel, link rate, and internet status. Setup cards appear only when something is missing (scan permission, Location, or the optional faster-scans tip).
- **Walk (signal finder):** tap **Start walking** and move around. The connected link's signal is read every second (Android does not throttle it) and the router is pinged once a second. The shape grows and shrinks with signal, haptics mark weak spots, and a timeline records the walk. **Finish** shows the strongest and weakest readings, time spent in weak signal, and typical router response. **Pin spot** saves a named place in one tap; saved spots are ranked weakest first, with fixes.
- **Networks:** nearby access points by band. The channel graph draws each network as a hill exactly as wide as its channel, names the peaks, emphasises your network and channels 1, 6 and 11, and tints signal zones; signal over time shows how steady each network is. Colors match the list. Also a channel suggestion that mentions your own network, and a segmented list with your network marked. It refreshes about every 4 s when scan throttling is off, otherwise every 30 s to stay within Android's 4-scans-per-2-minutes limit.
- **Wi-Fi speed:** measures only the hop between the phone and the router: median response time, steadiness, and loss from 25 pings, then a 5-second UDP burst to the router's discard port, paced just above the negotiated rate and clamped to it. Results go to History. Internet speed is optional and opens fast.com in a web view.
- **Faster scans:** detects Developer options and Wi-Fi scan throttling (`WifiManager.isScanThrottleEnabled` on Android 11+) and walks through turning throttling off, re-checking when you return. An ADB command is offered as an alternative.
- **Root and system installs (optional):** on rooted phones, a collapsed "Rooted phone?" option at the bottom of Faster scans offers "Use root". After the root manager grants it, scans are started with `cmd wifi start-scan` from the root shell, which Android does not throttle, and results arrive through the normal scan broadcast. On Android 9 and 10 root switches the global throttle setting off instead. Root is never requested without a tap, can be turned off, and falls back to normal scanning if access is revoked. Unrooted phones never see the option. When installed as a privileged system app (holding `NETWORK_SETTINGS`), Android already exempts its scans, and the app reports unlimited scanning.
- **Tools:** devices on the LAN, ping, DNS, port checks, and History. The floor-plan heatmap survey is still available from Walk for detailed mapping.

Signal levels use one scale everywhere: Excellent ≥ -55 dBm, Good ≥ -67, Fair ≥ -75, Weak ≥ -82, otherwise Dead zone. Signal colors are toned for light and dark themes and harmonized with dynamic color.

## Measurement limits

RSSI-based distance is approximate. The router upload burst reflects how fast this phone can push data over the air; download throughput to the phone needs a peer on the network, so the negotiated receive rate is shown instead. Some routers ignore pings; the test reports that rather than guessing. LAN discovery can miss isolated devices. Android may delay scans; unavailable readings are shown as unavailable.

The previous Cloudflare internet speed test is kept, unbuilt, in `archive/cloudflare-speedtest/`.

`composeApp` is the library (UI and domain). `androidApp` is the application.

## Build

```
./gradlew :composeApp:testDebugUnitTest :androidApp:lintDebug :androidApp:assembleDebug
```

The build needs JDK 17 and Android SDK platform **37.2**. `minSdk` is 24 and `targetSdk` is 36. Android CI runs tests, lint, and the debug build on pushes to main and pull requests.

Unit tests cover the signal-quality scale, walk and spot summaries, router latency verdicts, channel geometry (including 80+80 MHz), freshness and history, survey recovery/sampling/calibration, database migration preservation, LAN discovery, measured port history, DNS contracts, and tool target handling. The disposable-emulator smoke script in `scripts/android_smoke.py` exercises navigation and state recovery. This environment’s emulator system services failed during the UI run, so that smoke check remains unverified. Physical-device checks are still required for RF accuracy, sensor drift, throughput calibration, and battery use.

## License

Apache License 2.0. See [LICENSE](LICENSE).
