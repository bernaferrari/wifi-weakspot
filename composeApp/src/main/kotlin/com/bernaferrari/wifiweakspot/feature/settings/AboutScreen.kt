@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.wifiweakspot.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bernaferrari.wifiweakspot.core.designsystem.component.SignalMorphBlob
import com.bernaferrari.wifiweakspot.core.designsystem.component.SocialLogos
import com.bernaferrari.wifiweakspot.core.wifi.SignalQuality
import org.koin.compose.koinInject

private const val RepositoryUrl = "https://github.com/bernaferrari/wifi-weakspot"
private const val FeedbackEmail = "bernaferrari2@gmail.com"

/** The app, the code, a way to reach the author, and the author's other places. */
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val platform: com.bernaferrari.wifiweakspot.core.platform.PlatformActions = koinInject()
    val scheme = MaterialTheme.colorScheme
    Scaffold(topBar = {
        TopAppBar(title = {}, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
        })
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = 600.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SignalMorphBlob(quality = SignalQuality.Excellent, color = scheme.primaryContainer, size = 140.dp) {
                    Icon(Icons.Rounded.Wifi, null, Modifier.size(60.dp), tint = scheme.onPrimaryContainer)
                }
                Text("WiFi Weakspot", Modifier.padding(top = 20.dp), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("Find bad Wi-Fi at home", Modifier.padding(top = 6.dp), style = MaterialTheme.typography.titleMedium, color = scheme.onSurfaceVariant)
                Surface(shape = CircleShape, color = scheme.surfaceContainerHighest, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Version 1.0", Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.labelLarge, color = scheme.onSurfaceVariant)
                }

                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(top = 24.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    ActionCard(
                        title = "GitHub", body = "Open source · Apache 2.0", icon = Icons.Rounded.Code,
                        container = scheme.primaryContainer, content = scheme.onPrimaryContainer, badge = MaterialShapes.Cookie9Sided.toShape(),
                        shape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp, topEnd = 8.dp, bottomEnd = 8.dp),
                        onClick = { platform.openUrl(RepositoryUrl) }, modifier = Modifier.weight(1f)
                    )
                    ActionCard(
                        title = "Feedback", body = "Ideas and bug reports", icon = Icons.Rounded.Email,
                        container = scheme.secondaryContainer, content = scheme.onSecondaryContainer, badge = MaterialShapes.Clover4Leaf.toShape(),
                        shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp, topEnd = 32.dp, bottomEnd = 32.dp),
                        onClick = { platform.openUrl("mailto:$FeedbackEmail?subject=WiFi%20Weakspot") }, modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    "Elsewhere", Modifier.fillMaxWidth().padding(start = 8.dp, top = 24.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.titleSmall, color = scheme.primary
                )
                Elsewhere(onOpen = platform::openUrl)

                Column(Modifier.padding(top = 24.dp, start = 16.dp, end = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.VerifiedUser, null, Modifier.size(20.dp), tint = scheme.onSurfaceVariant)
                    Text("Your results stay on your device unless you share them.", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    TextButton(onClick = { platform.openUrl("$RepositoryUrl/blob/main/PRIVACY.md") }) {
                        Text("Privacy policy")
                    }
                }
            }
        }
    }
}

/** A bold card for one of the two things people come here for, its icon on an Expressive shape. */
@Composable
private fun ActionCard(
    title: String, body: String, icon: ImageVector, container: Color, content: Color,
    badge: Shape, shape: Shape, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Surface(onClick = onClick, shape = shape, color = container, contentColor = content, modifier = modifier.fillMaxHeight()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(Modifier.size(48.dp).clip(badge).background(content), contentAlignment = Alignment.Center) {
                    Icon(icon, null, Modifier.size(22.dp), tint = container)
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.OpenInNew, null, Modifier.size(18.dp), tint = content.copy(alpha = 0.7f))
            }
            Text(title, Modifier.padding(top = 8.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodySmall, color = content.copy(alpha = 0.8f), maxLines = 2)
        }
    }
}

/** The author's profiles as one connected group: round outer ends, tight inner joins. */
@Composable
private fun Elsewhere(onOpen: (String) -> Unit) {
    val links = listOf(
        Triple("GitHub", "https://github.com/bernaferrari", SocialLogos.GitHub),
        Triple("X", "https://x.com/bernaferrari", SocialLogos.X),
        Triple("Reddit", "https://www.reddit.com/user/bernaferrari", SocialLogos.Reddit),
        Triple("LinkedIn", "https://www.linkedin.com/in/bernaferrari", SocialLogos.LinkedIn)
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        links.forEachIndexed { index, (label, url, logo) ->
            val shape = when (index) {
                0 -> RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp, topEnd = 8.dp, bottomEnd = 8.dp)
                links.lastIndex -> RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp, topEnd = 28.dp, bottomEnd = 28.dp)
                else -> RoundedCornerShape(8.dp)
            }
            Surface(
                onClick = { onOpen(url) }, shape = shape,
                color = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f).heightIn(min = 56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) { Icon(logo, contentDescription = label, modifier = Modifier.size(24.dp)) }
            }
        }
    }
}
