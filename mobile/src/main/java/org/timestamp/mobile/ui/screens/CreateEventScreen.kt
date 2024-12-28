package org.timestamp.mobile.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.timestamp.lib.dto.EventDTO
import org.timestamp.lib.dto.EventUserDTO
import org.timestamp.lib.dto.GeoJsonFeature
import org.timestamp.lib.dto.TravelMode
import org.timestamp.mobile.TimestampActivity
import org.timestamp.mobile.getUser
import org.timestamp.mobile.repository.address
import org.timestamp.mobile.repository.copy
import org.timestamp.mobile.repository.headline
import org.timestamp.mobile.ui.elements.TimePickerDialog
import org.timestamp.mobile.ui.theme.Colors
import org.timestamp.mobile.ui.theme.UbuntuTypography3
import org.timestamp.mobile.viewmodels.EventViewModel
import org.timestamp.mobile.viewmodels.GeocodeViewModel
import org.timestamp.mobile.viewmodels.LocationViewModel
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Default parameters for locations if required
 */

const val UW_LATITUDE = 43.4723
const val UW_LONGITUDE = -80.5449

/**
 * This screen is used to create OR edit an event.
 * @param navigateBack The function to navigate back to the previous screen.
 */

@Composable
fun CreateEventScreen(
    navigateBack: () -> Unit
) {
    // Window values
    val activity = LocalContext.current as TimestampActivity
    val locVm: LocationViewModel = viewModel(activity)
    val eventVm: EventViewModel = viewModel(activity)
    val geoVm: GeocodeViewModel = viewModel(activity)
    val event = eventVm.viewEvent.value
    val user = getUser()

    // Event values
    val isNewEvent: Boolean = remember { event?.id == null } // Either event or id is null
    val canEdit: Boolean = remember { isNewEvent || user?.uid == event?.creator }
    var editEvent by remember { mutableStateOf(event ?: EventDTO()) }

    val onSave: () -> Unit = {
        if (isNewEvent) {
            eventVm.postEvent(editEvent)
        } else {
            eventVm.updateEvent(editEvent)
        }

        navigateBack()
    }

    val onEventUpdate: (EventDTO) -> Unit = {
        editEvent = it
    }

    LaunchedEffect(Unit) {
        // New event -> set current location
        if (!isNewEvent) return@LaunchedEffect

        val loc = locVm.location.value
        val lat = loc?.latitude ?: UW_LATITUDE
        val lon = loc?.longitude ?: UW_LONGITUDE

        // Get the information of the current location
        geoVm.reverseGeocode(lat, lon)?.let {
            val feature = it.features.firstOrNull()
            editEvent = editEvent.copy(
                latitude = lat,
                longitude = lon,
                description = feature?.properties?.headline() ?: "",
                address = feature?.properties?.address() ?: ""
            )
        }
    }

    Scaffold(
        topBar = {
            CreateEventTopBar(isNewEvent, navigateBack, onSave)
        },
        bottomBar = { DeviceNavBar() }
    ) {
        CreateEventFields(editEvent, canEdit, isNewEvent, onEventUpdate, it, 1.dp)
    }
}

/**
 * This function is the container for the fields in create/edit event.
 * @param event The event to create.
 */

