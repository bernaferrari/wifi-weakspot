package com.bernaferrari.wifiweakspot.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
internal sealed interface TopLevelDestination : NavKey

@Serializable internal data object HomeDestination : TopLevelDestination
@Serializable internal data object NetworksDestination : TopLevelDestination
@Serializable internal data object WalkDestination : TopLevelDestination
@Serializable internal data object SettingsDestination : TopLevelDestination

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

internal val topLevelDestinations = listOf<TopLevelDestination>(HomeDestination, NetworksDestination, WalkDestination, SettingsDestination)

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
