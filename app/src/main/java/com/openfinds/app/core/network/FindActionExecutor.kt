package com.openfinds.app.core.network

import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/** Executes the actual ring/vibrate/flash side effects when a Find command arrives for this device. */
@Singleton
class FindActionExecutor
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private var ringPlayer: MediaPlayer? = null
        private var torchCameraId: String? = null
        private var isTorchOn = false

        fun startRing() {
            stopRing()
            runCatching {
                val uri =
                    RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getValidRingtoneUri(context)
                ringPlayer =
                    MediaPlayer().apply {
                        setAudioStreamType(AudioManager.STREAM_ALARM)
                        setDataSource(context, uri)
                        isLooping = true
                        setVolume(1f, 1f)
                        prepare()
                        start()
                    }
            }.onFailure { Timber.w(it, "Unable to start find-device ringtone") }
        }

        fun stopRing() {
            ringPlayer?.apply {
                runCatching { if (isPlaying) stop() }
                release()
            }
            ringPlayer = null
        }

        fun startVibrate() {
            val vibrator = systemVibrator() ?: return
            val pattern = longArrayOf(0, 400, 200, 400, 200, 400)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, 0)
            }
        }

        fun stopVibrate() {
            systemVibrator()?.cancel()
        }

        fun startFlash() {
            runCatching {
                val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                val id =
                    torchCameraId ?: cameraManager.cameraIdList.firstOrNull { id ->
                        cameraManager.getCameraCharacteristics(id)
                            .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                    }?.also { torchCameraId = it } ?: return
                cameraManager.setTorchMode(id, true)
                isTorchOn = true
            }.onFailure { Timber.w(it, "Unable to enable flashlight") }
        }

        fun stopFlash() {
            if (!isTorchOn) return
            val id = torchCameraId ?: return
            runCatching {
                val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                cameraManager.setTorchMode(id, false)
            }
            isTorchOn = false
        }

        fun stopAll() {
            stopRing()
            stopVibrate()
            stopFlash()
        }

        private fun systemVibrator(): Vibrator? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
    }
