package com.bernaferrari.wifiweakspot.feature.history

import com.bernaferrari.wifiweakspot.core.i18n.appStrings
import com.bernaferrari.wifiweakspot.core.lan.identify
import com.bernaferrari.wifiweakspot.feature.lan.toDevice

import com.bernaferrari.wifiweakspot.core.database.entities.LanDeviceEntity
import com.bernaferrari.wifiweakspot.core.database.entities.SpeedTestResultEntity
import com.bernaferrari.wifiweakspot.core.database.entities.ToolResultEntity
import com.bernaferrari.wifiweakspot.core.database.entities.WifiScanResultEntity
import com.bernaferrari.wifiweakspot.core.router.LinkVerdict
import com.bernaferrari.wifiweakspot.core.router.RouterLatency
import com.bernaferrari.wifiweakspot.core.router.linkVerdict
import com.bernaferrari.wifiweakspot.core.tools.decodePortScan
import com.bernaferrari.wifiweakspot.core.util.fixed
import com.bernaferrari.wifiweakspot.core.wifi.SignalQuality
import com.bernaferrari.wifiweakspot.core.wifi.bandLabel
import com.bernaferrari.wifiweakspot.feature.linktest.LinkTestViewModel

enum class HistoryKind {
    Speed,
    Internet,
    Scan,
    Tool,
    Devices;

    /** Filter chip label, in the current language. */
    val label: String
        get() = when (this) {
            Speed -> appStrings.common.routerCheck
            Internet -> appStrings.history.kindInternetTests
            Scan -> appStrings.history.kindScans
            Tool -> appStrings.common.tools
            Devices -> appStrings.common.devices
        }
}

/** A headline number in the detail sheet, such as 412 Mbps. */
data class HistoryMetric(val value: String, val unit: String, val label: String)

/** One row inside a detail sheet: a network, a reply, an open port. */
data class HistoryLine(val title: String, val supporting: String? = null, val trailing: String? = null, val rssi: Int? = null)

/** Something that happened, written for people rather than as a database row. */
data class HistoryEntry(
    val key: String,
    val kind: HistoryKind,
    val timestamp: Long,
    val title: String,
    val summary: String,
    /** Signal at the time, which tints the row; null when it doesn't apply. */
    val rssi: Int? = null,
    val verdict: LinkVerdict? = null,
    val metrics: List<HistoryMetric> = emptyList(),
    val facts: List<Pair<String, String>> = emptyList(),
    val linesTitle: String? = null,
    val lines: List<HistoryLine> = emptyList(),
    val note: String? = null,
    /** PING, DNS or PORT_SCAN for tool entries. */
    val toolType: String? = null
)

/** One router test on the trend chart. */
data class RouterTestPoint(val timestamp: Long, val responseMs: Float?, val verdict: LinkVerdict, val rssi: Int? = null, val networkName: String? = null)

/** Rows saved less than this far apart belong to the same session. */
internal const val SESSION_GAP_MS = 2 * 60_000L

fun buildHistory(
    scans: List<WifiScanResultEntity>,
    devices: List<LanDeviceEntity>,
    tools: List<ToolResultEntity>,
    speedTests: List<SpeedTestResultEntity>
): List<HistoryEntry> = buildList {
    speedTests.forEach { add(speedEntry(it)) }
    addAll(withoutRepeats(sessions(scans) { it.timestamp }.map(::scanEntry)))
    devices.groupBy { it.networkKey }.values.forEach { networkDevices ->
        sessions(networkDevices) { it.lastSeen }.forEach { add(devicesEntry(it)) }
    }
    tools.forEach { add(toolEntry(it)) }
}.sortedByDescending { it.timestamp }

/** Scans repeat whenever Networks opens; within an hour, an unchanged neighbourhood shows once (newest). */
internal fun withoutRepeats(scans: List<HistoryEntry>): List<HistoryEntry> {
    val kept = mutableListOf<HistoryEntry>()
    scans.sortedByDescending { it.timestamp }.forEach { entry ->
        val last = kept.lastOrNull()
        val same = last != null && last.lines.map { it.title }.toSet() == entry.lines.map { it.title }.toSet() &&
            last.summary == entry.summary && last.timestamp - entry.timestamp <= REPEAT_WINDOW_MS
        if (!same) kept += entry
    }
    return kept
}

