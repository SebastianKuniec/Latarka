package com.kuniec.latarka

import android.hardware.SensorManager
import com.kuniec.latarka.FlashlightConfig
import com.kuniec.latarka.ShakeDetector
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ShakeDetectorTest {

    private lateinit var config: FlashlightConfig
    private lateinit var detector: ShakeDetector

    @Before
    fun setUp() {
        config = FlashlightConfig(
            gForceThreshold = 2.0f,
            slopDelayMs = 500L,
            shakeCount = 2,
            isDropPreventionEnabled = true
        )
        detector = ShakeDetector(config)
    }

    @Test
    fun `test single shake does not trigger toggle`() {
        val currentTime = 1000L
        // A shake with 3G force
        val x = 3.0f * SensorManager.GRAVITY_EARTH
        val result = detector.onSensorChanged(x, 0f, 0f, currentTime)
        
        assertFalse("Single shake should not trigger toggle", result)
    }

    @Test
    fun `test two shakes trigger toggle`() {
        var currentTime = 1000L
        val x = 3.0f * SensorManager.GRAVITY_EARTH
        
        // First shake
        detector.onSensorChanged(x, 0f, 0f, currentTime)
        
        // Advance time past slop delay
        currentTime += 600L
        
        // Second shake
        val result = detector.onSensorChanged(x, 0f, 0f, currentTime)
        
        assertTrue("Two shakes should trigger toggle", result)
    }

    @Test
    fun `test shake during drop is ignored`() {
        var currentTime = 1000L
        
        // Simulate a drop (near 0G total acceleration)
        detector.onSensorChanged(0.1f, 0.1f, 0.1f, currentTime)
        
        // Shake immediately after (within 3s cooldown)
        currentTime += 500L
        val x = 3.0f * SensorManager.GRAVITY_EARTH
        val result = detector.onSensorChanged(x, 0f, 0f, currentTime)
        
        assertFalse("Shake during or immediately after drop should be ignored", result)
    }

    @Test
    fun `test shakes too close together are ignored`() {
        var currentTime = 1000L
        val x = 3.0f * SensorManager.GRAVITY_EARTH
        
        // First shake
        detector.onSensorChanged(x, 0f, 0f, currentTime)
        
        // Second shake too soon (within slopDelayMs)
        currentTime += 100L 
        val result = detector.onSensorChanged(x, 0f, 0f, currentTime)
        
        assertFalse("Shakes too close together should be ignored", result)
    }

    @Test
    fun `test reset after stillness`() {
        var currentTime = 1000L
        val x = 3.0f * SensorManager.GRAVITY_EARTH
        
        // First shake
        detector.onSensorChanged(x, 0f, 0f, currentTime)
        
        // Wait past resetTimeoutMs (default 1000ms)
        currentTime += config.resetTimeoutMs + 100L
        detector.onSensorChanged(0f, 0f, 0f, currentTime)
        
        // Another shake (should be treated as first shake again)
        currentTime += 100L
        val result = detector.onSensorChanged(x, 0f, 0f, currentTime)
        
        assertFalse("Accumulator should have been reset after stillness", result)
    }

    @Test
    fun `test custom cooldown`() {
        val customCooldown = 3000L
        config = config.copy(cooldownMs = customCooldown)
        detector.updateConfig(config)

        var currentTime = 1000L
        val x = 3.0f * SensorManager.GRAVITY_EARTH

        // First shake
        detector.onSensorChanged(x, 0f, 0f, currentTime)
        currentTime += 600L
        // Second shake -> triggers toggle
        assertTrue(detector.onSensorChanged(x, 0f, 0f, currentTime))
        
        val toggleTime = currentTime

        // Shake during custom cooldown
        currentTime += 2000L 
        assertFalse("Should be in cooldown", detector.onSensorChanged(x, 0f, 0f, currentTime))

        // Shake after custom cooldown
        currentTime = toggleTime + customCooldown + 100L
        // First shake after cooldown
        detector.onSensorChanged(x, 0f, 0f, currentTime)
        currentTime += 600L
        // Second shake after cooldown -> triggers toggle
        assertTrue("Should trigger toggle after cooldown", detector.onSensorChanged(x, 0f, 0f, currentTime))
    }
}
