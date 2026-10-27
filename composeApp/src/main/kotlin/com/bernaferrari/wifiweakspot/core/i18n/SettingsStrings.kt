package com.bernaferrari.wifiweakspot.core.i18n

/** Settings, About, the color picker, and the first-run onboarding screen. */
interface SettingsStrings {
    // Local backup of surveys and room readings.
    val sectionBackup: String
    val backupScope: String
    val exportBackup: String
    val restoreBackup: String
    val restoreBackupExplanation: String
    val backupSaved: String
    val backupRestored: String
    val backupAlreadyRestored: String
    val backupExportFailed: String
    val backupRestoreFailed: String

    // Settings screen

    /** Section header above the "Faster scans" row. Short. */
    val sectionScanning: String

    /** "Faster scans" subtitle when Android still throttles scans (its default limit). */
    val fasterScansThrottled: String

    /** Section header for theme, colors, and signal display. Short. */
    val sectionAppearance: String

    /** Theme choice following the system light/dark setting. Segmented button: keep very short. */
    val themeAuto: String

    /** Light theme choice. Segmented button: keep very short. */
    val themeLight: String

    /** Dark theme choice. Segmented button: keep very short. */
    val themeDark: String

    /** Heading for the four visual color-style choices below the seed-color picker. */
    val colorStyle: String
    val styleExpressive: String
    val styleVibrant: String
    val styleTonal: String
    /** Color, Material, Finish: keep the engine variant's acronym. */
    val styleCmf: String

    /** Title above the buttons choosing how signal strength is shown (percent, dBm, ...). */
    val showSignalAs: String

    /** Section header for motion and haptics settings. Short. */
    val sectionComfort: String

    /** Switch row title. */
    val reduceMotionTitle: String

    /** Subtitle under "Haptic feedback". */
    val hapticsSubtitle: String

    /** Section header for background monitoring notifications. Short. */
    val sectionAlerts: String

    /** Switch row title: monitor nearby Wi-Fi in the background and notify. */
    val backgroundAlertsTitle: String

    /** Subtitle under "Background alerts". */
    val backgroundAlertsSubtitle: String

    /** Toast shown when the user declines the notification permission while turning alerts on. */
    val notificationsDenied: String

    /** Title of the slider choosing how often background alerts check Wi-Fi. */
    val checkEvery: String

    /** Slider value: an interval in seconds, e.g. "30 sec". Very short. */
    fun intervalSeconds(seconds: Int): String

    /** Slider value: an interval in whole minutes, e.g. "5 min". Very short. */
    fun intervalMinutes(minutes: Int): String

    /** Switch row title: notify when a new network appears. */
    val newNetworkAlertsTitle: String

    /** Switch row title: notify when the connected signal changes a lot. */
    val signalChangeAlertsTitle: String

    /** Subtitle under "Signal change alerts". */
    val signalChangeAlertsSubtitle: String

    /** Section header for app info. Short. */
    val sectionAbout: String

    /** Row title opening the About screen. [appName] is the brand, never translated. */
    fun aboutApp(appName: String): String

    /** App version label, e.g. "Version 1.0.0". [version] is the version number. */
    fun version(version: String): String

    /** Row title: replay the first-run onboarding. */
    val replayIntroTitle: String

    // Color picker (names are read by screen readers on each swatch)

    /** Swatch that uses colors taken from the user's wallpaper (screen reader label). */
    val wallpaperColors: String
    val colorIndigo: String
    val colorOrange: String
    val colorAmber: String
    val colorGreen: String
    val colorTeal: String
    val colorCyan: String
    val colorBlue: String
    val colorPurple: String
    val colorPink: String

    // About screen

    /** Tagline under the app name on the About screen. */
    val tagline: String

    /** Card subtitle under "GitHub" linking to the source code. Max two short lines. */
    val openSourceLicense: String

    /** Card title: email the author. Short. */
    val feedbackTitle: String

    /** Card subtitle under "Feedback". Max two short lines. */
    val feedbackBody: String

    /** Header above the author's social profile links. Short. */
    val elsewhere: String

    /** Privacy reassurance line on the About screen. */
    val privacySummary: String

    /** Text button opening the privacy policy. */
    val privacyPolicy: String

    // Onboarding

    /** Text button skipping the permission request for now. */
    val notNow: String

    /** Primary button when the permission is already granted. */
    val getStarted: String

    /**
     * Large headline on the welcome page. Contains a line break (\n) and a non-breaking hyphen in
     * "Wi‑Fi"; place the break where it balances the two lines in your language.
     */
    val welcomeHeadline: String

    /** Body text under the welcome headline. */
    val welcomeBody: String

    /** Feature row title: survey signal room by room. */
    val featureRoomsTitle: String
    val featureRoomsBody: String

    /** Feature row title: test router latency. */
    val featureRouterTitle: String
    val featureRouterBody: String

    /** Feature row title: list nearby networks and channels. */
    val featureNearbyTitle: String
    val featureNearbyBody: String
}
