package com.kuniec.latarka

import android.content.Context
import android.hardware.camera2.CameraManager
import android.util.Log

/**
 * Controller class for managing the device's flashlight state.
 *
 * This class uses the [CameraManager] API to interact with the
 * device's camera flash.
 *
 * @property context The application context used to access the camera service.
 */
class FlashlightController(private val context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var isFlashlightOn = false

    /**
     * Toggles the flashlight state between on and off.
     *
     * If the flashlight is currently on, it will be turned off, and vice versa.
     * It uses the first available camera ID that supports torch mode.
     */
    fun toggleFlashlight() {
        try {
            val cameraId = cameraManager.cameraIdList.firstOrNull()
            if (cameraId != null) {
                isFlashlightOn = !isFlashlightOn
                cameraManager.setTorchMode(cameraId, isFlashlightOn)
            }
        } catch (e: Exception) {
            Log.e("FlashlightController", "Error toggling flashlight", e)
        }
    }

    /**
     * Explicitly turns off the flashlight if it is currently on.
     */
    fun turnOff() {
        if (isFlashlightOn) {
            try {
                val cameraId = cameraManager.cameraIdList.firstOrNull()
                if (cameraId != null) {
                    cameraManager.setTorchMode(cameraId, false)
                    isFlashlightOn = false
                }
            } catch (e: Exception) {
                Log.e("FlashlightController", "Error turning off flashlight", e)
            }
        }
    }
}
