package com.openfinds.app.core.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class EmptyStateComponentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyState_showsTitleAndMessage() {
        composeTestRule.setContent {
            EmptyState(title = "No trusted devices yet", message = "Pair your first device to see it here.")
        }

        composeTestRule.onNodeWithText("No trusted devices yet").assertExists()
        composeTestRule.onNodeWithText("Pair your first device to see it here.").assertExists()
    }
}
