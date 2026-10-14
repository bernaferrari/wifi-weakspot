package com.pulsewave.app.feature.tools

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.IntOffset
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pulsewave.app.core.designsystem.PulseWaveAlpha
import com.pulsewave.app.core.designsystem.LocalReduceMotion
import com.pulsewave.app.core.designsystem.animateAccessibleFloat
import com.pulsewave.app.core.designsystem.PulseWaveShapes
import com.pulsewave.app.core.designsystem.rememberPulseWaveHapticFeedback
import androidx.compose.material3.CircularWavyProgressIndicator
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Speed
import com.pulsewave.app.core.designsystem.component.ShapeIcon

data class Tool(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val description: String
)

enum class ToolType {
    PING, DNS, PORT_SCAN
}

sealed interface ToolsUiState {
    data object Home : ToolsUiState
    data class Running(val tool: ToolType, val target: String) : ToolsUiState
    data class PingResult(val target: String, val results: List<String>) : ToolsUiState
    data class DnsResult(val query: String, val records: List<String>) : ToolsUiState
    data class PortScanResult(val host: String, val ports: List<Pair<Int, String>>) : ToolsUiState
    data class Error(val message: String) : ToolsUiState
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ToolsScreen(
    modifier: Modifier = Modifier,
    viewModel: ToolsViewModel = koinViewModel(),
    initialTarget: String = "",
    initialTool: ToolType? = null,
    onOpenSpeedTest: () -> Unit = {},
    onOpenDevices: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    /** Set when opened from a device; hides the hub and shows a back arrow. */
    onBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptics = rememberPulseWaveHapticFeedback()
    val reduceMotion = LocalReduceMotion.current

    BackHandler(enabled = uiState !is ToolsUiState.Home) {
        viewModel.reset()
    }

    var inputText by remember(initialTarget) { mutableStateOf(initialTarget) }
    LaunchedEffect(initialTarget, initialTool) {
        if (initialTarget.isNotBlank()) viewModel.reset()
    }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val tools = listOf(
        Tool("ping", "Ping", Icons.Outlined.Speed, "Check latency"),
        Tool("dns", "DNS", Icons.Outlined.Dns, "IPv4 addresses"),
        Tool("portscan", "Ports", Icons.Outlined.Security, "Scan open ports"),
    )

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(if (onBack != null) "Diagnose" else "Tools") },
                subtitle = { Text(if (onBack != null) "Ping and check ports" else "Test speed, find devices, diagnose") },
                navigationIcon = {
                    if (onBack != null) IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        AnimatedContent(
            targetState = uiState,
            modifier = Modifier.padding(padding).fillMaxSize(),
            transitionSpec = {
                when {
                    reduceMotion -> fadeIn(snap()) togetherWith fadeOut(snap())
                    // Home -> Running: slide up
                    initialState is ToolsUiState.Home && targetState is ToolsUiState.Running -> {
                        (slideInVertically { it / 4 } + fadeIn(tween(300))) togetherWith
                        (slideOutVertically { -it / 4 } + fadeOut(tween(200)))
                    }
                    // Running -> Result: slide from right
                    initialState is ToolsUiState.Running -> {
                        (slideInHorizontally { it / 3 } + fadeIn(tween(300))) togetherWith
                        (slideOutHorizontally { -it / 3 } + fadeOut(tween(200)))
                    }
                    // Result -> Home: slide down
                    targetState is ToolsUiState.Home -> {
                        (slideInVertically { -it / 4 } + fadeIn(tween(300))) togetherWith
                        (slideOutVertically { it / 4 } + fadeOut(tween(200)))
                    }
                    else -> {
                        fadeIn(tween(300)) togetherWith fadeOut(tween(200))
                    }
                }
            },
            label = "toolsTransition"
        ) { state ->
            when (state) {
                is ToolsUiState.Home -> {
                    ToolsHomeContent(
                        tools = tools,
                        inputText = inputText,
                        initialTool = initialTool,
                        onInputChange = { inputText = it },
                        showHub = onBack == null,
                        onOpenSpeedTest = onOpenSpeedTest,
                        onOpenDevices = onOpenDevices,
                        onOpenHistory = onOpenHistory,
                        onToolSelect = { tool ->
                            val type = when (tool.id) {
                                "ping" -> ToolType.PING
                                "dns" -> ToolType.DNS
                                "portscan" -> ToolType.PORT_SCAN
                                else -> null
                            }
                            if (type != null && inputText.isNotBlank()) {
                                haptics.click()
                                viewModel.startTool(type, inputText)
                            }
                        }
                    )
                }
                is ToolsUiState.Running -> {
                    RunningToolContent(
                        tool = state.tool,
                        target = state.target,
                        onCancel = {
                            haptics.light()
                            viewModel.stopTool()
                        }
                    )
                }
                is ToolsUiState.PingResult -> {
                    ResultContent(
                        title = "Ping",
                        target = state.target,
                        results = state.results,
                        onBack = {
                            haptics.light()
                            viewModel.reset()
                        }
                    )
                }
                is ToolsUiState.DnsResult -> {
                    ResultContent(
                        title = "DNS",
                        target = state.query,
                        results = state.records,
                        onBack = {
                            haptics.light()
                            viewModel.reset()
                        }
                    )
                }
                is ToolsUiState.Error -> {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Diagnostic failed", style = MaterialTheme.typography.headlineSmall)
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.reset() }) { Text("Try again") }
                    }
                }
                is ToolsUiState.PortScanResult -> {
                    PortScanResultContent(
                        host = state.host,
                        ports = state.ports,
                        onBack = {
                            haptics.light()
                            viewModel.reset()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ToolsHomeContent(
    tools: List<Tool>,
    inputText: String,
    onInputChange: (String) -> Unit,
    onToolSelect: (Tool) -> Unit,
    initialTool: ToolType? = null,
    onOpenSpeedTest: () -> Unit = {},
    onOpenDevices: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    showHub: Boolean = true,
    modifier: Modifier = Modifier
) {
    val inputFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val hasInput = inputText.isNotBlank()
    val haptics = rememberPulseWaveHapticFeedback()

    Column(
        modifier = modifier
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val scheme = MaterialTheme.colorScheme
        val hubItems = listOf(
            HubItem("Wi-Fi speed", "Phone to router: speed and response time", Icons.Rounded.Speed, MaterialShapes.Sunny, scheme.primaryContainer, scheme.onPrimaryContainer, onOpenSpeedTest),
            HubItem("Devices", "Everything connected to your network", Icons.Rounded.Devices, MaterialShapes.Clover8Leaf, scheme.tertiaryContainer, scheme.onTertiaryContainer, onOpenDevices),
            HubItem("History", "Past speed tests, scans, and tools", Icons.Rounded.History, MaterialShapes.Puffy, scheme.secondaryContainer, scheme.onSecondaryContainer, onOpenHistory)
        )
        if (showHub) Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
            hubItems.forEachIndexed { index, item ->
                SegmentedListItem(
                    onClick = { haptics.click(); item.onClick() },
                    shapes = ListItemDefaults.segmentedShapes(index = index, count = hubItems.size),
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    leadingContent = { ShapeIcon(item.icon, item.shape, item.container, item.content) },
                    supportingContent = { Text(item.subtitle) },
                    trailingContent = { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null) }
                ) { Text(item.title, style = MaterialTheme.typography.titleMedium) }
            }
        }

        if (showHub) Text(
            "Diagnose a host",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
        )
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange,
            label = { Text("Hostname or IP address") },
            placeholder = { Text("google.com or 8.8.8.8") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(inputFocusRequester),
            singleLine = true,
            shape = PulseWaveShapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            leadingIcon = {
                Icon(
                    Icons.Default.Language,
                    contentDescription = null,
                    tint = if (hasInput) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (hasInput) {
                    IconButton(onClick = { onInputChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            }
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("1.1.1.1", "8.8.8.8", "google.com").forEach { target ->
                AssistChip(
                    onClick = {
                        haptics.light()
                        onInputChange(target)
                    },
                    label = { Text(target, style = MaterialTheme.typography.labelMedium) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (inputText == target)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    border = null
                )
            }
        }

        initialTool?.let { type ->
            val toolId = when (type) { ToolType.PING -> "ping"; ToolType.DNS -> "dns"; ToolType.PORT_SCAN -> "portscan" }
            tools.find { it.id == toolId }?.let { requested ->
                FilledTonalButton(onClick = { onToolSelect(requested) }, enabled = hasInput, modifier = Modifier.fillMaxWidth()) {
                    Text("Run ${requested.name} on $inputText")
                }
            }
        }
        Text("DNS uses the system resolver for IPv4 addresses. Ports checks common TCP services.", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            tools.forEach { tool ->
                ToolButton(
                    tool = tool,
                    enabled = hasInput,
                    onClick = { onToolSelect(tool) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (!hasInput) {
            Text(
                "Enter a website or IP address, then pick a check.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

private data class HubItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val shape: androidx.graphics.shapes.RoundedPolygon,
    val container: androidx.compose.ui.graphics.Color,
    val content: androidx.compose.ui.graphics.Color,
    val onClick: () -> Unit
)

@Composable
fun ToolButton(
    tool: Tool,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.95f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "scale"
    )

    Card(
        onClick = onClick,
        enabled = enabled,
        shape = PulseWaveShapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = if (enabled)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainerLow,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = modifier
            .height(100.dp)
            .scale(scale)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                tool.icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = if (enabled)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = PulseWaveAlpha.medium)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                tool.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = PulseWaveAlpha.medium)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RunningToolContent(
    tool: ToolType,
    target: String,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "running")
    val pulse by infiniteTransition.animateAccessibleFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(100.dp).scale(pulse)
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularWavyProgressIndicator(
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            tool.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            target,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(48.dp))

        TextButton(
            onClick = onCancel,
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Cancel")
        }
    }
}

@Composable
fun ResultContent(
    title: String,
    target: String,
    results: List<String>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            FilledTonalIconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    target,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Card(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            shape = PulseWaveShapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            LazyColumn(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(results) { index, line ->
                    Text(
                        line,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = PulseWaveShapes.medium
        ) {
            Text("Done")
        }
    }
}

@Composable
fun PortScanResultContent(
    host: String,
    ports: List<Pair<Int, String>>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            FilledTonalIconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "Port Scan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    host,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.weight(1f))
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    "${ports.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (ports.isEmpty()) {
                item { Text("No open common TCP ports found. Ports may be closed, filtered, or the target may be unreachable.") }
            }
            itemsIndexed(ports) { index, (port, service) ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    shape = PulseWaveShapes.medium,
                    modifier = Modifier
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.LockOpen,
                                        null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "$port",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "Typical service: $service",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = PulseWaveShapes.medium
        ) {
            Text("Done")
        }
    }
}
