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
    Excellent(-55, "Excellent", "Excellent signal", "Everything should feel fast here."),
    Good(-67, "Good", "Good signal", "Video calls and streaming should be smooth."),
    Fair(-75, "Fair", "Okay signal", "Browsing works, but calls and games may stutter."),
    Weak(-82, "Weak", "Weak spot", "Expect buffering and dropouts. Move closer to the router or add a mesh point."),
    DeadZone(Int.MIN_VALUE, "Dead zone", "Dead zone", "The connection barely reaches. This spot needs another access point.");

    /** Bars to draw, 0-4. */
    val bars: Int get() = 4 - ordinal

    val isProblem: Boolean get() = this == Weak || this == DeadZone

    companion object {
        fun of(rssi: Int): SignalQuality = entries.first { rssi >= it.minRssi }

        /** Maps -95..-35 dBm onto 0..1 for meters and gauges. */
        fun fraction(rssi: Int): Float = ((rssi + 95) / 60f).coerceIn(0f, 1f)
    }
}
