package com.bernaferrari.wifiweakspot.android

import android.app.Application
import com.bernaferrari.wifiweakspot.di.initKoin

class WifiWeakspotApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(this)
    }
}
