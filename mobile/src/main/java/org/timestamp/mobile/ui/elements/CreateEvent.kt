package org.timestamp.mobile.ui.elements

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import org.timestamp.backend.model.EventDTO
import org.timestamp.backend.viewModels.EventDetailed
import org.timestamp.mobile.R
import org.timestamp.mobile.ui.theme.ubuntuFontFamily
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.sin
import androidx.compose.runtime.SideEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.util.Log

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestLocationPermission() {
    val permissionState = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)
    SideEffect {
        permissionState.launchPermissionRequest()
    }
}

@SuppressLint("MissingPermission")
fun fetchCurrentLocation(
    context: Context,
    onLocationRetrieved: (LatLng) -> Unit
) {
    val permissionCheck = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                onLocationRetrieved(LatLng(location.latitude, location.longitude))
            } else {
                onLocationRetrieved(LatLng(37.7749, -122.4194)) // default, San Fran
            }
        }.addOnFailureListener {
            onLocationRetrieved(LatLng(37.7749, -122.4194))
        }
    } else {
        onLocationRetrieved(LatLng(37.7749, -122.4194))
    }
}

@Composable
fun FetchLocationWrapper(
    context: Context,
    onLocationRetrieved: (LatLng) -> Unit
) {
    var permissionGranted by remember { mutableStateOf(false) }
    RequestLocationPermission()
    val permissionCheck = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    permissionGranted = permissionCheck == PackageManager.PERMISSION_GRANTED
    LaunchedEffect(permissionGranted) {
        if (permissionGranted) {
            fetchCurrentLocation(context, onLocationRetrieved)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEvent(
    onDismissRequest: () -> Unit,
    onConfirmation: (EventDetailed) -> Unit,
    isMock: Boolean,
    properties: DialogProperties = DialogProperties(),
) {
    var eventName by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf(false) }
    var eventTime by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(37.7749, -122.4194), 10f)
    }
    val context = LocalContext.current
    val defaultMockLocation = LatLng(37.7749, -122.4194)
    if (isMock) {
        // Use mock location
        selectedLocation = defaultMockLocation
        cameraPositionState.position = CameraPosition.fromLatLngZoom(defaultMockLocation, 15f)
    } else {
        // Real location fetching logic
        FetchLocationWrapper(context) { location ->
            selectedLocation = location
            cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 15f)
        }
    }

    val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    if (eventDate) {
        DatePickerDialog(
            onDateSelected = { dateMillis ->
                selectedDate = dateMillis?.let { dateFormatter.format(Date(it)) } ?: ""
                eventDate = false
            },
            onDismiss = { eventDate = false }
        )
    }
    if (eventTime) {
        val isToday = selectedDate == dateFormatter.format(Date())
        TimePickerDialog(
            onConfirm = { hour, minute ->
                selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
            },
            onDismiss = { eventTime = false },
        )
    }

   Dialog(
       onDismissRequest = { onDismissRequest() },
       properties = properties.let {
           DialogProperties(
               usePlatformDefaultWidth = false
           )
       }) {
       Card (
           modifier = Modifier
               .fillMaxWidth(0.92f)
               .height(640.dp)
               .shadow(6.dp, shape = RoundedCornerShape(32.dp))
               .background(Color.White, shape = RoundedCornerShape(32.dp)),
           shape = RoundedCornerShape(32.dp)
       ) {
           Column(
               modifier = Modifier
                   .background(Color.White)
                   .fillMaxSize(),
               verticalArrangement = Arrangement.Center,
               horizontalAlignment = Alignment.CenterHorizontally
           ) {
               Text(
                   modifier = Modifier
                       .padding(16.dp),
                   text = "Add Event",
                   color = Color.Black,
                   fontFamily = ubuntuFontFamily,
                   fontWeight = FontWeight.Bold,
                   fontSize = 24.sp
               )
               Divider(
                   color = Color.LightGray,
                   thickness = 2.dp,
                   modifier = Modifier
                       .fillMaxWidth(0.9f)
                       .padding(bottom = 16.dp)
               )
               TextField(
                   modifier = Modifier
                       .fillMaxWidth(0.9f)
                       .shadow(4.dp, shape = RoundedCornerShape(16.dp), ambientColor = Color(0x33000000))
                       .border(1.dp, Color.LightGray, shape = RoundedCornerShape(16.dp))
                       .background(Color.White, shape = RoundedCornerShape(16.dp)),
                   value = eventName,
                   onValueChange = {eventName = it},
                   placeholder = { Text(
                       text = "Event Name",
                       fontFamily = ubuntuFontFamily,
                       fontWeight = FontWeight.Bold) },
                   singleLine = true,
                   colors = TextFieldDefaults.textFieldColors(
                       backgroundColor = Color.Transparent,
                   )
               )
               Spacer(modifier = Modifier.height(12.dp))
               Row(
                   modifier = Modifier
                       .fillMaxWidth(),
                   horizontalArrangement = Arrangement.Center,
               ) {
                   TextField(
                       modifier = Modifier
                           .fillMaxWidth(0.45f)
                           .shadow(4.dp, shape = RoundedCornerShape(16.dp), ambientColor = Color(0x33000000))
                           .border(1.dp, Color.LightGray, shape = RoundedCornerShape(16.dp))
                           .background(Color.White, shape = RoundedCornerShape(16.dp)),
                       value = selectedDate,
                       onValueChange = { },
                       readOnly = true,
                       placeholder = {
                           Text(
                               text = "Event Date",
                               fontFamily = ubuntuFontFamily,
                               fontWeight = FontWeight.Bold
                           )
                       },
                       trailingIcon = {
                           IconButton(onClick = {eventDate = !eventDate}) {
                               Icon(
                                   imageVector = Icons.Default.DateRange,
                                   contentDescription = "select date"
                               )
                           }
                       },
                       colors = TextFieldDefaults.textFieldColors(
                           backgroundColor = Color.Transparent,
                       )
                   )
                   Spacer(modifier = Modifier.width(8.dp))
                   TextField(
                       modifier = Modifier
                           .fillMaxWidth(0.8f)
                           .shadow(4.dp, shape = RoundedCornerShape(16.dp), ambientColor = Color(0x33000000))
                           .border(1.dp, Color.LightGray, shape = RoundedCornerShape(16.dp))
                           .background(Color.White, shape = RoundedCornerShape(16.dp))
                           .clickable { eventTime = !eventTime },
                       enabled = false,
                       value = selectedTime,
                       onValueChange = {selectedTime = it},
                       placeholder = { Text(
                           text = "Event Time",
                           fontFamily = ubuntuFontFamily,
                           fontWeight = FontWeight.Bold) },
                       singleLine = true,
                       readOnly = true,
                       colors = TextFieldDefaults.textFieldColors(
                           backgroundColor = Color.Transparent,
                           textColor = Color.Black,
                       )
                   )
               }
               Spacer(modifier = Modifier.height(8.dp))
               Text(
                   text = "Select Event Location",
                   fontFamily = ubuntuFontFamily,
                   fontWeight = FontWeight.Bold,
                   fontSize = 18.sp
               )
               Spacer(modifier = Modifier.height(16.dp))
                   GoogleMap(
                       modifier = Modifier
                           .fillMaxWidth(0.95f)
                           .height(270.dp)
                           .clip(RoundedCornerShape(16.dp)),
                       cameraPositionState = cameraPositionState,
                       onMapClick = { latLng ->
                           selectedLocation = latLng
                       }
                   ) {
                       selectedLocation?.let {
                           Marker(
                               state = MarkerState(position = it),
                               title = "Selected Location"
                           )
                       }
                   }
               Spacer(modifier = Modifier.height(16.dp))
               Row(
                   modifier = Modifier
                       .fillMaxWidth(),
                   horizontalArrangement = Arrangement.Center,
               ) {
                   Button(
                       onClick = { onDismissRequest() },
                       colors = ButtonColors(
                           containerColor = Color(0xFF2A2B2E),
                           contentColor = Color(0xFFFFFFFF),
                           disabledContainerColor = Color(0xFF2A2B2E),
                           disabledContentColor = Color(0xFFFFFFFF)
                       ),
                       shape = RoundedCornerShape(24.dp),
                       modifier = Modifier
                           .width(130.dp)
                           .height(50.dp)
                           .shadow(4.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color(0x33000000))
                   ) {
                       Text(
                           text = "Cancel",
                           fontFamily = ubuntuFontFamily,
                           fontWeight = FontWeight.Bold,
                           fontSize = 24.sp,
                           color = Color(0xFFFFFFFF)
                       )
                   }
                   Spacer(modifier = Modifier.width(32.dp))
                   Button(
                       onClick = {
                           if (selectedDate.isNotEmpty() && selectedTime.isNotEmpty()) {
                               val date = dateFormatter.parse(selectedDate)
                               val parsedTime = timeFormatter.parse(selectedTime)

                               if (date != null && parsedTime != null) {
                                   val calendar = Calendar.getInstance().apply {
                                       time = date
                                   }
                                   val timeCalendar = Calendar.getInstance().apply {
                                       time = parsedTime
                                   }

                                   val selectedDateTime = LocalDateTime.of(
                                       calendar.get(Calendar.YEAR),
                                       calendar.get(Calendar.MONTH) + 1, // Months are zero-based
                                       calendar.get(Calendar.DAY_OF_MONTH),
                                       timeCalendar.get(Calendar.HOUR_OF_DAY),
                                       timeCalendar.get(Calendar.MINUTE)
                                   )

                                   onConfirmation(EventDetailed(
                                       name = eventName,
                                       arrival = selectedDateTime,
                                       latitude = selectedLocation?.latitude ?: 0.0,
                                       longitude = selectedLocation?.longitude ?: 0.0,
                                       description = "Selected Location",
                                       address = "Selected Address"
                                   ))

                                   Log.d("ADD EVENT", "EVENT ADDED")
                                   Log.d("selectedDate", selectedDate)
                               }
                           } else {
                               Log.d("ADD EVENT", "EVENT FAILED TO ADD")
                               Log.d("selectedDate", selectedDate)
                           }
                       },
                       colors = ButtonColors(
                           containerColor = Color(0xFFFF6F61),
                           contentColor = Color(0xFFFFFFFF),
                           disabledContainerColor = Color(0xFFFF6F61),
                           disabledContentColor = Color(0xFFFFFFFF)
                       ),
                       shape = RoundedCornerShape(24.dp),
                       modifier = Modifier
                           .width(130.dp)
                           .height(50.dp)
                           .shadow(4.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color(0x33000000))
                   ) {
                       Text(
                           text = "Add",
                           fontFamily = ubuntuFontFamily,
                           fontWeight = FontWeight.Bold,
                           fontSize = 24.sp,
                           color = Color(0xFFFFFFFF)
                       )
                   }
               }
           }
       }
   }
}