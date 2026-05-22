package com.kuniec.latarka

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.kuniec.latarka.ConfigurationScreen
import com.kuniec.latarka.ui.theme.LatarkaTheme
import org.junit.Rule
import org.junit.Test

class ConfigurationScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testServiceToggle() {
        composeTestRule.setContent {
            LatarkaTheme {
                ConfigurationScreen()
            }
        }

        // Find the "Service Enabled" switch and toggle it
        // We use stringResource(R.string.service_enabled) usually, but in tests we can use the text directly if we know it
        // Or better, use a testTag if we had added one. 
        // For now, let's look for the text.
        
        composeTestRule.onNodeWithText("Service Enabled").assertIsDisplayed()
        
        // Find the switch. It's in the same Row.
        // Since it's a simple screen, we can just find the switch.
        composeTestRule.onNode(hasSetTextAction().not().and(hasClickAction())).performClick()
        
        // This is a bit brittle without testTags. Let's assume the switch is clickable.
    }

    @Test
    fun testManualEntryModeToggle() {
        composeTestRule.setContent {
            LatarkaTheme {
                ConfigurationScreen()
            }
        }

        // Toggle Manual Entry Mode
        composeTestRule.onNodeWithText("Manual Entry Mode").performClick()

        // Verify that sliders are replaced by text fields (which have 📝 emoji in trailing icon)
        composeTestRule.onNodeWithText("📝").assertExists()
    }
}
