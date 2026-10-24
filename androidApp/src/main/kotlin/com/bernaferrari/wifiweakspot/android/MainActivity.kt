package com.bernaferrari.wifiweakspot.android

import com.bernaferrari.wifiweakspot.App

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)
        setContent {
            com.bernaferrari.wifiweakspot.AndroidApp()
        }
    }
}
