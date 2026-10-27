package com.bernaferrari.wifiweakspot.android

import android.app.Application
import com.bernaferrari.wifiweakspot.core.datastore.SettingsRepository
import com.bernaferrari.wifiweakspot.demo.SimulatedHome
import kotlinx.coroutines.runBlocking
import com.bernaferrari.wifiweakspot.core.rooms.RoomCheckRepository
import com.bernaferrari.wifiweakspot.core.rooms.RoomReading
import com.bernaferrari.wifiweakspot.core.util.nowMillis
import com.bernaferrari.wifiweakspot.demo.SimulatedPermissionGate
import com.bernaferrari.wifiweakspot.demo.simulationModule
import com.bernaferrari.wifiweakspot.di.appModule
import com.bernaferrari.wifiweakspot.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/** Separate application ID and volatile fixtures: never reads or scans the user's networks. */
class ScreenshotApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SimulatedHome.stationary = true
        val koin = startKoin {
            androidContext(this@ScreenshotApplication)
            modules(sharedModule, appModule, simulationModule)
        }.koin
        koin.get<SimulatedPermissionGate>().grant()
        runBlocking {
            koin.get<RoomCheckRepository>().saveAll(
                listOf("Living room" to -58, "Kitchen" to -68, "Bedroom" to -82).mapIndexed { index, (name, rssi) ->
                    RoomReading("sample-$index", name, rssi, rssi - 3,
                        SimulatedHome.linkSpeedMbps(rssi), SimulatedHome.Frequency,
                        SimulatedHome.Ssid, SimulatedHome.Bssid, 8, nowMillis() - index * 60_000,
                        routerMs = listOf(3.8f, 7.2f, 23.5f)[index])
                }
            )
            koin.get<SettingsRepository>().apply {
                setOnboardingDone()
                setDarkMode(true)
                setDynamicColor(false)
                setReduceMotion(true)
            }
        }
    }
}
