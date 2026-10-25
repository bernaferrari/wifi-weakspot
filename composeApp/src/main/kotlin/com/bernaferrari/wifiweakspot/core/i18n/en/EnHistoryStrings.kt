package com.bernaferrari.wifiweakspot.core.i18n.en

import com.bernaferrari.wifiweakspot.core.i18n.HistoryStrings

object EnHistoryStrings : HistoryStrings {
    override val today = "Today"
    override val yesterday = "Yesterday"
    override val weekdayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    override val weekdayShortNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    override val monthShortNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    override fun dateThisYear(weekday: String, day: Int, month: String) = "$weekday, $day $month"
    override fun dateOtherYear(weekday: String, day: Int, month: String, year: Int) = "$weekday, $day $month $year"
    override fun momentAt(day: String, time: String) = "$day at $time"

    override val filterAll = "All"
    override val kindInternetTests = "Internet tests"
    override val kindScans = "Scans"
    override val emptyTitle = "Nothing saved yet"
    override val emptySubtitle = "Router checks, network scans and tool results show up here, so you can see how your Wi-Fi changes over time."
    override fun trendBarsDescription(count: Int, readings: String) = "Last $count router tests: $readings"
    override val trendNoReply = "No reply from the router. Some routers ignore these checks."
    override val trendRecheckToCompare = "Recheck after a change to compare."
    override fun trendQuicker(usualMs: String) = "Quicker than usual ($usualMs ms)"
    override fun trendSlower(usualMs: String) = "Slower than usual ($usualMs ms)"
    override fun trendAboutSame(usualMs: String) = "About the same as usual ($usualMs ms)"

    override fun internetTitle(mbps: String) = "$mbps Mbps download"
    override fun routerTitle(ms: String) = "$ms ms to router"
    override fun networksNearby(count: Int) = if (count == 1) "1 network nearby" else "$count networks nearby"
    override fun strongestNetwork(network: String) = "Strongest: $network"
    override fun accessPoints(count: Int) = if (count == 1) "1 access point" else "$count access points"
    override fun devicesFound(count: Int) = if (count == 1) "1 device found" else "$count devices found"
    override fun listWithMore(list: String, more: Int) = if (more > 0) "$list and $more more" else list
    override fun pingSummary(averageMs: String, replied: Int, sent: Int) = "$averageMs ms · $replied of $sent replied"
    override fun lookupTitle(target: String) = "Look up $target"
    override val noRecords = "No records"
    override val lookupFailed = "Lookup failed"
    override fun openPortsTitle(target: String) = "Open ports on $target"
    override val noCommonPortsOpen = "None of the common ports were open"
    override val completed = "Completed"
    override val failed = "Failed"

    override val metricDownload = "Download"
    override val metricUpload = "Upload"
    override val metricLatency = "Latency"
    override val metricVariation = "Variation"
    override val metricLost = "Lost"
    override fun metricOnBand(band: String) = "on $band"
    override val metricReplied = "Replied"
    override val metricReplies = "Replies"
    override val metricAverage = "Average"
    override val factServer = "Server"
    override val factRoom = "Room"
    override val factBand = "Band"
    override val factSignal = "Signal"
    override val factAccessPoints = "Access points"
    override fun signalFact(quality: String, rssi: Int) = "$quality signal · $rssi dBm"

    override val linesNetworksStrongestFirst = "Networks, strongest first"
    override val linesRecords = "Records"
    override fun replyNumber(index: Int) = "Reply $index"
    override val scanNote = "Nearby networks on the same channel as yours compete for airtime."

    override val go = "GO"
    override val testInternetSpeedInstead = "Test internet speed instead"
    override val keepDeviceStill = "Keep your device still for a few seconds."
    override val connectToStart = "Connect to your Wi-Fi to start."
    override val lastTime = "Last time"
    override val couldNotSaveSpot = "Couldn't save this spot. Try again."
    override val connectFirst = "Connect to your Wi-Fi first. This check measures replies from your router."
    override val historyNotSaved = "Result measured, but history couldn't be saved."
    override val checkStopped = "The connection changed or the router check stopped. Keep this Wi-Fi connected and try again. Some routers block ping."

    override val internetSpeedTitle = "Internet speed · fast.com"
}
