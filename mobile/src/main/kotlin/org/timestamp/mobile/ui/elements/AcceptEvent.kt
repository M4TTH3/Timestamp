package org.timestamp.mobile.ui.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import org.timestamp.shared.dto.EventDTO
import org.timestamp.shared.dto.EventUserDTO
import org.timestamp.mobile.getActivity
import org.timestamp.mobile.ui.deprecated.UWATERLOO_LATITUDE
import org.timestamp.mobile.ui.deprecated.UWATERLOO_LONGITUDE
import org.timestamp.shared.viewmodel.EventViewModel
import org.timestamp.mobile.ui.theme.Colors
import org.timestamp.mobile.ui.theme.tsTypography
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

val testEvent = EventDTO(
    1,
    "Test Event",
    "New Event Upcoming Tomorrow",
    address = "200 University Ave W, Waterloo, ON N2L 3G1",
    description = "University of Waterloo",
    latitude = UWATERLOO_LATITUDE,
    longitude = UWATERLOO_LONGITUDE,
    arrival = OffsetDateTime.now().plusHours(3),
    users = listOf(
        EventUserDTO(
            id = "1",
            email = "test@gmail.com",
            name = "Test User"
        )
    )
)

@Composable
fun AcceptEvent(
    event: EventDTO = testEvent
) {
    val eventViewModel: EventViewModel = viewModel(LocalContext.current.getActivity())

    AcceptEventDialog(
        event,
        onDismiss = {
            eventViewModel.cancelPendingEvents()
        },
        onAccept = {
            eventViewModel.joinPendingEvent()
        }
    )
}

@Composable
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@OptIn(ExperimentalMaterial3Api::class)
private fun AcceptEventDialog(
    event: EventDTO = testEvent,
    onAccept: () -> Unit = {},
    onDismiss: () -> Unit = {}
) = BasicAlertDialog(
    onDismissRequest = {}, // Do nothing
    properties = DialogProperties(
        dismissOnBackPress = true,
        dismissOnClickOutside = false,
        usePlatformDefaultWidth = false,
    )
) {
    val creator = event.users.single()
    val dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
    val timeOnlyFormatter = DateTimeFormatter.ofPattern("h:mm a")

    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth(0.9f)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                event.name.ifBlank { "Join Event" },
                style = tsTypography.headlineSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            HorizontalDivider(thickness = 2.dp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = event.description,
                style = tsTypography.bodyLarge,
                modifier = Modifier.padding(),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )

            // Address
            Text(
                text = event.address,
                style = tsTypography.bodySmall,
                modifier = Modifier.padding(),
                color = Colors.Gray,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(event.arrival.format(dateTimeFormatter), style = tsTypography.bodySmall)
                Text(event.arrival.format(timeOnlyFormatter), style = tsTypography.bodySmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            GoogleMap(
                cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(
                        LatLng(event.latitude, event.longitude),
                        15f
                    )
                },
                googleMapOptionsFactory = {
                    GoogleMapOptions().apply {
                        liteMode(true)
                    }
                },
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
                    .clip(ShapeDefaults.Medium)
            ) {
//                Marker(rememberMarkerState(position = LatLng(event.latitude, event.longitude)))
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Sent by ${creator.email}", style = tsTypography.bodySmall)
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Colors.Gray,
                        contentColor = Colors.White
                    )
                ) {
                    Text("Cancel", style = tsTypography.bodyMedium, color = Colors.BlackFaint)
                }
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Colors.Bittersweet,
                        contentColor = Colors.White
                    )
                ) {
                    Text("Accept", style = tsTypography.bodyMedium, color = Colors.BlackFaint)
                }
            }
        }
    }
}