package com.bernaferrari.wifiweakspot.core.i18n

/**
 * The floor plan map (survey), the Rooms tab (walk), and the "Faster scans" setup screen.
 * Generic actions (Back, Cancel, Save, Delete, Close) live in [CommonStrings].
 */
interface SurveyStrings {
    val horizontalPosition: String
    val verticalPosition: String
    val measureHere: String
    val recheckReading: String
    val moveLeft: String
    val moveRight: String
    val moveUp: String
    val moveDown: String
    fun mapPosition(x: Int, y: Int): String
    fun pointName(id: Long): String

    // Floor plan map: toolbar status, one instruction at a time.

    /** Toolbar status while a reading is being taken. */
    val measuringHoldStill: String
    /** Toolbar status before the first reading. */
    val tapWhereYouAreStanding: String
    /** Toolbar button that opens the network picker. Short. */
    val chooseWifi: String
    /** Icon button: removes the most recent reading (accessibility label). */
    val undoLastReading: String
    /** Icon button: opens the list of readings (accessibility label). */
    val allReadings: String

    // Floor plan map: top bar and menu.

    /** Top bar title when no map is open. */
    val floorPlanTitle: String
    /** Number of readings on the current floor, e.g. "3 readings". Top bar subtitle, short. */
    fun readingCount(count: Int): String
    /** Overflow menu icon (accessibility label). */
    val mapOptions: String
    /** Menu item: adds a floor plan photo as the map background. */
    val usePlanPhoto: String
    /** Menu item: replaces the floor plan photo. */
    val changePhoto: String
    /** Menu item: removes the floor plan photo. */
    val removePhoto: String
    /** Menu item: exports readings as a CSV file. */
    val exportReadingsCsv: String

    // Floors. Floor 0 is the basement.

    /** Name of a floor: "Basement" for 0, otherwise "Floor 2". Used as a sheet title, button label, and accessibility label. */
    fun floorName(floor: Int): String
    /** Collapsed floor button: "B" for the basement, otherwise just the number. One or two characters. */
    fun floorShortLabel(floor: Int): String
    /** The only floor button when the map has one floor: "Floor B" for the basement, otherwise "Floor 1". Short. */
    fun floorButtonLabel(floor: Int): String
    /** Icon button that adds a floor (accessibility label): "Add a basement" for 0, otherwise "Add floor 2". */
    fun addFloor(floor: Int): String
    /** Accessibility action on a floor button (long press). */
    val deleteFloorReadingsAction: String
    /** Dialog body: how many readings on the floor will be deleted. */
    fun deleteFloorBody(count: Int): String

    // Network picker dialog.

    /** Dialog explanation under the title. */
    val oneRouterAtATime: String
    /** Empty state in the network list. */
    val noNetworksFound: String
    /** Refresh button label while a scan runs. */
    val scanning: String
    /** Button: scans again for networks. */
    val refresh: String
    /** Confirmation dialog title when changing the network of a map that has readings. */
    val switchWifiTitle: String
    /** Confirmation dialog body for switching networks. */
    val switchWifiBody: String
    /** Confirmation button for switching networks. */
    val clearAndSwitch: String

    // Delete map dialog.

    /** Dialog body. */
    val deleteMapBody: String

    // Readings sheet.

    /** Sheet subtitle when the floor has no readings. */
    val noReadingsYet: String
    /** Sheet subtitle explaining the sort order. */
    val weakestFirst: String

    // Empty state.

    /** Empty state title. */
    val mapYourHome: String
    /** Empty state body. */
    val mapYourHomeBody: String
    /** Empty state button. */
    val createMap: String

    // Rename dialog.

    /** Text field placeholder with example map names. */
    val namePlaceholder: String

    // Map canvas (accessibility).

    /** Image description of the floor plan photo. */
    val floorplanDescription: String
    /** Accessibility description of the map; [count] is the number of readings. */
    fun coverageMapDescription(count: Int): String

    // Floor plan map: messages (snackbars).

    val networkNotSeenHere: String
    val projectChangedWhileScanning: String
    val couldNotSaveReading: String
    /**
     * Name given to a new map: "Home" for [index] 1, then "Home 2", "Home 3"…
     * Saved as the map's name, which people can rename later.
     */
    fun defaultMapName(index: Int): String
    val stillSavingMap: String
    val noFreshScan: String
    val couldNotScan: String
    val chooseAccessPointFirst: String
    val saveMeasurementBeforeExport: String
    val couldNotExportProject: String

