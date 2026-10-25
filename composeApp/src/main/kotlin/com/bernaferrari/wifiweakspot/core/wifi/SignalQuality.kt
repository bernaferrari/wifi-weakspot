package com.bernaferrari.wifiweakspot.core.wifi

import com.bernaferrari.wifiweakspot.core.i18n.appStrings
/**
 * One plain-language scale for received signal strength, shared by every screen.
 *
 * Thresholds follow common home-network guidance: about -67 dBm is the floor for
 * smooth calls and streaming, and links below about -80 dBm drop or stall.
 */
enum class SignalQuality(val minRssi: Int) {
    Excellent(-55),
    Good(-67),
    Fair(-75),
    Weak(-82),
    DeadZone(Int.MIN_VALUE);

    val label: String get() = with(appStrings.domain) {
        when (this@SignalQuality) {
            Excellent -> qualityExcellentLabel
            Good -> qualityGoodLabel
            Fair -> qualityFairLabel
            Weak -> qualityWeakLabel
            DeadZone -> qualityDeadZoneLabel
        }
    }

    val headline: String get() = with(appStrings.domain) {
        when (this@SignalQuality) {
            Excellent -> qualityExcellentHeadline
            Good -> qualityGoodHeadline
            Fair -> qualityFairHeadline
            Weak -> qualityWeakHeadline
            DeadZone -> qualityDeadZoneLabel
        }
    }

    val explanation: String get() = with(appStrings.domain) {
        when (this@SignalQuality) {
            Excellent -> qualityExcellentExplanation
            Good -> qualityGoodExplanation
            Fair -> qualityFairExplanation
            Weak -> qualityWeakExplanation
            DeadZone -> qualityDeadZoneExplanation
        }
    }

    /** Bars to draw, 0-4. */
    val bars: Int get() = 4 - ordinal

    val isProblem: Boolean get() = this == Weak || this == DeadZone

    companion object {
        fun of(rssi: Int): SignalQuality = entries.first { rssi >= it.minRssi }

        /**
         * Fixed signal-level scale: -100 dBm = 0%, -50 dBm = 100%.
         * Matches the Windows WLAN signal-quality mapping, not a power or speed percentage.
         * https://learn.microsoft.com/en-us/windows/win32/api/wlanapi/ns-wlanapi-wlan_realtime_connection_quality
         */
        fun percentage(rssi: Int): Int = 2 * (rssi.coerceIn(-100, -50) + 100)

        /** Maps -95..-35 dBm onto 0..1 for meters and gauges. */
        fun fraction(rssi: Int): Float = ((rssi + 95) / 60f).coerceIn(0f, 1f)
    }
}
