package com.pulsewave.app.core.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    // Top-level destinations
    @Serializable data object Onboarding : Route
    @Serializable data object Overview : Route
    @Serializable data object Wifi : Route
    @Serializable data object Lan : Route
    @Serializable data object Tools : Route
    @Serializable data object SpeedTest : Route
    @Serializable data object Survey : Route
    @Serializable data object History : Route
    @Serializable data object Settings : Route

    // Detail destinations
    @Serializable data class WifiDetail(val bssid: String) : Route
    @Serializable data class LanDeviceDetail(val ip: String) : Route
    @Serializable data class ToolRun(val toolType: String, val target: String) : Route
    @Serializable data class ToolResult(val runId: Long) : Route
    @Serializable data class SurveySetup(val projectId: Long? = null) : Route
    @Serializable data class SurveyRun(val sessionId: Long) : Route
    @Serializable data class SurveyResult(val sessionId: Long) : Route
    @Serializable data class HistorySession(val sessionId: Long) : Route
    
    // Settings sub-destinations
    @Serializable data object PowerTools : Route
    @Serializable data object About : Route
}
