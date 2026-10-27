package com.bernaferrari.wifiweakspot.demo

import com.bernaferrari.wifiweakspot.core.export.ExportManager
import com.bernaferrari.wifiweakspot.core.platform.PlatformActions
import com.bernaferrari.wifiweakspot.core.platform.PlatformTheme
import org.koin.dsl.module

val webModule = module {
    includes(simulationModule)
    single<ExportManager> { ClipboardExportManager() }
    single<PlatformActions> { WebPlatformActions() }
    single<PlatformTheme> { WebPlatformTheme() }
}
