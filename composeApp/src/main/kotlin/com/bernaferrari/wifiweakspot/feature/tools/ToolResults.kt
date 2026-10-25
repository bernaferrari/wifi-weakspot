@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.wifiweakspot.feature.tools

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bernaferrari.wifiweakspot.core.designsystem.LocalReduceMotion
import com.bernaferrari.wifiweakspot.core.designsystem.component.segmentedShape
import com.bernaferrari.wifiweakspot.core.designsystem.signalColors
import com.bernaferrari.wifiweakspot.core.i18n.appStrings
import com.bernaferrari.wifiweakspot.core.i18n.strings
import com.bernaferrari.wifiweakspot.core.platform.PlatformActions
import com.bernaferrari.wifiweakspot.core.util.fixed
import com.bernaferrari.wifiweakspot.core.wifi.SignalQuality
import org.koin.compose.koinInject

private val ReplyTime = Regex("""time=([0-9.]+) ms""")

/** Private addresses are on your own network; everything else is out on the internet. */
internal fun isLocalAddress(target: String): Boolean {
    val parts = target.split('.').mapNotNull { it.toIntOrNull() }
    if (parts.size != 4) return target.endsWith(".local") || target.endsWith(".lan")
    return parts[0] == 10 || (parts[0] == 192 && parts[1] == 168) || (parts[0] == 172 && parts[1] in 16..31)
}

/** Ping verdicts, judged more strictly for devices at home than for servers across the internet. */
internal data class PingSummary(val replies: List<Float?>, val local: Boolean) {
    val answered = replies.filterNotNull()
    val average: Float? = answered.takeIf { it.isNotEmpty() }?.average()?.toFloat()
    val lost = replies.count { it == null }
    val quality: SignalQuality = when {
        average == null -> SignalQuality.DeadZone
        lost * 4 >= replies.size -> SignalQuality.Weak
        average < (if (local) 10f else 30f) -> SignalQuality.Excellent
        average < (if (local) 30f else 80f) -> SignalQuality.Good
        average < (if (local) 80f else 200f) -> SignalQuality.Fair
        else -> SignalQuality.Weak
    }
    /** Reply variation can come from any part of the path, or the host itself. */
    val steady: Boolean = answered.size < 2 || (answered.max() - answered.min()) <= maxOf(5f, (average ?: 0f) * 0.8f)
    val headline: String get() = when (quality) {
        SignalQuality.DeadZone -> appStrings.tools.pingDidntAnswer
        SignalQuality.Excellent, SignalQuality.Good -> if (steady) appStrings.tools.pingResponsive else appStrings.tools.pingResponsiveButUneven
        SignalQuality.Fair -> appStrings.tools.pingSlowToAnswer
        SignalQuality.Weak -> if (lost > 0) appStrings.tools.pingDroppingReplies else appStrings.tools.pingSlowToAnswer
    }
    val explanation: String get() = when {
        average == null -> appStrings.tools.pingNoAnswerLocal
        lost > 0 -> appStrings.tools.pingRepliesLost(lost, replies.size)
        !steady -> appStrings.tools.pingUneven
        local -> appStrings.tools.pingLocalExplanation
        else -> appStrings.tools.pingInternetExplanation
    }

    companion object {
        fun of(lines: List<String>, target: String) = PingSummary(
            replies = lines.filter { it.isNotBlank() }.map { ReplyTime.find(it)?.groupValues?.get(1)?.toFloatOrNull() },
            local = isLocalAddress(target)
        )
    }
}

/** What common ports usually mean, in words people know. */
internal fun portMeaning(port: Int, service: String): String = when (port) {
    80, 8080, 8000 -> appStrings.tools.portWebPage
    443, 8443 -> appStrings.tools.portSecureWebPage
    22 -> appStrings.tools.portRemoteLoginSsh
    23 -> appStrings.tools.portOldRemoteLoginTelnet
    21 -> appStrings.tools.portFileTransferFtp
    53 -> appStrings.tools.portAddressLookupsDns
    139, 445 -> appStrings.tools.portFileSharing
    548 -> appStrings.tools.portMacFileSharing
    631 -> appStrings.tools.portPrinting
    9100 -> appStrings.tools.portPrinting
    3389 -> appStrings.tools.portRemoteDesktop
    5900 -> appStrings.tools.portScreenSharing
    7000 -> "AirPlay"
    8008, 8009 -> "Chromecast"
    1900 -> appStrings.tools.portDeviceDiscoveryUpnp
    5000, 5001 -> appStrings.tools.portStorageOrAdminPage
    3306, 5432 -> appStrings.tools.portDatabase
    else -> serviceLabel(service).ifBlank { appStrings.tools.portService }
}

