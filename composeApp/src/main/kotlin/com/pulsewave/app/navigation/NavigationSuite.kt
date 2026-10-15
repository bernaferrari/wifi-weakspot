@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.pulsewave.app.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.pulsewave.app.core.designsystem.WindowWidth
import com.pulsewave.app.core.designsystem.rememberPulseWaveHapticFeedback

private class TabItem(val destination: NavKey, val label: String, val icon: ImageVector, val selectedIcon: ImageVector)

private val Tabs = listOf(
    TabItem(HomeDestination, "Home", Icons.Outlined.Home, Icons.Filled.Home),
    TabItem(NetworksDestination, "Networks", com.pulsewave.app.core.designsystem.component.NetworksIcons.Outlined, com.pulsewave.app.core.designsystem.component.NetworksIcons.Filled),
    TabItem(WalkDestination, "Walk", Icons.AutoMirrored.Outlined.DirectionsWalk, Icons.AutoMirrored.Filled.DirectionsWalk),
    TabItem(SettingsDestination, "Settings", Icons.Outlined.Settings, Icons.Filled.Settings)
)

/**
 * A bottom bar on phones and a rail on wide windows, shown only on the tabs: pushed screens get the
 * whole canvas. The content keeps one place in the tree whether or not the bar shows, so screens
 * keep their state and pushes animate instead of being rebuilt.
 */
@Composable
fun PulseWaveNavigationSuite(backStack: NavBackStack<NavKey>, content: @Composable () -> Unit) {
    val haptics = rememberPulseWaveHapticFeedback()
    val current = backStack.lastOrNull { it.isRootTab() } ?: HomeDestination
    val showBar = backStack.lastOrNull()?.isRootTab() == true
    val select: (NavKey) -> Unit = { tab ->
        if (tab != current) {
            haptics.click()
            backStack.selectTab(tab)
        }
    }
    val latest by rememberUpdatedState(content)
    val movable = remember { movableContentOf { latest() } }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth < WindowWidth.Medium) {
            Scaffold(
                contentWindowInsets = WindowInsets(0),
                bottomBar = {
                    AnimatedVisibility(
                        visible = showBar,
                        enter = expandVertically(expandFrom = Alignment.Top),
                        exit = shrinkVertically(shrinkTowards = Alignment.Top)
                    ) {
                        ShortNavigationBar {
                            Tabs.forEach { tab ->
                                val selected = tab.destination == current
                                ShortNavigationBarItem(
                                    selected = selected,
                                    onClick = { select(tab.destination) },
                                    icon = { Icon(if (selected) tab.selectedIcon else tab.icon, null) },
                                    label = { Text(tab.label) }
                                )
                            }
                        }
                    }
                }
            ) { padding ->
                // The bar already covers the gesture area; screens mustn't pad for it again.
                Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) { movable() }
            }
        } else {
            Row(Modifier.fillMaxSize()) {
                AnimatedVisibility(visible = showBar) {
                    WideNavigationRail {
                        Tabs.forEach { tab ->
                            val selected = tab.destination == current
                            WideNavigationRailItem(
                                selected = selected,
                                onClick = { select(tab.destination) },
                                icon = { Icon(if (selected) tab.selectedIcon else tab.icon, null) },
                                label = { Text(tab.label) },
                                railExpanded = false
                            )
                        }
                    }
                }
                Box(Modifier.weight(1f).fillMaxHeight()) { movable() }
            }
        }
    }
}
