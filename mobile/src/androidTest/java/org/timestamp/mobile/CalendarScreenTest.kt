package org.timestamp.mobile

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.google.firebase.auth.FirebaseUser
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.timestamp.mobile.ui.screens.CalendarScreen


class CalendarScreenTest {
    @JvmField
    @Rule
    val composeTestRule = createAndroidComposeRule<TimestampActivity>()
    private lateinit var mockFirebaseUser: FirebaseUser

    @Before
    fun setup() {
        mockFirebaseUser = mockk(relaxed = true)
    }
}