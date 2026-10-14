package com.pulsewave.app.feature.analyze

import com.pulsewave.app.core.wifi.WifiNetwork
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNotNull

/**
 * Unit tests for CongestionAnalyzer.
 * Tests channel scoring and recommendation logic.
 */
class CongestionAnalyzerTest {

    private fun createNetwork(
        ssid: String = "TestNet",
        frequency: Int,
        rssi: Int,
        channelWidth: Int = 20,
        centerFreq0: Int = 0,
        centerFreq1: Int = 0,
        is80Plus80: Boolean = false
    ): WifiNetwork = WifiNetwork(
        ssid = ssid,
        bssid = "AA:BB:CC:DD:EE:FF",
        rssi = rssi,
        frequency = frequency,
        channelWidth = channelWidth,
        security = "WPA2",
        capabilities = "",
        centerFreq0 = centerFreq0,
        centerFreq1 = centerFreq1,
        is80Plus80 = is80Plus80,
        timestamp = System.currentTimeMillis()
    )

    @Test
    fun emptyNetworkList_returnsScoresWithZeroCongestion() {
        val networks = emptyList<WifiNetwork>()
        
        val scores = CongestionAnalyzer.analyze(networks, FrequencyBand.BAND_2_4)
        
        // Should return scores for all channels (no congestion = score 0)
        assertEquals(14, scores.size)
        assertTrue(scores.all { it.score == 0.0 })
    }

    @Test
    fun fiveGHzBand_returnsCorrectChannels() {
        val networks = emptyList<WifiNetwork>()
        
        val scores = CongestionAnalyzer.analyze(networks, FrequencyBand.BAND_5)
        
        // Should return 25 channels
        assertEquals(25, scores.size)
        assertEquals(36, scores.first().channelNumber)
        assertEquals(165, scores.last().channelNumber)
    }

    @Test
    fun sixGHzBand_returnsCorrectChannels() {
        val networks = emptyList<WifiNetwork>()
        
        val scores = CongestionAnalyzer.analyze(networks, FrequencyBand.BAND_6)
        
        // Should return channels 1-233 in steps of 4 = 59 channels
        assertEquals(59, scores.size)
        assertEquals(1, scores.first().channelNumber)
        assertEquals(5955, scores.first().centerFreq)
        assertEquals(233, scores.last().channelNumber)
        assertEquals(7115, scores.last().centerFreq)
    }

    @Test
    fun channelWithStrongNearbyNetwork_hasHigherScore() {
        // Channel 6 is at 2437 MHz
        val networkOnChannel6 = createNetwork(
            ssid = "TestNet",
            frequency = 2437, // Channel 6
            rssi = -40 // Strong signal
        )
        
        val scores = CongestionAnalyzer.analyze(listOf(networkOnChannel6), FrequencyBand.BAND_2_4)
        
        // Channel 6 should have a high score (more congestion)
        val channel6Score = scores.find { it.channelNumber == 6 }
        val channel1Score = scores.find { it.channelNumber == 1 }
        
        assertNotNull(channel6Score)
        assertNotNull(channel1Score)
        
        // Channel 6 should have higher score due to nearby network
        assertTrue(channel6Score!!.score > channel1Score!!.score)
    }

    @Test
    fun weakSignalNetwork_contributesLessToCongestion() {
        val strongNetwork = createNetwork(
            ssid = "Strong",
            frequency = 2437, // Channel 6
            rssi = -30 // Very strong
        )
        
        val weakNetwork = createNetwork(
            ssid = "Weak",
            frequency = 2437, // Channel 6
            rssi = -90 // Weak
        )
        
        val scoresWithStrong = CongestionAnalyzer.analyze(listOf(strongNetwork), FrequencyBand.BAND_2_4)
        val scoresWithWeak = CongestionAnalyzer.analyze(listOf(weakNetwork), FrequencyBand.BAND_2_4)
        
        val channel6WithStrong = scoresWithStrong.find { it.channelNumber == 6 }
        val channel6WithWeak = scoresWithWeak.find { it.channelNumber == 6 }
        
        assertNotNull(channel6WithStrong)
        assertNotNull(channel6WithWeak)
        
        // Strong signal should contribute much more to congestion score
        assertTrue(channel6WithStrong!!.score > channel6WithWeak!!.score * 10)
    }

    @Test
    fun bestChannel_isMarkedAsRecommendationFor24GHz() {
        // Create networks on channels 1 and 6
        val networks = listOf(
            createNetwork("Net1", 2412, -50), // Channel 1
            createNetwork("Net2", 2437, -50), // Channel 6
        )
        
        val scores = CongestionAnalyzer.analyze(networks, FrequencyBand.BAND_2_4)
        
        // Channel 11 should be recommended (least congested non-overlapping channel)
        val recommendedChannels = scores.filter { it.recommendation }
        assertEquals(1, recommendedChannels.size)
        
        // For 2.4GHz, should prefer 1, 6, or 11
        assertTrue(recommendedChannels.first().channelNumber in listOf(1, 6, 11))
    }

    @Test
    fun channelScoreRating_isCorrectlyAssigned() {
        // Low score should be "Open Road"
        val lowScore = ChannelScore(1, 2412, 500.0)
        assertEquals(TrafficRating.LOW, lowScore.rating)
        
        // Medium score should be "Moderate"
        val mediumScore = ChannelScore(6, 2437, 2000.0)
        assertEquals(TrafficRating.MEDIUM, mediumScore.rating)
        
        // High score should be "Congested"
        val highScore = ChannelScore(11, 2462, 4500.0)
        assertEquals(TrafficRating.HIGH, highScore.rating)
        
        // Severe score should be "Gridlock"
        val severeScore = ChannelScore(6, 2437, 8000.0)
        assertEquals(TrafficRating.SEVERE, severeScore.rating)
    }

