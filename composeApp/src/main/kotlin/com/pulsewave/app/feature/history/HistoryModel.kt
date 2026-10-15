package com.pulsewave.app.feature.history

import com.pulsewave.app.core.database.entities.LanDeviceEntity
import com.pulsewave.app.core.database.entities.SpeedTestResultEntity
import com.pulsewave.app.core.database.entities.ToolResultEntity
import com.pulsewave.app.core.database.entities.WifiScanResultEntity
import com.pulsewave.app.core.router.LinkVerdict
import com.pulsewave.app.core.router.RouterLatency
import com.pulsewave.app.core.router.linkVerdict
import com.pulsewave.app.core.tools.decodePortScan
import com.pulsewave.app.core.util.fixed
import com.pulsewave.app.core.wifi.SignalQuality
import com.pulsewave.app.core.wifi.bandLabel
import com.pulsewave.app.feature.linktest.LinkTestViewModel

enum class HistoryKind(val label: String) {
    Speed("Connection checks"),
    Scan("Scans"),
    Tool("Tools"),
    Devices("Devices")
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
    sessions(devices) { it.lastSeen }.forEach { add(devicesEntry(it)) }
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
    val latest = points.lastOrNull() ?: return ""
    val earlier = points.dropLast(1).mapNotNull { it.responseMs }.sorted()
    val now = latest.responseMs ?: return "No reply from the router. Some routers ignore these checks."
    if (earlier.isEmpty()) return "Recheck after a change to compare."
    val usual = earlier[earlier.size / 2]
    return when {
        now < usual - 3f -> "Quicker than usual (${usual.fixed(1)} ms)"
        now > usual + 3f -> "Slower than usual (${usual.fixed(1)} ms)"
        else -> "About the same as usual (${usual.fixed(1)} ms)"
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

private fun signalText(rssi: Int) = "${SignalQuality.of(rssi).label} signal · $rssi dBm"

private fun speedEntry(test: SpeedTestResultEntity): HistoryEntry {
    if (!test.isRouterTest) {
        return HistoryEntry(
            key = "speed-${test.id}",
            kind = HistoryKind.Speed,
            timestamp = test.timestamp,
            title = "${test.downloadMbps.fixed(0)} Mbps download",
            summary = test.serverName,
            metrics = listOfNotNull(
                HistoryMetric(test.downloadMbps.fixed(0), "Mbps", "Download"),
                test.uploadMbps.takeIf { it >= 0f }?.let { HistoryMetric(it.fixed(0), "Mbps", "Upload") },
                HistoryMetric(test.pingMs.fixed(0), "ms", "Latency")
            ),
            facts = listOf("Server" to test.serverName)
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
            response != null -> "${response.fixed(0)} ms to router"
            else -> "Router didn't answer"
        },
        summary = listOfNotNull(test.room, verdict.headline.removeSuffix(" here")).joinToString(" · "),
        rssi = test.rssi,
        verdict = verdict,
        metrics = listOfNotNull(
            response?.let { HistoryMetric(it.fixed(if (it < 10f) 1 else 0), "ms", "Response") },
            test.jitterMs.takeIf { response != null && it >= 0f }?.let { HistoryMetric(it.fixed(1), "ms", "Jitter") },
            test.rssi?.let { HistoryMetric("$it", "dBm", "Signal") }
        ),
        facts = listOfNotNull(
            test.room?.let { "Room" to it },
            test.ssid?.let { "Network" to it },
            "Router" to router,
            test.frequencyMhz?.let { "Band" to bandLabel(it) },
            test.rssi?.let { "Signal" to signalText(it) },
            test.lossPercent?.let { "Lost replies" to "${it.fixed(0)}%" }
        ),
        note = verdict.explanation + if (test.uploadMbps >= 0f) " Earlier sender-rate estimates are hidden because delivery to the router was not verified." else ""
    )
}

private fun scanEntry(rows: List<WifiScanResultEntity>): HistoryEntry {
    // Newest reading of each access point in the session.
    val latest = rows.sortedByDescending { it.timestamp }.distinctBy { it.bssid }
    val networks = latest.groupBy { it.ssid.ifBlank { "Hidden network" } }
        .map { (ssid, aps) ->
            val best = aps.maxBy { it.rssi }
            HistoryLine(
                title = ssid,
                supporting = listOfNotNull(
                    if (aps.size > 1) "${aps.size} access points" else null,
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
        title = if (networks.size == 1) "1 network nearby" else "${networks.size} networks nearby",
        summary = "Strongest: ${strongest.title}",
        rssi = strongest.rssi,
        metrics = buildList {
            add(HistoryMetric("${networks.size}", "", "Networks"))
            listOf("2.4 GHz", "5 GHz", "6 GHz").forEach { band -> bands[band]?.let { add(HistoryMetric("$it", "", "on $band")) } }
        },
        facts = listOf("Access points" to "${latest.size}"),
        linesTitle = "Networks, strongest first",
        lines = networks,
        note = "Nearby networks on the same channel as yours compete for airtime."
    )
}

private fun devicesEntry(rows: List<LanDeviceEntity>): HistoryEntry {
    // Named devices first, so the summary reads "MacBook, iPad, TV" rather than addresses.
    val named = rows.sortedWith(compareBy<LanDeviceEntity> { (it.customName ?: it.announcedName ?: it.hostname) == null }.thenBy { it.ipAddress.split('.').lastOrNull()?.toIntOrNull() ?: 0 })
    fun LanDeviceEntity.label() = customName ?: announcedName ?: hostname ?: vendor ?: ipAddress
    return HistoryEntry(
        key = "devices-${rows.maxOf { it.lastSeen }}",
        kind = HistoryKind.Devices,
        timestamp = rows.maxOf { it.lastSeen },
        title = if (rows.size == 1) "1 device found" else "${rows.size} devices found",
        summary = named.take(3).joinToString(", ") { it.label() } + if (rows.size > 3) " and ${rows.size - 3} more" else "",
        metrics = listOf(
            HistoryMetric("${rows.size}", "", "Devices"),
            HistoryMetric("${rows.count { it.isOnline }}", "", "Replied")
        ),
        linesTitle = "Devices",
        lines = named.map {
            HistoryLine(
                title = it.label(),
                supporting = listOfNotNull(it.ipAddress, it.vendor?.takeIf { v -> v != it.label() }).joinToString(" · "),
                trailing = if (it.isOnline) "Replied" else null
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
    return when (result.toolType) {
        "PING" -> {
            val lines = result.resultJson.lines().filter { it.isNotBlank() }
            val times = lines.mapNotNull { ReplyTime.find(it)?.groupValues?.get(1)?.toFloatOrNull() }
            val average = times.takeIf { it.isNotEmpty() }?.average()
            HistoryEntry(
                key = key,
                kind = HistoryKind.Tool,
                timestamp = result.timestamp,
                title = "Ping ${result.target}",
                summary = if (average != null) "${average.fixed(0)} ms · ${times.size} of ${lines.size} replied" else "No replies",
                metrics = listOfNotNull(
                    HistoryMetric("${times.size}/${lines.size}", "", "Replies"),
                    average?.let { HistoryMetric(it.fixed(1), "ms", "Average") },
                    times.minOrNull()?.let { HistoryMetric(it.fixed(1), "ms", "Fastest") }
                ),
                linesTitle = "Replies",
                lines = lines.mapIndexed { index, line ->
                    val time = ReplyTime.find(line)?.groupValues?.get(1)
                    if (time != null) HistoryLine("Reply ${index + 1}", trailing = "$time ms")
                    else HistoryLine("Reply ${index + 1}", supporting = line.substringAfter(": ", line), trailing = "Lost")
                }
            )
        }
        "DNS" -> {
            val records = result.resultJson.lines().filter { it.isNotBlank() && !it.startsWith("Error") }
            HistoryEntry(
                key = key,
                kind = HistoryKind.Tool,
                timestamp = result.timestamp,
                title = "Look up ${result.target}",
                summary = if (result.isSuccess) {
                    records.firstOrNull()?.substringAfter(": ")?.let { first ->
                        if (records.size > 1) "$first and ${records.size - 1} more" else first
                    } ?: "No records"
                } else result.resultJson.removePrefix("Error: ").ifBlank { "Lookup failed" },
                linesTitle = "Records",
                lines = records.map { HistoryLine(it.substringAfter(": "), trailing = it.substringBefore(": ")) }
            )
        }
        "PORT_SCAN" -> {
            val ports = decodePortScan(result.resultJson)
            HistoryEntry(
                key = key,
                kind = HistoryKind.Tool,
                timestamp = result.timestamp,
                title = "Open ports on ${result.target}",
                summary = if (ports.isEmpty()) "None of the common ports were open"
                else ports.take(3).joinToString(", ") { "${it.first} ${it.second}" } + if (ports.size > 3) " and ${ports.size - 3} more" else "",
                metrics = listOf(HistoryMetric("${ports.size}", "", "Open ports")),
                linesTitle = "Open ports".takeIf { ports.isNotEmpty() },
                lines = ports.map { HistoryLine(it.second, trailing = "TCP ${it.first}") }
            )
        }
        else -> HistoryEntry(
            key = key,
            kind = HistoryKind.Tool,
            timestamp = result.timestamp,
            title = "${result.toolType.lowercase().replaceFirstChar { it.uppercase() }} ${result.target}",
            summary = if (result.isSuccess) "Completed" else "Failed",
            lines = result.resultJson.lines().filter { it.isNotBlank() }.map { HistoryLine(it) }
        )
    }
}