/** The scanner's service name, with its stored "unknown" marker shown in the user's language. */
internal fun serviceLabel(service: String): String =
    if (service == UNKNOWN_SERVICE) appStrings.common.unknown else service

/** Stored in port scan results when no service name is known; never shown as is. */
internal const val UNKNOWN_SERVICE = "unknown"

@Composable
private fun ResultHero(value: String, unit: String, label: String, quality: SignalQuality, headline: String, explanation: String) {
    val colors = signalColors(quality)
    Column(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = MaterialTheme.typography.displayMedium.copy(fontFeatureSettings = "tnum"), fontWeight = FontWeight.SemiBold)
            if (unit.isNotEmpty()) {
                Text(unit, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 6.dp, bottom = 8.dp))
            }
        }
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Surface(shape = RoundedCornerShape(20.dp), color = colors.container, contentColor = colors.onContainer, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(headline, style = MaterialTheme.typography.titleMedium)
                Text(explanation, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ResultActions(onRetry: () -> Unit, onDone: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledTonalButton(onClick = onDone, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f).heightIn(min = 52.dp)) { Text(strings.common.done) }
        Button(onClick = onRetry, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f).heightIn(min = 52.dp)) { Text(strings.common.checkAgain) }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 8.dp))
}

@Composable
private fun ResultRow(title: String, supporting: String?, trailing: String?, shape: androidx.compose.ui.graphics.Shape, modifier: Modifier = Modifier) {
    Surface(shape = shape, color = MaterialTheme.colorScheme.surfaceContainer, modifier = modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                supporting?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            trailing?.let { Text(it, style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

private fun <T> LazyListScope.rows(items: List<T>, content: @Composable (T, androidx.compose.ui.graphics.Shape, Modifier) -> Unit) {
    itemsIndexed(items) { index, item ->
        content(item, segmentedShape(index, items.size, outer = 20.dp, inner = 4.dp), Modifier.padding(top = if (index == 0) 0.dp else 2.dp))
    }
}

@Composable
private fun ResultList(onRetry: () -> Unit, onDone: () -> Unit, modifier: Modifier, content: LazyListScope.() -> Unit) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        content()
        item { ResultActions(onRetry, onDone) }
    }
}

@Composable
fun PingResultContent(target: String, lines: List<String>, onRetry: () -> Unit, onDone: () -> Unit, modifier: Modifier = Modifier) {
    val summary = remember(lines, target) { PingSummary.of(lines, target) }
    ResultList(onRetry, onDone, modifier) {
        item {
            ResultHero(
                value = summary.average?.let { it.fixed(if (it < 10f) 1 else 0) } ?: "—",
                unit = if (summary.average != null) "ms" else "",
                label = strings.tools.pingRepliedLabel(target, summary.answered.size, summary.replies.size),
                quality = summary.quality,
                headline = summary.headline,
                explanation = summary.explanation
            )
        }
        if (summary.replies.size > 1) {
            item { SectionTitle(strings.history.metricReplies) }
            item { ReplyBars(summary) }
        }
        if (summary.answered.size > 1) {
            item {
                Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniStat(strings.common.fastest, "${summary.answered.min().fixed(1)} ms", Modifier.weight(1f))
                    MiniStat(strings.tools.slowest, "${summary.answered.max().fixed(1)} ms", Modifier.weight(1f))
                    MiniStat(strings.history.metricLost, "${summary.lost}", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = modifier) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"), fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** One bar per reply, so a single slow or lost reply stands out from a steady run. */
@Composable
private fun ReplyBars(summary: PingSummary) {
    val colors = signalColors(summary.quality)
    val lostColor = MaterialTheme.colorScheme.error
    val reduceMotion = LocalReduceMotion.current
    val grow = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val spec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
    LaunchedEffect(Unit) { grow.animateTo(1f, spec) }
    val peak = summary.answered.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    val replyLost = strings.common.noReply
    val description = summary.replies.joinToString { it?.let { ms -> "${ms.fixed(1)} ms" } ?: replyLost }
    val chartDescription = strings.tools.repliesContentDescription(description)
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
        Canvas(Modifier.fillMaxWidth().height(96.dp).padding(16.dp).semantics { contentDescription = chartDescription }) {
            val slots = summary.replies.size.coerceAtLeast(4)
            val slot = size.width / slots
            val bar = minOf(slot * 0.6f, 28.dp.toPx())
            summary.replies.forEachIndexed { index, ms ->
                val x = index * slot + (slot - bar) / 2
                if (ms == null) {
                    drawRoundRect(lostColor, Offset(x, size.height - 4.dp.toPx()), Size(bar, 4.dp.toPx()), CornerRadius(2.dp.toPx()))
                } else {
                    val h = (size.height * (ms / peak).coerceIn(0.08f, 1f) * grow.value).coerceAtLeast(4.dp.toPx())
                    drawRoundRect(colors.accent, Offset(x, size.height - h), Size(bar, h), CornerRadius(bar / 2))
                }
            }
        }
    }
}

@Composable
fun DnsResultContent(query: String, lines: List<String>, onRetry: () -> Unit, onDone: () -> Unit, modifier: Modifier = Modifier) {
    val records = lines.filter { it.isNotBlank() && !it.startsWith("Error") }
    ResultList(onRetry, onDone, modifier) {
        item {
            ResultHero(
                value = "${records.size}",
                unit = strings.tools.dnsAddressUnit(records.size),
                label = strings.tools.dnsForQuery(query),
                quality = if (records.isEmpty()) SignalQuality.DeadZone else SignalQuality.Excellent,
                headline = if (records.isEmpty()) strings.tools.dnsNotFound else strings.tools.dnsFound,
                explanation = if (records.isEmpty()) strings.tools.dnsNotFoundExplanation
                else strings.tools.dnsFoundExplanation(query)
            )
        }
        if (records.isNotEmpty()) {
            item { SectionTitle(strings.tools.dnsAddressesTitle) }
            rows(records) { line, shape, mod -> ResultRow(line.substringAfter(": "), null, line.substringBefore(": "), shape, mod) }
        }
    }
}

@Composable
fun PortsResultContent(host: String, ports: List<Pair<Int, String>>, onRetry: () -> Unit, onDone: () -> Unit, modifier: Modifier = Modifier) {
    val platform: PlatformActions = koinInject()
    val web = ports.firstOrNull { it.first in setOf(80, 8080, 8000) }?.let { "http://$host${if (it.first == 80) "" else ":${it.first}"}" }
        ?: ports.firstOrNull { it.first in setOf(443, 8443) }?.let { "https://$host${if (it.first == 443) "" else ":${it.first}"}" }
    ResultList(onRetry, onDone, modifier) {
        item {
            ResultHero(
                value = "${ports.size}",
                unit = strings.tools.portsOpenUnit(ports.size),
                label = strings.tools.portsCommonServicesOn(host),
                quality = if (ports.isEmpty()) SignalQuality.Good else SignalQuality.Excellent,
                headline = if (ports.isEmpty()) strings.tools.portsNothingOpen else strings.tools.portsOffersServices(ports.size),
                explanation = if (ports.isEmpty()) strings.tools.portsNoneAnswered
                else strings.tools.portsOffersExplanation
            )
        }
        if (web != null) {
            item {
                FilledTonalButton(onClick = { platform.openUrl(web) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text(strings.tools.openItsWebPage)
                }
            }
        }
        if (ports.isNotEmpty()) {
            item { SectionTitle(strings.common.openPorts) }
            rows(ports) { (port, service), shape, mod -> ResultRow(portMeaning(port, service), serviceLabel(service).takeIf { it.isNotBlank() && portMeaning(port, service) != it }, "TCP $port", shape, mod) }
        }
    }
}
