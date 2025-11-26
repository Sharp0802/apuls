package com.apulsetech.apuls

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.lang.RuntimeException

object Notify {
    private val toneGen: ToneGenerator by lazy {
        ToneGenerator(AudioManager.STREAM_ALARM, 100)
    }

    private fun doBeep(ms: Int) {
        if (!beep) return

        try {
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, ms)
        } catch (e: RuntimeException) {
            Log.e("DeviceCommViewModel", "Failed to beep", e)
        }
    }

    private fun doVibrate(ms: Int) {
        if (!vibrate) return

        val man = App.context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val vibrator = man.defaultVibrator

        vibrator.vibrate(VibrationEffect.createOneShot(ms.toLong(), 255))
    }

    var beep by mutableStateOf(true)
    var vibrate by mutableStateOf(true)

    fun tag() {
        doBeep(10)
        doVibrate(10)
    }
}
