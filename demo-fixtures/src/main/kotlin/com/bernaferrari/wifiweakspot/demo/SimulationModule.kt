package com.bernaferrari.wifiweakspot.demo

import com.bernaferrari.wifiweakspot.core.database.dao.WifiWeakspotDao
import com.bernaferrari.wifiweakspot.core.domain.NetworkStatusRepository
import com.bernaferrari.wifiweakspot.core.lan.LanScanner
import com.bernaferrari.wifiweakspot.core.monitoring.BackgroundMonitor
import com.bernaferrari.wifiweakspot.core.permissions.PermissionGate
import com.bernaferrari.wifiweakspot.core.root.RootAccess
import com.bernaferrari.wifiweakspot.core.router.RouterProbe
import com.bernaferrari.wifiweakspot.core.tools.DnsClient
import com.bernaferrari.wifiweakspot.core.tools.PingClient
import com.bernaferrari.wifiweakspot.core.tools.PortScanner
import com.bernaferrari.wifiweakspot.core.wifi.RadioMonitor
import com.bernaferrari.wifiweakspot.core.wifi.ScanThrottleManager
import com.bernaferrari.wifiweakspot.core.wifi.WifiScanner
import org.koin.dsl.bind
import org.koin.dsl.module

/** Shared sample radios and in-memory data for browser and Android demo hosts. */
val simulationModule = module {
    single<com.bernaferrari.wifiweakspot.core.backup.BackupService> { com.bernaferrari.wifiweakspot.core.backup.NoBackupService }
    single { InMemoryPreferences() } bind androidx.datastore.core.DataStore::class
    single<WifiWeakspotDao> { InMemoryDao() }

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

    single<BackgroundMonitor> { SimulatedBackgroundMonitor() }
}
