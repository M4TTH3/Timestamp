package org.timestamp.mobile.ui.elements

import androidx.compose.foundation.background
import androidx.compose.material.AlertDialog
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import org.timestamp.mobile.ui.theme.Colors
import org.timestamp.mobile.ui.theme.ubuntuFontFamily
import java.time.LocalDateTime
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()
    var invalidDateDialog by remember { mutableStateOf(false) }
    val today = remember {
        Calendar.getInstance(TimeZone.getTimeZone("America/New_York")).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    if (invalidDateDialog) {
        AlertDialog(
            onDismissRequest = { invalidDateDialog = false },
            title = {
                Text(
                    text = "Invalid Date",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Please select a date that is today or later.")
            },
            confirmButton = {
                TextButton(
                    onClick = { invalidDateDialog = false }
                ) {
                    Text("OK")
                }
            }
        )
    }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val rawSelectedDate = datePickerState.selectedDateMillis
                val selectedDate = Calendar.getInstance(TimeZone.getTimeZone("America/New_York")).apply {
                    if (rawSelectedDate != null) {
                        timeInMillis = rawSelectedDate
                    }
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                if (selectedDate >= today) {
                    onDateSelected(selectedDate)
                } else {
                    invalidDateDialog = true
                }
            },
                colors = ButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Colors.Bittersweet,
                    disabledContentColor = Colors.Bittersweet,
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
                    contentColor = Colors.Bittersweet,
                    disabledContentColor = Colors.Bittersweet,
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
