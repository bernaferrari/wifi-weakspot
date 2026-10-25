package com.bernaferrari.wifiweakspot.core.wifi

import com.bernaferrari.wifiweakspot.core.i18n.appStrings
enum class SignalDisplay {
    Percentage, Bars, Dbm;

    /** "%" and "dBm" are symbols; only "Bars" is translated. */
    val label: String get() = when (this) {
        Percentage -> "%"
        Bars -> appStrings.domain.signalDisplayBars
        Dbm -> "dBm"
    }

    /** Bars still use a friendly numeric level in chart readouts; details retain raw dBm. */
    fun reading(rssi: Int): String = if (this == Dbm) "$rssi dBm" else "${SignalQuality.percentage(rssi)}%"
}
