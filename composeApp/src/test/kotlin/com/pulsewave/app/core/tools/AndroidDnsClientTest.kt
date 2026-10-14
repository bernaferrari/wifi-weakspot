package com.pulsewave.app.core.tools

import com.pulsewave.app.core.model.tools.DnsRecordType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.net.InetAddress

class AndroidDnsClientTest {
    @Test fun filtersAAndAaaaWithoutNetworkCalls() = runTest {
        val ipv4 = InetAddress.getByAddress(byteArrayOf(127, 0, 0, 1))
        val ipv6 = InetAddress.getByAddress(ByteArray(16).also { it[15] = 1 })
        val client = AndroidDnsClient { arrayOf(ipv4, ipv6) }
        assertEquals(listOf(DnsRecordType.A), client.lookup("example.test", DnsRecordType.A, null).records.map { it.type })
        assertEquals(listOf(DnsRecordType.AAAA), client.lookup("example.test", DnsRecordType.AAAA, null).records.map { it.type })
    }
    @Test fun rejectsUnsupportedOptionsBeforeResolving() = runTest {
        val client = AndroidDnsClient { error("Must not resolve unsupported options") }
        assertFalse(client.lookup("example.test", DnsRecordType.MX, null).isSuccess)
        assertFalse(client.lookup("example.test", DnsRecordType.A, "1.1.1.1").isSuccess)
    }
}
