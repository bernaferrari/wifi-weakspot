package com.pulsewave.app.feature.analyze

enum class FrequencyBand(val label: String) {
    BAND_2_4("2.4 GHz"),
    BAND_5("5 GHz"),
    BAND_6("6 GHz");

    fun contains(frequency: Int): Boolean = when (this) {
        BAND_2_4 -> frequency in 2400..2500
        BAND_5 -> frequency in 5150..5924
        BAND_6 -> frequency in 5925..7125
    }
}
