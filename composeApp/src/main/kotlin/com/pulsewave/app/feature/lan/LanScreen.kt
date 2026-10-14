@file:OptIn(ExperimentalMaterial3Api::class, androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.pulsewave.app.feature.lan

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pulsewave.app.core.designsystem.PulseWaveAlpha
import com.pulsewave.app.core.designsystem.LocalReduceMotion
import com.pulsewave.app.core.designsystem.animateAccessibleFloat
import com.pulsewave.app.core.designsystem.PulseWaveShapes
import com.pulsewave.app.core.designsystem.component.LinearWavyProgressIndicator
import com.pulsewave.app.core.designsystem.rememberPulseWaveHapticFeedback
import com.pulsewave.app.core.model.lan.LanDevice
import org.koin.compose.viewmodel.koinViewModel

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanScreen(
    modifier: Modifier = Modifier,
    viewModel: LanViewModel = koinViewModel(),
    onDeviceClick: (String) -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val haptics = rememberPulseWaveHapticFeedback()

    LaunchedEffect(Unit) {
        viewModel.startScan()
    }

    val devices = when (val state = uiState) {
        is LanUiState.Success -> state.devices
        else -> emptyList()
    }
    val isScanning = uiState is LanUiState.Scanning

    val infiniteTransition = rememberInfiniteTransition(label = "Rotation")
    val rotation by infiniteTransition.animateAccessibleFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    val pulseScale by infiniteTransition.animateAccessibleFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Devices") },
                subtitle = { Text("Everything connected to your Wi-Fi") },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = {
                            haptics.light()
                            onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            haptics.click()
                            if (isScanning) viewModel.stopScan() else viewModel.startScan()
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = if (isScanning) "Cancel scan" else "Scan",
                            modifier = if (isScanning) Modifier.rotate(rotation) else Modifier
                        )
                    }
                }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            // The header card shows progress; the pull indicator would overlap it.
            isRefreshing = false,
            onRefresh = {
                haptics.click()
                viewModel.startScan()
            },
            modifier = Modifier.padding(padding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = PulseWaveShapes.extraLarge,
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier.background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = PulseWaveAlpha.strong)
                                )
                            )
                        )
                    ) {
                        Icon(
                            Icons.Default.DevicesOther,
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(140.dp)
                                .offset(x = 40.dp, y = 40.dp)
                                .rotate(-15f),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = PulseWaveAlpha.subtle)
                        )

                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Your network",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = PulseWaveAlpha.strong)
                                    )
                                    Text(
                                        when {
                                            isScanning -> "Looking for devices…"
                                            devices.isEmpty() -> "No devices yet"
                                            devices.size == 1 -> "1 device"
                                            else -> "${devices.size} devices"
                                        },
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (devices.isNotEmpty()) Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = PulseWaveAlpha.light),
                                    modifier = Modifier.scale(if (isScanning) pulseScale else 1f)
                                ) {
                                    Text(
                                        "${devices.size}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(20.dp))

                            if (isScanning) {
                                LinearWavyProgressIndicator(
                                    progress = { 1f },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Checking every address on your network",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.TouchApp,
                                        null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = PulseWaveAlpha.strong)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "Pull down or tap refresh to look again",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = PulseWaveAlpha.strong)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (uiState is LanUiState.Error) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Couldn't look for devices", style = MaterialTheme.typography.titleMedium)
                            Text((uiState as LanUiState.Error).message)
                            TextButton(onClick = { viewModel.startScan() }) { Text("Retry") }
                        }
                    }
                }
            } else if (devices.isEmpty() && !isScanning) {
                item {
                    com.pulsewave.app.core.designsystem.component.EmptyStateWithIllustration(
                        illustration = { com.pulsewave.app.core.designsystem.component.LanDeviceIllustration() },
                        title = if (uiState is LanUiState.Cancelled) "Scan cancelled" else "No devices discovered",
                        subtitle = "Discovery checks ping and three common TCP ports. Devices that block all probes may not appear."
                    )
                }
            }

            itemsIndexed(devices, key = { _, device -> device.ipAddress }) { index, device ->
                DeviceCard(
                    device = device,
                    onClick = {
                        haptics.click()
                        onDeviceClick(device.ipAddress)
                    },
                    modifier = Modifier
                )
            }
        }
        }
    }
}

@Composable
fun DeviceCard(device: LanDevice, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "pressScale"
    )

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = PulseWaveShapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = modifier.scale(scale)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = PulseWaveShapes.medium,
                color = if (device.isOnline)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (device.isOnline) Icons.Default.Smartphone else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (device.isOnline)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    device.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    device.ipAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(if (device.isOnline) "Replied to this scan" else "Cached neighbor · reachability unconfirmed", style = MaterialTheme.typography.labelSmall)
                device.macAddress?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
