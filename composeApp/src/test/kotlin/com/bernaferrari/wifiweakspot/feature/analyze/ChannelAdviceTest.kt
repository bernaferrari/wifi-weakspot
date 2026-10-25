package com.bernaferrari.wifiweakspot.feature.analyze

import com.bernaferrari.wifiweakspot.core.wifi.WifiNetwork
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChannelAdviceTest {
    private fun scores(neighborFrequency: Int) = CongestionAnalyzer.analyze(
        listOf(WifiNetwork("Neighbor", "aa:bb:cc:dd:ee:ff", -50, neighborFrequency, 20, "WPA2", "", 1)),
        FrequencyBand.BAND_2_4
    )

    @Test fun tiedCurrentChannelNeverSuggestsAnUnnecessaryChange() {
        val hint = channelHint(scores(2462), 6)!!
        assertEquals(6, hint.channel)
        assertEquals("Your channel isn’t crowded", hint.label)
    }

    @Test fun equalNonstandardChannelAlsoAvoidsFalseImprovementClaim() {
        assertEquals(2, channelHint(scores(2462), 2)!!.channel)
    }

    @Test fun lowerObservedOverlapIsDescribedAsOverlapRatherThanPerformance() {
        val hint = channelHint(scores(2437), 6)!!
        assertEquals(1, hint.channel)
        assertEquals("Less crowded than channel 6", hint.label)
    }

    @Test fun missingRouterSupportDoesNotInventAFiveGhzRecommendation() {
        val scores = CongestionAnalyzer.analyze(emptyList(), FrequencyBand.BAND_5)
        assertNull(channelHint(scores, 36))
    }

    @Test fun equallyCrowdedChannelsDoNotBecomeAClearSignalVerdict() {
        val busy = listOf(2412, 2437, 2462).flatMap { frequency ->
            List(2) { WifiNetwork("Neighbor", "ap-$frequency-$it", -40, frequency, 20, "WPA2", "", 1) }
        }
        val scores = CongestionAnalyzer.analyze(busy, FrequencyBand.BAND_2_4)
        val hint = channelHint(scores, 6)!!
        assertEquals(6, hint.channel)
        assertEquals("No better channel found", hint.label)
    }
}
