package com.bernaferrari.wifiweakspot.core.i18n.en

import com.bernaferrari.wifiweakspot.core.i18n.SurveyStrings

object EnSurveyStrings : SurveyStrings {
    override val measuringHoldStill = "Measuring… hold still"
    override val tapWhereYouAreStanding = "Tap the map where you're standing"
    override val chooseWifi = "Choose Wi-Fi"
    override val undoLastReading = "Undo last reading"
    override val allReadings = "All readings"
    override val floorPlanTitle = "Floor plan"
    override fun readingCount(count: Int) = if (count == 1) "1 reading" else "$count readings"
    override val mapOptions = "Map options"
    override val usePlanPhoto = "Use a photo of your plan"
    override val changePhoto = "Change the photo"
    override val removePhoto = "Remove the photo"
    override val exportReadingsCsv = "Export readings (CSV)"
    override fun floorName(floor: Int) = if (floor == 0) "Basement" else "Floor $floor"
    override fun floorShortLabel(floor: Int) = if (floor == 0) "B" else "$floor"
    override fun floorButtonLabel(floor: Int) = "Floor ${floorShortLabel(floor)}"
    override fun addFloor(floor: Int) = if (floor == 0) "Add a basement" else "Add floor $floor"
    override val deleteFloorReadingsAction = "Delete this floor's readings"
    override fun deleteFloorBody(count: Int) =
        if (count == 1) "Its 1 reading will be deleted." else "Its $count readings will be deleted."

    override val oneRouterAtATime = "A map follows one router at a time. Mesh systems show each unit separately."
    override val noNetworksFound = "No networks found yet. Check that Wi-Fi and location are on."
    override val scanning = "Scanning…"
    override val refresh = "Refresh"
    override val switchWifiTitle = "Switch to another Wi-Fi?"
    override val switchWifiBody = "This clears the readings on every floor of this map. Create a new map to keep both."
    override val clearAndSwitch = "Clear and switch"

    override val deleteMapBody = "All readings on every floor will be deleted."

    override val noReadingsYet = "No readings here yet. Tap the map where you're standing."
    override val weakestFirst = "Weakest first, so problem spots are on top."
    override val mapYourHome = "Map your home"
    override val mapYourHomeBody = "See your Wi-Fi on a map of your home. Use a photo of your plan or a blank grid."
    override val createMap = "Create a map"

    override val namePlaceholder = "Upstairs, Office…"

    override val floorplanDescription = "Project floorplan"
    override fun coverageMapDescription(count: Int) =
        "Coverage map with ${readingCount(count)}. Tap where you are standing to measure."

    override val networkNotSeenHere = "That Wi-Fi didn't show up here. Nothing was saved."
    override val projectChangedWhileScanning = "The project changed while scanning. No point was saved."
    override val couldNotSaveReading = "Couldn't save that reading. Your other readings are safe."
    override fun defaultMapName(index: Int) = if (index == 1) "Home" else "Home $index"
    override val stillSavingMap = "One moment, still saving the map."
    override val noFreshScan = "Android did not provide a fresh scan. Try again shortly."
    override val couldNotScan = "Could not scan. Check Wi-Fi and location settings."
    override val chooseAccessPointFirst = "Choose an access point before mapping its coverage."
    override val saveMeasurementBeforeExport = "Save at least one measurement before exporting."
    override val couldNotExportProject = "Could not export this project. Try again."

    override val connectToYourWifi = "Connect to your Wi-Fi"
    override fun checkingRoom(room: String) = "Checking $room… keep still"
    override val connectedWifi = "Connected Wi-Fi"
    override val connectedNoReading = "Your device is connected, but no signal reading is available yet."
    override val connectToCheck = "Connect to the Wi-Fi you want to check."
    override fun removedRoom(room: String) = "Removed $room"
    override val undo = "Undo"
    override val roomsEmptyHint =
        "Stand in a room and save a reading. Check another room to find where the signal gets weaker."
    override val noWifiSignal = "No Wi-Fi signal"
    override val saveRoom = "Save room"
    override fun dbStronger(delta: Int) = "$delta dB stronger"
    override fun dbWeaker(delta: Int) = "$delta dB weaker"
    override val savedRooms = "Saved rooms"
    override fun deleteRoom(room: String) = "Delete $room"
    override fun roomDescription(room: String, quality: String, reading: String) =
        "$room, $quality signal, $reading. Review readings and recheck this spot."

