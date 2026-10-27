package com.bernaferrari.wifiweakspot.core.i18n

/**
 * The Networks screen (channel chart, signal history, network list and details), the chart
 * guide and explorer, the channel overlap sheet, and the Home screen.
 *
 * "Ch" is the short form of "channel" used where space is tight. Band names ("2.4 GHz"),
 * dBm readings and MHz widths arrive already formatted.
 */
interface AnalyzeStrings {

    // Shared across the Networks and Home screens

    /** Short channel label, e.g. "Ch 6". Must stay very short (chart cursor, list rows). */
    fun channelShort(channel: Int): String

    /**
     * Second line of the text shared for one network, e.g. "-52 dBm · Ch 6 · 2.4 GHz".
     * [band] is a formatted band name.
     */
    fun shareNetworkLine(rssi: Int, channel: Int, band: String): String

    /** Marks an open network with no password (icon description and info chip). */
    val noPassword: String

    /** Title shown when the phone's Wi-Fi radio is turned off. */
    val wifiOffTitle: String

    /** Explains why Location must be on; no GPS is used. */
    val locationOffBody: String

    // Networks screen

    /** Body of the card asking for location access on the Networks screen. */
    val scanPermissionBody: String

    val wifiOffBody: String

    /** Title shown when the phone's Location setting is off. */
    val locationOffTitle: String

    /** Button opening the system Location settings. */
    val turnOnLocation: String

    /** Title when Android has throttled Wi-Fi scanning. */
    val scanPausedTitle: String

    /** Body when Android has throttled scanning; [seconds] until the next scan is allowed. */
    fun scanPausedBody(seconds: Long): String

    /** Button opening the setting that turns off scan throttling. */
    val turnOffThrottling: String

    val readyToScanBody: String

    /** Button that starts a Wi-Fi scan. */
    val scan: String

    /** Title of the channel chart, and the description of the button that shows it. */
    val channels: String

    /** Title of the signal history chart, and the description of the button that shows it. */
    val signalOverTime: String

    /** Icon button description: opens the chart guide. */
    val howToReadThisChart: String

    /** Icon button description: opens the signal history full screen. */
    val expandSignalHistory: String

    /** Icon button description: opens the channel chart full screen. */
    val expandChannels: String

    /** Shown in the empty chart; [band] is a band name such as "5 GHz". */
    fun noBandNetworksNearby(band: String): String

    /** Heading above the network list; [band] is a band name such as "5 GHz". */
    fun networksOnBand(count: Int, band: String): String

    /** Button that reveals [count] very weak networks folded at the bottom of the list. */
    fun showVeryWeakNetworks(count: Int): String

    /** Button that folds [count] very weak networks away again. */
    fun hideVeryWeakNetworks(count: Int): String

    /** Under each band button: how many networks that band has. Very short; "none" for 0. */
    fun bandNetworkCount(count: Int): String

    /** Accessibility state of a network row highlighted on the chart. */
    val highlightedOnChart: String

    /** Accessibility state of a network row not highlighted on the chart. */
    val notHighlightedOnChart: String

    /** Description of the arrow button opening a network's details; [ssid] may be empty. */
    fun detailsForNetwork(ssid: String): String

    /** Network row subtitle for a guest/sibling network of your router; [quality] e.g. "Good". */
    fun alsoYourRouter(quality: String): String

    /** Small badge on the network you're connected to. Very short. */
    val connected: String

    // Shared scan report (plain text sent through the share sheet)

    /** First line of the shared report. Keep the brand name. */
    val scanReportHeading: String

    /** [time] is a formatted date and time. */
    fun scanReportTime(time: String): String

    fun scanReportTotal(count: Int): String

    /** Heading before the list of networks in the shared report. */
    val scanReportNetworksHeading: String

    /** One line per network, e.g. "• Home - -52dBm, Ch 6, WPA2". [security] is a protocol name. */
    fun scanReportLine(ssid: String, rssi: Int, channel: Int, security: String): String

    // Channel advice and overlap ratings

    /** Advice chip: your current channel has little overlap. */
    val channelLooksClear: String

    /** Advice chip: no channel scored better than yours. */
    val noChannelImprovement: String

    /** Advice chip beside a suggested channel number; [currentChannel] is your channel. */
    fun lessOverlapThanChannel(currentChannel: Int): String

    /** Advice chip beside a suggested channel number when you're not on this band. */
    val suggestedChannel: String

    /** Explains the scan-based comparison without promising a speed increase. */
    val channelAdviceBasis: String

    /** Heading for measured overlap bars. */
    val channelOverlapTitle: String

    /** Final router step. */
    val channelApplyChange: String

    /** Clarifies where the setting must be changed. */
    val channelChangeNote: String

    /** Router instructions for the inspected band and suggested channel. */
    fun channelChangeInstructions(channel: Int, band: String): String

    /** Accessibility text for the advice chip: [advice] is the chip text, [channel] the number. */
    fun channelAdviceDescription(advice: String, channel: Int): String

    /** Overlap rating labels (chip). Short. */
    val lowOverlap: String
    val moderateOverlap: String
    val highOverlap: String
    val veryHighOverlap: String

    // Channel chart

    /**
     * Accessibility description of the channel chart. [busiestChannel] is null when there's no
     * network; [busiestCount] is how many networks sit on it.
     */
    fun channelGraphDescription(networkCount: Int, busiestChannel: Int?, busiestCount: Int): String

