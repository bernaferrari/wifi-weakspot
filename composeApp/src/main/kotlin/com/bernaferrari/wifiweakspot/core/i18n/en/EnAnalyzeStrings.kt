package com.bernaferrari.wifiweakspot.core.i18n.en

import com.bernaferrari.wifiweakspot.core.i18n.AnalyzeStrings

object EnAnalyzeStrings : AnalyzeStrings {

    private fun networks(count: Int) = if (count == 1) "1 network" else "$count networks"

    override fun channelShort(channel: Int) = "Ch $channel"
    override fun shareNetworkLine(rssi: Int, channel: Int, band: String) = "$rssi dBm · Ch $channel · $band"
    override val noPassword = "No password"
    override val wifiOffTitle = "Wi-Fi is off"
    override val locationOffBody = "Android needs Location on to list nearby Wi-Fi. WiFi\u00A0Weakspot doesn't use GPS."

    override val scanPermissionBody = "Android only shares nearby network names with apps that have location access. WiFi\u00A0Weakspot never records where you are."
    override val wifiOffBody = "Turn on Wi-Fi to see nearby networks."
    override val locationOffTitle = "Location is off"
    override val turnOnLocation = "Turn on Location"
    override val scanPausedTitle = "Android paused scanning"
    override fun scanPausedBody(seconds: Long) = "The next scan is allowed in $seconds s. Turn off scan throttling to skip the wait."
    override val turnOffThrottling = "Turn off throttling"
    override val readyToScanBody = "Tap scan to see every network nearby."
    override val scan = "Scan"
    override val channels = "Channels"
    override val signalOverTime = "Signal over time"
    override val howToReadThisChart = "How to read this chart"
    override val expandSignalHistory = "Expand signal history"
    override val expandChannels = "Expand channels"
    override fun noBandNetworksNearby(band: String) = "No $band networks nearby."
    override fun networksOnBand(count: Int, band: String) =
        if (count == 0) "No networks on $band" else "${networks(count)} on $band"
    override fun showVeryWeakNetworks(count: Int) =
        if (count == 1) "Show 1 very weak network" else "Show $count very weak networks"
    override fun hideVeryWeakNetworks(count: Int) =
        if (count == 1) "Hide 1 very weak network" else "Hide $count very weak networks"
    override fun bandNetworkCount(count: Int) = if (count == 0) "none" else networks(count)
    override val highlightedOnChart = "Highlighted on the chart"
    override val notHighlightedOnChart = "Not highlighted on the chart"
    override fun detailsForNetwork(ssid: String) = "Details for ${ssid.ifEmpty { "hidden network" }}"
    override fun alsoYourRouter(quality: String) = "$quality · Also your router"
    override val connected = "Connected"

    override val scanReportHeading = "WiFi\u00A0Weakspot Scan Report"
    override fun scanReportTime(time: String) = "Time: $time"
    override fun scanReportTotal(count: Int) = "Total Networks: $count"
    override val scanReportNetworksHeading = "Networks:"
    override fun scanReportLine(ssid: String, rssi: Int, channel: Int, security: String) =
        "• $ssid - ${rssi}dBm, Ch $channel, $security"
    override val channelLooksClear = "Your channel looks clear"
    override val noChannelImprovement = "No improvement found in this scan"
    override fun lessOverlapThanChannel(currentChannel: Int) = "Less overlap than channel $currentChannel"
    override val leastOverlapInScan = "Least overlap in this scan"
    override fun channelAdviceDescription(advice: String, channel: Int) = "$advice: channel $channel"
    override val lowOverlap = "Low overlap"
    override val moderateOverlap = "Moderate overlap"
    override val highOverlap = "High overlap"
    override val veryHighOverlap = "Very high overlap"

    override fun channelGraphDescription(networkCount: Int, busiestChannel: Int?, busiestCount: Int) =
        "Channel graph, ${networks(networkCount)}." +
            (if (busiestChannel != null) " Busiest channel $busiestChannel with $busiestCount." else "")
    override fun chartNetworkLabel(name: String, isYou: Boolean, extraNames: Int) =
        name + (if (isYou) " (you)" else "") + (if (extraNames > 0) " +$extraNames" else "")
    override val busyCaption = "Busy"
    override val axisStrong = "Strong"
    override val axisOkay = "Okay"
    override val axisWeak = "Weak"
    override fun selectedNetworkSummary(reading: String, quality: String, channel: Int) = "$reading · $quality · Ch $channel"
    override val clearSelectedNetwork = "Clear selected network"
    override val tapWaveForDetails = "Tap a wave for network details"

    override val chartGuideTitle = "Reading the charts"
    override val guideTallerBody = "Height is signal strength."
    override val guideOverlapBody = "Overlapping networks share spectrum and can compete for airtime."
    override val guideWiderBody = "A wider channel occupies more spectrum and can overlap more neighbors."
    override val guideSameRouterBody = "+1: Names that appear to come from the same radio share a hill."
    override val guideCrowdBody = "Green means less overlap; red means more overlap. The strip under 2.4 GHz shows overlap. Tap it for counts."
    override val guideSteadyBody = "A jumpy line shows changing signal strength. Tap a line to pause, then slide sideways through time or vertically between networks. Resume live to watch new readings."
    override val guideHighlightBody = "Tap a hill or row to highlight it. Drag horizontally across the chart to explore."

