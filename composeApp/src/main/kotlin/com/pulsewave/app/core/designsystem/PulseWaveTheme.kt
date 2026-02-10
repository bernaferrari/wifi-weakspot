@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.pulsewave.app.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicMaterialThemeState
import com.pulsewave.app.core.datastore.SettingsRepository
import org.koin.compose.koinInject

// Default Seed: Indigo
private val DefaultSeed = Color(0xFF3F51B5)

@Composable
fun PulseWaveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val settingsRepository: SettingsRepository = koinInject()
    val dynamicColor by settingsRepository.dynamicColor.collectAsState(initial = true)
    val seedColorInt by settingsRepository.seedColor.collectAsState(initial = DefaultSeed.value.toInt())
    
    val seedColor = Color(seedColorInt)
    
    // Try to get system scheme if dynamic color is enabled
    val systemScheme = if (dynamicColor) getSystemColorScheme(darkTheme) else null
    
    val colorScheme = systemScheme ?: rememberDynamicMaterialThemeState(
        seedColor = seedColor,
        isDark = darkTheme,
        style = PaletteStyle.Vibrant,
    ).colorScheme

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = androidx.compose.material3.MotionScheme.expressive(),
        shapes = PulseWaveMaterialShapes,
        typography = PulseWaveTypography,
        content = content
    )
}
