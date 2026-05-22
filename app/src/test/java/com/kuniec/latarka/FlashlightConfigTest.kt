package com.kuniec.latarka

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.kuniec.latarka.FlashlightConfig
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FlashlightConfigTest {

    @Test
    fun `test default values`() {
        val config = FlashlightConfig()
        assertEquals(2.8f, config.gForceThreshold)
        assertEquals(500L, config.slopDelayMs)
        assertEquals(2, config.shakeCount)
        assertTrue(config.isEnabled)
    }

    @Test
    fun `test save and load`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val originalConfig = FlashlightConfig(
            gForceThreshold = 3.5f,
            slopDelayMs = 1000L,
            shakeCount = 3,
            isEnabled = false,
            isSnappingEnabled = false,
            isDropPreventionEnabled = false,
            isManualEntryMode = true,
            snappingStepG = 0.5f,
            snappingStepMs = 100
        )

        FlashlightConfig.save(context, originalConfig)
        val loadedConfig = FlashlightConfig.load(context)

        assertEquals(originalConfig.gForceThreshold, loadedConfig.gForceThreshold)
        assertEquals(originalConfig.slopDelayMs, loadedConfig.slopDelayMs)
        assertEquals(originalConfig.shakeCount, loadedConfig.shakeCount)
        assertEquals(originalConfig.isEnabled, loadedConfig.isEnabled)
        assertEquals(originalConfig.isSnappingEnabled, loadedConfig.isSnappingEnabled)
        assertEquals(originalConfig.isDropPreventionEnabled, loadedConfig.isDropPreventionEnabled)
        assertEquals(originalConfig.isManualEntryMode, loadedConfig.isManualEntryMode)
        assertEquals(originalConfig.snappingStepG, loadedConfig.snappingStepG)
        assertEquals(originalConfig.snappingStepMs, loadedConfig.snappingStepMs)
    }
}
