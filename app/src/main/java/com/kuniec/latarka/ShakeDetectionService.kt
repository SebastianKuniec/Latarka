package com.kuniec.latarka

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat

/**
 * A foreground service that listens for accelerometer events to detect a shake gesture.
 *
 * This service implements [SensorEventListener] and uses a high-pass filter to isolate
 * linear acceleration from gravity. It also includes free-fall detection to prevent
 * accidental activation during a drop and a cooldown period to avoid stroboscopic effects.
 *
 * The service maintains a [PowerManager.WakeLock] to ensure the CPU continues to process
 * sensor data when the screen is off.
 */
class ShakeDetectionService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var flashlightController: FlashlightController
    private lateinit var shakeDetector: ShakeDetector
    private lateinit var wakeLock: PowerManager.WakeLock

    /**
     * Initializes the service, sensor manager, and notification.
     * Acquires a [PowerManager.PARTIAL_WAKE_LOCK] and registers the accelerometer listener.
     */
    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        flashlightController = FlashlightController(this)
        val config = FlashlightConfig.load(this)
        shakeDetector = ShakeDetector(config)

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ShakeDetection::WakeLock")
        wakeLock.acquire(30 * 60 * 1000L /* 30 minutes */)

        createNotificationChannel()
        startForeground(1, createNotification())

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
    }

    /**
     * Re-loads the configuration when the service is started.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        shakeDetector.updateConfig(FlashlightConfig.load(this))
        return START_STICKY
    }

    /**
     * The service does not support binding.
     */
    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Processes accelerometer data to detect shake or drop events.
     *
     * 1. Detects free-fall (drop) by checking if total acceleration is near 0.
     * 2. Applies a high-pass filter to remove the gravity component.
     * 3. Toggles the flashlight if a shake gesture is detected, respecting cooldowns and drop prevention.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val shouldToggle = shakeDetector.onSensorChanged(
                event.values[0],
                event.values[1],
                event.values[2],
                System.currentTimeMillis()
            )
            if (shouldToggle) {
                flashlightController.toggleFlashlight()
            }
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    /**
     * Unregisters the sensor listener and releases the wake lock.
     */
    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        if (wakeLock.isHeld) wakeLock.release()
    }

    /**
     * Creates the notification channel required for the foreground service.
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "shake_service",
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    /**
     * Builds the notification displayed when the service is active.
     */
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "shake_service")
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
