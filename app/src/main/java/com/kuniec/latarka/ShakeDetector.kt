package com.kuniec.latarka

import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Pure logic class for detecting shake gestures from accelerometer data.
 * This class is independent of Android Framework classes except for constants,
 * making it easily unit-testable.
 */
class ShakeDetector(private var config: FlashlightConfig) {

    private var lastShakeTime: Long = 0
    private var lastToggleTime: Long = 0
    private var lastDropTime: Long = 0
    private var shakeCountAccumulator = 0

    // Filter variables for gravity isolation (High-pass filter)
    private var gravityX = 0f
    private var gravityY = 0f
    private var gravityZ = 0f
    private val alpha = 0.8f

    /**
     * Updates the configuration used by the detector.
     */
    fun updateConfig(newConfig: FlashlightConfig) {
        config = newConfig
    }

    /**
     * Processes raw accelerometer values and determines if a flashlight toggle should occur.
     *
     * @param x Raw X-axis acceleration
     * @param y Raw Y-axis acceleration
     * @param z Raw Z-axis acceleration
     * @param currentTime Current time in milliseconds
     * @return true if the shake count requirement is met and a toggle should occur, false otherwise.
     */
    fun onSensorChanged(x: Float, y: Float, z: Float, currentTime: Long): Boolean {
        // 1. Drop Detection (Free fall logic)
        if (config.isDropPreventionEnabled) {
            val totalRawAccel = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
            if (totalRawAccel < 0.3f) {
                lastDropTime = currentTime
            }
        }

        // 2. High-pass filter to isolate acceleration from gravity
        gravityX = alpha * gravityX + (1 - alpha) * x
        gravityY = alpha * gravityY + (1 - alpha) * y
        gravityZ = alpha * gravityZ + (1 - alpha) * z

        val linearX = x - gravityX
        val linearY = y - gravityY
        val linearZ = z - gravityZ

        // 3. Calculate G-force from linear acceleration
        val totalLinearAccel = sqrt(linearX * linearX + linearY * linearY + linearZ * linearZ)
        val gForce = totalLinearAccel / SensorManager.GRAVITY_EARTH

        // 4. Logic to detect shake
        if (gForce > config.gForceThreshold) {
            // Ignore shakes during or immediately after a drop (3 seconds cooldown)
            if (config.isDropPreventionEnabled && currentTime - lastDropTime < 3000) return false

            // Cooldown after toggle to prevent flickering
            if (currentTime - lastToggleTime < config.cooldownMs) return false

            if (currentTime - lastShakeTime > config.slopDelayMs) {
                shakeCountAccumulator++
                lastShakeTime = currentTime

                if (shakeCountAccumulator >= config.shakeCount) {
                    lastToggleTime = currentTime
                    shakeCountAccumulator = 0
                    return true
                }
            }
        } else {
            // Reset accumulator if device is still for a while
            if (currentTime - lastShakeTime > config.resetTimeoutMs) {
                shakeCountAccumulator = 0
            }
        }
        return false
    }
}
