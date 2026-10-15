@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.pulsewave.app.feature.history

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.NetworkPing
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.WifiFind
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.graphics.shapes.RoundedPolygon
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulsewave.app.core.database.dao.PulseWaveDao
import com.pulsewave.app.core.designsystem.LocalReduceMotion
import com.pulsewave.app.core.designsystem.component.EmptyStateWithIllustration
import com.pulsewave.app.core.designsystem.component.HistoryIllustration
import com.pulsewave.app.core.designsystem.component.ShapeIcon
import com.pulsewave.app.core.designsystem.component.segmentedShape
import com.pulsewave.app.core.designsystem.component.polygon
import com.pulsewave.app.core.designsystem.signalColors
import com.pulsewave.app.core.router.LinkVerdict
import com.pulsewave.app.core.util.fixed
import com.pulsewave.app.core.wifi.SignalQuality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import org.koin.compose.koinInject

private class HistoryData(val entries: List<HistoryEntry>, val routerTests: List<RouterTestPoint>)

/** Router test verdicts borrow the signal palette and shapes: more lobes, better Wi-Fi. */
internal val LinkVerdict.quality: SignalQuality
    get() = when (this) {
        LinkVerdict.Great -> SignalQuality.Excellent
        LinkVerdict.Good -> SignalQuality.Good
        LinkVerdict.Slow -> SignalQuality.Weak
        LinkVerdict.Unreliable -> SignalQuality.DeadZone
    }

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    dao: PulseWaveDao = koinInject(),
    onBack: (() -> Unit)? = null
) {
    val data by remember(dao) {
        combine(dao.getRecentWifiScans(), dao.getAllLanDevices(), dao.getToolHistory(), dao.getSpeedTestHistory()) { scans, devices, tools, speed ->
            HistoryData(buildHistory(scans, devices, tools, speed), routerTests(speed))
        }.flowOn(Dispatchers.Default)
    }.collectAsStateWithLifecycle(initialValue = null)

    var filterName by rememberSaveable { mutableStateOf<String?>(null) }
    val filter = filterName?.let { name -> HistoryKind.entries.firstOrNull { it.name == name } }
    var selectedKey by rememberSaveable { mutableStateOf<String?>(null) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("History") },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { padding ->
        val loaded = data ?: return@Scaffold
        if (loaded.entries.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                EmptyStateWithIllustration(
                    illustration = { HistoryIllustration() },
                    title = "Nothing saved yet",
                    subtitle = "Wi-Fi speed tests, network scans and tool results show up here, so you can see how your Wi-Fi changes over time."
                )
            }
            return@Scaffold
        }
        val kinds = HistoryKind.entries.filter { kind -> loaded.entries.any { it.kind == kind } }
        val visible = if (filter == null) loaded.entries else loaded.entries.filter { it.kind == filter }
        val days = visible.groupBy { dayLabel(it.timestamp) }
        val showTrend = loaded.routerTests.isNotEmpty() && (filter == null || filter == HistoryKind.Speed)

        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 32.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            if (showTrend) {
                item(key = "trend") {
                    RouterTrendCard(
                        points = loaded.routerTests,
                        onClick = { selectedKey = loaded.entries.firstOrNull { it.kind == HistoryKind.Speed && it.verdict != null }?.key },
                        modifier = Modifier.animateItem().padding(bottom = 16.dp)
                    )
                }
            }
            if (kinds.size > 1) {
                item(key = "filters") {
                    KindFilters(kinds, filter, onSelect = { filterName = it?.name }, modifier = Modifier.animateItem().padding(bottom = 8.dp))
                }
            }
            days.forEach { (day, entries) ->
                item(key = "day-$day") {
                    Text(
                        day,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.animateItem().padding(start = 8.dp, top = 16.dp, bottom = 8.dp)
                    )
                }
                itemsIndexed(entries, key = { _, entry -> entry.key }) { index, entry ->
                    HistoryRow(
                        entry = entry,
                        shape = segmentedShape(index, entries.size),
                        onClick = { selectedKey = entry.key },
                        modifier = Modifier.animateItem().padding(top = if (index == 0) 0.dp else 2.dp)
                    )
                }
            }
        }

        loaded.entries.firstOrNull { it.key == selectedKey }?.let { entry ->
            HistoryDetailSheet(entry, onDismiss = { selectedKey = null })
        }
    }
}

