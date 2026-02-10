package com.pulsewave.app.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp
import com.pulsewave.app.core.designsystem.PulseWaveShapes
import com.pulsewave.app.core.designsystem.component.LanDeviceIllustration
import com.pulsewave.app.core.designsystem.component.PrivacyIllustration
import com.pulsewave.app.core.designsystem.component.SurveyIllustration
import com.pulsewave.app.core.designsystem.component.WifiNetworkIllustration
import com.pulsewave.app.core.designsystem.rememberPulseWaveHapticFeedback

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onComplete: () -> Unit = {}
) {
    var currentPage by remember { mutableStateOf(0) }
    val haptics = rememberPulseWaveHapticFeedback()
    val pages = listOf(
        OnboardingPage(
            illustration = { mod -> WifiNetworkIllustration(mod, size = 180.dp) },
            title = "Analyze Your WiFi",
            description = "See nearby networks, pick clearer channels, and understand interference at a glance."
        ),
        OnboardingPage(
            illustration = { mod -> LanDeviceIllustration(mod, size = 180.dp) },
            title = "Network Diagnostics",
            description = "Ping, traceroute, DNS lookup, port scans, and speed tests in one focused toolkit."
        ),
        OnboardingPage(
            illustration = { mod -> SurveyIllustration(mod, size = 180.dp) },
            title = "Site Surveys",
            description = "Walk your space and build heatmaps to pinpoint dead zones and weak coverage."
        ),
        OnboardingPage(
            illustration = { mod -> PrivacyIllustration(mod, size = 180.dp) },
            title = "Privacy First",
            description = "Everything stays on device. No accounts, no tracking, and no hidden uploads."
        )
    )

    val colorScheme = MaterialTheme.colorScheme
    val background = Brush.verticalGradient(
        0f to colorScheme.surface,
        0.5f to colorScheme.surfaceContainerLow,
        1f to colorScheme.surfaceContainerHigh
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background)
    ) {
        OnboardingBackdrop(
            modifier = Modifier.fillMaxSize(),
            primary = colorScheme.primary,
            secondary = colorScheme.secondary,
            tertiary = colorScheme.tertiary
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(300)) +
                        slideInHorizontally { if (targetState > initialState) it / 6 else -it / 6 } +
                        scaleIn(initialScale = 0.98f, animationSpec = spring(stiffness = Spring.StiffnessLow)))
                        .togetherWith(
                            fadeOut(animationSpec = tween(200)) +
                                slideOutHorizontally { if (targetState > initialState) -it / 8 else it / 8 } +
                                scaleOut(targetScale = 1.02f, animationSpec = tween(200))
                        )
                },
                label = "onboarding_page"
            ) { pageIndex ->
                val page = pages[pageIndex]

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 260.dp),
                        shape = PulseWaveShapes.extraLarge,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(PulseWaveShapes.extraLarge)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                colorScheme.primary.copy(alpha = 0.18f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )

                            AssistChip(
                                onClick = {},
                                enabled = false,
                                label = { Text("Step ${pageIndex + 1} of ${pages.size}") },
                                colors = AssistChipDefaults.assistChipColors(
                                    disabledContainerColor = colorScheme.surfaceContainerLow,
                                    disabledLabelColor = colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.align(Alignment.TopStart)
                            )

                            page.illustration(
                                Modifier
                                    .align(Alignment.Center)
                                    .scale(1.05f)
                            )
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    Text(
                        text = "PulseWave",
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.primary
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        page.title,
                        style = MaterialTheme.typography.displaySmall,
                        color = colorScheme.onSurface
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        page.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                pages.forEachIndexed { index, _ ->
                    val isSelected = index == currentPage
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 36.dp else 12.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "dotWidth"
                    )
                    Surface(
                        modifier = Modifier
                            .height(10.dp)
                            .width(width),
                        shape = PulseWaveShapes.extraSmall,
                        color = if (isSelected) colorScheme.primary else colorScheme.surfaceVariant
                    ) {}
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentPage > 0) {
                    TextButton(
                        onClick = {
                            haptics.light()
                            currentPage--
                        }
                    ) {
                        Text("Back")
                    }
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                if (currentPage < pages.size - 1) {
                    Button(
                        onClick = {
                            haptics.click()
                            currentPage++
                        },
                        shape = PulseWaveShapes.extraLarge,
                        modifier = Modifier
                            .height(56.dp)
                            .widthIn(min = 160.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary
                        )
                    ) {
                        Text("Continue")
                    }
                } else {
                    Button(
                        onClick = {
                            haptics.success()
                            onComplete()
                        },
                        modifier = Modifier
                            .height(56.dp)
                            .fillMaxWidth(),
                        shape = PulseWaveShapes.extraLarge,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Get Started", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingBackdrop(
    modifier: Modifier = Modifier,
    primary: Color,
    secondary: Color,
    tertiary: Color
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val topBlob = Path().apply {
            moveTo(width * 0.1f, 0f)
            quadraticTo(width * 0.45f, height * 0.12f, width * 0.75f, height * 0.02f)
            quadraticTo(width * 0.98f, 0f, width, height * 0.18f)
            lineTo(width, 0f)
            close()
        }
        drawPath(topBlob, color = primary.copy(alpha = 0.08f), style = Fill)

        val midBlob = Path().apply {
            moveTo(0f, height * 0.55f)
            quadraticTo(width * 0.2f, height * 0.46f, width * 0.45f, height * 0.58f)
            quadraticTo(width * 0.7f, height * 0.72f, width, height * 0.62f)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(midBlob, color = secondary.copy(alpha = 0.07f), style = Fill)

        drawCircle(
            color = tertiary.copy(alpha = 0.1f),
            radius = width * 0.28f,
            center = androidx.compose.ui.geometry.Offset(width * 0.85f, height * 0.2f)
        )

        drawCircle(
            color = primary.copy(alpha = 0.08f),
            radius = width * 0.22f,
            center = androidx.compose.ui.geometry.Offset(width * 0.15f, height * 0.75f)
        )

        drawCircle(
            color = secondary.copy(alpha = 0.06f),
            radius = width * 0.18f,
            center = androidx.compose.ui.geometry.Offset(width * 0.85f, height * 0.82f)
        )

        drawCircle(
            color = tertiary.copy(alpha = 0.08f),
            radius = width * 0.16f,
            center = androidx.compose.ui.geometry.Offset(width * 0.4f, height * 0.28f)
        )
    }
}

data class OnboardingPage(
    val illustration: @Composable (Modifier) -> Unit,
    val title: String,
    val description: String
)
