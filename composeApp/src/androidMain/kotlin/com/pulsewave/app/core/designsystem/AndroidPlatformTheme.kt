package com.pulsewave.app.core.designsystem

import android.app.Activity
import android.content.ContextWrapper
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.pulsewave.app.core.platform.PlatformTheme

class AndroidPlatformTheme : PlatformTheme {
    @Composable
    override fun dynamicColorScheme(dark: Boolean): ColorScheme? = getSystemColorScheme(dark)

    @Composable
    override fun ApplySystemBars(dark: Boolean) {
        val view = LocalView.current
        SideEffect {
            var context = view.context
            while (context is ContextWrapper && context !is Activity) context = context.baseContext
            (context as? Activity)?.window?.let { window ->
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.isAppearanceLightStatusBars = !dark
                controller.isAppearanceLightNavigationBars = !dark
            }
        }
    }

    @Composable
    override fun systemReducedMotion(): Boolean = rememberSystemMotionReduced()
}
