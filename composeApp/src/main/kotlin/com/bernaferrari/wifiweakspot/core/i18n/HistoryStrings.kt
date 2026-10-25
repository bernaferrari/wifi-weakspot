package com.bernaferrari.wifiweakspot.core.i18n

/**
 * History (the timeline of saved checks, scans and tool runs), the Router check screen, and the
 * Internet speed screen. Numbers arrive already formatted ("12.5"); units such as ms, Mbps and
 * dBm stay as symbols.
 */
interface HistoryStrings {
    // Dates and times

    /** Day header in History for entries saved today. */
    val today: String
    /** Day header in History for entries saved yesterday. */
    val yesterday: String
    /** Full weekday names, Monday first (7 entries). Used as day headers within the past week. */
    val weekdayNames: List<String>
    /** Abbreviated weekday names, Monday first (7 entries), as in "Mon, 12 Oct". */
    val weekdayShortNames: List<String>
    /** Abbreviated month names, January first (12 entries), as in "Mon, 12 Oct". */
    val monthShortNames: List<String>
    /** Day header for an older date in the current year, e.g. "Mon, 12 Oct". [weekday] and [month] are the short names above. */
    fun dateThisYear(weekday: String, day: Int, month: String): String
    /** Day header for a date in another year, e.g. "Mon, 12 Oct 2025". */
    fun dateOtherYear(weekday: String, day: Int, month: String, year: Int): String
    /** When something happened, e.g. "Today at 14:32". [day] is one of the day labels above, [time] is "14:32". */
    fun momentAt(day: String, time: String): String

    // Filter chips (keep short)

    /** Filter chip showing every kind of entry. Keep short. */
    val filterAll: String
    /** Filter chip / kind label for internet speed tests. Keep short. */
    val kindInternetTests: String
    /** Filter chip / kind label for Wi-Fi scans. Keep short. */
    val kindScans: String

    // History screen

    /** Empty state title when nothing has been saved. */
    val emptyTitle: String
    /** Empty state explanation under [emptyTitle]. */
    val emptySubtitle: String
    /** Accessibility description of the router trend bar chart. [readings] is a comma list of readings like "4.2 ms" or [noReplyReading]. */
    fun trendBarsDescription(count: Int, readings: String): String

    // Router trend insight (one sentence under the trend chart)

    val trendNoReply: String
    /** Shown after the first router check, before there's anything to compare with. */
    val trendRecheckToCompare: String
    /** [usualMs] is the typical response time, like "4.5"; the unit ms follows it. */
    fun trendQuicker(usualMs: String): String
    fun trendSlower(usualMs: String): String
    fun trendAboutSame(usualMs: String): String

    // Entry titles and summaries

    /** Title of an internet speed test entry. [mbps] is the download speed, like "412". */
    fun internetTitle(mbps: String): String
    /** Title of a router check entry. [ms] is the response time, like "4". */
    fun routerTitle(ms: String): String
    /** Title of a scan entry. */
    fun networksNearby(count: Int): String
    /** Summary of a scan entry; [network] is a network name. */
    fun strongestNetwork(network: String): String
    /** Supporting text for a network seen through several access points. */
    fun accessPoints(count: Int): String
    /** Title of a device discovery entry. */
    fun devicesFound(count: Int): String
    /** A short comma-separated list followed by how many were left out, e.g. "MacBook, iPad, TV and 4 more". When [more] is 0, just [list]. */
    fun listWithMore(list: String, more: Int): String
    /** Summary of a ping entry, e.g. "15 ms · 2 of 3 replied". [averageMs] is already formatted. */
    fun pingSummary(averageMs: String, replied: Int, sent: Int): String
    /** Title of a DNS lookup entry; [target] is a host name. */
    fun lookupTitle(target: String): String
    val noRecords: String
    val lookupFailed: String
    /** Title of a port scan entry; [target] is a host or IP. */
    fun openPortsTitle(target: String): String
    val noCommonPortsOpen: String
    /** Summary of a tool run that finished. */
    val completed: String
    /** Summary of a tool run that failed. */
    val failed: String

    // Detail sheet metric tiles (keep short, single line)

    val metricDownload: String
    val metricUpload: String
    val metricLatency: String
    /** Jitter, how much response times varied. */
    val metricVariation: String
    /** Share of pings with no reply. */
    val metricLost: String
    /** Number of networks on a band; [band] is like "5 GHz". */
    fun metricOnBand(band: String): String
    /** Devices that answered; also the trailing label on a device row. */
    val metricReplied: String
    /** Ping replies received; also the Router check stat label. */
    val metricReplies: String
    val metricAverage: String

    // Detail sheet facts (label column)

    val factServer: String
    val factRoom: String
    /** Wi-Fi frequency band. */
    val factBand: String
    val factSignal: String
    val factAccessPoints: String
    /** Signal value, e.g. "Good signal · -55 dBm". [quality] is a signal quality label like "Good". */
    fun signalFact(quality: String, rssi: Int): String

    // Detail sheet lists

    val linesNetworksStrongestFirst: String
    val linesRecords: String
    /** Row title for the nth ping reply. */
    fun replyNumber(index: Int): String
    /** Explanatory note under a scan's details. */
    val scanNote: String

    // Router check screen

    /** Label inside the big start button. Very short (2–3 characters ideal). */
    val go: String
    /** Footer link to the fast.com speed test. */
    val testInternetSpeedInstead: String
    val keepDeviceStill: String
    val connectToStart: String
    /** Card title above the previous router check result. */
    val lastTime: String
    val couldNotSaveSpot: String
    val connectFirst: String
    val historyNotSaved: String
    val checkStopped: String

    // Internet speed screen

    /** Top bar title of the embedded fast.com page. */
    val internetSpeedTitle: String
}
