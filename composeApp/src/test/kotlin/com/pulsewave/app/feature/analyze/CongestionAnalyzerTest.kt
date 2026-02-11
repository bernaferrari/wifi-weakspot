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
        rssi: Int
    ): WifiNetwork = WifiNetwork(
        ssid = ssid,
        bssid = "AA:BB:CC:DD:EE:FF",
        rssi = rssi,
        frequency = frequency,
        channelWidth = 20,
        security = "WPA2",
        capabilities = "",
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
        assertEquals(233, scores.last().channelNumber)
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
        
        val scores = CongestionAnalyzer.analyze(networks, FrequencyBand.BAND_5)
        
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
}
