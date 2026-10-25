@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.wifiweakspot.navigation

import com.bernaferrari.wifiweakspot.core.i18n.CommonStrings
import com.bernaferrari.wifiweakspot.core.i18n.strings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.outlined.MeetingRoom
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
import com.bernaferrari.wifiweakspot.core.designsystem.WindowWidth
import com.bernaferrari.wifiweakspot.core.designsystem.rememberWifiWeakspotHapticFeedback

private class TabItem(val destination: NavKey, val label: (CommonStrings) -> String, val icon: ImageVector, val selectedIcon: ImageVector)

private val Tabs = listOf(
    TabItem(HomeDestination, { it.tabHome }, Icons.Outlined.Home, Icons.Filled.Home),
    TabItem(NetworksDestination, { it.tabNetworks }, com.bernaferrari.wifiweakspot.core.designsystem.component.NetworksIcons.Outlined, com.bernaferrari.wifiweakspot.core.designsystem.component.NetworksIcons.Filled),
    TabItem(WalkDestination, { it.tabRooms }, Icons.Outlined.MeetingRoom, Icons.Filled.MeetingRoom),
    TabItem(SettingsDestination, { it.tabSettings }, Icons.Outlined.Settings, Icons.Filled.Settings)
)

/**
 * A bottom bar on phones and a rail on wide windows. Screens opened from a tab keep the bar, so
 * they read as part of that tab; immersive ones (the floor plan, scan setup) get the whole canvas. The content keeps one place in the tree whether or not the bar shows, so screens
 * keep their state and pushes animate instead of being rebuilt.
 */
@Composable
fun WifiWeakspotNavigationSuite(backStack: NavBackStack<NavKey>, content: @Composable () -> Unit) {
    val haptics = rememberWifiWeakspotHapticFeedback()
    val current = backStack.lastOrNull { it.isRootTab() } ?: HomeDestination
    // Tools opened from a tab stay inside it, so the bar stays; only immersive screens hide it.
    val showBar = backStack.lastOrNull().let { it !is FloorPlanDestination && it !is ScanSetupDestination }
    val select: (NavKey) -> Unit = { tab ->
        // Tapping the current tab from one of its screens goes back to the tab's start.
        if (tab != current || backStack.lastOrNull() != current) {
            haptics.click()
            backStack.selectTab(tab)
        }
    }
    val latest by rememberUpdatedState(content)
    val movable = remember { movableContentOf { latest() } }

    // Fade-through clears both screens briefly; the backdrop must stay opaque in either layout.
    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
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
                                    label = { Text(tab.label(strings.common)) }
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
                                label = { Text(tab.label(strings.common)) },
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
