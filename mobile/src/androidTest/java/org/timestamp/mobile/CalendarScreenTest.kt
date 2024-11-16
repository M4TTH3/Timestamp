package org.timestamp.mobile

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import androidx.compose.ui.test.junit4.createComposeRule


class CalendarScreenTest {
    @JvmField
    @Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun elementsAreDisplayed() {
        composeTestRule.activity.setContent {
            CalendarScreen()
        }
        composeTestRule.onNodeWithText("Calendar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("calendar").assertIsDisplayed()
    }

    @Test
    fun testCalendarScreenElements() {
        composeTestRule.activity.setContent {
            CalendarScreen()
        }
        composeTestRule.onNodeWithTag("calendar").assertExists()
    }
}