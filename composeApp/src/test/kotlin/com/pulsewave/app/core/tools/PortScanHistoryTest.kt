package com.pulsewave.app.core.tools

import org.junit.Assert.assertEquals
import org.junit.Test

class PortScanHistoryTest {
    @Test fun roundTripsMeasuredPortsAndEmptySuccessfulScan() {
        val ports = listOf(22 to "SSH", 8080 to "HTTP-Proxy")
        assertEquals(ports, decodePortScan(encodePortScan(ports)))
        assertEquals(emptyList<Pair<Int, String>>(), decodePortScan(encodePortScan(emptyList())))
    }
    @Test fun rejectsMalformedOrOutOfRangeHistory() {
        assertEquals(listOf(443 to "HTTPS"), decodePortScan("garbage,0:none,65536:none,443:HTTPS,443:duplicate"))
    }
}
