package com.bernaferrari.wifiweakspot.core.lan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.CancellationException
import org.junit.Test

class LanDiscoveryTest {
    @Test fun keepsCachedNeighborsEvenWhenOtherHostsReply() {
        assertEquals(setOf("192.168.1.2", "192.168.1.3"),
            mergeDiscoveredHosts(setOf("192.168.1.2"), setOf("192.168.1.2", "192.168.1.3")))
    }
    @Test fun returnsCachedNeighborsWhenNoPingReplies() {
        assertEquals(setOf("192.168.1.3"), mergeDiscoveredHosts(emptySet(), setOf("192.168.1.3")))
    }
    @Test fun tcpFallbackConfirmsPingBlockingDeviceAndStopsAfterReply() = runTest {
        val ports = mutableListOf<Int>()
        assertTrue(reachableWithFallback("192.168.1.3", { false }, { _, port -> ports.add(port); port == 443 }))
        assertEquals(listOf(80, 443), ports)
    }
    @Test fun pingReplyAvoidsTcpAndFailedProbesNeverConfirmReachability() = runTest {
        assertTrue(reachableWithFallback("192.168.1.3", { true }, { _, _ -> error("No TCP needed") }))
        val ports = mutableListOf<Int>()
        assertFalse(reachableWithFallback("192.168.1.3", { false }, { _, port -> ports.add(port); false }))
        assertEquals(listOf(80, 443, 445), ports)
    }
    @Test fun cancellationCannotTurnIntoAnOfflineResult() = runTest {
        try {
            reachableWithFallback("192.168.1.3", { throw CancellationException("Cancelled") }, { _, _ -> error("Must not continue") })
            error("Cancellation should propagate")
        } catch (_: CancellationException) { }
    }
}
