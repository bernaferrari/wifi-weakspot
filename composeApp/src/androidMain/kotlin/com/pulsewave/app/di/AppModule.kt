package com.pulsewave.app.di

import com.pulsewave.app.core.data.AndroidNetworkStatusRepository
import com.pulsewave.app.core.datastore.createDataStore
import com.pulsewave.app.core.database.getDatabaseBuilder
import com.pulsewave.app.core.database.PulseWaveDatabase
import com.pulsewave.app.core.domain.NetworkStatusRepository
import com.pulsewave.app.core.export.AndroidExportManager
import com.pulsewave.app.core.export.ExportManager
import com.pulsewave.app.core.lan.AndroidLanScanner
import com.pulsewave.app.core.lan.LanScanner
import com.pulsewave.app.core.monitoring.BackgroundMonitorManager
import com.pulsewave.app.core.permissions.AndroidPermissionGate
import com.pulsewave.app.core.permissions.PermissionGate
import com.pulsewave.app.core.tools.AndroidDnsClient
import com.pulsewave.app.core.tools.AndroidPingClient
import com.pulsewave.app.core.tools.AndroidPortScanner
import com.pulsewave.app.core.tools.DnsClient
import com.pulsewave.app.core.tools.PingClient
import com.pulsewave.app.core.tools.PortScanner
import com.pulsewave.app.core.wifi.AndroidScanThrottleManager
import com.pulsewave.app.core.wifi.AndroidWifiScanner
import com.pulsewave.app.core.wifi.ScanThrottleManager
import com.pulsewave.app.core.wifi.WifiScanner
import com.pulsewave.app.core.router.AndroidRouterProbe
import com.pulsewave.app.core.router.RouterProbe
import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    // DataStore & Settings
    single { createDataStore(androidContext()) }

    // Database
    single<PulseWaveDatabase> {
        getDatabaseBuilder(androidContext())
            .setDriver(androidx.sqlite.driver.bundled.BundledSQLiteDriver())
            .build()
    }
    single { get<PulseWaveDatabase>().dao() }

    // WiFi
    single<com.pulsewave.app.core.root.RootAccess> { com.pulsewave.app.core.root.AndroidRootAccess(androidContext()) }
    single { AndroidWifiScanner(androidContext(), get()) } bind WifiScanner::class
    single { AndroidScanThrottleManager(androidContext(), get()) } bind ScanThrottleManager::class

    // Network Tools
    factory { AndroidPingClient() } bind PingClient::class
    factory { AndroidDnsClient() } bind DnsClient::class
    factory { AndroidPortScanner() } bind PortScanner::class
    factory { AndroidLanScanner(androidContext()) } bind LanScanner::class
    factory { AndroidRouterProbe(androidContext()) } bind RouterProbe::class

    // Platform Services
    single { AndroidPermissionGate(androidContext()) } bind PermissionGate::class
    single { AndroidExportManager(androidContext()) } bind ExportManager::class
    single<NetworkStatusRepository> { AndroidNetworkStatusRepository(androidContext()) }

    // Background Monitoring
    single { BackgroundMonitorManager(androidContext()) } bind com.pulsewave.app.core.monitoring.BackgroundMonitor::class

    // Platform hooks for the shared UI
    single<com.pulsewave.app.core.platform.PlatformActions> { com.pulsewave.app.core.platform.AndroidPlatformActions(androidContext()) }
    single<com.pulsewave.app.core.platform.PlatformTheme> { com.pulsewave.app.core.designsystem.AndroidPlatformTheme() }
    single<com.pulsewave.app.core.wifi.RadioMonitor> { com.pulsewave.app.core.wifi.AndroidRadioMonitor(androidContext()) }
}

fun initKoin(application: Application) {
    startKoin {
        androidContext(application)
        modules(sharedModule, appModule)
    }
}
