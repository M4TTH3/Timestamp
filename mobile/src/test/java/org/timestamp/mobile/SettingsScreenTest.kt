package org.timestamp.mobile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.google.firebase.auth.FirebaseUser
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// Unit tests for the Settings screen
class SettingsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockFirebaseUser: FirebaseUser

    @Test
    fun testSettingsScreenRenders() {
        composeTestRule.setContent {
            SettingsScreen(currentUser = null, onSignOutClick = {})
        }

        // Check if visual elements render with no issues
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Account Information").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Out").assertIsDisplayed()
        composeTestRule.onNodeWithText("Account Preferences").assertIsDisplayed()
    }
}