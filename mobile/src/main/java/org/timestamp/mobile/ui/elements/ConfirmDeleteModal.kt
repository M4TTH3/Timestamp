package org.timestamp.mobile.ui.elements

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.timestamp.mobile.ui.theme.Colors

@Composable
@Preview
fun ConfirmDeleteModal(
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {}
) = AlertDialog(
    onDismissRequest = onDismiss,
    title = {
        Text("Are you sure?", color = Color.DarkGray)
    },
    icon = {
        Icon(
            Icons.Outlined.Cancel,
            contentDescription = null,
            modifier = Modifier.size(50.dp)
        )
    },
    iconContentColor = Colors.BittersweetDark,
    confirmButton = {
        TextButton(onClick = onConfirm) {
            Text("Delete", color = Colors.BittersweetDark)
        }
    },
    dismissButton = {
        TextButton(onClick = onDismiss) {
            Text("Cancel", color = Colors.Gray)
        }
    },
    containerColor = Colors.Platinum
)