package com.bernaferrari.wifiweakspot.core.tools

import com.bernaferrari.wifiweakspot.core.model.tools.DnsRecord
import com.bernaferrari.wifiweakspot.core.model.tools.DnsRecordType
import com.bernaferrari.wifiweakspot.core.model.tools.DnsResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

class AndroidDnsClient(
    private val resolver: (String) -> Array<InetAddress> = { InetAddress.getAllByName(it) }
) : DnsClient {
    override suspend fun lookup(
        query: String,
        recordType: DnsRecordType,
        server: String?
    ): DnsResult = withContext(Dispatchers.IO) {
        val start = System.nanoTime()
        if (server != null || recordType !in setOf(DnsRecordType.A, DnsRecordType.AAAA)) {
            return@withContext DnsResult(query, "System resolver", emptyList(), 0L, false,
                "Only A and AAAA records through the system resolver are supported.")
        }
        try {
            // Java InetAddress only supports A/AAAA records easily
            // For full DNS support (MX, TXT), we'd need a library like dnsjava.
            // For now, we wrap InetAddress for basic A/AAAA lookup.
            val addresses = resolver(query)

            currentCoroutineContext().ensureActive()
            val records = addresses.filter { (it.address.size == 4) == (recordType == DnsRecordType.A) }.map { addr ->
                DnsRecord(
                    type = if (addr.address.size == 4) DnsRecordType.A else DnsRecordType.AAAA,
                    name = query,
                    value = addr.hostAddress ?: "",
                    ttl = 0 // InetAddress doesn't provide TTL
                )
            }

            DnsResult(
                query = query,
                server = "System resolver",
                records = records,
                queryTimeMs = (System.nanoTime() - start) / 1_000_000,
                isSuccess = records.isNotEmpty(),
                error = if (records.isEmpty()) "No ${recordType.name} addresses returned" else null
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            DnsResult(
                query = query,
                server = "System resolver",
                records = emptyList(),
                queryTimeMs = (System.nanoTime() - start) / 1_000_000,
                isSuccess = false,
                error = "Address lookup failed. Check the host name and your connection."
            )
        }
    }
}
