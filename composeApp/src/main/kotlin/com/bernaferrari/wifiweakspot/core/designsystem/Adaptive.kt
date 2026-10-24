@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.wifiweakspot.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class NavTab(val label: String, val icon: ImageVector, val selectedIcon: ImageVector)

/** Window widths from Material's size classes. */
object WindowWidth {
    val Medium = 600.dp
    val Expanded = 840.dp
}

/**
 * Bottom navigation bar on phones; a navigation rail beside the content on tablets,
 * foldables, and desktop windows, where a bar would stretch awkwardly.
 */
@Composable
fun AdaptiveNavigation(
    tabs: List<NavTab>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        if (maxWidth >= WindowWidth.Medium) {
            Row(Modifier.fillMaxSize()) {
                WideNavigationRail {
                    tabs.forEachIndexed { index, tab ->
                        WideNavigationRailItem(
                            selected = index == selected,
                            onClick = { onSelect(index) },
                            icon = { Icon(if (index == selected) tab.selectedIcon else tab.icon, contentDescription = null) },
                            label = { Text(tab.label) },
                            railExpanded = false
                        )
                    }
                }
                Box(Modifier.weight(1f).fillMaxHeight()) { content() }
            }
        } else {
            Scaffold(
                contentWindowInsets = WindowInsets(0),
                bottomBar = {
                    ShortNavigationBar {
                        tabs.forEachIndexed { index, tab ->
                            ShortNavigationBarItem(
                                selected = index == selected,
                                onClick = { onSelect(index) },
                                icon = { Icon(if (index == selected) tab.selectedIcon else tab.icon, contentDescription = null) },
                                label = { Text(tab.label) }
                            )
                        }
                    }
                }
            ) { padding ->
                Box(Modifier.padding(padding).consumeWindowInsets(padding).fillMaxSize()) { content() }
            }
        }
    }
}

/** Keeps single-column screens at a comfortable reading width on large windows. */
@Composable
fun ReadableWidth(maxWidth: Dp = 720.dp, content: @Composable () -> Unit) {
    // Opaque, so the screen underneath never shows through the margins mid-transition.
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.TopCenter) {
        Box(Modifier.widthIn(max = maxWidth).fillMaxHeight()) { content() }
    }
}

/**
 * One scrolling column on phones; two side-by-side columns on wide windows, so
 * dashboards use the space instead of stretching cards edge to edge.
 */
@Composable
fun AdaptiveColumns(
    contentPadding: androidx.compose.foundation.layout.PaddingValues,
    modifier: Modifier = Modifier,
    primary: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
    secondary: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    val spacing = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    BoxWithConstraints(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        if (maxWidth >= WindowWidth.Expanded) {
            Row(
                Modifier.widthIn(max = 1240.dp).fillMaxSize(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                androidx.compose.foundation.lazy.LazyColumn(Modifier.weight(1f), contentPadding = contentPadding, verticalArrangement = spacing, content = primary)
                androidx.compose.foundation.lazy.LazyColumn(Modifier.weight(1f), contentPadding = contentPadding, verticalArrangement = spacing, content = secondary)
            }
        } else {
            androidx.compose.foundation.lazy.LazyColumn(Modifier.fillMaxSize(), contentPadding = contentPadding, verticalArrangement = spacing) {
                primary()
                secondary()
            }
        }
    }
}
