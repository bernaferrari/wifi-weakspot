package com.pulsewave.app.di

import com.pulsewave.app.core.datastore.SettingsRepository
import com.pulsewave.app.core.rooms.RoomCheckRepository
import com.pulsewave.app.feature.analyze.AnalyzeViewModel
import com.pulsewave.app.feature.lan.LanDeviceDetailViewModel
import com.pulsewave.app.feature.lan.LanViewModel
import com.pulsewave.app.feature.linktest.LinkTestViewModel
import com.pulsewave.app.feature.overview.OverviewViewModel
import com.pulsewave.app.feature.survey.SurveyViewModel
import com.pulsewave.app.feature.tools.ToolsViewModel
import com.pulsewave.app.feature.walk.WalkViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Screens, view models, and repositories shared by every platform. Each platform adds a
 * module with its device services (Wi-Fi scanner, storage, sensors, [com.pulsewave.app.core.platform.PlatformActions]).
 */
val sharedModule = module {
    factoryOf(::AnalyzeViewModel)
    factoryOf(::ToolsViewModel)
    factoryOf(::LanViewModel)
    factoryOf(::LinkTestViewModel)
    factoryOf(::SurveyViewModel)
    factoryOf(::OverviewViewModel)
    factoryOf(::LanDeviceDetailViewModel)
    factoryOf(::WalkViewModel)

    single { SettingsRepository(get()) }
    single { RoomCheckRepository(get()) }
}
