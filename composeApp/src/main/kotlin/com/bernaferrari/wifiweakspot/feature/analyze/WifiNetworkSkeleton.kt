package com.bernaferrari.wifiweakspot.feature.analyze

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bernaferrari.wifiweakspot.core.designsystem.WifiWeakspotAlpha
import com.bernaferrari.wifiweakspot.core.designsystem.WifiWeakspotShapes
import com.bernaferrari.wifiweakspot.core.designsystem.ShimmerPlaceholder

@Composable
fun WifiNetworkSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = WifiWeakspotShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WifiWeakspotAlpha.light)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.height(24.dp)
            ) {
                repeat(4) {
                    ShimmerPlaceholder(
                        visible = true,
                        modifier = Modifier.width(6.dp).fillMaxHeight((it + 1) * 0.25f),
                        shape = WifiWeakspotShapes.extraSmall
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                ShimmerPlaceholder(
                    visible = true,
                    modifier = Modifier.width(140.dp).height(20.dp)
                )
                Spacer(Modifier.height(8.dp))
                ShimmerPlaceholder(
                    visible = true,
                    modifier = Modifier.width(100.dp).height(14.dp)
                )
            }

            ShimmerPlaceholder(
                visible = true,
                modifier = Modifier.size(40.dp, 24.dp),
                shape = WifiWeakspotShapes.extraSmall
            )
        }
    }
}
