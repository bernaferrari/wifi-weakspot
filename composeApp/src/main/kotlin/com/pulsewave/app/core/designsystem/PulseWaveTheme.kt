@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.pulsewave.app.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity
import android.content.ContextWrapper
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.material3.rememberDynamicMaterialThemeState
import com.pulsewave.app.core.datastore.SettingsRepository
import org.koin.compose.koinInject

// Default Seed: Indigo
private val DefaultSeed = Color(0xFF3F51B5)

@Composable
fun PulseWaveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    reduceMotion: Boolean = false,
    content: @Composable () -> Unit
) {
    val settingsRepository: SettingsRepository = koinInject()
    val darkPreference by settingsRepository.isDarkMode.collectAsState(initial = null)
    val effectiveDark = darkPreference ?: darkTheme
    val view = LocalView.current
    SideEffect {
        var context = view.context
        while (context is ContextWrapper && context !is Activity) context = context.baseContext
        (context as? Activity)?.window?.let { window ->
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.isAppearanceLightStatusBars = !effectiveDark
            controller.isAppearanceLightNavigationBars = !effectiveDark
        }
    }
    val dynamicColor by settingsRepository.dynamicColor.collectAsState(initial = true)
    val seedColorInt by settingsRepository.seedColor.collectAsState(initial = 0xFF3F51B5.toInt())

    val seedColor = Color(seedColorInt)

    // Try to get system scheme if dynamic color is enabled
    val systemScheme = if (dynamicColor) getSystemColorScheme(effectiveDark) else null

    val colorScheme = systemScheme ?: rememberDynamicMaterialThemeState(
        seedColor = seedColor,
        isDark = effectiveDark,
        style = PaletteStyle.Vibrant,
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
