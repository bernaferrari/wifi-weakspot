package com.bernaferrari.wifiweakspot.core.i18n

/** Shared actions, navigation, permissions, and small reusable components. */
interface CommonStrings {
    fun deleteItemTitle(name: String): String
    val hiddenNetworkShort: String
    val network: String
    val tools: String
    val maker: String
    val noReply: String
    val fastest: String
    val address: String
    val responseTime: String
    val checkAgain: String
    val rename: String
    val stop: String
    val unknown: String
    val couldNotSaveChanges: String
    val couldNotDeleteItem: String

    val hiddenNetwork: String
    val fasterScans: String
    val openWifiSettings: String
    val signalUnavailable: String
    val devices: String
    val openPorts: String
    val nameLabel: String
    val historyTitle: String
    val routerCheck: String
    val testWebsite: String
    val yourRouter: String
    val yourNetwork: String

    /** Web demo action that opens content outside the app. */
    val openInNewTab: String
    val mapSharingAndroidOnly: String
    // Generic actions, reused across screens.

    /** Back navigation button (accessibility label for the arrow icon). */
    val back: String
    /** Close button or icon (accessibility label). */
    val close: String
    /** Dismisses a dialog without applying changes. */
    val cancel: String
    /** Saves the current edit. */
    val save: String
    /** Confirms and finishes a flow. */
    val done: String
    /** Opens the system share sheet. Short: also used as a small FAB label. */
    val share: String
    /** Deletes an item. */
    val delete: String

    // Bottom navigation tabs. Keep very short: they sit under icons in a 4-item bar.

    /** Tab: the main overview screen. */
    val tabHome: String
    /** Tab: list of nearby Wi-Fi networks. */
    val tabNetworks: String
    /** Tab: room-by-room signal survey. */
    val tabRooms: String
    /** Tab: app settings. */
    val tabSettings: String

    // Full-screen location permission prompt.

    /** Headline on the screen that asks for location permission to scan Wi-Fi. */
    val permissionScreenTitle: String
    /** Primary button that triggers the system permission dialog. */
    val permissionAllowAccess: String
    /** Secondary button that opens the app's page in system settings. */
    val openAppSettings: String

    // Permission card (one per permission type).

    // Full-screen permission welcome.

    // Export sheet.

    // Signal badge.

    /**
     * Accessibility description of a signal badge.
     * @param quality the signal quality word, e.g. "Good".
     * @param reading the formatted signal reading, e.g. "72%" or "-60 dBm".
     */
    fun signalBadgeDescription(quality: String, reading: String): String

    // Scan throttle radar and status bar.

    /** Big status word in the middle of a radar dial. Very short, shown in caps. */
    val scanReady: String
    /** Small caption under [scanReady]: how many scans are available. */
    fun scansCount(count: Int): String
    /** Tiny caption under a large number of scans remaining, e.g. "3" / "left". One short word. */
    val scansLeftCaption: String
    /** Tiny caption under a countdown timer, e.g. "1:30" / "waiting". One short word. */
    val scanWaitingCaption: String
    /** Tiny caption shown when scan limits are disabled. Very short, shown in caps. */
    val scanDevMode: String
    /** Status line: a scan can start now. */
    val readyToScan: String
    /** Status line: how many scans are still allowed in this window. */
    fun scansRemaining(count: Int): String
    /** Status line with a countdown. @param time minutes and seconds already formatted, e.g. "1:05". */
    fun nextScanIn(time: String): String
    /** Status line while the scan limit is being determined. */
    val checking: String

    // Relative time. Keep short; the English uses non-breaking spaces so it never wraps.

    /** Age of something seen less than a minute ago. */
    val justNow: String
    /** Age in minutes, abbreviated. */
    fun minutesAgo(minutes: Long): String
    /** Age in hours, abbreviated. */
    fun hoursAgo(hours: Long): String
    /** Age in days, abbreviated. */
    fun daysAgo(days: Long): String
}