    // Rooms tab.

    /** Headline when not connected. */
    val connectToYourWifi: String
    /** Subtitle while saving; [room] is the room name the person typed. */
    fun checkingRoom(room: String): String
    /** Subtitle shown when the connected network's name is hidden. */
    val connectedWifi: String
    /** Subtitle when connected but no reading yet. */
    val connectedNoReading: String
    /** Subtitle when not connected. */
    val connectToCheck: String
    /** Snackbar after removing a room; [room] is its name. Has an Undo action. */
    fun removedRoom(room: String): String
    /** Snackbar action that brings back a removed room. Short. */
    val undo: String
    /** Empty state under the signal meter. */
    val roomsEmptyHint: String
    /** Accessibility label of the signal meter when there's no signal. */
    val noWifiSignal: String
    /** Button: saves a reading for a room. Short (half-width button). */
    val saveRoom: String
    /** Change since the last check, in a list row: "6 dB stronger". [delta] is positive. Short. */
    fun dbStronger(delta: Int): String
    /** Change since the last check, in a list row: "6 dB weaker". [delta] is positive. Short. */
    fun dbWeaker(delta: Int): String
    /** Section header above the saved rooms list. */
    val savedRooms: String
    /** Swipe-to-delete icon (accessibility label); [room] is its name. */
    fun deleteRoom(room: String): String
    /**
     * Accessibility description of a saved room row. [quality] is a short quality label
     * (e.g. "Good"), [reading] e.g. "-55 dBm".
     */
    fun roomDescription(room: String, quality: String, reading: String): String

    // Rooms tab: messages (snackbars).

    val noSignalToSave: String
    val allowScansToLink: String
    val connectToSpotNetwork: String
    /** Snackbar after saving a room's first reading. */
    fun savedRoom(room: String): String
    val couldNotSaveStableReading: String

    // Room picker sheet.

    /** Sheet title. */
    val whereAreYou: String
    /** Placeholder of the custom room name field. */
    val somewhereElse: String

    // Room history sheet.

    /** When the room was last saved; [time] is a relative time like "5 min ago". */
    fun savedAt(time: String): String
    /** Tip shown when a room's signal is weak. */
    val moveRouterTip: String
    val betterThanLastTime: String
    val worseThanLastTime: String
    val sameAsLastTime: String
    /** Section header above older readings. */
    val earlierChecks: String
    /** Button: removes the room and its history. Short (half-width button). */
    val remove: String
    /** Hint when on another network; [ssid] is the room's network, null or blank if hidden. */
    fun connectToRecheck(ssid: String?): String

    // Faster scans setup. Text inside **double asterisks** is a setting name and is highlighted; keep the markers.

    val stepDeveloperOptionsTitle: String
    val stepDeveloperOptionsBody: String
    /** Button that opens the system "About phone" screen. */
    val openAboutPhone: String
    val stepThrottlingTitle: String
    val stepThrottlingBody: String
    /** Button that opens the system Developer options. */
    val openDeveloperOptions: String
    val stepComeBackTitle: String
    val stepComeBackBody: String
    /** Toggle that hides the ADB instructions. */
    val hideComputerMethod: String
    /** Toggle that shows the ADB instructions. */
    val useAdbInstead: String
    /** Clipboard label for the copied ADB command (may be shown by the system). */
    val adbCommandClipLabel: String
    /** Toggle that hides the root option. */
    val hideRootOption: String
    /** Toggle that shows the root option. */
    val useRootInstead: String
    /** Footnote at the bottom of the screen. */
    val scanLimitFootnote: String
    val unlimitedWithRootTitle: String
    val unlimitedOnTitle: String
    val unlimitedOnBody: String
    val scansLimitedTitle: String
    val scansLimitedAndroid9Body: String
    /** Root card title while scanning through root. */
    val usingRoot: String
    /** Root card title when root is available. */
    val deviceRooted: String
    val rootScanningBody: String
    val rootDeniedBody: String
    val rootNotNeededBody: String
    val rootOfferBody: String
    /** Button: stops scanning through root. */
    val stopUsingRoot: String
    val waitingForRootManager: String
    /** Button after root access was denied. */
    val tryRootAgain: String
    /** Button: starts scanning through root. */
    val useRoot: String
    /** Copy button label after copying. Short. */
    val copied: String
    /** Copy button label. Short. */
    val copyCommand: String
}
