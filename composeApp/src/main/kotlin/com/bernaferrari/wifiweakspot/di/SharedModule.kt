package com.bernaferrari.wifiweakspot.di

import com.bernaferrari.wifiweakspot.core.datastore.SettingsRepository
import com.bernaferrari.wifiweakspot.core.rooms.RoomCheckRepository
import com.bernaferrari.wifiweakspot.feature.analyze.AnalyzeViewModel
import com.bernaferrari.wifiweakspot.feature.lan.LanDeviceDetailViewModel
import com.bernaferrari.wifiweakspot.feature.lan.LanViewModel
import com.bernaferrari.wifiweakspot.feature.linktest.LinkTestViewModel
import com.bernaferrari.wifiweakspot.feature.overview.OverviewViewModel
import com.bernaferrari.wifiweakspot.feature.survey.SurveyViewModel
import com.bernaferrari.wifiweakspot.feature.tools.ToolsViewModel
import com.bernaferrari.wifiweakspot.feature.walk.WalkViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Screens, view models, and repositories shared by every platform. Each platform adds a
 * module with its device services (Wi-Fi scanner, storage, sensors, [com.bernaferrari.wifiweakspot.core.platform.PlatformActions]).
 */
val sharedModule = module {
    factory { com.bernaferrari.wifiweakspot.feature.settings.BackupViewModel(get()) }
    factoryOf(::AnalyzeViewModel)
    factoryOf(::ToolsViewModel)
    factoryOf(::LanViewModel)
    factoryOf(::LinkTestViewModel)
    factoryOf(::SurveyViewModel)
    factoryOf(::OverviewViewModel)
    factoryOf(::LanDeviceDetailViewModel)
    factory { WalkViewModel(get(), get()) }

    single { SettingsRepository(get()) }
    single { RoomCheckRepository(get()) }
}