    /**
     * Name drawn above a network's hill or line. [name] is already shortened. [isYou] marks
     * your own network; [extraNames] counts other names from the same router ("+2").
     */
    fun chartNetworkLabel(name: String, isYou: Boolean, extraNames: Int): String


    /** Chart axis labels for signal strength, top to bottom. One very short word each. */
    val axisStrong: String
    val axisOkay: String
    val axisWeak: String

    /** Selected network's reading under its name, e.g. "-52 dBm · Good · Ch 6". */
    fun selectedNetworkSummary(reading: String, quality: String, channel: Int): String

    /** Icon button description: clears the highlighted network. */
    val clearSelectedNetwork: String

    /** Hint under the chart when nothing is selected. */
    val tapWaveForDetails: String

    // Chart guide

    /** Title of the chart guide sheet. */
    val chartGuideTitle: String

    val guideTallerBody: String
    val guideOverlapBody: String
    val guideWiderBody: String

    val guideSameRouterBody: String
    val guideSteadyBody: String
    val guideHighlightBody: String

    // Chart explorer (full screen)

    /** Heading of the list under the full-screen chart: networks touching [channel]. */
    fun channelOverlapHeading(count: Int, channel: Int): String

    /** Row subtitle for a wide network centered on another channel ([homeChannel]). */
    fun fromChannel(homeChannel: Int, reading: String): String

    // Channel overlap sheet

    val estimatedChannelOverlap: String

    /** Suggested channel, e.g. "Try channel 6". */
    fun tryChannel(channel: Int): String

    val lowestOverlapFor20Mhz: String

    /** Disclaimer under the suggestion. */
    val overlapDisclaimer: String

    /** Title above the bar chart of overlap scores per channel. */
    val overlapScore: String

    // Signal history chart

    val collectingReadings: String

    /** Accessibility description of the signal history chart. */
    fun signalHistoryDescription(networkCount: Int): String

    /** Accessibility state while the chart follows live readings. */
    val historyLive: String

    /**
     * Accessibility state while paused on a reading. [reading] is null when the network has
     * no reading at that point; [readingsAgo] counts scans back from the latest.
     */
    fun historyPaused(networkName: String, reading: String?, readingsAgo: Int): String

    /** Accessibility actions on the signal history chart. */
    val strongerNetwork: String
    val weakerNetwork: String

    /** Time axis labels at the left and right ends. One very short word each. */
    val axisOlder: String
    val axisLatest: String

    /** Title under the chart when nothing is inspected. */
    val exploreSignalHistory: String

    val tapLineToCompare: String
    val noReadingAtPoint: String

    /** Inspected value, e.g. "-52 dBm · Good · 3 readings ago"; 0 means the latest reading. */
    fun inspectedReading(reading: String, quality: String, readingsAgo: Int): String

    /** Button that returns the chart to live readings. Short. */
    val resumeLive: String

    // Network details

    /** e.g. "-52 dBm · seen 2 min ago"; [seen] is a relative time phrase. */
    fun seenReading(rssi: Int, seen: String): String

    /** Row labels in the details card. */
    val factChannel: String
    val factWidth: String
    val factSecurity: String
    val factDistance: String

    /** Channel width when Android doesn't report it. */
    val notAvailable: String

    /** Button that follows this network in the signal history chart. */
    val watch: String

    val addToFavorites: String
    val removeFromFavorites: String

    // Home screen

    /** [pingMs] is a formatted number, e.g. "4.2". */
    fun routerCheckLastTime(pingMs: String): String

    fun devicesFoundLastScan(count: Int): String
    val devicesSubtitle: String
    val testWebsiteSubtitle: String

    /** History tile subtitle: number of saved checks; 0 shows a generic subtitle. */
    fun savedChecks(count: Int): String

    /** Connection card headline when Wi-Fi is on but not connected. */
    val notOnWifi: String

    /** Status next to a green dot: Android confirmed internet access. */
    val online: String

    /** Status next to a red dot: Android did not confirm internet access. */
    val internetNotConfirmed: String

    /** Info chip. */
    fun channelChip(channel: Int): String

    /** Info chip: negotiated link rate in Mbps. */
    fun linkRateChip(mbps: Int): String

    val chooseNetwork: String
    val turnOnWifi: String

    /** Icon button description: dismisses a card. */
    val dismiss: String

    val findWeakSpotsTitle: String
    val findWeakSpotsBody: String
    val findWeakSpotsAction: String

    /** Nearby card title. */
    fun networksNearby(count: Int): String

    /** Nearby card line when the last scan is old. */
    val nearbyStale: String

    /** Nearby card line when you're not connected to a scanned network. */
    val nearbyNoConnection: String

    /** Nearby card line: [count] other routers overlap your [channel]. */
    fun nearbyOverlap(count: Int, channel: Int): String

    // Connection advice

    val adviceNoWifiBody: String
    val adviceNoInternetTitle: String
    val adviceNoInternetBody: String
    val adviceNoSignalTitle: String
    val adviceNoSignalBody: String
    val adviceWeakSignalTitle: String
    val adviceWeakSignalBody: String
    val adviceGoodSignalTitle: String
    val adviceGoodSignalBody: String
}
