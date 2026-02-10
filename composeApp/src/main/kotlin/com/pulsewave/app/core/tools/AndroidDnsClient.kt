package com.pulsewave.app.core.tools

import com.pulsewave.app.core.model.tools.DnsRecord
import com.pulsewave.app.core.model.tools.DnsRecordType
import com.pulsewave.app.core.model.tools.DnsResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

class AndroidDnsClient : DnsClient {
    override suspend fun lookup(
        query: String,
        recordType: DnsRecordType,
        server: String?
    ): DnsResult = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            // Java InetAddress only supports A/AAAA records easily
            // For full DNS support (MX, TXT), we'd need a library like dnsjava.
            // For now, we wrap InetAddress for basic A/AAAA lookup.
            val addresses = InetAddress.getAllByName(query)
            
            val records = addresses.map { addr ->
                DnsRecord(
                    type = if (addr.address.size == 4) DnsRecordType.A else DnsRecordType.AAAA,
                    name = query,
                    value = addr.hostAddress ?: "",
                    ttl = 0 // InetAddress doesn't provide TTL
                )
            }

            DnsResult(
                query = query,
                server = "System Default",
                records = records,
                queryTimeMs = System.currentTimeMillis() - start,
                isSuccess = true,
                error = null
            )
        } catch (e: Exception) {
            DnsResult(
                query = query,
                server = "System Default",
                records = emptyList(),
                queryTimeMs = System.currentTimeMillis() - start,
                isSuccess = false,
                error = e.message
            )
        }
    }
}
