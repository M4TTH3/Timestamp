package org.timestamp.mobile.ui.elements

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import org.timestamp.backend.viewModels.EventDetailed
import org.timestamp.mobile.R
import org.timestamp.mobile.models.AppViewModel
import org.timestamp.mobile.ui.theme.ubuntuFontFamily
import java.time.format.DateTimeFormatter

@Composable
fun EventMap(locationName: String, eventName: String, eventLocation: LatLng) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(eventLocation, 15f)
    }
    val markerState = rememberMarkerState(position = eventLocation)
    GoogleMap(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp)),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = markerState,
            title = locationName,
            snippet = eventName
        )
    }
}

@Composable
fun EventBox(data: EventDetailed, viewModel: AppViewModel = viewModel()) {
    var isExpanded by remember { mutableStateOf(false) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Define the box content
    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .shadow(
                shape = RoundedCornerShape(12.dp),
                elevation = 4.dp,
            )
            .border(width = 2.dp, Color.Black, shape = RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { isExpanded = !isExpanded }  // Toggle expand/collapse on click
            .padding(horizontal = 16.dp)
            .animateContentSize()  // Smooth transition for height change
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = data.name,
                fontSize = 24.sp,
                fontFamily = ubuntuFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .widthIn(max = 285.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(painter = painterResource(id = R.drawable.user_icon),
                contentDescription = "user icon",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(24.dp)
                    .offset(y = 8.dp))
            IconButton(
                onClick = {
                    isDropdownExpanded = !isDropdownExpanded
                },
                modifier = Modifier
                    .size(24.dp)
                    .offset(y = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.dots_icon),
                    contentDescription = "dots icon",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = data.description,
            fontSize = 16.sp,
            fontFamily = ubuntuFontFamily,
            style = TextStyle(lineHeight = 14.sp),
            modifier = Modifier
                .fillMaxWidth()
        )
        Text(
            text = data.address,
            fontSize = 12.sp,
            fontFamily = ubuntuFontFamily,
            color = Color.LightGray,
            modifier = Modifier
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()

        ) {
            Icon(painter = painterResource(id = R.drawable.location_icon),
                contentDescription = "location icon",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(18.dp))
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "temp" + "km",
                fontFamily = ubuntuFontFamily,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(20.dp))
            Icon(painter = painterResource(id = R.drawable.car_icon),
                contentDescription = "transportation icon",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "temp" + "min",
                fontFamily = ubuntuFontFamily,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            val formatter = DateTimeFormatter.ofPattern("h:mm a")
            val formattedTime: String = data.arrival.format(formatter)
            Text(
                text = formattedTime,
                fontFamily = ubuntuFontFamily,
                fontSize = 14.sp,
                modifier = Modifier
                    .offset(x = (-4).dp)
            )
            Icon(painter = painterResource(id = R.drawable.clock_icon),
                contentDescription = "clock icon",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(18.dp))
        }

        DropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = {
                isDropdownExpanded = false
            },
            modifier = Modifier
                .align(Alignment.End),
            offset = DpOffset(x = 220.dp, y = (-130).dp)
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Edit",
                        fontFamily = ubuntuFontFamily,
                        fontSize = 16.sp
                    )
                },
                onClick = {
                    /*TODO*/
                    isDropdownExpanded = false
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Remove",
                        fontFamily = ubuntuFontFamily,
                        fontSize = 16.sp,
                        color = Color.Red,
                    )
                },
                onClick = {
                    viewModel.deleteEvent(data.id!!)
                    isDropdownExpanded = false
                }
            )
        }

        // Conditionally show extra content when expanded
        if (isExpanded) {
            EventMap(locationName = data.description, eventName = data.name, eventLocation = LatLng(data.latitude, data.longitude))
        }

        Divider(
            color = Color.LightGray,
            thickness = 1.5.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
        )

        Icon(
            painter = painterResource(id = if (isExpanded) R.drawable.arrow_drop_up else R.drawable.arrow_drop_down),
            contentDescription = if (isExpanded) "arrow drop up icon" else "arrow drop down icon",
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}
