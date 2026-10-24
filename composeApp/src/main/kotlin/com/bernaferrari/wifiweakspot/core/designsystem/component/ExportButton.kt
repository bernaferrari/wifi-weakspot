package com.bernaferrari.wifiweakspot.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Elegant export FAB with format options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportFab(
    onExportCsv: () -> Unit,
    onExportJson: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {}
) {
    val scale by animateFloatAsState(
        targetValue = if (expanded) 1.1f else 1f,
        animationSpec = if (com.bernaferrari.wifiweakspot.core.designsystem.LocalReduceMotion.current) snap() else spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "fabScale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = if (com.bernaferrari.wifiweakspot.core.designsystem.LocalReduceMotion.current) snap() else spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "fabRotation"
    )

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Mini FABs - animated visibility
        AnimatedVisibility(
            visible = expanded,
            enter = if (com.bernaferrari.wifiweakspot.core.designsystem.LocalReduceMotion.current) EnterTransition.None else fadeIn() + scaleIn(),
            exit = if (com.bernaferrari.wifiweakspot.core.designsystem.LocalReduceMotion.current) ExitTransition.None else fadeOut() + scaleOut()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Share option
                MiniFab(
                    icon = Icons.Default.Share,
                    label = "Share",
                    onClick = {
                        onExpandedChange(false)
                        onShare()
                    }
                )
                // JSON option
                MiniFab(
                    icon = Icons.Outlined.Description,
                    label = "JSON",
                    onClick = {
                        onExpandedChange(false)
                        onExportJson()
                    }
                )
                // CSV option
                MiniFab(
                    icon = Icons.Default.FileDownload,
                    label = "CSV",
                    onClick = {
                        onExpandedChange(false)
                        onExportCsv()
                    }
                )
            }
        }

        // Main FAB
        LargeFloatingActionButton(
            onClick = { onExpandedChange(!expanded) },
            shape = LargeFabShape,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            modifier = modifier.scale(scale)
        ) {
            Icon(
                Icons.Default.FileDownload,
                contentDescription = "Export",
                modifier = Modifier.size(28.dp).graphicsLayer { rotationZ = rotation },
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun MiniFab(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = SmallFabShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 3.dp
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        SmallFloatingActionButton(
            onClick = onClick,
            shape = SmallFabShape,
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

private val LargeFabShape = RoundedCornerShape(28.dp)
private val SmallFabShape = RoundedCornerShape(16.dp)

/**
 * Compact export button for app bars
 */
@Composable
fun ExportButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Export"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = if (com.bernaferrari.wifiweakspot.core.designsystem.LocalReduceMotion.current) snap() else spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    FilledTonalButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Export sheet bottom content
 */
@Composable
fun ExportSheetContent(
    onExportCsv: () -> Unit,
    onExportJson: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Export Data",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Choose a format to export or share your scan results",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        // CSV Export
        OutlinedButton(
            onClick = onExportCsv,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.FileDownload, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text("Export as CSV", style = MaterialTheme.typography.titleMedium)
        }

        // JSON Export
        OutlinedButton(
            onClick = onExportJson,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Outlined.Description, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text("Export as JSON", style = MaterialTheme.typography.titleMedium)
        }

        // Share
        Button(
            onClick = onShare,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Share, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text("Share Summary", style = MaterialTheme.typography.titleMedium)
        }
    }
}
