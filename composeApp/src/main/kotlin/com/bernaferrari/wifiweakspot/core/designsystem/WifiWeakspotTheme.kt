@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.wifiweakspot.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.materialkolor.material3.rememberDynamicMaterialThemeState
import com.bernaferrari.wifiweakspot.core.datastore.SettingsRepository
import com.bernaferrari.wifiweakspot.core.platform.PlatformTheme
import org.koin.compose.koinInject

/** Preferences already loaded by the app-level theme; screens must not restart at defaults. */
@Immutable
internal data class ThemeSettings(
    val darkMode: Boolean? = null,
    val dynamicColor: Boolean = true,
    val seedColor: Int = 0xFF3F51B5.toInt(),
    val reduceMotion: Boolean = false,
    val colorStyle: ColorStyle = ColorStyle.Expressive,
    val effectiveDark: Boolean = false,
    val resolvedSeed: Color = Color(seedColor)
)

internal val LocalThemeSettings = staticCompositionLocalOf { ThemeSettings() }

@Composable
fun WifiWeakspotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val settingsRepository: SettingsRepository = koinInject()
    val platform: PlatformTheme = koinInject()
    val darkPreference by settingsRepository.isDarkMode.collectAsState(initial = null)
    val reduceMotionSetting by settingsRepository.reduceMotion.collectAsState(initial = false)
    val dynamicColor by settingsRepository.dynamicColor.collectAsState(initial = true)
    val seedColorInt by settingsRepository.seedColor.collectAsState(initial = 0xFF3F51B5.toInt())
    val colorStyle by settingsRepository.colorStyle.collectAsState(initial = ColorStyle.Expressive)
    val effectiveDark = darkPreference ?: darkTheme
    val reduceMotion = reduceMotionSetting || platform.systemReducedMotion()
    platform.ApplySystemBars(effectiveDark)

    // Wallpaper remains a color source; the selected style also applies to that source.
    val wallpaperScheme = if (dynamicColor) platform.dynamicColorScheme(effectiveDark) else null
    val resolvedSeed = wallpaperScheme?.primary ?: Color(seedColorInt)
    val colorScheme = rememberDynamicMaterialThemeState(
        seedColor = resolvedSeed,
        isDark = effectiveDark,
        style = colorStyle.palette
    ).colorScheme

    CompositionLocalProvider(
        LocalThemeSettings provides ThemeSettings(
            darkMode = darkPreference, dynamicColor = dynamicColor, seedColor = seedColorInt,
            reduceMotion = reduceMotionSetting, colorStyle = colorStyle,
            effectiveDark = effectiveDark, resolvedSeed = resolvedSeed
        ),
        LocalReduceMotion provides reduceMotion
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            motionScheme = if (reduceMotion) NoMotionScheme else MotionScheme.expressive(),
            shapes = Shapes(),
            typography = WifiWeakspotTypography,
            content = content
        )
    }
}
