package com.pulsewave.app.core.lan

import com.pulsewave.app.core.model.lan.LanDevice

/** What a device most likely is, guessed from its name and maker. */
enum class DeviceKind(val label: String) {
    Router("Router"),
    ThisPhone("This phone"),
    Phone("Phone or tablet"),
    Computer("Computer"),
    Tv("TV or streamer"),
    Speaker("Speaker"),
    Printer("Printer"),
    Console("Game console"),
    Camera("Camera"),
    SmartHome("Smart home device"),
    Unknown("Device")
}

/** How a device is shown: a friendly name, its kind, and a short line of detail. */
data class DeviceIdentity(val name: String, val kind: DeviceKind, val detail: String)

private val NameHints: List<Pair<DeviceKind, List<String>>> = listOf(
    DeviceKind.Router to listOf("router", "gateway", "modem", "openwrt", "fritz", "mesh", "deco", "eero", "orbi"),
    DeviceKind.Tv to listOf("tv", "chromecast", "roku", "firetv", "fire-tv", "aftmm", "bravia", "shield", "appletv", "apple-tv", "googletv", "oled", "qled"),
    DeviceKind.Phone to listOf("iphone", "ipad", "android", "pixel", "galaxy", "phone", "redmi", "oneplus", "tablet"),
    DeviceKind.Computer to listOf("macbook", "imac", "mac", "mac-mini", "laptop", "desktop", "pc", "thinkpad", "windows", "ubuntu", "workstation", "nas", "synology", "raspberrypi"),
    DeviceKind.Speaker to listOf("echo", "alexa", "homepod", "sonos", "speaker", "nest-audio", "google-home", "nest-mini"),
    DeviceKind.Printer to listOf("printer", "epson", "brother", "canon", "laserjet", "officejet", "deskjet"),
    DeviceKind.Console to listOf("xbox", "playstation", "ps4", "ps5", "nintendo", "switch"),
    DeviceKind.Camera to listOf("cam", "doorbell", "ring-", "arlo", "wyze", "blink"),
    DeviceKind.SmartHome to listOf("esp", "shelly", "tasmota", "plug", "bulb", "light", "tuya", "hue", "nest", "thermostat", "vacuum", "roborock", "tado")
)

private val VendorHints: List<Pair<DeviceKind, List<String>>> = listOf(
    DeviceKind.SmartHome to listOf("espressif", "tuya", "shelly", "signify", "philips lighting", "itead", "ecobee", "roborock"),
    DeviceKind.Printer to listOf("hewlett", "hp inc", "epson", "brother", "canon", "lexmark", "kyocera"),
    DeviceKind.Speaker to listOf("sonos", "bose"),
    DeviceKind.Console to listOf("nintendo", "sony interactive"),
    DeviceKind.Tv to listOf("roku", "vizio", "hisense", "tcl"),
    DeviceKind.Camera to listOf("hikvision", "dahua", "wyze", "arlo"),
    DeviceKind.Computer to listOf("raspberry", "synology", "intel", "dell", "lenovo", "asustek")
)

/** Words are matched whole or as prefixes so "pc" doesn't fire inside "epcot". */
private fun String.hasHint(hint: String): Boolean =
    split('-', '_', '.', ' ').any { word -> word == hint || (hint.length >= 4 && word.startsWith(hint)) } ||
        (hint.length >= 5 && contains(hint))

private val BrandCasing = mapOf(
    "macbook" to "MacBook", "iphone" to "iPhone", "ipad" to "iPad", "imac" to "iMac", "appletv" to "Apple TV",
    "homepod" to "HomePod", "playstation" to "PlayStation", "xbox" to "Xbox", "chromecast" to "Chromecast"
)

/** Turns "living-room-tv.lan" into "Living room TV". */
internal fun friendlyHostname(hostname: String): String? {
    val base = hostname.substringBefore('.').trim()
    if (base.isEmpty() || base.all { it.isDigit() || it == '-' }) return null
    val words = base.split('-', '_', ' ').filter { it.isNotBlank() }
    return words.mapIndexed { index, word ->
        when (val lower = word.lowercase()) {
            "tv", "pc", "nas", "hp", "lg" -> word.uppercase()
            in BrandCasing -> BrandCasing.getValue(lower)
            else -> if (index == 0) word.replaceFirstChar { it.uppercase() } else word
        }
    }.joinToString(" ")
}

