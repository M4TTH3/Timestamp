package org.timestamp.mobile

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.junit.Rule
import org.junit.Test
import org.timestamp.mobile.ui.elements.CreateEvent

class CreateEventTest {
    @JvmField @Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun elementsAreDisplayed() {
        composeTestRule.activity.setContent {
            CreateEvent(onDismissRequest = {},
                onConfirmation = {})
        }
        composeTestRule.onNodeWithText("Add Event").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add").assertIsDisplayed()
        composeTestRule.onNodeWithText("Event Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Event Date").assertIsDisplayed()
        composeTestRule.onNodeWithText("Event Time").assertIsDisplayed()
    }

    @Test
    fun addButtonWorks() {
        var confirmed = false
        composeTestRule.activity.setContent {
            CreateEvent(onDismissRequest = {},
                onConfirmation = {confirmed = true})
        }

        composeTestRule.onNodeWithText("Add").performClick()
        assert(confirmed)
    }

    @Test
    fun dismissButtonWorks() {
        var dismissed = false
        composeTestRule.activity.setContent {
            CreateEvent(onDismissRequest = {dismissed = true},
                onConfirmation = {})
        }

        composeTestRule.onNodeWithText("Cancel").performClick()
        assert(dismissed)
    }

    @Test
    fun datePickerTest() {
        composeTestRule.activity.setContent {
            CreateEvent(onDismissRequest = {},
                onConfirmation = {})
        }
        composeTestRule.onNodeWithContentDescription("select date").performClick()
        composeTestRule.onNodeWithText("OK").assertIsDisplayed()
    }

    @Test
    fun timePickerTest() {
        composeTestRule.activity.setContent {
            CreateEvent(onDismissRequest = {},
                onConfirmation = {})
        }
        composeTestRule.onNodeWithText("Event Time").performClick()
        composeTestRule.onNodeWithText("OK").assertIsDisplayed()
    }

}