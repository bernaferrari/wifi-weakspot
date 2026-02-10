package com.pulsewave.app.core.designsystem

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

class PulseWaveHapticFeedback(private val view: View) {
    fun click() {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    }

    fun light() {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    fun heavy() {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    fun success() {
        // Android doesn't have a standard "Success" haptic constant in classic View system easily accessible
        // reusing CONFIRM if available (API 30+) or fallback
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    fun error() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }
}

@Composable
fun rememberPulseWaveHapticFeedback(): PulseWaveHapticFeedback {
    val view = LocalView.current
    return remember(view) { PulseWaveHapticFeedback(view) }
}
