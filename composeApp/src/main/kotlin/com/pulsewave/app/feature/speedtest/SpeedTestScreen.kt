@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.pulsewave.app.feature.speedtest

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pulsewave.app.core.designsystem.PulseWaveShapes
import com.pulsewave.app.core.designsystem.PulseWaveAlpha
import com.pulsewave.app.core.designsystem.component.LinearWavyProgressIndicator
import com.pulsewave.app.core.designsystem.rememberPulseWaveHapticFeedback
import org.koin.compose.viewmodel.koinViewModel

sealed interface SpeedTestState {
    data object Idle : SpeedTestState
    data class Running(val phase: String, val progress: Float, val currentMbps: Float) : SpeedTestState
    data class Complete(val download: Float, val upload: Float, val ping: Float) : SpeedTestState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedTestScreen(
    modifier: Modifier = Modifier,
    viewModel: SpeedTestViewModel = koinViewModel(),
    onBack: (() -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsState()
    val haptics = rememberPulseWaveHapticFeedback()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Speed Test") },
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
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val currentState = state) {
                is SpeedTestState.Idle -> {
                    SpeedGauge(value = 0f, maxValue = 100f, label = "Mbps", isRunning = false)
                    Spacer(Modifier.height(48.dp))
                    Button(
                        onClick = { 
                            haptics.click()
                            viewModel.startTest() 
                        },
                        modifier = Modifier.height(56.dp).fillMaxWidth(0.7f),
                        shape = PulseWaveShapes.extraLarge,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Start Speed Test", style = MaterialTheme.typography.titleMedium)
                    }
                }
                is SpeedTestState.Running -> {
                    SpeedGauge(
                        value = currentState.currentMbps,
                        maxValue = 100f,
                        label = currentState.phase,
                        isRunning = true
                    )
                    Spacer(Modifier.height(24.dp))
                    
                    LinearWavyProgressIndicator(
                        progress = { currentState.progress },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                        amplitude = 4.dp,
                        wavelength = 20.dp
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    Text(
                        currentState.phase, 
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(Modifier.height(32.dp))
                    OutlinedButton(
                        onClick = { 
                            haptics.light()
                            viewModel.cancelTest() 
                        },
                        shape = PulseWaveShapes.extraLarge,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Cancel")
                    }
                }
                is SpeedTestState.Complete -> {
                    // Trigger success haptic when complete
                    LaunchedEffect(currentState) {
                        haptics.success()
                    }
                    
                    SpeedGauge(value = currentState.download, maxValue = 200f, label = "Download", isRunning = false)
                    Spacer(Modifier.height(32.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ResultCard("Download", "${currentState.download}", "Mbps", MaterialTheme.colorScheme.primaryContainer)
                        ResultCard("Upload", "${currentState.upload}", "Mbps", MaterialTheme.colorScheme.secondaryContainer)
                        ResultCard("Ping", "${currentState.ping}", "ms", MaterialTheme.colorScheme.tertiaryContainer)
                    }
                    
                    Spacer(Modifier.height(40.dp))
                    FilledTonalButton(
                        onClick = { 
                            haptics.click()
                            viewModel.startTest() 
                        },
                        modifier = Modifier.height(52.dp),
                        shape = PulseWaveShapes.large
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Test Again")
                    }
                }
            }
        }
    }
}

@Composable
fun SpeedGauge(value: Float, maxValue: Float, label: String, isRunning: Boolean) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    
    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "gaugeValue"
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier.size(260.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isRunning || value > 0) {
            Canvas(modifier = Modifier.fillMaxSize().blur(20.dp)) {
                val strokeWidth = 28.dp.toPx()
                val startAngle = 135f
                val sweepAngle = 270f
                val valueSweep = (animatedValue / maxValue) * sweepAngle
                val progressColor = androidx.compose.ui.graphics.lerp(
                    primaryColor, 
                    tertiaryColor, 
                    (animatedValue / maxValue).coerceIn(0f, 1f)
                )
                drawArc(
                    color = progressColor.copy(alpha = if (isRunning) glowAlpha else PulseWaveAlpha.light),
                    startAngle = startAngle,
                    sweepAngle = valueSweep.coerceAtMost(sweepAngle),
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth),
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                )
            }
        }
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 24.dp.toPx()
            val startAngle = 135f
            val sweepAngle = 270f

            drawArc(
                color = surfaceVariant,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            )

            val valueSweep = (animatedValue / maxValue) * sweepAngle
            val progressColor = androidx.compose.ui.graphics.lerp(
                primaryColor, 
                tertiaryColor, 
                (animatedValue / maxValue).coerceIn(0f, 1f)
            )
            drawArc(
                color = progressColor,
                startAngle = startAngle,
                sweepAngle = valueSweep.coerceAtMost(sweepAngle),
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${animatedValue}",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                label, 
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ResultCard(label: String, value: String, unit: String, containerColor: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = PulseWaveShapes.extraLarge,
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label, 
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value, 
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                unit,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
