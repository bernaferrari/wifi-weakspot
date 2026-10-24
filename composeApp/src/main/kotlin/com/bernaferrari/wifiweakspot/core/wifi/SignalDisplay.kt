package com.bernaferrari.wifiweakspot.core.wifi

enum class SignalDisplay(val label: String) {
    Percentage("%"), Bars("Bars"), Dbm("dBm");

    /** Bars still use a friendly numeric level in chart readouts; details retain raw dBm. */
    fun reading(rssi: Int): String = if (this == Dbm) "$rssi dBm" else "${SignalQuality.percentage(rssi)}%"
}
