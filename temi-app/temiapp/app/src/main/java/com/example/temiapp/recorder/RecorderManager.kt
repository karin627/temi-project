package com.example.temiapp.recorder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.temiapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

private const val MEDIA_RECORDER_CONSTRUCTOR_DEPRECATION_API_LEVEL = 31

class RecorderManager(context: Context) {
    companion object {
        fun requiredPermissions() = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS,
        )
    }

    private var recorder: MediaRecorder? = null
    private var onUpdateMicrophoneAmplitude: (Int) -> Unit = { }
    private var microphoneAmplitudeUpdateJob: Job? = null
    private val amplitudeReportPeriod: Long
    private val context: Context

    init {
        this.context = context
        this.amplitudeReportPeriod =
            context.resources.getInteger(R.integer.recorder_amplitude_report_period).toLong()
    }

    fun start(context: Context, filename: String) {
        recorder?.apply {
            stop()
            release()
        }

        recorder =
            if (Build.VERSION.SDK_INT >= MEDIA_RECORDER_CONSTRUCTOR_DEPRECATION_API_LEVEL) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }

        recorder!!.apply {
            setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(filename)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e("whisper-input", "prepare() failed")
            }

            start()
        }

        // Start a job to periodically report current amplitude
        microphoneAmplitudeUpdateJob?.cancel()
        microphoneAmplitudeUpdateJob = CoroutineScope(Dispatchers.Main).launch {
            while (recorder != null) {
                val amplitude = recorder?.maxAmplitude ?: 0
                onUpdateMicrophoneAmplitude(amplitude)
                delay(amplitudeReportPeriod)
            }
        }
    }

    fun stop() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null

        microphoneAmplitudeUpdateJob?.cancel()
        microphoneAmplitudeUpdateJob = null
    }

    // Assign onUpdateMicrophoneAmplitude callback
//    fun setOnUpdateMicrophoneAmplitude(onUpdateMicrophoneAmplitude: (Int) -> Unit) {
//        this.onUpdateMicrophoneAmplitude = onUpdateMicrophoneAmplitude
//    }

    // Returns whether all of the permissions are granted.
    fun allPermissionsGranted(context: Context): Boolean {
        for (permission in requiredPermissions()) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }
}