# WiFi Weakspot Privacy Policy

**Effective date: October 24, 2026**  
**Developer: Bernardo Ferrari**  
**Android package: `com.bernaferrari.wifiweakspot`**

## Your Wi-Fi data stays with you

WiFi Weakspot is a Wi-Fi analyzer designed to work on your device. No account is required. The app has no advertising, analytics, tracking SDKs, or automatic crash-reporting service.

**We do not collect or upload your Wi-Fi scans, network identifiers, device discoveries, room readings, floor plans, or test history to the developer or a developer-operated server. We do not sell your data or use it for advertising.**

The app does process network information locally to provide its features. Optional network tools, external websites, and sharing work as described below.

## Information processed on your device

Depending on the features you use and the permissions you grant, the app can access:

- **Wi-Fi information:** network names (SSIDs), access-point identifiers (BSSIDs), signal strength, channels, frequency bands, security type, and connection details.
- **Local network information:** IP addresses, device names, available hardware addresses and manufacturer information, announced services, and responses to discovery or diagnostic requests.
- **Your saved work:** room names, signal readings, timestamps, router-check and tool results, coverage-map points, and floor-plan images you choose to import.
- **Preferences:** appearance, favorite networks, signal-display preferences, and background-alert settings.

This information is used to display nearby signals, compare readings, build coverage estimates, run diagnostics, and provide the settings or alerts you request. Saved records are kept in the app's private storage on your device. They are not synchronized to a developer cloud service.

## Permissions and background alerts

Android restricts access to Wi-Fi scans and network names because Wi-Fi identifiers can reveal location. The app requests location permission for these Wi-Fi features. **It does not request GPS coordinates or track your geographic movements.** Positions you place on a coverage map are positions within that map, not GPS locations.

Network and Wi-Fi permissions support connection information, scanning, and diagnostics. You choose any floor-plan image through Android's image picker; the app does not browse your photo library.

If you enable background alerts, the app can continue checking nearby Wi-Fi networks through an Android foreground service and show notifications. Android classifies this as a location foreground service because it performs Wi-Fi scans. It does not add GPS tracking or send scan results to us. You can turn off alerts in the app, stop the service, or revoke permissions in Android settings.

## Network requests and external services

WiFi Weakspot is a network tool, so some features send network traffic:

- Router checks, device discovery, ping, and port checks communicate with the router, devices, or hosts being checked. Those destinations can see the requests and the source network address.
- DNS lookups, including name resolution needed for other tests, use your device's system resolver. Your configured DNS provider can receive those queries.
- Internet speed testing loads **fast.com**, a Netflix service, when you choose that feature. It connects to third-party servers and can process your IP address, browser/device information, cookies, and website storage under its own privacy practices.
- Opening a router page, the source repository, a social profile, or another external link contacts that destination. Websites opened in the app's web view or your browser have their own privacy practices.

These requests are part of the feature you use. The app does not attach your saved room readings, floor plans, or scan history to external website requests.

Android, Google Play, your browser, and your email provider may separately process information under their own privacy policies, such as installation or diagnostic information. This policy describes the app's practices, not those platform services.

## Sharing is your choice

Exporting, sharing, or copying a result is initiated by you. Reports, files, and images may contain network names, identifiers, device addresses, room names, or floor-plan details. Review them before choosing a recipient or another app. The receiving app or service controls what happens to its copy; copying also places the selected information on Android's clipboard.

## Storage, retention, and deletion

Saved records remain on your device until you delete them using the available in-app controls or clear the app's storage in Android settings. Uninstalling normally removes app-private data, but Android may offer to keep it; choose removal if you want it deleted. Shared exports, original images, and copies saved in other apps must be deleted separately. Temporary export files may remain in the app's cache until it is cleared or Android removes them.

The app's backup configuration excludes its database, saved floor-plan copies, and main settings from Android cloud backup and device transfer. There is no developer-held copy of your network records for us to retrieve or delete.

Android's app-storage protections apply to local records. The app does not promise additional encryption for exported files or control the security of another app, a diagnostic destination, or your device.

## Contact and policy updates

For privacy questions, contact **Bernardo Ferrari** at [bernaferrari2@gmail.com](mailto:bernaferrari2@gmail.com). If you contact us, we receive your email address and whatever you choose to include so we can respond; the app does not automatically attach diagnostics or network records to your message. Avoid sending passwords or other unnecessary private information.

This policy covers the Android app. Visiting this policy on GitHub is subject to [GitHub's privacy statement](https://docs.github.com/en/site-policy/privacy-policies/github-general-privacy-statement).

If the app's privacy practices change, this policy will be updated with a new effective date. The current policy is available from the app's About screen and in the [public source repository](https://github.com/bernaferrari/wifi-weakspot).
