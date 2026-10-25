package com.bernaferrari.wifiweakspot.core.i18n

/** The "Test a website" tools (ping, DNS, ports) and the devices-on-your-network screens. */
interface ToolsStrings {
    // Shared actions on these screens.

    /** Button that retries after something failed. */
    val tryAgain: String

    // Tools home.

    /** Placeholder in the search-style field where the user types a host to test. */
    val targetPlaceholder: String
    /** Accessibility label for the button that clears the target field. */
    val clearTarget: String
    /** Section header above the list of checks. Very short. */
    val checkSectionTitle: String
    /** Section header above recently tested targets. Very short. */
    val recentSectionTitle: String

    /** Short hint under [toolPortsName]. Tight space. */
    val toolPortsDescription: String

    /** The ping check, phrased as the question it answers. */
    val pingQuestion: String
    /** The DNS lookup, phrased as the question it answers. */
    val dnsQuestion: String
    /** Subtitle under [dnsQuestion]. */
    val dnsDetail: String
    /** The port check, phrased as the question it answers. */
    val portsQuestion: String

    /** Big title while a port check runs. See [runningPingTitle]. */
    val runningPortScanTitle: String

    /** Headline when a check failed with an error. */
    val diagnosticFailed: String
    /** Error shown when the user typed a URL, path, or spaces instead of a host. */
    val invalidTarget: String
    /** Fallback error when a check failed without a reason. */
    val diagnosticCouldNotComplete: String
    /** Error when the Wi-Fi network changed while checking ports. */
    val connectionChangedRunAgain: String

    // Results, shared.

    /** Button that opens the web page a device or host serves. */
    val openItsWebPage: String

    // Ping result.

    /** Line under the average reply time: the target, then how many pings got a reply. */
    fun pingRepliedLabel(target: String, answered: Int, total: Int): String
    /** Small stat label: slowest reply. Very short. */
    val slowest: String
    /** Accessibility description of the reply chart; [replies] is a comma-separated list like "4.2 ms, lost". */
    fun repliesContentDescription(replies: String): String

    /** Ping verdict: no reply at all. */
    val pingDidntAnswer: String
    val pingResponsive: String
    val pingResponsiveButUneven: String
    val pingDroppingReplies: String
    val pingSlowToAnswer: String

    /** Explanation when a device on the home network didn't answer. */
    val pingNoAnswerLocal: String
    /** Explanation when some replies were lost: [lost] of [total] pings had no reply. */
    fun pingRepliesLost(lost: Int, total: Int): String
    /** Explanation when reply times varied a lot. */
    val pingUneven: String
    /** Explanation of the number for a device on the home network. */
    val pingLocalExplanation: String
    /** Explanation of the number for an internet host. */
    val pingInternetExplanation: String

    // DNS result.

    /** Unit next to the big count of addresses found ("3 addresses"). Only the noun. */
    fun dnsAddressUnit(count: Int): String
    /** Line under the count: which name was looked up. */
    fun dnsForQuery(query: String): String
    val dnsNotFound: String
    val dnsFound: String
    val dnsNotFoundExplanation: String
    fun dnsFoundExplanation(query: String): String
    /** Section header listing the addresses found. */
    val dnsAddressesTitle: String

    // Ports result.

    /** Unit next to the big count of open ports ("3 open"). Very short. */
    val portsOpenUnit: String
    /** Line under the count. */
    fun portsCommonServicesOn(host: String): String
    val portsNothingOpen: String
    /** Headline: how many services the host offers. */
    fun portsOffersServices(count: Int): String
    val portsNoneAnswered: String
    val portsOffersExplanation: String

    // What common ports usually mean, in everyday words. Protocol names in parentheses stay as is.

    val portWebPage: String
    val portSecureWebPage: String
    val portRemoteLoginSsh: String
    val portOldRemoteLoginTelnet: String
    val portFileTransferFtp: String
    val portAddressLookupsDns: String
    val portFileSharing: String
    val portMacFileSharing: String
    val portPrinting: String
    val portRemoteDesktop: String
    val portScreenSharing: String
    val portDeviceDiscoveryUpnp: String
    val portStorageOrAdminPage: String
    val portDatabase: String
    /** Fallback name for a port with no known service. */
    val portService: String

    // Devices list.

    /** Subtitle while the first scan runs. */
    val devicesLooking: String
    /** Subtitle before anything was found. */
    val devicesFindOnWifi: String
    /** Subtitle: how many devices were found on the named Wi-Fi network. */
    fun devicesOnNetworkNamed(count: Int, ssid: String): String
    /** Subtitle: how many devices were found, when the network name is unknown. */
    fun devicesOnYourNetwork(count: Int): String
    /** Error card title when the scan failed. */
    val couldntLookForDevices: String
    /** Fallback error message when the scan failed without a reason. */
    val scanFailed: String
    /** Error when the Wi-Fi network changed during the scan. */
    val connectionChangedLookAgain: String
    /** Empty state title after the user stopped the scan. */
    val stoppedLooking: String
    /** Empty state title when the scan found nothing. */
    val noDevicesFound: String
    val noDevicesFoundSubtitle: String
    /** Group header for all other devices, with their count. */
    fun otherDevicesGroup(count: Int): String
    /** Row that expands devices which didn't say what they are. */
    fun quietDevices(count: Int): String
    /** Subtitle under [quietDevices]. */
    val quietDevicesSubtitle: String
    /** Accessibility label: collapses the quiet devices. */
    val hide: String
    /** Accessibility label: expands the quiet devices. */
    val show: String
    /** Scan progress line; [percent] is 0–100. */
    fun checkingLocalAddresses(percent: Int): String
    /** Device row subtitle for a device that didn't answer; [detail] is its usual subtitle. */
    fun deviceDidntAnswer(detail: String): String

    // Device detail.

    val deviceNotScannedYet: String
    val deviceAnsweredLastScan: String
    val deviceSeenButNoAnswer: String
    /** Button that checks the device's open ports. Short. */
    val actionCheckPorts: String
    val openRouterSettings: String
    /** Section header above the device facts. */
    val detailsTitle: String
    /** Fact label: the host name the device announces on the network. */
    val factNetworkName: String
    /** Fact label: MAC address. */
    val factHardwareAddress: String
    /** Value for [factHardwareAddress] when Android doesn't reveal it. */
    val macHiddenByAndroid: String
    /** Fact label: the custom name the user gave the device. */
    val factYourName: String
    /** Shown when the device's ports were never checked. */
    val portsNotCheckedYet: String
    /**
     * Shown when no common port was open; [moment] is a relative or absolute time phrase
     * like "Today at 9:41" or "5 min ago", capitalized as if it started a sentence.
     */
    fun portsNoneOpenWhenChecked(moment: String): String
    /** Footnote under the open ports: when they were checked. [moment] as in [portsNoneOpenWhenChecked]. */
    fun portsCheckedAt(moment: String): String
    /** Rename dialog title. */
    val renameDeviceTitle: String
    /** Helper under the rename field. Mentions the brand "WiFi Weakspot", which stays untranslated (keep its non-breaking space). */
    val renameSupporting: String
    val connectionChangedOpenAgain: String
    val couldntLoadDetails: String
    val scanBeforeNaming: String
}