    @Test
    fun multipleNetworksOnSameChannel_combineCongestion() {
        val singleNetwork = listOf(
            createNetwork("Net1", 2437, -50)
        )
        
        val multipleNetworks = listOf(
            createNetwork("Net1", 2437, -50),
            createNetwork("Net2", 2437, -50),
            createNetwork("Net3", 2437, -50),
        )
        
        val singleScore = CongestionAnalyzer.analyze(singleNetwork, FrequencyBand.BAND_2_4)
            .find { it.channelNumber == 6 }
        val multipleScore = CongestionAnalyzer.analyze(multipleNetworks, FrequencyBand.BAND_2_4)
            .find { it.channelNumber == 6 }
        
        assertNotNull(singleScore)
        assertNotNull(multipleScore)
        
        // Multiple networks should have higher congestion score
        assertTrue(multipleScore!!.score > singleScore!!.score * 2)
    }

    @Test
    fun fiveGHzBestChannel_isLowestScore() {
        val networks = listOf(
            createNetwork("Net1", 5180, -50), // Channel 36
            createNetwork("Net2", 5200, -50), // Channel 40
        )
        
        val scores = CongestionAnalyzer.analyze(networks, FrequencyBand.BAND_5, setOf(36, 40, 44, 48))
        
        val recommended = scores.filter { it.recommendation }
        assertEquals(1, recommended.size)
        
        // Should recommend a channel that's NOT 36 or 40 (lower score)
        val recommendedChannel = recommended.first()
        val channel36Score = scores.find { it.channelNumber == 36 }
        val channel40Score = scores.find { it.channelNumber == 40 }
        
        assertNotNull(channel36Score)
        assertNotNull(channel40Score)
        
        assertTrue(recommendedChannel.score <= channel36Score!!.score)
        assertTrue(recommendedChannel.score <= channel40Score!!.score)
    }

    @Test
    fun wideNetwork_reachesNearbyChannelButNotTheFarSideOfTheBand() {
        val wide = createNetwork(frequency = 5180, centerFreq0 = 5210, rssi = -40, channelWidth = 80)
        val scores = CongestionAnalyzer.analyze(listOf(wide), FrequencyBand.BAND_5)
        val channel36 = scores.find { it.channelNumber == 36 }
        val channel165 = scores.find { it.channelNumber == 165 }
        assertNotNull(channel36)
        assertNotNull(channel165)
        assertTrue(channel36!!.score > 0.0)
        assertEquals(0.0, channel165!!.score, 0.0)
    }

    @Test
    fun narrowNetwork_doesNotScoreAChannelThirtyMegahertzAway() {
        val narrow = createNetwork(frequency = 5210, rssi = -40, channelWidth = 20)
        val scores = CongestionAnalyzer.analyze(listOf(narrow), FrequencyBand.BAND_5)
        val channel36 = scores.find { it.channelNumber == 36 }
        assertNotNull(channel36)
        assertEquals(0.0, channel36!!.score, 0.0)
    }

    @Test
    fun sixGigahertzNetwork_scoresChannelOne() {
        val network = createNetwork(frequency = 5955, rssi = -40, channelWidth = 20)
        val scores = CongestionAnalyzer.analyze(listOf(network), FrequencyBand.BAND_6)
        val channel1 = scores.find { it.channelNumber == 1 }
        assertNotNull(channel1)
        assertEquals(5955, channel1!!.centerFreq)
        assertTrue(channel1.score > 0.0)
    }

    @Test
    fun primary36With80MHzCenter_scoresAllFourOccupiedChannels() {
        val network = createNetwork(frequency = 5180, centerFreq0 = 5210, channelWidth = 80, rssi = -40)
        val scores = CongestionAnalyzer.analyze(listOf(network), FrequencyBand.BAND_5)
        listOf(36, 40, 44, 48).forEach { channel ->
            assertTrue("Occupied channel $channel", scores.single { it.channelNumber == channel }.score > 0)
        }
        assertEquals(0.0, scores.single { it.channelNumber == 52 }.score, 0.0)
    }

    @Test
    fun split80Plus80_scoresBothSegmentsAndLeavesGapEmpty() {
        val network = createNetwork(frequency = 5180, centerFreq0 = 5210, centerFreq1 = 5775,
            channelWidth = 80, is80Plus80 = true, rssi = -40)
        val scores = CongestionAnalyzer.analyze(listOf(network), FrequencyBand.BAND_5)
        listOf(36, 40, 44, 48, 149, 153, 157, 161).forEach { channel ->
            assertTrue(scores.single { it.channelNumber == channel }.score > 0)
        }
        assertEquals(0.0, scores.single { it.channelNumber == 100 }.score, 0.0)
    }

    @Test
    fun noObservationsAndUnknownRouterSupport_doNotInventRecommendations() {
        assertTrue(CongestionAnalyzer.analyze(emptyList(), FrequencyBand.BAND_2_4).none { it.recommendation })
        assertTrue(CongestionAnalyzer.analyze(listOf(createNetwork(frequency = 5180, rssi = -40)),
            FrequencyBand.BAND_5).none { it.recommendation })
        assertTrue(CongestionAnalyzer.analyze(listOf(createNetwork(frequency = 5955, rssi = -40)),
            FrequencyBand.BAND_6).none { it.recommendation })
    }

    @Test
    fun knownRouterSupport_neverRecommendsOtherChannels() {
        val scores = CongestionAnalyzer.analyze(listOf(createNetwork(frequency = 5180, rssi = -40)),
            FrequencyBand.BAND_5, setOf(36, 40))
        assertEquals(40, scores.single { it.recommendation }.channelNumber)
    }
}
