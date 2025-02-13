package org.timestamp.mobile.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import org.timestamp.lib.dto.EventDTO
import org.timestamp.mobile.ui.screens.CreateEventFields
import org.timestamp.mobile.ui.screens.Fields
import org.timestamp.mobile.ui.theme.Colors

/**
 * A view of an event that slides from the bottom.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalViewEventSheet(
    event: EventDTO,
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val fields = Fields(event = event, canEdit = false, isNewEvent = false)

    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
        sheetState = sheetState,
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = true),
        containerColor = Colors.Bittersweet
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight(0.85f)
        ){
            CreateEventFields(fields)
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        onClick()
                    },
                color = Color.Transparent
            ) {}
        }
    }
}