@Composable
private fun KindFilters(kinds: List<HistoryKind>, selected: HistoryKind?, onSelect: (HistoryKind?) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        (listOf<HistoryKind?>(null) + kinds).forEach { kind ->
            val isSelected = kind == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(kind) },
                label = { Text(kind?.label ?: "All") },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                } else null
            )
        }
    }
}

private class EntryLook(val icon: ImageVector, val shape: RoundedPolygon, val container: Color, val content: Color)

@Composable
private fun HistoryEntry.look(): EntryLook {
    val scheme = MaterialTheme.colorScheme
    return when (kind) {
        HistoryKind.Speed -> {
            val quality = verdict?.quality
            val colors = quality?.let { signalColors(it) }
            EntryLook(Icons.Rounded.Speed, quality?.polygon() ?: MaterialShapes.Cookie9Sided, colors?.container ?: scheme.primaryContainer, colors?.onContainer ?: scheme.onPrimaryContainer)
        }
        HistoryKind.Scan -> EntryLook(Icons.Rounded.WifiFind, MaterialShapes.Sunny, scheme.primaryContainer, scheme.onPrimaryContainer)
        HistoryKind.Devices -> EntryLook(Icons.Rounded.Devices, MaterialShapes.Clover4Leaf, scheme.secondaryContainer, scheme.onSecondaryContainer)
        HistoryKind.Tool -> EntryLook(
            when (toolType) {
                "PING" -> Icons.Rounded.NetworkPing
                "DNS" -> Icons.Rounded.Dns
                "PORT_SCAN" -> Icons.Rounded.Shield
                else -> Icons.Rounded.Terminal
            },
            MaterialShapes.Square, scheme.tertiaryContainer, scheme.onTertiaryContainer
        )
    }
}

@Composable
private fun EntryIcon(entry: HistoryEntry, size: Dp) {
    val look = entry.look()
    ShapeIcon(look.icon, look.shape, look.container, look.content, size = size, iconSize = size * 0.48f)
}

