package org.timestamp.mobile.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vsnappy1.timepicker.TimePicker
import com.vsnappy1.timepicker.data.model.TimePickerTime
import org.timestamp.mobile.ui.theme.Colors
import org.timestamp.mobile.ui.theme.ubuntuFontFamily
import java.time.LocalDateTime

@Composable
fun TimePickerDialog(
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit,
    initialTime: LocalDateTime? = null
) {
    var time by remember { mutableStateOf(initialTime ?: LocalDateTime.now()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .shadow(6.dp, shape = RoundedCornerShape(32.dp))
                .background(
                    color = MaterialTheme.colors.primary,
                    shape = RoundedCornerShape(32.dp)
                )
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(color = MaterialTheme.colors.primary)
                    .fillMaxWidth()
            ) {
                TimePicker(
                    onTimeSelected = { h, m ->
                        time = time.withMinute(m).withHour(h)
                    },
                    modifier = Modifier
                        .padding(8.dp),
                    time = TimePickerTime(
                        hour = time.hour,
                        minute = time.minute
                    ),
                )
                Button(
                    onClick = {
                        onConfirm(time.hour, time.minute)
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Colors.Bittersweet, // Background color
                        contentColor = MaterialTheme.colors.primary  // Text color
                    ),
                ) {
                    androidx.compose.material3.Text(
                        text = "OK",
                        fontFamily = ubuntuFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colors.secondary
                    )
                }
            }
        }
    }
}
