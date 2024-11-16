package org.timestamp.mobile

import androidx.activity.compose.setContent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.timestamp.mobile.ui.elements.DatePickerDialog
import java.util.Calendar

//class DatePickerTest {
//    @JvmField @Rule
//    val composeTestRule = createAndroidComposeRule<MainActivity>()
//
//    @Test
//    fun testDatePickerDialog_selectValidDateAndConfirm() {
//        var selectedDate: Long? = null
//        composeTestRule.activity.setContent{
//            DatePickerDialog(
//                onDateSelected = { dateMillis ->
//                    selectedDate = dateMillis
//                },
//                onDismiss = {}
//            )
//        }
//
//        composeTestRule.onNodeWithText("OK").assertExists()
//        composeTestRule.onNodeWithText("Cancel").assertExists()
//
//        val calendar = Calendar.getInstance().apply {
//            add(Calendar.DAY_OF_MONTH, 1)
//        }
//        val selectedDateInMillis = calendar.timeInMillis
//
//        composeTestRule.onNodeWithText("OK").performClick()
//
//        assert(selectedDate == selectedDateInMillis)
//    }

    @Test
    fun testDatePickerDialog_cancelSelection() {
        var selectedDate: Long? = null

        composeTestRule.activity.setContent{
            DatePickerDialog(
                onDateSelected = { dateMillis ->
                    selectedDate = dateMillis
                },
                onDismiss = {}
            )
        }

        composeTestRule.onNodeWithText("Cancel").performClick()
        assert(selectedDate == null)
    }

}