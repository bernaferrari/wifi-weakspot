package com.bernaferrari.wifiweakspot.core.wifi

import com.bernaferrari.wifiweakspot.core.i18n.appStrings
import kotlin.math.roundToInt
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
         * Signal strength as a percentage of what Wi-Fi needs, not of power or speed.
         *
         * Anchored to the verdicts above so the number and the label always agree: Excellent is
         * 90-100%, Good 70-89%, Fair 50-69%, Weak 30-49%, and a dead zone is below 30%. The curve
         * is steeper toward the weak end, where each dB changes the experience most, and reaches
         * 0% at -90 dBm, where Wi-Fi stops being usable. -50 dBm and stronger is 100%.
         */
        fun percentage(rssi: Int): Int {
            val dbm = rssi.coerceIn(-90, -50)
            val (low, high) = scale.zipWithNext().first { (a, b) -> dbm in a.first..b.first }
            val t = (dbm - low.first).toFloat() / (high.first - low.first)
            return (low.second + t * (high.second - low.second)).roundToInt()
        }

        /** (dBm, percentage) anchors, weakest first; the verdict floors map to round numbers. */
        private val scale = listOf(-90 to 0, -82 to 30, -75 to 50, -67 to 70, -55 to 90, -50 to 100)

        /** Maps -95..-35 dBm onto 0..1 for meters and gauges. */
        fun fraction(rssi: Int): Float = ((rssi + 95) / 60f).coerceIn(0f, 1f)
    }
}
