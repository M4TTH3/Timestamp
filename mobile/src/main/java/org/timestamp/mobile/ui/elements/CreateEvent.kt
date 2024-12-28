package org.timestamp.mobile.ui.elements

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.ContentAlpha
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseUser
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import org.timestamp.lib.dto.EventDTO
import org.timestamp.lib.dto.LocationDTO
import org.timestamp.lib.util.toOffset
import org.timestamp.mobile.TimestampActivity
import org.timestamp.mobile.getUser
import org.timestamp.mobile.ui.theme.ubuntuFontFamily
import org.timestamp.mobile.viewmodels.GeocodeViewModel
import org.timestamp.mobile.viewmodels.LocationViewModel
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*


const val UWATERLOO_LATITUDE = 43.4723
const val UWATERLOO_LONGITUDE = -80.5449

// Default location is UWATERLOO if no location is found at all
val UWATERLOO = LocationDTO(UWATERLOO_LATITUDE, UWATERLOO_LONGITUDE)

@Composable
fun CreateEvent(
    onDismissRequest: () -> Unit,
    onConfirmation: (EventDTO) -> Unit,
    isMock: Boolean = false,
    properties: DialogProperties = DialogProperties(),

    /**
     * The event we can pass and load into the form.
     * Edit access IFF the current user is the creator of the event OR id is null.
     */
    loadEvent: EventDTO? = null,
    currentUser: FirebaseUser?
) {
    val curUser = getUser()!! // Assert NOT null, as the user should be logged in

    val geoVm: GeocodeViewModel = viewModel(LocalContext.current as TimestampActivity)
    val locVm: LocationViewModel = viewModel(LocalContext.current as TimestampActivity)

    val isNewEvent: Boolean = loadEvent == null
    val canEdit: Boolean = isNewEvent || loadEvent!!.creator == curUser.uid

    // State to edit the event
    val event = remember {
        // We don't want to rerender the location on the map. Don't collect state
        val curLocation = locVm.location.value ?: UWATERLOO
        mutableStateOf(loadEvent ?: EventDTO(
            creator = curUser.uid,
            latitude = curLocation.latitude,
            longitude = curLocation.longitude,
        ))
    }

    var eventName by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf(false) }
    var eventTime by remember { mutableStateOf(false) }
    val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var locationName by remember { mutableStateOf("") }
    var locationAddress by remember { mutableStateOf("") }
    var isLoadingLocation by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    val cameraPositionState = rememberCameraPositionState {}

    LaunchedEffect(Unit) {
        // If it's a new event, geocode the current location into the map
        if (isNewEvent) {
            val geocodeDTO = geoVm.currentLocPhotonDTO()
            geocodeDTO?.features?.firstOrNull()?.let {

            }
        }
    }

    LaunchedEffect(event.value.latitude, event.value.longitude) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(
            LatLng(event.value.latitude, event.value.longitude),
            if (loadEvent != null) 15f else 10f
        )
    }

    LaunchedEffect(locationName) {
        if (!isSearchActive) {
            query = locationName
        }
    }

    // TODO: Remove this when the location fetching is fixed

    if (eventDate) {
        DatePickerDialog(
            onDateSelected = { date ->
                eventDate = false
                selectedDate = date},
            onDismiss = { eventDate = false },
            initialDate = selectedDate
        )
    }
    if (eventTime) {
        TimePickerDialog(
            onConfirm = { hour, minute ->
                selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                eventTime = false
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
               .background(MaterialTheme.colors.primary, shape = RoundedCornerShape(32.dp)),
           shape = RoundedCornerShape(32.dp)
       ) {
           Column(
               modifier = Modifier
                   .background(MaterialTheme.colors.primary)
                   .fillMaxSize(),
               verticalArrangement = Arrangement.Center,
               horizontalAlignment = Alignment.CenterHorizontally
           ) {
               Text(
                   modifier = Modifier
                       .padding(16.dp),
                   text = if (currentUser?.uid != loadEvent?.creator && loadEvent != null) "View Event" else if (loadEvent == null) "Add Event" else "Edit Event",
                   color = MaterialTheme.colors.secondary,
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
                       .background(MaterialTheme.colors.primary, shape = RoundedCornerShape(16.dp)),
                   value = eventName,
                   onValueChange = {eventName = it},
                   placeholder = { Text(
                       text = "Event Name",
                       fontFamily = ubuntuFontFamily,
                       fontWeight = FontWeight.Bold) },
                   singleLine = true,
                   enabled = if (currentUser?.uid == loadEvent?.creator || loadEvent == null) true else false,
                   colors = TextFieldDefaults.textFieldColors(
                       backgroundColor = Color.Transparent,
                       textColor = MaterialTheme.colors.secondary
                   ),
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
                           .background(MaterialTheme.colors.primary, shape = RoundedCornerShape(16.dp)),
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
                           IconButton(onClick = {
                               if (currentUser?.uid == loadEvent?.creator || loadEvent == null) {
                                   eventDate = !eventDate
                               }
                           }) {
                               Icon(
                                   imageVector = Icons.Default.DateRange,
                                   contentDescription = "select date"
                               )
                           }
                       },
                       colors = TextFieldDefaults.textFieldColors(
                           backgroundColor = Color.Transparent,
                           textColor = MaterialTheme.colors.secondary
                       )
                   )
                   Spacer(modifier = Modifier.width(8.dp))
                   TextField(
                       modifier = Modifier
                           .fillMaxWidth(0.8f)
                           .shadow(4.dp, shape = RoundedCornerShape(16.dp), ambientColor = Color(0x33000000))
                           .border(1.dp, Color.LightGray, shape = RoundedCornerShape(16.dp))
                           .background(MaterialTheme.colors.primary, shape = RoundedCornerShape(16.dp))
                           .clickable {
                               if (currentUser?.uid == loadEvent?.creator || loadEvent == null) {
                                   eventTime = !eventTime
                               }
                                      },
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
                           textColor = MaterialTheme.colors.secondary,
                       )
                   )
               }
               Spacer(modifier = Modifier.height(12.dp))
               if (isSearchActive) {
                   TextField(
                       value = query,
                       onValueChange = { text -> query = text },
                       placeholder = {
                           Text(
                               text = "Type to search...",
                               fontFamily = ubuntuFontFamily,
                               fontWeight = FontWeight.Bold,
                               fontSize = 16.sp,
                               color = MaterialTheme.colors.secondary.copy(alpha = ContentAlpha.medium)
                           )
                       },
                       textStyle = TextStyle(
                           fontFamily = ubuntuFontFamily,
                           fontWeight = FontWeight.Bold,
                           fontSize = 16.sp,
                           color = MaterialTheme.colors.secondary
                       ),
                       singleLine = true,
                       modifier = Modifier
                           .fillMaxWidth(0.9f)
                           .height(56.dp)
                           .shadow(
                               4.dp,
                               shape = RoundedCornerShape(16.dp),
                               ambientColor = Color(0x33000000)
                           )
                           .border(1.dp, Color.LightGray, shape = RoundedCornerShape(16.dp))
                           .background(
                               MaterialTheme.colors.primary,
                               shape = RoundedCornerShape(16.dp)
                           ),
                       colors = TextFieldDefaults.textFieldColors(
                           backgroundColor = Color.Transparent,
                           textColor = MaterialTheme.colors.secondary,
                           cursorColor = MaterialTheme.colors.secondary,
                           focusedIndicatorColor = Color.Transparent,
                           unfocusedIndicatorColor = Color.Transparent,
                           placeholderColor = MaterialTheme.colors.secondary.copy(alpha = ContentAlpha.medium)
                       ),
                       enabled = currentUser?.uid == loadEvent?.creator || loadEvent == null
                   )
               } else {
                   Box(
                       modifier = Modifier
                           .fillMaxWidth(0.9f)
                           .height(56.dp)
                           .shadow(
                               4.dp,
                               shape = RoundedCornerShape(16.dp),
                               ambientColor = Color(0x33000000)
                           )
                           .border(1.dp, Color.LightGray, shape = RoundedCornerShape(16.dp))
                           .background(
                               MaterialTheme.colors.primary,
                               shape = RoundedCornerShape(16.dp)
                           )
                           .clickable {
                               isSearchActive = true
                           },
                       contentAlignment = Alignment.CenterStart
                   ) {
                       Text(
                           text = event.value.address.ifEmpty { "Search for a location..." },
                           modifier = Modifier
                               .fillMaxWidth()
                               .padding(horizontal = 16.dp, vertical = 12.dp),
                           color = if (locationName.isNotEmpty()) MaterialTheme.colors.secondary else MaterialTheme.colors.secondary.copy(alpha = ContentAlpha.medium),
                           fontFamily = ubuntuFontFamily,
                           fontWeight = FontWeight.Bold,
                           fontSize = 16.sp
                       )
                   }
               }
               Spacer(modifier = Modifier.height(16.dp))
                   GoogleMap(
                       modifier = Modifier
                           .fillMaxWidth(0.95f)
                           .height(270.dp)
                           .clip(RoundedCornerShape(16.dp)),
                       cameraPositionState = cameraPositionState,
                       onMapClick = { latLng ->
                           selectedLocation = latLng
                           isLoadingLocation = true
                           if (currentUser?.uid == loadEvent?.creator || loadEvent == null) {
                               // TODO: Remove this when the location fetching is fixed
                           }
                       }
                   ) {
                       selectedLocation?.let {
                           Marker(
                               state = MarkerState(position = it),
                               title = "Selected Location"
                           )
                       }
                   }
               Spacer(modifier = Modifier.height(12.dp))
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
                   if (currentUser?.uid == loadEvent?.creator || loadEvent == null) {
                       Spacer(modifier = Modifier.width(32.dp));
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
                                       val currentDateTime = LocalDateTime.now()
                                       Log.d("Time", selectedDateTime.toOffset().toString())
                                       if (selectedDateTime.isAfter(currentDateTime)) {
                                           if (loadEvent != null) {
                                               onConfirmation(
                                                   EventDTO(
                                                       name = eventName,
                                                       arrival = selectedDateTime.toOffset(),
                                                       latitude = selectedLocation?.latitude ?: 0.0,
                                                       longitude = selectedLocation?.longitude ?: 0.0,
                                                       description = locationName,
                                                       address = locationAddress,
                                                       id = loadEvent.id,
                                                   )
                                               )
                                           } else {
                                               onConfirmation(
                                                   EventDTO(
                                                       name = eventName,
                                                       arrival = selectedDateTime.toOffset(),
                                                       latitude = selectedLocation?.latitude ?: 0.0,
                                                       longitude = selectedLocation?.longitude ?: 0.0,
                                                       description = locationName,
                                                       address = locationAddress,
                                                   )
                                               )
                                           }
                                           Log.d("ADD EVENT", "EVENT ADDED")
                                           Log.d("selectedDate", selectedDate)
                                       }
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
                               text = if (loadEvent == null) "Add" else "Edit",
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
}
