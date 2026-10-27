package com.bernaferrari.wifiweakspot.di

import com.bernaferrari.wifiweakspot.core.data.AndroidNetworkStatusRepository
import com.bernaferrari.wifiweakspot.core.datastore.createDataStore
import com.bernaferrari.wifiweakspot.core.database.getDatabaseBuilder
import com.bernaferrari.wifiweakspot.core.database.WifiWeakspotDatabase
import com.bernaferrari.wifiweakspot.core.domain.NetworkStatusRepository
import com.bernaferrari.wifiweakspot.core.export.AndroidExportManager
import com.bernaferrari.wifiweakspot.core.export.ExportManager
import com.bernaferrari.wifiweakspot.core.lan.AndroidLanScanner
import com.bernaferrari.wifiweakspot.core.lan.LanScanner
import com.bernaferrari.wifiweakspot.core.monitoring.BackgroundMonitorManager
import com.bernaferrari.wifiweakspot.core.permissions.AndroidPermissionGate
import com.bernaferrari.wifiweakspot.core.permissions.PermissionGate
import com.bernaferrari.wifiweakspot.core.tools.AndroidDnsClient
import com.bernaferrari.wifiweakspot.core.tools.AndroidPingClient
import com.bernaferrari.wifiweakspot.core.tools.AndroidPortScanner
import com.bernaferrari.wifiweakspot.core.tools.DnsClient
import com.bernaferrari.wifiweakspot.core.tools.PingClient
import com.bernaferrari.wifiweakspot.core.tools.PortScanner
import com.bernaferrari.wifiweakspot.core.wifi.AndroidScanThrottleManager
import com.bernaferrari.wifiweakspot.core.wifi.AndroidWifiScanner
import com.bernaferrari.wifiweakspot.core.wifi.ScanThrottleManager
import com.bernaferrari.wifiweakspot.core.wifi.WifiScanner
import com.bernaferrari.wifiweakspot.core.router.AndroidRouterProbe
import com.bernaferrari.wifiweakspot.core.router.RouterProbe
import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlinx.coroutines.launch

val appModule = module {
    // DataStore & Settings
    single { createDataStore(androidContext()) }

    // Database
    single<WifiWeakspotDatabase> {
        getDatabaseBuilder(androidContext())
            .setDriver(androidx.sqlite.driver.bundled.BundledSQLiteDriver())
            .build()
    }
    single { get<WifiWeakspotDatabase>().dao() }
    single<com.bernaferrari.wifiweakspot.core.backup.BackupService> {
        com.bernaferrari.wifiweakspot.core.backup.AndroidBackupService(androidContext(), get(), get())
    }

    // WiFi
    single<com.bernaferrari.wifiweakspot.core.root.RootAccess> { com.bernaferrari.wifiweakspot.core.root.AndroidRootAccess(androidContext()) }
    single { AndroidWifiScanner(androidContext(), get(), get<ScanThrottleManager>()::recordScan) } bind WifiScanner::class
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
    single { BackgroundMonitorManager(androidContext()) } bind com.bernaferrari.wifiweakspot.core.monitoring.BackgroundMonitor::class

    // Platform hooks for the shared UI
    single<com.bernaferrari.wifiweakspot.core.platform.PlatformActions> { com.bernaferrari.wifiweakspot.core.platform.AndroidPlatformActions(androidContext()) }
    single<com.bernaferrari.wifiweakspot.core.platform.PlatformTheme> { com.bernaferrari.wifiweakspot.core.designsystem.AndroidPlatformTheme() }
    single<com.bernaferrari.wifiweakspot.core.wifi.RadioMonitor> { com.bernaferrari.wifiweakspot.core.wifi.AndroidRadioMonitor(androidContext()) }
}

fun initKoin(application: Application) {
    val koin = startKoin {
        androidContext(application)
        modules(sharedModule, appModule)
    }.koin
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO).launch {
        // Resume the durable Room -> DataStore handoff after an interrupted restore.
        try {
            (koin.get<com.bernaferrari.wifiweakspot.core.backup.BackupService>() as
                com.bernaferrari.wifiweakspot.core.backup.AndroidBackupService).recoverPendingImports()
        } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
        catch (_: Exception) { android.util.Log.w("Backup", "Pending restore will be retried when storage is available") }
    }
}
