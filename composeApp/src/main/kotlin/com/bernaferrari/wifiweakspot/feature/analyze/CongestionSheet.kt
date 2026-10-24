package com.bernaferrari.wifiweakspot.feature.analyze

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bernaferrari.wifiweakspot.core.designsystem.WifiWeakspotAlpha
import com.bernaferrari.wifiweakspot.core.designsystem.WifiWeakspotShapes

@Composable
fun CongestionInsightSheet(
    scores: List<ChannelScore>,
    modifier: Modifier = Modifier
) {
    if (scores.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Estimated channel overlap",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            val avgScore = scores.map { it.score }.average()
            val overallRating = when {
                avgScore < 1000 -> TrafficRating.LOW
                avgScore < 3000 -> TrafficRating.MEDIUM
                avgScore < 6000 -> TrafficRating.HIGH
                else -> TrafficRating.SEVERE
            }

            Surface(
                color = overlapColor(overallRating).copy(alpha = WifiWeakspotAlpha.light),
                shape = WifiWeakspotShapes.small
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(overlapColor(overallRating))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        overallRating.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = overlapColor(overallRating)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Best Channel Recommendation
        val bestChannel = scores.find { it.recommendation }
        if (bestChannel != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = WifiWeakspotShapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                   modifier = Modifier.padding(16.dp),
                   verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "Try channel ${bestChannel.channelNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Lowest estimated overlap for 20 MHz channels",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = WifiWeakspotAlpha.strong)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Based on nearby access points and signal strength, not measured traffic. " +
                "Check your router’s supported channels and local rules before changing settings. " +
                "5 and 6 GHz suggestions require known router support; missing wide-channel centers " +
                "show only the primary 20 MHz channel.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        // Chart
        Text(
            "Overlap score",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(16.dp))

        // Horizontal Bar Chart (Scrollable Row of Vertical Bars)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            modifier = Modifier.fillMaxWidth().height(140.dp)
        ) {
            items(scores) { item ->
                CongestionBar(item)
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun CongestionBar(item: ChannelScore) {
    val rating = item.rating
    val color = overlapColor(rating)

    // Normalize height: Max score typically ~10000?
    // Let's cap at 6000 for visual full height
    val normalizedHeight = (item.score / 6000.0).coerceIn(0.1, 1.0).toFloat()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.fillMaxHeight().width(32.dp)
    ) {
        // An arbitrary heuristic score, never a measured utilization percentage.
        Text(
            "${item.score.toInt()}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
             modifier = Modifier.padding(bottom = 4.dp),
             maxLines = 1
        )

        // Bar
        Box(
            modifier = Modifier
                .width(12.dp)
                .fillMaxHeight(normalizedHeight)
                .clip(WifiWeakspotShapes.extraSmall)
                .background(color)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "${item.channelNumber}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun overlapColor(rating: TrafficRating): Color = when (rating) {
    TrafficRating.LOW -> MaterialTheme.colorScheme.primary
    TrafficRating.MEDIUM -> MaterialTheme.colorScheme.secondary
    TrafficRating.HIGH, TrafficRating.SEVERE -> MaterialTheme.colorScheme.error
}
