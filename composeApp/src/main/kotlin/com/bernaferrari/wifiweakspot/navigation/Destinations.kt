package com.bernaferrari.wifiweakspot.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

// Tabs: each replaces the root of the back stack.
@Serializable data object HomeDestination : NavKey
@Serializable data object NetworksDestination : NavKey
@Serializable data object WalkDestination : NavKey
@Serializable data object SettingsDestination : NavKey

// Full screens pushed over the tabs.
@Serializable data object DevicesDestination : NavKey
@Serializable data class DeviceDestination(val ip: String) : NavKey
@Serializable data class ToolsDestination(val target: String = "", val tool: String = "") : NavKey
@Serializable data object SpeedTestDestination : NavKey
@Serializable data object InternetSpeedDestination : NavKey
@Serializable data object HistoryDestination : NavKey
@Serializable data object FloorPlanDestination : NavKey
@Serializable data object AboutDestination : NavKey
@Serializable data object ScanSetupDestination : NavKey

fun NavKey.isRootTab(): Boolean =
    this is HomeDestination || this is NetworksDestination || this is WalkDestination || this is SettingsDestination

/** Switching tabs replaces the root and drops anything pushed above it. */
fun NavBackStack<NavKey>.selectTab(tab: NavKey) {
    val index = indexOfFirst { it.isRootTab() }
    if (index < 0) { add(tab); return }
    this[index] = tab
    while (size > index + 1) removeAt(lastIndex)
}

/** Opens [destination], replacing it if it's already on top (a double tap never stacks copies). */
fun NavBackStack<NavKey>.open(destination: NavKey) {
    if (lastOrNull() == destination) return
    add(destination)
}

/**
 * Closes [screen] if it's still on top (taps during the closing animation do nothing) and never
 * removes the last entry: an empty back stack crashes navigation.
 */
fun NavBackStack<NavKey>.pop(screen: NavKey? = null) {
    if (size <= 1) return
    if (screen != null && lastOrNull() != screen) return
    removeAt(lastIndex)
}

/** Lets Navigation 3 save and restore the whole back stack. */
val wifiWeakspotNavConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(HomeDestination::class, HomeDestination.serializer())
            subclass(NetworksDestination::class, NetworksDestination.serializer())
            subclass(WalkDestination::class, WalkDestination.serializer())
            subclass(SettingsDestination::class, SettingsDestination.serializer())
            subclass(DevicesDestination::class, DevicesDestination.serializer())
            subclass(DeviceDestination::class, DeviceDestination.serializer())
            subclass(ToolsDestination::class, ToolsDestination.serializer())
            subclass(SpeedTestDestination::class, SpeedTestDestination.serializer())
            subclass(InternetSpeedDestination::class, InternetSpeedDestination.serializer())
            subclass(HistoryDestination::class, HistoryDestination.serializer())
            subclass(FloorPlanDestination::class, FloorPlanDestination.serializer())
            subclass(AboutDestination::class, AboutDestination.serializer())
            subclass(ScanSetupDestination::class, ScanSetupDestination.serializer())
        }
    }
}
