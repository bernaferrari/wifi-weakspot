package com.pulsewave.demo

import com.pulsewave.app.core.database.dao.PulseWaveDao
import com.pulsewave.app.core.domain.NetworkStatusRepository
import com.pulsewave.app.core.export.ExportManager
import com.pulsewave.app.core.lan.LanScanner
import com.pulsewave.app.core.monitoring.BackgroundMonitor
import com.pulsewave.app.core.permissions.PermissionGate
import com.pulsewave.app.core.platform.PlatformActions
import com.pulsewave.app.core.platform.PlatformTheme
import com.pulsewave.app.core.root.RootAccess
import com.pulsewave.app.core.router.RouterProbe
import com.pulsewave.app.core.tools.DnsClient
import com.pulsewave.app.core.tools.PingClient
import com.pulsewave.app.core.tools.PortScanner
import com.pulsewave.app.core.wifi.RadioMonitor
import com.pulsewave.app.core.wifi.ScanThrottleManager
import com.pulsewave.app.core.wifi.WifiScanner
import org.koin.dsl.bind
import org.koin.dsl.module

/** Browser stand-ins for the phone's services; screens and view models are the app's own. */
val webModule = module {
    single { InMemoryPreferences() } bind androidx.datastore.core.DataStore::class
    single<PulseWaveDao> { InMemoryDao() }

    single<RootAccess> { NoRootAccess() }
    single<WifiScanner> { SimulatedWifiScanner() }
    single<ScanThrottleManager> { SimulatedScanThrottle() }
    single<NetworkStatusRepository> { SimulatedNetworkStatus() }
    single { SimulatedPermissionGate() } bind PermissionGate::class
    single<RadioMonitor> { SimulatedRadios() }

    factory<PingClient> { SimulatedPingClient() }
    factory<DnsClient> { SimulatedDnsClient() }
    factory<PortScanner> { SimulatedPortScanner() }
    factory<LanScanner> { SimulatedLanScanner() }
    factory<RouterProbe> { SimulatedRouterProbe() }

    single<ExportManager> { ClipboardExportManager() }
    single<BackgroundMonitor> { SimulatedBackgroundMonitor() }

    single<PlatformActions> { WebPlatformActions() }
    single<PlatformTheme> { WebPlatformTheme() }
}
