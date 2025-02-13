package org.timestamp.mobile

import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.google.firebase.auth.FirebaseUser
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.timestamp.lib.dto.EventDTO
import org.timestamp.lib.util.toOffset
import org.timestamp.mobile.ui.deprecated.CreateEvent
import java.time.LocalDateTime

class CreateEventTest {
    @JvmField
    @Rule
    val composeTestRule = createAndroidComposeRule<TimestampActivity>()

    private lateinit var mockFirebaseUser: FirebaseUser

    @Before
    fun setUp() {
        mockFirebaseUser = mockk(relaxed = true)
    }

    @Test
    fun setupTestContent() {
        composeTestRule.activity.setContent {
            CreateEvent(
                onDismissRequest = {},
                onConfirmation = {},
                isMock = true,
                loadEvent = null,
                currentUser = mockFirebaseUser,
            )
        }
    }

    @Test
    fun elementsAreDisplayed() {
        composeTestRule.activity.setContent {
            CreateEvent(
                onDismissRequest = {},
                onConfirmation = {},
                isMock = true,
                loadEvent = null,
                currentUser = mockFirebaseUser,
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
        var confirmedEvent: EventDTO? = null
        composeTestRule.activity.setContent {
            CreateEvent(
                onDismissRequest = {},
                onConfirmation = { event -> confirmedEvent = event },
                isMock = true,
                loadEvent = null,
                currentUser = mockFirebaseUser
            )
        }
        composeTestRule.waitForIdle()
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
                isMock = true,
                loadEvent = null,
                currentUser = mockFirebaseUser
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Cancel").performClick()
        assert(dismissed.value)
    }

    @Test
    fun datePickerTest() {
        composeTestRule.activity.setContent {
            CreateEvent(
                onDismissRequest = {},
                onConfirmation = {},
                isMock = true,
                loadEvent = null,
                currentUser = mockFirebaseUser
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
                isMock = true,
                loadEvent = null,
                currentUser = mockFirebaseUser
            )
        }
        composeTestRule.onNodeWithText("Event Time").performClick()
        composeTestRule.onNodeWithText("OK").assertIsDisplayed()
    }

    @Test
    fun validEventSubmission() {
        var confirmedEvent: EventDTO? = null
        composeTestRule.activity.setContent {
            CreateEvent(
                onDismissRequest = {},
                onConfirmation = { event -> confirmedEvent = event },
                isMock = true,
                loadEvent = null,
                currentUser = mockFirebaseUser
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Event Name").performTextInput("Test Event")
        composeTestRule.onNodeWithText("Event Date").performClick()
        composeTestRule.onNodeWithText("OK").performClick()
        composeTestRule.onNodeWithText("Event Time").performClick()
        composeTestRule.onNodeWithText("OK").performClick()
        composeTestRule.onNodeWithText("Add").performClick()
        assert(confirmedEvent != null)
        assert(confirmedEvent?.name == "Test Event")
    }

    @Test
    fun editEventPopulatesFields() {
        val existingEvent = EventDTO(
            id = 123,
            name = "Existing Event",
            arrival = LocalDateTime.now().plusDays(1).toOffset(),
            latitude = 37.7749,
            longitude = -122.4194,
            description = "Test Location",
            address = "123 Test St."
        )
        composeTestRule.activity.setContent {
            CreateEvent(
                onDismissRequest = {},
                onConfirmation = {},
                isMock = false,
                loadEvent = existingEvent,
                currentUser = mockFirebaseUser
            )
        }
        composeTestRule.onNodeWithText("Edit Event").assertIsDisplayed()
        composeTestRule.onNodeWithText("Existing Event").assertIsDisplayed()
    }

    @Test
    fun searchPredictionsAreDisplayed() {
        composeTestRule.activity.setContent {
            CreateEvent(
                onDismissRequest = {},
                onConfirmation = {},
                isMock = false,
                loadEvent = null,
                currentUser = mockFirebaseUser
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("200 University Ave W, Waterloo, ON N2L 3G1, Canada", useUnmergedTree = true).performClick()
        composeTestRule.onNodeWithText("200 University Ave W, Waterloo, ON N2L 3G1, Canada").performTextClearance()
        composeTestRule.onNodeWithText("Type to search...").assertExists()
        composeTestRule.onNodeWithText("Type to search...").performTextInput("Toronto")
    }
}