internal const val REPEAT_WINDOW_MS = 60 * 60_000L

/** Router tests oldest first, for the trend chart. */
fun routerTests(speedTests: List<SpeedTestResultEntity>): List<RouterTestPoint> {
    val tests = speedTests.filter { it.isRouterTest }
    val latest = tests.maxByOrNull { it.timestamp } ?: return emptyList()
    // Unknown legacy identities cannot safely be combined into a cross-network trend.
    val scoped = if (latest.ssid.isNullOrBlank()) listOf(latest) else tests.filter { it.ssid == latest.ssid }
    return scoped.sortedBy { it.timestamp }.map {
        RouterTestPoint(it.timestamp, it.pingMs.takeIf { ms -> ms >= 0f }, it.verdict, it.rssi, it.ssid)
    }
}

/** One plain sentence comparing the latest router test with the ones before it. */
fun trendInsight(points: List<RouterTestPoint>): String {
    val s = appStrings.history
    val latest = points.lastOrNull() ?: return ""
    val earlier = points.dropLast(1).mapNotNull { it.responseMs }.sorted()
    val now = latest.responseMs ?: return s.trendNoReply
    if (earlier.isEmpty()) return s.trendRecheckToCompare
    val usual = earlier[earlier.size / 2]
    return when {
        now < usual - 3f -> s.trendQuicker(usual.fixed(1))
        now > usual + 3f -> s.trendSlower(usual.fixed(1))
        else -> s.trendAboutSame(usual.fixed(1))
    }
}

/** Groups rows saved close together, newest session first. */
internal fun <T> sessions(rows: List<T>, time: (T) -> Long): List<List<T>> {
    val sorted = rows.sortedByDescending(time)
    val groups = mutableListOf<MutableList<T>>()
    sorted.forEach { row ->
        val current = groups.lastOrNull()
        if (current != null && time(current.last()) - time(row) <= SESSION_GAP_MS) current += row
        else groups += mutableListOf(row)
    }
    return groups
}

private val SpeedTestResultEntity.isRouterTest get() = serverName.startsWith(LinkTestViewModel.ROUTER_PREFIX) || serverName.startsWith(LinkTestViewModel.LEGACY_ROUTER_PREFIX)

internal val SpeedTestResultEntity.verdict: LinkVerdict
    get() {
        val answered = pingMs >= 0f
        val latency = RouterLatency(
            medianMs = if (answered) pingMs else Float.NaN,
            jitterMs = jitterMs.coerceAtLeast(0f),
            lossPercent = lossPercent ?: if (answered) 0f else 100f,
            samples = if (answered) 1 else 0
        )
        return linkVerdict(latency)
    }

private fun signalText(rssi: Int) = appStrings.history.signalFact(SignalQuality.of(rssi).label, rssi)

private fun speedEntry(test: SpeedTestResultEntity): HistoryEntry {
    val s = appStrings.history
    if (!test.isRouterTest) {
        return HistoryEntry(
            key = "speed-${test.id}",
            kind = HistoryKind.Internet,
            timestamp = test.timestamp,
            title = s.internetTitle(test.downloadMbps.fixed(0)),
            summary = test.serverName,
            metrics = listOfNotNull(
                HistoryMetric(test.downloadMbps.fixed(0), "Mbps", s.metricDownload),
                test.uploadMbps.takeIf { it >= 0f }?.let { HistoryMetric(it.fixed(0), "Mbps", s.metricUpload) },
                HistoryMetric(test.pingMs.fixed(0), "ms", s.metricLatency)
            ),
            facts = listOf(s.factServer to test.serverName)
        )
    }
    val router = test.serverName.removePrefix(LinkTestViewModel.ROUTER_PREFIX).removePrefix(LinkTestViewModel.LEGACY_ROUTER_PREFIX)
    val response = test.pingMs.takeIf { it >= 0f }
    val verdict = test.verdict
    return HistoryEntry(
        key = "speed-${test.id}",
        kind = HistoryKind.Speed,
        timestamp = test.timestamp,
        title = when {
            response != null -> s.routerTitle(response.fixed(0))
            else -> com.bernaferrari.wifiweakspot.core.i18n.appStrings.common.noReply
        },
        summary = listOfNotNull(test.room, verdict.headline).joinToString(" · "),
        rssi = test.rssi,
        verdict = verdict,
        metrics = listOfNotNull(
            response?.let { HistoryMetric(it.fixed(if (it < 10f) 1 else 0), "ms", com.bernaferrari.wifiweakspot.core.i18n.appStrings.common.responseTime) },
            test.jitterMs.takeIf { response != null && it >= 0f }?.let { HistoryMetric("±" + it.fixed(1), "ms", s.metricVariation) },
            test.lossPercent?.let { HistoryMetric(it.fixed(0), "%", s.metricLost) }
        ),
        facts = listOfNotNull(
            test.room?.let { s.factRoom to it },
            test.ssid?.let { com.bernaferrari.wifiweakspot.core.i18n.appStrings.common.network to it },
            com.bernaferrari.wifiweakspot.core.i18n.appStrings.domain.deviceKindRouter to router,
            test.frequencyMhz?.let { s.factBand to bandLabel(it) },
            test.rssi?.let { s.factSignal to signalText(it) },
        ),
        note = verdict.explanation
    )
}

