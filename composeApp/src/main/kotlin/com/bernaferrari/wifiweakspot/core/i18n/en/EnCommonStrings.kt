package com.bernaferrari.wifiweakspot.core.i18n.en

import com.bernaferrari.wifiweakspot.core.i18n.CommonStrings

object EnCommonStrings : CommonStrings {
    override fun deleteItemTitle(name: String) = "Delete $name?"

    override val couldNotSaveChanges = "Couldn’t save the changes. Try again."
    override val couldNotDeleteItem = "Couldn’t delete this item. Try again."

    override val hiddenNetworkShort = "Hidden"
    override val network = "Network"
    override val tools = "Tools"
    override val maker = "Maker"
    override val noReply = "No reply"
    override val fastest = "Fastest"
    override val address = "Address"
    override val responseTime = "Response time"
    override val checkAgain = "Recheck"
    override val rename = "Rename"
    override val stop = "Stop"
    override val unknown = "Unknown"

    override val hiddenNetwork = "Hidden network"
    override val fasterScans = "Faster scans"
    override val openWifiSettings = "Open Wi-Fi settings"
    override val signalUnavailable = "Signal unavailable"
    override val devices = "Devices"
    override val openPorts = "Open ports"
    override val nameLabel = "Name"
    override val historyTitle = "History"
    override val routerCheck = "Router check"
    override val testWebsite = "Test a website"
    override val yourRouter = "Your router"
    override val yourNetwork = "Your network"

    override val openInNewTab = "Open in a new tab"
    override val mapSharingAndroidOnly = "Sharing the map works in the Android app."
    override val back = "Back"
    override val close = "Close"
    override val cancel = "Cancel"
    override val save = "Save"
    override val done = "Done"
    override val share = "Share"
    override val delete = "Delete"

    override val tabHome = "Home"
    override val tabNetworks = "Networks"
    override val tabRooms = "Rooms"
    override val tabSettings = "Settings"

    override val permissionScreenTitle = "Allow access to nearby Wi-Fi"
    override val permissionAllowAccess = "Allow access"
    override val openAppSettings = "Open app settings"

    override fun signalBadgeDescription(quality: String, reading: String) = "$quality signal, $reading"

    override val scanReady = "READY"
    override fun scansCount(count: Int) = if (count == 1) "1 scan" else "$count scans"
    override val scansLeftCaption = "left"
    override val scanWaitingCaption = "waiting"
    override val scanDevMode = "DEV MODE"
    override val readyToScan = "Ready to scan"
    override fun scansRemaining(count: Int) = if (count == 1) "1 scan remaining" else "$count scans remaining"
    override fun nextScanIn(time: String) = "Next scan in $time"
    override val checking = "Checking..."
    override val justNow = "just\u00A0now"
    override fun minutesAgo(minutes: Long) = "$minutes\u00A0min\u00A0ago"
    override fun hoursAgo(hours: Long) = "$hours\u00A0h\u00A0ago"
    override fun daysAgo(days: Long) = "$days\u00A0d\u00A0ago"
}