    override fun channelOverlapHeading(count: Int, channel: Int) = when (count) {
        0 -> "Channel $channel is clear"
        1 -> "1 network overlaps channel $channel"
        else -> "$count networks overlap channel $channel"
    }
    override fun fromChannel(homeChannel: Int, reading: String) = "From channel $homeChannel · $reading"

    override val estimatedChannelOverlap = "Estimated channel overlap"
    override fun tryChannel(channel: Int) = "Try channel $channel"
    override val lowestOverlapFor20Mhz = "Lowest estimated overlap for 20 MHz channels"
    override val overlapDisclaimer = "Based on nearby access points and signal strength, not measured traffic. " +
        "Check your router’s supported channels and local rules before changing settings. " +
        "5 and 6 GHz suggestions require known router support; missing wide-channel centers " +
        "show only the primary 20 MHz channel."
    override val overlapScore = "Overlap score"

    override val collectingReadings = "Collecting readings… The lines appear after a couple of scans."
    override fun signalHistoryDescription(networkCount: Int) =
        "Signal history for ${networks(networkCount)}. Tap a line to inspect. Arrow keys move through time and networks."
    override val historyLive = "Live"
    override fun historyPaused(networkName: String, reading: String?, readingsAgo: Int) =
        "$networkName, ${reading ?: "no reading"}, paused, ${readingsAgo(readingsAgo)}"
    override val strongerNetwork = "Stronger network"
    override val weakerNetwork = "Weaker network"
    override val axisOlder = "older"
    override val axisLatest = "latest"
    override val exploreSignalHistory = "Explore signal history"
    override val tapLineToCompare = "Tap a line, then drag to compare"
    override val noReadingAtPoint = "No reading at this point"
    override fun inspectedReading(reading: String, quality: String, readingsAgo: Int) =
        "$reading · $quality · " + if (readingsAgo == 0) "latest reading" else readingsAgo(readingsAgo)
    private fun readingsAgo(count: Int) = if (count == 1) "1 reading ago" else "$count readings ago"
    override val resumeLive = "Resume live"

    override fun seenReading(rssi: Int, seen: String) = "$rssi dBm · seen $seen"
    override val factChannel = "Channel"
    override val factWidth = "Width"
    override val factSecurity = "Security"
    override val factDistance = "Distance estimate"
    override val notAvailable = "Not available"
    override val watch = "Watch"
    override val addToFavorites = "Add to favorites"
    override val removeFromFavorites = "Remove from favorites"

    override fun routerCheckLastTime(pingMs: String) = "$pingMs ms last time"
    override fun devicesFoundLastScan(count: Int) = "$count found last scan"
    override val devicesSubtitle = "Who's connected"
    override val testWebsiteSubtitle = "Ping, DNS, ports"
    override fun savedChecks(count: Int) = when (count) {
        0 -> "Past results"
        1 -> "1 check"
        else -> "$count checks"
    }
    override val notOnWifi = "Not on Wi-Fi"
    override val online = "Online"
    override val internetNotConfirmed = "Internet not confirmed"
    override fun channelChip(channel: Int) = "Channel $channel"
    override fun linkRateChip(mbps: Int) = "Link rate $mbps Mbps"
    override val chooseNetwork = "Choose a network"
    override val turnOnWifi = "Turn on Wi-Fi"
    override val dismiss = "Dismiss"
    override val findWeakSpotsTitle = "Find weak spots"
    override val findWeakSpotsBody = "See which rooms are weak."
    override val findWeakSpotsAction = "Check your rooms"
    override fun networksNearby(count: Int) = "${networks(count)} nearby"
    override val nearbyStale = "From your last scan. Tap to refresh."
    override val nearbyNoConnection = "See channels and signal for each network."
    override fun nearbyOverlap(count: Int, channel: Int) = when (count) {
        0 -> "No nearby overlap detected on channel $channel."
        1 -> "1 nearby signal overlaps your channel $channel."
        else -> "$count nearby signals overlap your channel $channel."
    }

    override val adviceNoWifiBody = "Connect to the network you want to check. You can still run internet tests and network tools using your current connection."
    override val adviceNoInternetTitle = "Check internet access first"
    override val adviceNoInternetBody = "Android has not confirmed internet access on this Wi-Fi connection. Check for a sign-in page, then test connectivity to your router and an internet host."
    override val adviceNoSignalTitle = "Read your Wi-Fi signal"
    override val adviceNoSignalBody = "Allow Wi-Fi scans in Networks to see signal measurements. Compare signal in the room where you notice problems."
    override val adviceWeakSignalTitle = "Compare coverage near your router"
    override val adviceWeakSignalBody = "The current Wi-Fi signal is weak. Run a router check here, then repeat near the router. Use a survey of your own network to find weak spots."
    override val adviceGoodSignalTitle = "Compare router responsiveness"
    override val adviceGoodSignalBody = "Signal strength looks usable and Android reports internet access. If the connection feels slow, compare router response here and near the router; strong signal alone does not guarantee performance."
}