    override val noSignalToSave = "No Wi-Fi signal to save. Connect to your Wi-Fi first."
    override val allowScansToLink = "Allow Wi-Fi scans in Networks so this reading can be linked to your network."
    override val connectToSpotNetwork = "Connect to this spot's network before rechecking it."
    override fun savedRoom(room: String) = "Saved $room"
    override val couldNotSaveStableReading = "Couldn't save a stable reading. Keep still on the same Wi-Fi and try again."
    override val whereAreYou = "Where are you?"
    override val somewhereElse = "Somewhere else"

    override fun savedAt(time: String) = "Saved $time"
    override val moveRouterTip = "Try moving the router higher, more central, or closer to this room."
    override val betterThanLastTime = "Better than last time"
    override val worseThanLastTime = "Worse than last time"
    override val sameAsLastTime = "Same as last time"
    override val earlierChecks = "Earlier checks"
    override val remove = "Remove"
    override fun connectToRecheck(ssid: String?) =
        "Connect to ${ssid?.ifBlank { null } ?: "the same Wi-Fi"} to check again."

    override val stepDeveloperOptionsTitle = "Turn on Developer options"
    override val stepDeveloperOptionsBody =
        "In Settings, open **About phone** and tap **Build number** 7 times. You may need to enter your screen lock."
    override val openAboutPhone = "Open About phone"
    override val stepThrottlingTitle = "Turn off Wi-Fi scan throttling"
    override val stepThrottlingBody =
        "In **Developer options**, scroll down to **Networking** and switch off **Wi-Fi scan throttling**."
    override val openDeveloperOptions = "Open Developer options"
    override val stepComeBackTitle = "Come back here"
    override val stepComeBackBody =
        "WiFi\u00A0Weakspot checks automatically when you return. You can switch throttling back on at any time."
    override val hideComputerMethod = "Hide computer method"
    override val useAdbInstead = "Prefer a computer? Use ADB"
    override val adbCommandClipLabel = "ADB command"
    override val hideRootOption = "Hide root option"
    override val useRootInstead = "Rooted device? Use root instead"
    override val scanLimitFootnote = "Android limits scans to save battery. You can switch the limit back on anytime."
    override val unlimitedWithRootTitle = "Unlimited scans with root"
    override val unlimitedOnTitle = "Unlimited scans are on"
    override val unlimitedOnBody = "Nearby networks and channel graphs refresh every few seconds."
    override val scansLimitedTitle = "Scans are limited"
    override val scansLimitedAndroid9Body =
        "Android 9 allows 4 scans every 2 minutes and can't turn this off. WiFi\u00A0Weakspot reuses recent results in between."
    override val usingRoot = "Using root"
    override val deviceRooted = "Your device is rooted"
    override val rootScanningBody = "Scans go through the system shell, so there's nothing to change in Developer options."
    override val rootDeniedBody =
        "Root access was denied. Allow WiFi\u00A0Weakspot in your root manager and try again, or use Developer options below."
    override val rootNotNeededBody = "Scans are already unlimited. Root isn't needed."
    override val rootOfferBody =
        "Skip Developer options and let WiFi\u00A0Weakspot scan through root. Your root manager will ask once."
    override val stopUsingRoot = "Stop using root"
    override val waitingForRootManager = "Waiting for your root manager…"
    override val tryRootAgain = "Try root again"
    override val useRoot = "Use root"
    override val copied = "Copied"
    override val copyCommand = "Copy command"
}
