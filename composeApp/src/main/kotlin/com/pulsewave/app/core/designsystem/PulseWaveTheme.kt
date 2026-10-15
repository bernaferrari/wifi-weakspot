@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.pulsewave.app.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.material3.rememberDynamicMaterialThemeState
import com.pulsewave.app.core.datastore.SettingsRepository
import com.pulsewave.app.core.platform.PlatformTheme
import org.koin.compose.koinInject

@Composable
fun PulseWaveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val settingsRepository: SettingsRepository = koinInject()
    val platform: PlatformTheme = koinInject()
    val darkPreference by settingsRepository.isDarkMode.collectAsState(initial = null)
    val reduceMotionSetting by settingsRepository.reduceMotion.collectAsState(initial = false)
    val dynamicColor by settingsRepository.dynamicColor.collectAsState(initial = true)
    val seedColorInt by settingsRepository.seedColor.collectAsState(initial = 0xFF3F51B5.toInt())
    val effectiveDark = darkPreference ?: darkTheme
    val reduceMotion = reduceMotionSetting || platform.systemReducedMotion()
    platform.ApplySystemBars(effectiveDark)

    val colorScheme = (if (dynamicColor) platform.dynamicColorScheme(effectiveDark) else null)
        ?: rememberDynamicMaterialThemeState(
            seedColor = Color(seedColorInt),
            isDark = effectiveDark,
            style = PaletteStyle.Vibrant
        ).colorScheme

    CompositionLocalProvider(LocalReduceMotion provides reduceMotion) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            motionScheme = if (reduceMotion) NoMotionScheme else MotionScheme.expressive(),
            shapes = Shapes(),
            typography = PulseWaveTypography,
            content = content
        )
    }
}
