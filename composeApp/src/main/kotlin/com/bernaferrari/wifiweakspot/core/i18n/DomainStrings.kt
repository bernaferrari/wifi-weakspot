package com.bernaferrari.wifiweakspot.core.i18n

import com.bernaferrari.wifiweakspot.core.lan.DeviceKind
import com.bernaferrari.wifiweakspot.core.monitoring.MonitorSignalLevel

/**
 * Text produced by the app's core logic rather than by a screen: signal-quality names, device
 * kinds, router verdicts, room insights, background-monitor notifications, and messages from the
 * network tools that end up on screen.
 */
interface DomainStrings {

    // region Wi-Fi bands and display

    /** Settings choice for showing signal as bars (other choices are "%" and "dBm"). Segmented button, keep very short. */
    val signalDisplayBars: String

    // endregion

    // region Signal quality scale (strongest to weakest)

    /** Short quality label, used on chips and chart legends. Keep short. */
    val qualityExcellentLabel: String
    /** Headline for a reading at this quality, e.g. on a large signal card. */
    val qualityExcellentHeadline: String
    /** One-sentence explanation under the headline. */
    val qualityExcellentExplanation: String

    /** Short quality label. Keep short. */
    val qualityGoodLabel: String
    val qualityGoodHeadline: String
    val qualityGoodExplanation: String

    /** Short quality label for a middling signal ("Okay" in English). Keep short. */
    val qualityFairLabel: String
    val qualityFairHeadline: String
    val qualityFairExplanation: String

    /** Short quality label. Keep short. */
    val qualityWeakLabel: String
    /** Headline for a weak reading: a spot in the home with weak Wi-Fi. */
    val qualityWeakHeadline: String
    val qualityWeakExplanation: String

    /** Short quality label: almost no signal reaches this place. Keep short. */
    val qualityDeadZoneLabel: String
    val qualityDeadZoneExplanation: String

    // endregion

    // region Devices on the local network

    /** Device kind: the home router. */
    val deviceKindRouter: String
    /** Device kind and name for the phone running the app. */
    val deviceKindThisDevice: String
    val deviceKindPhone: String
    val deviceKindComputer: String
    val deviceKindTv: String
    val deviceKindSpeaker: String
    val deviceKindPrinter: String
    val deviceKindConsole: String
    val deviceKindCamera: String
    val deviceKindSmartHome: String
    /** Generic device kind when nothing more is known. */
    val deviceKindUnknown: String

    /** Name for a device that has no name, maker, or known kind. */
    val unnamedDevice: String

    /**
     * Name for a device known only by its maker and guessed kind, e.g. "Samsung TV or streamer"
     * or, when [kind] is [DeviceKind.Unknown], "Samsung device".
     * @param maker Short company name, such as "Samsung" (not translated).
     */
    fun makerDevice(maker: String, kind: DeviceKind): String

    // endregion

    // region Router response check

    /** Headline of a router check where replies were quick and steady. */
    val verdictGreatHeadline: String
    val verdictGreatExplanation: String
    val verdictGoodHeadline: String
    val verdictGoodExplanation: String
    val verdictSlowHeadline: String
    val verdictSlowExplanation: String
    /** Headline when too many replies were lost to judge the router. */
    val verdictUnreliableHeadline: String
    val verdictUnreliableExplanation: String

    // endregion

    // region Room checks

    /** Suggested room names offered when saving a spot. The person may also type their own. */
    /** Exactly 12 entries: living, kitchen, bedroom, office, kids, bathroom, dining, hall, garage, basement, balcony, garden. */
    val roomSuggestions: List<String>

    /** Coverage headline before any room was checked. */
    val coverageNoRooms: String
    /** Coverage headline when exactly one room is weak. @param room Room name the person chose. */
    fun coverageWeakInRoom(room: String): String
    /** Coverage headline when several rooms are weak. @param count Number of weak rooms (2 or more). */
    fun coverageWeakInRooms(count: Int): String
    /** Coverage headline when nothing is weak. @param count Number of rooms checked (1 or more). */
    fun coverageRoomsCheckedNoneWeak(count: Int): String

    /**
     * Tip when a weak room used a high band.
     * @param room Room name. @param band Band name such as "5 GHz" or "6 GHz".
     */
    fun tipHighBandWeak(room: String, band: String): String
    /**
     * Tip when the signal differs a lot between rooms.
     * @param dropDb Difference in dB (20 or more). @param bestRoom Strongest room. @param worstRoom Weakest room.
     */
    fun tipSignalDrop(dropDb: Int, bestRoom: String, worstRoom: String): String
    /** Tip when several access points served the rooms. @param count Number of access points (2 or more). */
    fun tipMultipleAccessPoints(count: Int): String
    /** General router placement tip. */
    val tipRouterPlacement: String

    /** Shown under a room's first saved reading. "·" separates two short phrases. */
    val spotFirstReading: String
    /** Shown when the latest reading cannot be compared with the previous one. */
    val spotDifferentNetwork: String

    /**
     * One line comparing a room's reading with the previous one, e.g. "Signal 6 dB stronger since
     * last check" or "Signal about the same · access point changed; band changed".
     * @param deltaDb Change in dB, positive = stronger. Differences under 4 dB count as "about the same".
     * @param accessPointChanged The phone used a different access point this time.
     * @param bandChanged The phone used a different band this time.
     */
    fun spotComparison(deltaDb: Int, accessPointChanged: Boolean, bandChanged: Boolean): String

    // endregion

    // region Background monitor (Android notifications)

    /** Notification channel name shown in Android settings. */
    val monitorChannelName: String
    /** Notification channel description shown in Android settings. Keep the brand as-is. */
    val monitorChannelDescription: String
    /** Notification channel name for alerts. */
    val alertsChannelName: String
    val alertsChannelDescription: String

    /** Error when the monitor service failed to start. */
    val monitorStartFailed: String
    /** Status when no fresh scan arrived in time. */
    val monitorWaitingForScanLimited: String
    /** Ongoing notification text before the first scan arrives. */
    val monitorWaitingForScan: String
    /**
     * Ongoing notification text, e.g. "12 networks · Last scan: 14:05".
     * @param networkCount Networks found in the last scan. @param lastScanTime Clock time, already formatted.
     */
    fun monitorStatus(networkCount: Int, lastScanTime: String): String

    /** Alert title when new networks appear. @param count Number of new networks listed (1 to 3). */
    fun newNetworksTitle(count: Int): String
    /**
     * Alert text listing new networks. One name: "\"Name\" is now in range". Two or more: the first two
     * names, plus "and more" when there are more than two.
     * @param names Network names, not translated (1 to 3).
     */
    fun newNetworksText(names: List<String>): String

    /** Alert title when the strongest network's signal changed a lot. */
    val signalChangeTitle: String
    /**
     * Alert text, e.g. "\"Home\" signal is now good (-58 dBm)".
     * @param networkName Network name. @param level New signal level. @param rssi Signal in dBm.
     */
    fun signalChangeText(networkName: String, level: MonitorSignalLevel, rssi: Int): String

    // endregion

    // region Sharing and export

    /** Title of the Android share sheet for an exported file. */
    val exportFileChooserTitle: String
    /** Description of an exported chart image. @param name Chart file name. */
    fun chartDescription(name: String): String

    // endregion

    // region Network tools and device discovery errors

    /** Device discovery error while a VPN is active. */
    val lanVpnUnavailable: String
    /** Device discovery error without a local network. */
    val lanConnectFirst: String
    /** Network label when the phone isn't on a local network. Keep short. */
    val lanNotConnected: String

    // endregion
}
