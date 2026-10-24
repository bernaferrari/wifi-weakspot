package com.bernaferrari.wifiweakspot.core.wifi

fun getVendorName(bssid: String): String? {
    val cleanBssid = bssid.replace(":", "").uppercase()
    if (cleanBssid.length < 6) return null
    
    val oui = cleanBssid.substring(0, 6)
    
    return when {
        // Common OUIs
        oui.startsWith("00000C") || oui.startsWith("000142") -> "Cisco"
        oui.startsWith("000393") || oui.startsWith("000502") || oui.startsWith("3C0754") || oui.startsWith("F01898") -> "Apple"
        oui.startsWith("00054E") || oui.startsWith("00054F") -> "SpaceX (Starlink)"
        oui.startsWith("001A11") || oui.startsWith("F88FCA") || oui.startsWith("1C53F9") -> "Google"
        oui.startsWith("000C41") || oui.startsWith("000F66") || oui.startsWith("E894F6") -> "Linksys"
        oui.startsWith("0014D1") || oui.startsWith("0024B2") || oui.startsWith("C03F0E") -> "Netgear"
        oui.startsWith("001D0F") || oui.startsWith("C025E9") || oui.startsWith("F81A67") -> "TP-Link"
        oui.startsWith("0418D6") || oui.startsWith("00112F") || oui.startsWith("D8C718") -> "Ubiquiti"
        oui.startsWith("0026CB") || oui.startsWith("0026CC") || oui.startsWith("3476C5") -> "Intel"
        oui.startsWith("00E04C") || oui.startsWith("FCADB3") -> "Realtek"
        oui.startsWith("AC8B9D") -> "ASUS"
        oui.startsWith("288023") || oui.startsWith("D46E0E") -> "Amazon (eero)"
        else -> null
    }
}