private fun scanEntry(rows: List<WifiScanResultEntity>): HistoryEntry {
    val s = appStrings.history
    // Newest reading of each access point in the session.
    val latest = rows.sortedByDescending { it.timestamp }.distinctBy { it.bssid }
    val networks = latest.groupBy { it.ssid.ifBlank { com.bernaferrari.wifiweakspot.core.i18n.appStrings.common.hiddenNetwork } }
        .map { (ssid, aps) ->
            val best = aps.maxBy { it.rssi }
            HistoryLine(
                title = ssid,
                supporting = listOfNotNull(
                    if (aps.size > 1) s.accessPoints(aps.size) else null,
                    aps.map { bandLabel(it.frequency) }.distinct().sorted().joinToString(" + ")
                ).joinToString(" · "),
                trailing = "${best.rssi} dBm",
                rssi = best.rssi
            )
        }
        .sortedByDescending { it.rssi }
    val strongest = networks.first()
    val bands = latest.groupingBy { bandLabel(it.frequency) }.eachCount()
    return HistoryEntry(
        key = "scan-${rows.maxOf { it.timestamp }}",
        kind = HistoryKind.Scan,
        timestamp = rows.maxOf { it.timestamp },
        title = s.networksNearby(networks.size),
        summary = s.strongestNetwork(strongest.title),
        rssi = strongest.rssi,
        metrics = buildList {
            add(HistoryMetric("${networks.size}", "", com.bernaferrari.wifiweakspot.core.i18n.appStrings.common.tabNetworks))
            listOf("2.4 GHz", "5 GHz", "6 GHz").forEach { band -> bands[band]?.let { add(HistoryMetric("$it", "", s.metricOnBand(band))) } }
        },
        facts = listOf(s.factAccessPoints to "${latest.size}"),
        linesTitle = s.linesNetworksStrongestFirst,
        lines = networks,
        note = s.scanNote
    )
}

private fun devicesEntry(rows: List<LanDeviceEntity>): HistoryEntry {
    val s = appStrings.history
    // Named devices first, so the summary reads "MacBook, iPad, TV" rather than addresses.
    val named = rows.sortedWith(compareBy<LanDeviceEntity> { (it.customName ?: it.announcedName ?: it.hostname) == null }.thenBy { it.ipAddress.split('.').lastOrNull()?.toIntOrNull() ?: 0 })
    // The same friendly names as the Devices screen ("Living room TV", not "living-room-tv").
    fun LanDeviceEntity.label() = toDevice().identify(null, null).name
    return HistoryEntry(
        key = "devices-${rows.first().networkKey}-${rows.maxOf { it.lastSeen }}",
        kind = HistoryKind.Devices,
        timestamp = rows.maxOf { it.lastSeen },
        title = s.devicesFound(rows.size),
        summary = s.listWithMore(named.take(3).joinToString(", ") { it.label() }, (rows.size - 3).coerceAtLeast(0)),
        metrics = listOf(
            HistoryMetric("${rows.size}", "", com.bernaferrari.wifiweakspot.core.i18n.appStrings.common.devices),
            HistoryMetric("${rows.count { it.isOnline }}", "", s.metricReplied)
        ),
        linesTitle = com.bernaferrari.wifiweakspot.core.i18n.appStrings.common.devices,
        lines = named.map {
            HistoryLine(
                title = it.label(),
                supporting = listOfNotNull(it.ipAddress, it.vendor?.takeIf { v -> v != it.label() }).joinToString(" · "),
                trailing = if (it.isOnline) s.metricReplied else null
            )
        }
    )
}

