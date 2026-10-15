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
import androidx.compose.foundation.horizontalScroll
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
    onBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptics = rememberPulseWaveHapticFeedback()
    val reduceMotion = LocalReduceMotion.current

    com.pulsewave.app.core.platform.LocalBackHandler.current(uiState !is ToolsUiState.Home) {
        viewModel.reset()
    }

    var inputText by remember(initialTarget) { mutableStateOf(initialTarget) }
    LaunchedEffect(initialTarget, initialTool) {
        if (initialTarget.isNotBlank()) viewModel.reset()
    }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val tools = listOf(
        Tool("ping", "Ping", Icons.Outlined.Speed, "Does it answer?"),
        Tool("dns", "DNS", Icons.Outlined.Dns, "Find its address"),
        Tool("portscan", "Ports", Icons.Outlined.Security, "What's open?"),
    )

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text("Test a website") },
                subtitle = { Text("See if a site or device answers, and how fast") },
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
                is ToolsUiState.PingResult -> PingResultContent(
                    target = state.target,
                    lines = state.results,
                    onRetry = { haptics.click(); viewModel.startTool(ToolType.PING, state.target) },
                    onDone = { haptics.light(); viewModel.reset() }
                )
                is ToolsUiState.DnsResult -> DnsResultContent(
                    query = state.query,
                    lines = state.records,
                    onRetry = { haptics.click(); viewModel.startTool(ToolType.DNS, state.query) },
                    onDone = { haptics.light(); viewModel.reset() }
                )
                is ToolsUiState.Error -> {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Diagnostic failed", style = MaterialTheme.typography.headlineSmall)
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.reset() }) { Text("Try again") }
                    }
                }
                is ToolsUiState.PortScanResult -> PortsResultContent(
                    host = state.host,
                    ports = state.ports,
                    onRetry = { haptics.click(); viewModel.startTool(ToolType.PORT_SCAN, state.host) },
                    onDone = { haptics.light(); viewModel.reset() }
                )
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
    modifier: Modifier = Modifier
) {
    val inputFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val hasInput = inputText.isNotBlank()
    // Your router is the most useful thing to test at home, so it's the first suggestion.
    val statusRepository: com.pulsewave.app.core.domain.NetworkStatusRepository = org.koin.compose.koinInject()
    val status by remember(statusRepository) { statusRepository.getCurrentNetworkStatus(refreshIntervalMs = 10_000) }
        .collectAsState(initial = com.pulsewave.app.core.model.NetworkStatus.Disconnected)
    val suggestions = listOfNotNull(
        status.gateway.takeIf { status.isConnected && it.isNotBlank() }?.let { "Your router" to it },
        "Google" to "google.com",
        "Cloudflare" to "1.1.1.1"
    )
    val haptics = rememberPulseWaveHapticFeedback()

    Column(
        modifier = modifier
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
            modifier = Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState())
        ) {
            suggestions.forEach { (label, target) ->
                AssistChip(
                    onClick = {
                        haptics.light()
                        onInputChange(target)
                    },
                    label = { Text(label, style = MaterialTheme.typography.labelLarge) },
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
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = modifier
            .heightIn(min = 120.dp)
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
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                tool.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                tool.description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
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