@Composable
fun CreateEventFields(
    // We want to hide mutations to the event from the parent
    event: EventDTO,
    canEdit: Boolean = false,
    isNewEvent: Boolean,
    onEventUpdate: (EventDTO) -> Unit,
    innerPadding: PaddingValues = PaddingValues(0.dp),
    dividerThickness: Dp? = null
) {
    Box(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .background(MaterialTheme.colors.primary)
    ) {
        dividerThickness?.let{ HorizontalDivider(color = Colors.Gray, thickness = it) }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 32.dp),
        ) {
            OutlinedTextField(
                value = event.name,
                onValueChange = { s -> onEventUpdate(event.copy(name = s)) },
                placeholder = { Text(event.name.ifBlank { "Event Name" }) },
                enabled = canEdit,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = InputColours()
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.weight(1f).padding(end = 10.dp)
                ) {
                    EventArrivalDate(event, onEventUpdate)
                }
                Box(
                    modifier = Modifier.weight(1f).padding(start = 10.dp)
                ) {
                    EventArrivalTime(event, onEventUpdate)
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
            MapWithSearch(event, onEventUpdate, isNewEvent)
            Spacer(modifier = Modifier.height(48.dp))
            TransportationInput(event, onEventUpdate)
        }

        Text(
            "On time in no time.",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 30.dp),
                style = UbuntuTypography3.titleLarge,
            color = Colors.Platinum
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventArrivalDate(
    event: EventDTO,
    onEventUpdate: (EventDTO) -> Unit
) {
    val eventTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val arrivalDate = event.arrival.format(eventTimeFormatter)

    // States
    var showModal by remember { mutableStateOf(false) }

    // Date picker state & Settings
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = event.arrival.toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
            }
        }
    )

    OutlinedTextField(
        value = arrivalDate,
        onValueChange = { },
        placeholder = { Text("Event Date") },
        trailingIcon = {
            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
        },
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(arrivalDate) {
                onPress { showModal = true }
            },
        colors = InputColours()
    )

    if (!showModal) return

    DatePickerDialog(
        onDismissRequest = { showModal = false },
        confirmButton = {
            TextButton(
                onClick = {
                    showModal = false
                    val newDate = datePickerState.selectedDateMillis!!
                    val instant = Instant.fromEpochMilliseconds(newDate).toJavaInstant()

                    // Must be UTC, because they selected as if it were UTC
                    val dateOffset = OffsetDateTime.ofInstant(instant, ZoneOffset.UTC)
                    val newArrival = event.arrival
                        .withDayOfMonth(dateOffset.dayOfMonth)
                        .withMonth(dateOffset.monthValue)
                        .withYear(dateOffset.year)

                    onEventUpdate(event.copy(arrival = newArrival))
                }
            ) {
                Text("Confirm")
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

/**
 * This function is the arrival time for the create/edit event screen.
 * @param event The event to create.
 * @param onEventUpdate The function to update the event.
 */
@Composable
private fun EventArrivalTime(
    event: EventDTO,
    onEventUpdate: (EventDTO) -> Unit
) {

    val eventTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    val arrivalTime = event.arrival.format(eventTimeFormatter)

    var showModal by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = arrivalTime,
        onValueChange = { },
        placeholder = { Text("Event Time") },
        trailingIcon = {
            Icon(Icons.Default.AccessTime, contentDescription = "Select Time")
        },
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(arrivalTime) {
                onPress { showModal = true }
            },
        colors = InputColours()
    )

    if (!showModal) return

    TimePickerDialog(
        onDismiss = { showModal = false },
        onConfirm = { h, m ->
            showModal = false
            onEventUpdate(event.copy(arrival = event.arrival.withHour(h).withMinute(m)))
        },
        initialTime = event.arrival.toLocalDateTime()
    )
}

/**
 * This function is the map with search for the create/edit event screen.
 * @param event The event to create.
 * @param onEventUpdate The function to update the event.
 * @param isNewEvent Whether the event is new or not.
 */
@Composable
private fun MapWithSearch(
    event: EventDTO,
    onEventUpdate: (EventDTO) -> Unit,
    isNewEvent: Boolean = false
) {

    val geoVm: GeocodeViewModel = viewModel(LocalContext.current as TimestampActivity)
    val cameraPositionState = rememberCameraPositionState {}
    var showModal by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun updateEvent(latLng: LatLng, name: String?) {
        // On a map click, we want to reverse geocode any name we can find
        scope.launch {
            val info = geoVm.reverseGeocode(latLng.latitude, latLng.longitude)
            info?.let { f ->
                val feature = f.features.firstOrNull()
                onEventUpdate(event.copy(
                    latitude = latLng.latitude,
                    longitude = latLng.longitude,
                    description = name ?: feature?.properties?.headline() ?: "",
                    address = f.features.firstOrNull()?.properties?.address() ?: ""
                ))
            }
        }
    }

    LaunchedEffect(
        event.latitude,
        event.longitude
    ) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(
            LatLng(event.latitude, event.longitude),
            15f
        )
    }

    Box {
        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp)),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                updateEvent(latLng, null)
            },
            onPOIClick = {
                updateEvent(it.latLng, it.name)
            }
        )
        {
            Marker(state = MarkerState(position = LatLng(event.latitude, event.longitude)))
        }

        // Display the current address in the Top Left
        TextButton(
            onClick = { showModal = true },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(5.dp)
                .border(1.dp, Colors.Gray, RoundedCornerShape(16.dp))
                .background(Colors.White, RoundedCornerShape(16.dp))
                .fillMaxWidth(0.7f)
                .height(38.dp)
                .clipToBounds(),
            contentPadding = PaddingValues(horizontal = 10.dp)
        ) {
            val font = UbuntuTypography3.labelSmall
            Text(
                event.address.ifBlank { "Search for a location" },
                fontFamily = font.fontFamily,
                fontSize = font.fontSize,
                fontWeight = font.fontWeight,
                color = Colors.Gray,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )

            Icon(
                Icons.Default.Search,
                contentDescription = "Search Address",
                tint = Colors.Platinum,
            )
        }
    }

    if (!showModal) return

    SearchLocationModal(event) { feature ->
        // We want to set the location information
        showModal = false
        event.copy(feature, setLoc = true)?.let {
            onEventUpdate(it)
        }
    }
}

