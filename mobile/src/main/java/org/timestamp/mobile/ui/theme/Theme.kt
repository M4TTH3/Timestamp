package org.timestamp.mobile.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.material.MaterialTheme

@Composable
fun TimestampTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme {
        content()
    }
}