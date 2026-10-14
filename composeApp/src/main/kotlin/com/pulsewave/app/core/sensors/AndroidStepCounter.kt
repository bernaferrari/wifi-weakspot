package com.pulsewave.app.core.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AndroidStepCounter(private val context: Context) : StepCounter, SensorEventListener {
    private val sensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    private val stepDetector by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    }

    private val _steps = MutableSharedFlow<Int>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val steps = _steps.asSharedFlow()
    override val isAvailable: Boolean get() = stepDetector != null

    override fun startListening() {
        val sensor = stepDetector ?: return
        try {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        } catch (_: SecurityException) {
            // TYPE_STEP_DETECTOR requires ACTIVITY_RECOGNITION on API 29+.
        }
    }

    override fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
            // Emits 1.0 whenever a step is taken
            if (event.values[0] == 1.0f) {
                _steps.tryEmit(1)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
