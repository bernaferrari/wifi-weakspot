package com.bernaferrari.wifiweakspot.core.i18n.en

import com.bernaferrari.wifiweakspot.core.i18n.ToolsStrings

object EnToolsStrings : ToolsStrings {
    override val tryAgain = "Try again"

    override val targetPlaceholder = "Website or IP address"
    override val clearTarget = "Clear"
    override val checkSectionTitle = "Check"
    override val recentSectionTitle = "Recent"

    override val toolPortsDescription = "What's open"

    override val pingQuestion = "Does it answer?"
    override val dnsQuestion = "What's its address?"
    override val dnsDetail = "DNS lookup"
    override val portsQuestion = "What does it offer?"

    override val runningPortScanTitle = "Checking ports"

    override val diagnosticFailed = "Diagnostic failed"
    override val invalidTarget = "Enter a hostname or IP address without a URL path or spaces."
    override val diagnosticCouldNotComplete = "The diagnostic could not complete."
    override val connectionChangedRunAgain = "The connection changed. Run this check again on the new network."

    override val openItsWebPage = "Open its web page"

    override fun pingRepliedLabel(target: String, answered: Int, total: Int) = "$target · $answered of $total replied"
    override val slowest = "Slowest"
    override fun repliesContentDescription(replies: String) = "Replies: $replies"

    override val pingDidntAnswer = "Didn't answer"
    override val pingResponsive = "Responsive"
    override val pingResponsiveButUneven = "Responsive, but uneven"
    override val pingDroppingReplies = "Dropping replies"
    override val pingSlowToAnswer = "Slow to answer"

    override val pingNoAnswerLocal = "It may be off, asleep, or set to ignore pings."
    override fun pingRepliesLost(lost: Int, total: Int) =
        "$lost of $total replies went missing. Compare a router check with an internet host to help locate the problem; this test alone cannot identify the cause."
    override val pingUneven = "Reply times varied. Compare a router check here and near the router, then test an internet host."
    override val pingLocalExplanation = "Time to reach this device and back. The device itself can affect the result."
    override val pingInternetExplanation = "Time to reach this host and back. Your connection, the route and the host all affect the result."

    override fun dnsAddressUnit(count: Int) = if (count == 1) "address" else "addresses"
    override fun dnsForQuery(query: String) = "For $query"
    override val dnsNotFound = "Couldn't find it"
    override val dnsFound = "Found it"
    override val dnsNotFoundExplanation = "Check the spelling, or try again; the name may not exist."
    override fun dnsFoundExplanation(query: String) = "Your device can find $query."
    override val dnsAddressesTitle = "Addresses"

    override fun portsOpenUnit(count: Int) = "open"

    override fun portsCommonServicesOn(host: String) = "Common services on $host"
    override val portsNothingOpen = "Nothing open"
    override fun portsOffersServices(count: Int) = if (count == 1) "It offers 1 service" else "It offers $count services"
    override val portsNoneAnswered = "No common service answered."
    override val portsOffersExplanation = "Things like a settings page or file sharing."

    override val portWebPage = "Web page"
    override val portSecureWebPage = "Secure web page"
    override val portRemoteLoginSsh = "Remote login (SSH)"
    override val portOldRemoteLoginTelnet = "Old remote login (Telnet)"
    override val portFileTransferFtp = "File transfer (FTP)"
    override val portAddressLookupsDns = "Address lookups (DNS)"
    override val portFileSharing = "File sharing"
    override val portMacFileSharing = "Mac file sharing"
    override val portPrinting = "Printing"
    override val portRemoteDesktop = "Remote desktop"
    override val portScreenSharing = "Screen sharing"
    override val portDeviceDiscoveryUpnp = "Device discovery (UPnP)"
    override val portStorageOrAdminPage = "Storage or admin page"
    override val portDatabase = "Database"
    override val portService = "Service"
    override val devicesLooking = "Looking for devices…"
    override val devicesFindOnWifi = "Find devices on your Wi-Fi"
    override fun devicesOnNetworkNamed(count: Int, ssid: String) = "$count on $ssid"
    override fun devicesOnYourNetwork(count: Int) = "$count on your network"
    override val couldntLookForDevices = "Couldn't look for devices"
    override val scanFailed = "Scan failed"
    override val connectionChangedLookAgain = "The connection changed. Look again on this network."
    override val stoppedLooking = "Stopped looking"
    override val noDevicesFound = "No devices found"
    override val noDevicesFoundSubtitle =
        "Some devices stay quiet when asked. Try again in a moment, or check that this device is on your home Wi-Fi."
    override fun otherDevicesGroup(count: Int) = "Other devices · $count"
    override fun quietDevices(count: Int) = if (count == 1) "1 quiet device" else "$count quiet devices"
    override val quietDevicesSubtitle = "They didn't say what they are"
    override val hide = "Hide"
    override val show = "Show"
    override fun checkingLocalAddresses(percent: Int) = "Checking local addresses · $percent%"
    override fun deviceDidntAnswer(detail: String) = "$detail · didn't answer"

    override val deviceNotScannedYet = "Not found in a scan yet"
    override val deviceAnsweredLastScan = "Answered the last scan"
    override val deviceSeenButNoAnswer = "Seen on the network, but didn't answer"
    override val actionCheckPorts = "Check ports"
    override val openRouterSettings = "Open router settings"
    override val detailsTitle = "Details"
    override val factNetworkName = "Network name"
    override val factHardwareAddress = "Hardware address"
    override val macHiddenByAndroid = "Hidden by Android"
    override val factYourName = "Your name for it"
    override val portsNotCheckedYet = "Not checked yet."
    override fun portsNoneOpenWhenChecked(moment: String) =
        "None of the common ports were open when checked ${moment.replaceFirstChar { it.lowercase() }}."
    override fun portsCheckedAt(moment: String) = "Checked ${moment.replaceFirstChar { it.lowercase() }}."
    override val renameDeviceTitle = "Rename device"
    override val renameSupporting = "Leave empty to use the name WiFi Weakspot found."
    override val connectionChangedOpenAgain = "The connection changed. Open this device again on the new network."
    override val couldntLoadDetails = "Couldn't load saved details. Try opening this device again."
    override val scanBeforeNaming = "Scan this device before saving a name."

}
