package com.bernaferrari.wifiweakspot.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator

@Composable
internal fun rememberNavigationState(): NavigationState {
    val selected = rememberSerializable { mutableStateOf<TopLevelDestination>(HomeDestination) }
    val stacks = topLevelDestinations.associateWith { root ->
        key(root) { rememberNavBackStack(wifiWeakspotNavConfiguration, root) }
    }
    return remember(selected, stacks) { NavigationState(selected, stacks) }
}

internal class NavigationState(
    selected: MutableState<TopLevelDestination>,
    private val stacks: Map<TopLevelDestination, NavBackStack<NavKey>>,
) {
    var selectedTab by selected
        private set

    private val activeStack get() = stacks.getValue(selectedTab)
    val currentDestination: NavKey get() = activeStack.last()

    fun selectTab(tab: TopLevelDestination) {
        if (tab == selectedTab) {
            // Reselecting the active tab takes the user back to its root.
            activeStack.subList(1, activeStack.size).clear()
        } else {
            selectedTab = tab
        }
    }

    fun open(destination: NavKey) {
        val existing = activeStack.indexOf(destination)
        if (existing < 0) {
            activeStack.add(destination)
        } else {
            activeStack.subList(existing + 1, activeStack.size).clear()
        }
    }

    fun pop(expected: NavKey? = null) {
        if (expected != null && currentDestination != expected) return
        if (activeStack.size > 1) {
            activeStack.removeAt(activeStack.lastIndex)
        } else {
            selectedTab = HomeDestination
        }
    }

    @Composable
    fun entries(entryProvider: (NavKey) -> NavEntry<NavKey>): List<NavEntry<NavKey>> {
        val decorated = stacks.mapValues { (tab, stack) ->
            key(tab) {
                rememberDecoratedNavEntries(
                    backStack = stack,
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                    entryProvider = entryProvider,
                )
            }
        }
        val active = decorated.getValue(selectedTab)
        // NavDisplay can preview Back to Home from the root of another tab.
        return if (selectedTab == HomeDestination) active else decorated.getValue(HomeDestination) + active
    }
}
