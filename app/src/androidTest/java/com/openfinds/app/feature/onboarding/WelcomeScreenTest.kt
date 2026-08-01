package com.openfinds.app.feature.onboarding

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class WelcomeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun tappingGetStarted_invokesCallback() {
        var tapped = false

        composeTestRule.setContent {
            WelcomeScreen(onGetStarted = { tapped = true })
        }

        composeTestRule.onNodeWithText("Get started").performClick()

        assert(tapped) { "Expected onGetStarted to be invoked after tapping the button" }
    }

    @Test
    fun welcomeScreen_showsAllHighlights() {
        composeTestRule.setContent { WelcomeScreen(onGetStarted = {}) }

        composeTestRule.onNodeWithText("No cloud, ever").assertExists()
        composeTestRule.onNodeWithText("Encrypted pairing").assertExists()
        composeTestRule.onNodeWithText("Instant find").assertExists()
    }
}
