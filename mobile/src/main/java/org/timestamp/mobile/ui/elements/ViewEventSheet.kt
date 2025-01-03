package org.timestamp.mobile.ui.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val fields = remember { Fields(event = event, canEdit = false, isNewEvent = false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = true),
        containerColor = Colors.Bittersweet
    ) {
        Box(
            modifier = Modifier.fillMaxHeight(0.85f)
        ){
            CreateEventFields(fields)
        }
    }
}