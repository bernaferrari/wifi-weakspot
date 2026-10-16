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
    Excellent(-55, "Excellent", "Excellent signal", "Great for anything."),
    Good(-67, "Good", "Good signal", "Fine for video calls and streaming."),
    Fair(-75, "Okay", "Okay signal", "Fine for browsing, calls may stutter."),
    Weak(-82, "Weak", "Weak spot", "Expect slow pages and dropped calls."),
    DeadZone(Int.MIN_VALUE, "Dead zone", "Dead zone", "Wi-Fi barely works here.");

    /** Bars to draw, 0-4. */
    val bars: Int get() = 4 - ordinal

    val isProblem: Boolean get() = this == Weak || this == DeadZone

    companion object {
        fun of(rssi: Int): SignalQuality = entries.first { rssi >= it.minRssi }

        /**
         * A 0–100 score where bigger is better, like a battery level: -50 dBm or stronger is 100,
         * each dB below that costs two points, so -64 is 72 and -89 is 22.
         */
        fun score(rssi: Int): Int = (2 * (rssi + 100)).coerceIn(0, 100)

        /** Maps -95..-35 dBm onto 0..1 for meters and gauges. */
        fun fraction(rssi: Int): Float = ((rssi + 95) / 60f).coerceIn(0f, 1f)
    }
}