private val ReplyTime = Regex("""time=([0-9.]+) ms""")

private fun toolEntry(result: ToolResultEntity): HistoryEntry {
    val key = "tool-${result.id}"
    return toolDetails(result, key).copy(toolType = result.toolType)
}

private fun toolDetails(result: ToolResultEntity, key: String): HistoryEntry {
    val s = appStrings.history
    return when (result.toolType) {
        "PING" -> {
            val lines = result.resultJson.lines().filter { it.isNotBlank() }
            val times = lines.mapNotNull { ReplyTime.find(it)?.groupValues?.get(1)?.toFloatOrNull() }
            val average = times.takeIf { it.isNotEmpty() }?.average()
            HistoryEntry(
                key = key,
                kind = HistoryKind.Tool,
                timestamp = result.timestamp,
                title = "Ping · ${result.target}",
                summary = if (average != null) s.pingSummary(average.fixed(0), times.size, lines.size) else com.bernaferrari.wifiweakspot.core.i18n.appStrings.common.noReply,
                metrics = listOfNotNull(
                    HistoryMetric("${times.size}/${lines.size}", "", s.metricReplies),
                    average?.let { HistoryMetric(it.fixed(1), "ms", s.metricAverage) },
                    times.minOrNull()?.let { HistoryMetric(it.fixed(1), "ms", com.bernaferrari.wifiweakspot.core.i18n.appStrings.common.fastest) }
                ),
                linesTitle = s.metricReplies,
                lines = lines.mapIndexed { index, line ->
                    val time = ReplyTime.find(line)?.groupValues?.get(1)
                    if (time != null) HistoryLine(s.replyNumber(index + 1), trailing = "$time ms")
                    else HistoryLine(s.replyNumber(index + 1), supporting = line.substringAfter(": ", line), trailing = s.metricLost)
                }
            )
        }
        "DNS" -> {
            val records = result.resultJson.lines().filter { it.isNotBlank() && !it.startsWith("Error") }
            HistoryEntry(
                key = key,
                kind = HistoryKind.Tool,
                timestamp = result.timestamp,
                title = s.lookupTitle(result.target),
                summary = if (result.isSuccess) {
                    records.firstOrNull()?.substringAfter(": ")?.let { first ->
                        s.listWithMore(first, records.size - 1)
                    } ?: s.noRecords
                } else s.lookupFailed,
                linesTitle = s.linesRecords,
                lines = records.map { HistoryLine(it.substringAfter(": "), trailing = it.substringBefore(": ")) }
            )
        }
        "PORT_SCAN" -> {
            val ports = decodePortScan(result.resultJson)
            HistoryEntry(
                key = key,
                kind = HistoryKind.Tool,
                timestamp = result.timestamp,
                title = s.openPortsTitle(result.target),
                summary = if (ports.isEmpty()) s.noCommonPortsOpen
                else s.listWithMore(ports.take(3).joinToString(", ") { "${it.first} ${it.second}" }, (ports.size - 3).coerceAtLeast(0)),
                metrics = listOf(HistoryMetric("${ports.size}", "", com.bernaferrari.wifiweakspot.core.i18n.appStrings.common.openPorts)),
                linesTitle = com.bernaferrari.wifiweakspot.core.i18n.appStrings.common.openPorts.takeIf { ports.isNotEmpty() },
                lines = ports.map { HistoryLine(com.bernaferrari.wifiweakspot.feature.tools.serviceLabel(it.second), trailing = "TCP ${it.first}") }
            )
        }
        else -> HistoryEntry(
            key = key,
            kind = HistoryKind.Tool,
            timestamp = result.timestamp,
            title = listOf(result.toolType.lowercase().replaceFirstChar { it.uppercase() }, result.target).joinToString(" · "),
            summary = if (result.isSuccess) s.completed else s.failed,
            lines = result.resultJson.lines().filter { it.isNotBlank() }.map { HistoryLine(it) }
        )
    }
}
