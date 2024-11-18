package org.timestamp.mobile

import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.timestamp.backend.viewModels.EventDetailed
import org.timestamp.mobile.ui.elements.CreateEvent

class CreateEventTest {
    @JvmField @Rule
    val composeTestRule = createAndroidComposeRule<TimestampActivity>()

    @Test
    fun elementsAreDisplayed() {
        composeTestRule.activity.setContent {
            CreateEvent(
                onDismissRequest = {},
                onConfirmation = {},
                isMock = true
            )
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
        var confirmedEvent: EventDetailed? = null
        composeTestRule.activity.setContent {
            CreateEvent(
                onDismissRequest = {},
                onConfirmation = { event -> confirmedEvent = event },
                isMock = true
            )
        }
        composeTestRule.onNodeWithText("Add").performClick()
        assert(confirmedEvent == null)
    }

    @Test
    fun dismissButtonWorks() {
        val dismissed = mutableStateOf(false)
        composeTestRule.activity.setContent {
            CreateEvent(
                onDismissRequest = { dismissed.value = true },
                onConfirmation = {},
                isMock = true
            )
        }

        composeTestRule.onNodeWithText("Cancel").performClick()
        assert(dismissed.value)
    }

    @Test
    fun datePickerTest() {
        composeTestRule.activity.setContent {
            CreateEvent(
                onDismissRequest = {},
                onConfirmation = {},
                isMock = true
            )
        }
        composeTestRule.onNodeWithContentDescription("select date").performClick()
        composeTestRule.onNodeWithText("OK").assertIsDisplayed()
    }

    @Test
    fun timePickerTest() {
        composeTestRule.activity.setContent {
            CreateEvent(
                onDismissRequest = {},
                onConfirmation = {},
                isMock = true
            )
        }
        composeTestRule.onNodeWithText("Event Time").performClick()
        composeTestRule.onNodeWithText("OK").assertIsDisplayed()
    }

}