/**
 * This function is the search location modal for the create/edit event screen.
 * @param callback The function to run when the user selects a location.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchLocationModal(
    event: EventDTO,
    callback: (GeoJsonFeature?) -> Unit
) {
    val geoVm: GeocodeViewModel = viewModel(LocalContext.current as TimestampActivity)
    val searchResults by geoVm.searchResults.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // We want it to intercept parent clicks
    fun callbackHandler(feature: GeoJsonFeature?) {
        callback(feature)
        geoVm.clear()
    }

    fun Modifier.disableClick() = this.clickable(false) {}
    fun closeOnClick() = callbackHandler(null)

    @Composable
    fun LocalSearchBar() = DockedSearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    geoVm.search(it, event.latitude, event.longitude)
                },
                onSearch = { },
                onExpandedChange = {},
                expanded = true,
                placeholder = { Text("Search for a location...") },
                leadingIcon = {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go Back",
                        modifier = Modifier.clickable { closeOnClick() }
                    )
                },
                trailingIcon = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear Search",
                        modifier = Modifier.clickable { searchQuery = "" }
                    )
                }
            )
        },
        expanded = true,
        onExpandedChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = (50).dp)
            .disableClick(),
        colors = SearchBarDefaults.colors(
            containerColor = Colors.White,
            dividerColor = Colors.Gray
        )
    ) {
        Column(Modifier.verticalScroll(rememberScrollState()).disableClick()) {
            searchResults?.features?.forEach {
                val name = it.properties.headline()
                val supportingContent = it.properties.address()

                ListItem(
                    headlineContent = { SingleLineText(name) },
                    supportingContent = { SingleLineText(supportingContent) },
                    modifier = Modifier
                        .clickable { callbackHandler(it) }
                        .fillMaxWidth(),
                    colors = ListItemDefaults.colors(
                        containerColor = Colors.White
                    )
                )
            }
        }
    }

    Dialog(
        onDismissRequest = {
            closeOnClick()
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        LocalSearchBar()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransportationInput(
    event: EventDTO,
    onEventUpdate: (EventDTO) -> Unit
) {
    val user = getUser()
    val uid = user!!.uid

    val travelMode: TravelMode? = event.users.firstOrNull { it.id == uid }?.travelMode
    var expanded by remember { mutableStateOf(false) }

    fun updateTransportation(mode: TravelMode?) {
        val users = event.users.filterNot { it.id == uid }
        val cur = event.users.firstOrNull { it.id == uid }

        when(cur) {
            null -> onEventUpdate(event.copy(users = users + EventUserDTO(id = uid, travelMode = mode)))
            else -> onEventUpdate(event.copy(users = users + cur.copy(travelMode = mode)))
        }
    }

    @Composable
    // Bro I have to do this dumb thing because menu box would go underneath
    // the supporting text otherwise
    fun SupportingText(): @Composable () -> Unit = { Text("Specify for smart reminders!") }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = travelMode?.name ?: "",
            onValueChange = {},
            placeholder = { Text("Transportation: Auto") },
            supportingText = if (expanded) null else SupportingText(),
            readOnly = true,
            singleLine = true,
            trailingIcon = {
                when (travelMode) {
                    null -> Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Transportation")
                    else -> Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear Transportation",
                        modifier = Modifier.clickable {
                            updateTransportation(null)
                            expanded = false // Somehow this is needed, and it runs after the parent???
                        }
                    )
                }
           },
            colors = InputColours(),
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable, true)
        )

        // The dropdown menu for the transportation
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = Colors.White,
        ) {
            TravelMode.entries.sortedBy { it.name }.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(mode.name) },
                    onClick = {
                        updateTransportation(mode)
                        expanded = false
                    }
                )
            }
        }
    }
}


/**
 * Little hack to make the text field clickable
 * and wait for the user to lift their finger
 * @param success The function to run when the user lifts their finger.
 */
private suspend fun PointerInputScope.onPress(
    success: () -> Unit
) {
    this.awaitEachGesture {
        awaitFirstDown(pass = PointerEventPass.Initial)
        val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
        if (upEvent != null) success()
    }
}

@Composable
private fun SingleLineText(name: String) {
    Text(
        name,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/**
 * This function is the top bar for the create/edit event screen.
 * @param isNewEvent Whether the event is new or not.
 * @param onBack The function to navigate back to the previous screen.
 * @param onSave The function to save the event.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateEventTopBar(
    isNewEvent: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit
) {

    @Composable
    fun Actions() {
        if (isNewEvent) {
            IconButton(onClick = onSave) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        } else {
            TextButton(onClick = onSave) {
                Text("Save")
            }
        }
    }

    TopAppBar(
        title = {
            Text("Create Event")
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = { Actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Colors.White
        )
    )
}

/**
 * This colors the bottom navbar of the device !!! (USED IN SCAFFOLD) !!!
 * @param color The color to use.
 * @see CreateEventScreen
 */
@Composable
private fun DeviceNavBar(color: Color = Colors.Bittersweet) {
    Box(modifier = Modifier
        .windowInsetsBottomHeight(WindowInsets.navigationBars)
        .fillMaxSize()
        .background(color)
    )
}

@Composable
private fun InputColours() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Colors.White,
    unfocusedContainerColor = Colors.White,
    focusedBorderColor = Colors.Gray,
    unfocusedBorderColor = Colors.Gray,
    focusedTextColor = Colors.Black,
    unfocusedTextColor = Colors.Black,
    focusedPlaceholderColor = Colors.Gray,
    unfocusedPlaceholderColor = Colors.Gray
)