@Composable
private fun HistoryRow(entry: HistoryEntry, shape: Shape, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(start = 16.dp, end = 20.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EntryIcon(entry, 44.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(entry.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    entry.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                timeOfDay(entry.timestamp),
                style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun VerdictBadge(verdict: LinkVerdict) {
    val colors = signalColors(verdict.quality)
    Surface(shape = RoundedCornerShape(50), color = colors.container, contentColor = colors.onContainer) {
        Text(
            verdict.headline.removeSuffix(" here"),
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun RouterTrendCard(points: List<RouterTestPoint>, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val latest = points.last()
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Speed to your router", style = MaterialTheme.typography.titleMedium)
                    Text(
                        momentLabel(latest.timestamp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                VerdictBadge(latest.verdict)
            }
            Row(verticalAlignment = Alignment.Bottom) {
                val (value, unit) = when {
                    latest.uploadMbps != null -> latest.uploadMbps.fixed(0) to "Mbps"
                    latest.responseMs != null -> latest.responseMs.fixed(0) to "ms"
                    else -> "No reply" to ""
                }
                Text(
                    value,
                    style = MaterialTheme.typography.displayMedium.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.SemiBold
                )
                if (unit.isNotEmpty()) {
                    Text(
                        unit,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
                    )
                }
            }
            if (points.size >= 2) {
                RouterTrendBars(points.takeLast(MaxTrendBars), Modifier.fillMaxWidth().height(88.dp))
            }
            Text(trendInsight(points), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

internal const val MaxTrendBars = 16

/** One bar per router test, oldest on the left; the latest one is fully opaque. */
@Composable
fun RouterTrendBars(points: List<RouterTestPoint>, modifier: Modifier = Modifier) {
    val colors = points.map { signalColors(it.verdict.quality).accent }
    val missing = MaterialTheme.colorScheme.outlineVariant
    val reduceMotion = LocalReduceMotion.current
    val grow = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val growSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
    LaunchedEffect(Unit) { grow.animateTo(1f, growSpec) }
    val peak = points.mapNotNull { it.uploadMbps }.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    val description = "Last ${points.size} router tests: " + points.joinToString { it.uploadMbps?.let { mbps -> "${mbps.fixed(0)} Mbps" } ?: "no reply" }
    Canvas(modifier.semantics { contentDescription = description }) {
        // At least a week's worth of slots, so two tests don't become two giant slabs.
        val slots = points.size.coerceAtLeast(7)
        val slot = size.width / slots
        val barWidth = min(36.dp, (slot * 0.6f).toDp()).toPx()
        val radius = CornerRadius(barWidth / 2, barWidth / 2)
        // Oldest on the left, filling toward the right as tests accumulate.
        val first = 0
        points.forEachIndexed { index, point ->
            val fraction = point.uploadMbps?.let { (it / peak).coerceIn(0.06f, 1f) } ?: 0.06f
            val height = (size.height * fraction * grow.value).coerceAtLeast(barWidth.coerceAtMost(size.height * 0.06f))
            val latest = index == points.lastIndex
            drawRoundRect(
                color = if (point.uploadMbps == null) missing else colors[index].copy(alpha = if (latest) 1f else 0.45f),
                topLeft = Offset((first + index) * slot + (slot - barWidth) / 2, size.height - height),
                size = Size(barWidth, height),
                cornerRadius = radius
            )
        }
    }
}

@Composable
private fun HistoryDetailSheet(entry: HistoryEntry, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item(key = "header") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    EntryIcon(entry, 56.dp)
                    Column(Modifier.weight(1f)) {
                        Text(entry.title, style = MaterialTheme.typography.headlineSmall)
                        Text(momentLabel(entry.timestamp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (entry.metrics.isNotEmpty()) {
                item(key = "metrics") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                        entry.metrics.forEach { MetricTile(it, Modifier.weight(1f)) }
                    }
                }
            }
            entry.note?.let { note ->
                item(key = "note") {
                    val colors = entry.verdict?.let { signalColors(it.quality) }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = colors?.container ?: MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = colors?.onContainer ?: MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            entry.verdict?.let { Text(it.headline, style = MaterialTheme.typography.titleMedium) }
                            Text(note, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            if (entry.facts.isNotEmpty()) {
                item(key = "facts") {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                            entry.facts.forEachIndexed { index, (label, value) ->
                                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        value,
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (entry.lines.isNotEmpty()) {
                entry.linesTitle?.let { title ->
                    item(key = "lines-title") {
                        Text(
                            title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 6.dp)
                        )
                    }
                }
                itemsIndexed(entry.lines) { index, line -> DetailLine(line, segmentedShape(index, entry.lines.size, outer = 20.dp, inner = 4.dp)) }
            }
            item(key = "footer") {
                Text(
                    "A saved result. Run it again to see how things are right now.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, top = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun MetricTile(metric: HistoryMetric, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = modifier) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(metric.value, style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"), fontWeight = FontWeight.SemiBold, maxLines = 1)
                if (metric.unit.isNotEmpty()) {
                    Text(
                        metric.unit,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier.padding(start = 2.dp, bottom = 3.dp)
                    )
                }
            }
            Text(metric.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun DetailLine(line: HistoryLine, shape: Shape) {
    Surface(shape = shape, color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            line.rssi?.let { rssi ->
                val quality = SignalQuality.of(rssi)
                val colors = signalColors(quality)
                Box(Modifier.size(24.dp).clip(quality.polygon().toShape()).background(colors.accent))
            }
            Column(Modifier.weight(1f)) {
                Text(line.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                line.supporting?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            line.trailing?.let {
                Text(it, style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
