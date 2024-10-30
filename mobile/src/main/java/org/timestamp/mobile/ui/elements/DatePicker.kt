package org.timestamp.mobile.ui.elements

import androidx.compose.foundation.background
import androidx.compose.material.ButtonDefaults
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.timestamp.mobile.ui.theme.bittersweet
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    val calendar = Calendar.getInstance()
    val todayInMillis = calendar.timeInMillis

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {

                val selectedDate = datePickerState.selectedDateMillis
                if (selectedDate != null && selectedDate >= todayInMillis) {
                    onDateSelected(selectedDate)
                }
                onDismiss()
            },
                colors = ButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = bittersweet,
                    disabledContentColor = bittersweet,
                    disabledContainerColor = Color.Transparent
                )
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = bittersweet,
                    disabledContentColor = bittersweet,
                    disabledContainerColor = Color.Transparent
                )
            ) {
                Text("Cancel")
            }
        },
    ) {
        DatePicker(
            state = datePickerState,
        )
    }
}
