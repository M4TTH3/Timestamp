package org.timestamp.mobile

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.google.firebase.auth.FirebaseUser
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.timestamp.lib.dto.EventDTO
import org.timestamp.mobile.viewmodels.EventViewModel
import org.timestamp.mobile.ui.deprecated.ViewUsers

class ViewUsersTest {
    @JvmField @Rule
    val composeTestRule = createAndroidComposeRule<TimestampActivity>()

    private lateinit var mockEventViewModel: EventViewModel
    private lateinit var mockFirebaseUser: FirebaseUser
    private lateinit var mockEvent: EventDTO

    @Before
    fun setup() {
        mockEventViewModel = mockk(relaxed = true)
        mockFirebaseUser = mockk(relaxed = true)
        mockEvent = mockk(relaxed = true)
    }

    @Test
    fun testInviteLink() {
        composeTestRule.activity.setContent {
            ViewUsers(
                event = mockEvent,
                onDismissRequest = {},
                currentUser = mockFirebaseUser,
                viewModel = mockEventViewModel,
                isToday = false
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("link icon").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("link icon").performClick()

    }

    @Test
    fun testUI() {
        composeTestRule.activity.setContent {
            ViewUsers(
                event = mockEvent,
                onDismissRequest = {},
                currentUser = mockFirebaseUser,
                viewModel = mockEventViewModel,
                isToday = false
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("user ETA").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("user distance").assertIsDisplayed()
    }
}