package com.entrymyslot.app

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.entrymyslot.app.core.components.EntryBottomNavigation
import com.entrymyslot.app.core.theme.EntryMySlotTheme
import com.entrymyslot.app.screens.auth.AuthScreen
import org.junit.Rule
import org.junit.Test

/** These tests never submit credentials or make booking/account API calls. */
class AuthUiRegressionTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun recoveryBackReturnsToLoginAndPreservesEmail() {
        compose.setContent { EntryMySlotTheme { AuthScreen() } }
        compose.onAllNodes(hasSetTextAction()).onFirst().performTextInput("member@example.com")
        compose.onNodeWithText("Forgot password?").performScrollTo().performClick()
        compose.onNodeWithText("SEND RESET LINK").assertExists()

        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }

        compose.onNodeWithText("LOGIN").assertExists()
        compose.onAllNodes(hasSetTextAction()).onFirst().assertTextContains("member@example.com")
    }

    @Test
    fun registrationValidationAndTabSwitchRemainLocal() {
        compose.setContent { EntryMySlotTheme { AuthScreen() } }
        compose.onNodeWithText("Register").performScrollTo().performClick()
        compose.onNodeWithText("CREATE ACCOUNT").performScrollTo().performClick()
        compose.onNodeWithText("Full name is required").assertExists()
        // The tab precedes the separate "Already have an account? Login" footer link.
        compose.onAllNodesWithText("Login").onFirst().performScrollTo().performClick()
        compose.onNodeWithText("LOGIN").assertExists()
    }

    @Test
    fun passwordVisibilityHasAccessibleActionLabels() {
        compose.setContent { EntryMySlotTheme { AuthScreen() } }
        compose.onAllNodes(hasSetTextAction())[1].performTextInput("sample-password")
        compose.onNodeWithContentDescription("Show password").performClick()
        compose.onNodeWithContentDescription("Hide password").assertExists()
        compose.onNodeWithContentDescription("Hide password").performClick()
        compose.onNodeWithContentDescription("Show password").assertExists()
    }

    @Test
    fun bottomNavigationExposesTheSelectedTab() {
        compose.setContent {
            EntryMySlotTheme {
                var selected by remember { mutableStateOf("Home") }
                EntryBottomNavigation(selectedItem = selected, onItemSelected = { selected = it })
            }
        }
        compose.onNodeWithContentDescription("Home").assertIsSelected()
        compose.onNodeWithContentDescription("Search").performClick()
        compose.onNodeWithContentDescription("Search").assertIsSelected()
        compose.onNodeWithContentDescription("Home").assertIsNotSelected()
    }
}
