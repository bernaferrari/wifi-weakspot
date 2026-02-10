package com.pulsewave.app.di

import com.pulsewave.app.core.data.AndroidNetworkStatusRepository
import com.pulsewave.app.core.datastore.createDataStore
import com.pulsewave.app.core.datastore.SettingsRepository
import com.pulsewave.app.core.database.getDatabaseBuilder
import com.pulsewave.app.core.database.PulseWaveDatabase
import com.pulsewave.app.core.domain.NetworkStatusRepository
import com.pulsewave.app.core.export.AndroidExportManager
import com.pulsewave.app.core.export.ExportManager
import com.pulsewave.app.core.lan.AndroidLanScanner
import com.pulsewave.app.core.lan.LanScanner
import com.pulsewave.app.core.permissions.AndroidPermissionGate
import com.pulsewave.app.core.permissions.PermissionGate
import com.pulsewave.app.core.sensors.AndroidOrientationProvider
import com.pulsewave.app.core.sensors.AndroidStepCounter
import com.pulsewave.app.core.sensors.OrientationProvider
import com.pulsewave.app.core.sensors.StepCounter
import com.pulsewave.app.core.speedtest.AndroidSpeedTestClient
import com.pulsewave.app.core.speedtest.SpeedTestClient
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
import com.pulsewave.app.feature.analyze.AnalyzeViewModel
import com.pulsewave.app.feature.lan.LanDeviceDetailViewModel
import com.pulsewave.app.feature.lan.LanViewModel
import com.pulsewave.app.feature.overview.OverviewViewModel
import com.pulsewave.app.feature.speedtest.SpeedTestViewModel
import com.pulsewave.app.feature.survey.SurveyViewModel
import com.pulsewave.app.feature.tools.ToolsViewModel
import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    // ViewModels
    factoryOf(::AnalyzeViewModel)
    factoryOf(::ToolsViewModel)
    factoryOf(::LanViewModel)
    factoryOf(::SpeedTestViewModel)
    factoryOf(::SurveyViewModel)
    factoryOf(::OverviewViewModel)
    factoryOf(::LanDeviceDetailViewModel)

    // DataStore & Settings
    single { createDataStore(androidContext()) }
    single { SettingsRepository(get()) }

    // Database
    single<PulseWaveDatabase> {
        getDatabaseBuilder(androidContext())
            .setDriver(androidx.sqlite.driver.bundled.BundledSQLiteDriver())
            .build()
    }
    single { get<PulseWaveDatabase>().dao() }

    // WiFi
    single { AndroidWifiScanner(androidContext()) } bind WifiScanner::class
    single { AndroidScanThrottleManager(androidContext()) } bind ScanThrottleManager::class

    // Network Tools
    factory { AndroidPingClient() } bind PingClient::class
    factory { AndroidDnsClient() } bind DnsClient::class
    factory { AndroidPortScanner() } bind PortScanner::class
    factory { AndroidLanScanner() } bind LanScanner::class
    factory { AndroidSpeedTestClient() } bind SpeedTestClient::class

    // Platform Services
    single { AndroidPermissionGate(androidContext()) } bind PermissionGate::class
    single { AndroidExportManager(androidContext()) } bind ExportManager::class
    single<NetworkStatusRepository> { AndroidNetworkStatusRepository(androidContext()) }
    single<StepCounter> { AndroidStepCounter(androidContext()) }
    single<OrientationProvider> { AndroidOrientationProvider(androidContext()) }
}

fun initKoin(application: Application) {
    startKoin {
        androidContext(application)
        modules(appModule)
    }
}