/** Short maker name: "Samsung Electronics Co.,Ltd" becomes "Samsung". */
internal fun shortVendor(vendor: String): String =
    vendor.substringBefore(',').split(' ').firstOrNull { it.isNotBlank() }?.trim().orEmpty().ifEmpty { vendor }

/** Apple and Cast devices put their hardware in TXT records: "AppleTV14,1", "AudioAccessory5,1". */
private fun kindFromModel(model: String): DeviceKind? {
    val m = model.lowercase()
    return when {
        m.startsWith("appletv") || m.contains("chromecast") || m.contains("google tv") -> DeviceKind.Tv
        m.startsWith("audioaccessory") || m.contains("homepod") || m.contains("nest audio") || m.contains("google home") || m.contains("nest mini") -> DeviceKind.Speaker
        m.startsWith("macbook") || m.startsWith("macmini") || m.startsWith("imac") || m.startsWith("mac") -> DeviceKind.Computer
        m.startsWith("ipad") || m.startsWith("iphone") -> DeviceKind.Phone
        else -> null
    }
}

/** What a device offers says what it is: a Cast receiver is a screen, an IPP endpoint a printer. */
private fun kindFromServices(services: List<String>): DeviceKind? {
    fun has(vararg types: String) = types.any { it in services }
    return when {
        has("_ipp._tcp", "_printer._tcp", "_pdl-datastream._tcp", "_scanner._tcp") -> DeviceKind.Printer
        has("_googlecast._tcp", "_androidtvremote2._tcp", "_amzn-wplay._tcp") -> DeviceKind.Tv
        has("_sonos._tcp") -> DeviceKind.Speaker
        has("_hap._tcp", "_miio._udp", "_matter._tcp") -> DeviceKind.SmartHome
        has("_nvstream._tcp", "_rfb._tcp", "_smb._tcp", "_workstation._tcp", "_ssh._tcp") -> DeviceKind.Computer
        // AirPlay without Apple's companion protocol is a third-party TV (Samsung, LG, Sony).
        has("_airplay._tcp") && !has("_companion-link._tcp") -> DeviceKind.Tv
        has("_raop._tcp", "_spotify-connect._tcp") && !has("_companion-link._tcp") -> DeviceKind.Speaker
        else -> null
    }
}

fun LanDevice.identify(gateway: String?, phoneIp: String?): DeviceIdentity {
    val said = announcedName?.lowercase().orEmpty()
    val host = hostname?.lowercase().orEmpty()
    val maker = vendor?.lowercase().orEmpty()
    fun byName(text: String) = NameHints.firstOrNull { (_, hints) -> hints.any { text.hasHint(it) } }?.first
    val kind = when {
        !gateway.isNullOrBlank() && ipAddress == gateway -> DeviceKind.Router
        !phoneIp.isNullOrBlank() && ipAddress == phoneIp -> DeviceKind.ThisPhone
        else -> model?.let(::kindFromModel)
            ?: byName(said)
            ?: kindFromServices(services)
            ?: byName(host)
            ?: VendorHints.firstOrNull { (_, hints) -> hints.any { maker.contains(it) } }?.first
            ?: DeviceKind.Unknown
    }
    val makerName = vendor?.let(::shortVendor)
    val name = customName?.takeIf { it.isNotBlank() }
        ?: when (kind) {
            DeviceKind.Router -> "Your router"
            DeviceKind.ThisPhone -> "This phone"
            else -> null
        }
        ?: announcedName?.takeIf { it.isNotBlank() }
        ?: hostname?.let(::friendlyHostname)
        ?: makerName?.let { if (kind == DeviceKind.Unknown) "$it device" else "$it ${kind.label.lowercase()}" }
        ?: if (kind == DeviceKind.Unknown) "Unnamed device" else kind.label
    val detail = listOfNotNull(
        // This phone's own name ("Galaxy S25") is the most useful detail it has.
        announcedName?.takeIf { kind == DeviceKind.ThisPhone && customName == null },
        // The kind already shows as the row's icon, so the line keeps to maker and address.
        makerName?.takeIf { !name.contains(it, ignoreCase = true) },
        ipAddress
    ).joinToString(" · ")
    return DeviceIdentity(name, kind, detail)
}
