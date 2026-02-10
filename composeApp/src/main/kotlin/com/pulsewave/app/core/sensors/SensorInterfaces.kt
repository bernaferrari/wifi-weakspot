package com.pulsewave.app.core.sensors

import kotlinx.coroutines.flow.Flow

interface StepCounter {
    /**
     * Emits a signal (usually 1) whenever a step is detected.
     */
    val steps: Flow<Int>

    fun startListening()
    fun stopListening()
}

interface OrientationProvider {
    /**
     * Emits the current azimuth (heading) in degrees [0, 360).
     */
    val azimuth: Flow<Float>

    fun startListening()
    fun stopListening()
}
