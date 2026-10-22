package com.pulsewave.app.core.wifi

/**
 * One plain-language scale for received signal strength, shared by every screen.
 *
 * Thresholds follow common home-network guidance: about -67 dBm is the floor for
 * smooth calls and streaming, and links below about -80 dBm drop or stall.
 */
enum class SignalQuality(
    val minRssi: Int,
    val label: String,
    val headline: String,
    val explanation: String
) {
    Excellent(-55, "Excellent", "Excellent signal", "A strong connection to the router. Signal alone doesn’t measure internet speed."),
    Good(-67, "Good", "Good signal", "A solid signal from the router."),
    Fair(-75, "Okay", "Okay signal", "A usable signal, with less room for interference."),
    Weak(-82, "Weak", "Weak spot", "A weak signal. Compare this spot with one closer to the router."),
    DeadZone(Int.MIN_VALUE, "Dead zone", "Dead zone", "Very little signal reaches this spot.");

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
