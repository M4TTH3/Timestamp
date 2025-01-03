package org.timestamp.mobile.ui.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import org.timestamp.mobile.ui.theme.Colors


@Composable
fun ProgressButton(
    progress: Float,
    modifier: Modifier = Modifier
        .size(50.dp),
    innerText: @Composable () -> Unit = {},
    supportingText: @Composable () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures {
                    onClick()
                }
            }
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = modifier,
                gapSize = 0.dp,
                color = Colors.Bittersweet,
                trackColor = Colors.Bittersweet.copy(alpha = 0.2f)
            )
            innerText()
        }

        Spacer(modifier = Modifier.height(8.dp))
        supportingText()
    }

}