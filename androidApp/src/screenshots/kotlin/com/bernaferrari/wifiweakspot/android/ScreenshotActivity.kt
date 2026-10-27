package com.bernaferrari.wifiweakspot.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.bernaferrari.wifiweakspot.AndroidApp
import com.bernaferrari.wifiweakspot.core.datastore.SettingsRepository
import com.bernaferrari.wifiweakspot.demo.SimulatedHome
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent.get

/** Capture-only entry point for an example walk's changing signal; absent from production builds. */
class ScreenshotActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SimulatedHome.stationary = !intent.getBooleanExtra("moving", false)
        // The walk can be captured in either theme; the screenshot app otherwise starts dark.
        intent.getStringExtra("theme")?.let { theme ->
            runBlocking { get<SettingsRepository>(SettingsRepository::class.java).setDarkMode(theme == "dark") }
        }
        WindowCompat.enableEdgeToEdge(window)
        setContent { AndroidApp() }
    }
}
