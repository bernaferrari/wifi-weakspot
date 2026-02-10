package com.pulsewave.app.android

import android.app.Application
import com.pulsewave.app.di.initKoin

class PulseWaveApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(this)
    }
}
