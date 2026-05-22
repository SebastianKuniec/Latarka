package com.kuniec.latarka

import android.content.Context

/**
 * Data class representing the configuration parameters for the shake-to-flashlight feature.
 *
 * This class includes settings for sensitivity, jitter prevention, and safety features
 * like drop prevention. It also provides methods to persist and load these settings
 * using [android.content.SharedPreferences].
 *
 * @property gForceThreshold The minimum G-force required to trigger a shake detection.
 * @property slopDelayMs The minimum time in milliseconds between two consecutive shakes.
 * @property shakeCount The number of shakes required within a time window to toggle the flashlight.
 * @property isEnabled Whether the shake detection service is active.
 * @property isSnappingEnabled Whether the UI sliders should snap to predefined step values.
 * @property isDropPreventionEnabled Whether to ignore shakes during or immediately after a free-fall.
 * @property isManualEntryMode Whether the UI should show text fields instead of sliders.
 * @property snappingStepG The step size for G-force threshold snapping.
 * @property snappingStepMs The step size for time-based parameter snapping.
 */
data class FlashlightConfig(
    val gForceThreshold: Float = 2.8f,
    val slopDelayMs: Long = 500L,
    val shakeCount: Int = 2,
    val isEnabled: Boolean = true,
    val isSnappingEnabled: Boolean = true,
    val isDropPreventionEnabled: Boolean = true,
    val isManualEntryMode: Boolean = false,
    val snappingStepG: Float = 0.1f,
    val snappingStepMs: Int = 50,
    val cooldownMs: Long = 1500L,
    val resetTimeoutMs: Long = 1000L
) {
    companion object {
        private const val PREFS_NAME = "flashlight_prefs"
        private const val KEY_THRESHOLD = "threshold"
        private const val KEY_DELAY = "delay"
        private const val KEY_COUNT = "count"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_SNAPPING = "snapping"
        private const val KEY_DROP = "drop"
        private const val KEY_MANUAL_ENTRY = "manual_entry"
        private const val KEY_STEP_G = "step_g"
        private const val KEY_STEP_MS = "step_ms"
        private const val KEY_COOLDOWN = "cooldown"
        private const val KEY_RESET_TIMEOUT = "reset_timeout"

        /**
         * Loads the [FlashlightConfig] from [android.content.SharedPreferences].
         *
         * @param context The application context to access the preferences.
         * @return A [FlashlightConfig] instance with saved values or defaults.
         */
        fun load(context: Context): FlashlightConfig {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return FlashlightConfig(
                gForceThreshold = prefs.getFloat(KEY_THRESHOLD, 2.8f),
                slopDelayMs = prefs.getLong(KEY_DELAY, 500L),
                shakeCount = prefs.getInt(KEY_COUNT, 2),
                isEnabled = prefs.getBoolean(KEY_ENABLED, true),
                isSnappingEnabled = prefs.getBoolean(KEY_SNAPPING, true),
                isDropPreventionEnabled = prefs.getBoolean(KEY_DROP, true),
                isManualEntryMode = prefs.getBoolean(KEY_MANUAL_ENTRY, false),
                snappingStepG = prefs.getFloat(KEY_STEP_G, 0.1f),
                snappingStepMs = prefs.getInt(KEY_STEP_MS, 50),
                cooldownMs = prefs.getLong(KEY_COOLDOWN, 1500L),
                resetTimeoutMs = prefs.getLong(KEY_RESET_TIMEOUT, 1000L)
            )
        }

        /**
         * Saves the given [FlashlightConfig] to [android.content.SharedPreferences].
         *
         * @param context The application context to access the preferences.
         * @param config The configuration instance to save.
         */
        fun save(context: Context, config: FlashlightConfig) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().apply {
                putFloat(KEY_THRESHOLD, config.gForceThreshold)
                putLong(KEY_DELAY, config.slopDelayMs)
                putInt(KEY_COUNT, config.shakeCount)
                putBoolean(KEY_ENABLED, config.isEnabled)
                putBoolean(KEY_SNAPPING, config.isSnappingEnabled)
                putBoolean(KEY_DROP, config.isDropPreventionEnabled)
                putBoolean(KEY_MANUAL_ENTRY, config.isManualEntryMode)
                putFloat(KEY_STEP_G, config.snappingStepG)
                putInt(KEY_STEP_MS, config.snappingStepMs)
                putLong(KEY_COOLDOWN, config.cooldownMs)
                putLong(KEY_RESET_TIMEOUT, config.resetTimeoutMs)
                apply()
            }
        }
    }
}
