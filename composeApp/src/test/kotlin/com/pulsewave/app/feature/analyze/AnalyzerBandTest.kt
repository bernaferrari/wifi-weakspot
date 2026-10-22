package com.pulsewave.app.feature.analyze

import com.pulsewave.app.core.model.NetworkStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyzerBandTest {
    private fun connected(frequency: Int) = NetworkStatus.Disconnected.copy(isConnected = true, frequency = frequency)

    @Test fun lateConnectionReplacesTheDisconnectedFallback() {
        assertEquals(FrequencyBand.BAND_2_4, analyzerBand(NetworkStatus.Disconnected, null))
        assertEquals(FrequencyBand.BAND_5, analyzerBand(connected(5180), null))
        assertEquals(FrequencyBand.BAND_6, analyzerBand(connected(5955), null))
    }

    @Test fun manualBandChoiceSurvivesConnectionArrivalAndChanges() {
        val chosen = FrequencyBand.BAND_2_4
        assertEquals(chosen, analyzerBand(NetworkStatus.Disconnected, chosen))
        assertEquals(chosen, analyzerBand(connected(5180), chosen))
        assertEquals(chosen, analyzerBand(connected(5955), chosen))
    }

    @Test fun disconnectedOrUnknownFrequenciesDoNotSelectAPhantomConnectedBand() {
        assertEquals(FrequencyBand.BAND_2_4, analyzerBand(NetworkStatus.Disconnected.copy(frequency = 5180), null))
        assertEquals(FrequencyBand.BAND_2_4, analyzerBand(connected(0), null))
        assertEquals(FrequencyBand.BAND_6, analyzerBand(connected(0), FrequencyBand.BAND_6))
    }
}
