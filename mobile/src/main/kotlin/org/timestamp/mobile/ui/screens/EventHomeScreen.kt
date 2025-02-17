package org.timestamp.mobile.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch
import org.timestamp.shared.dto.*
import org.timestamp.mobile.NavBarV2
import org.timestamp.mobile.TimestampActivity
import org.timestamp.mobile.getActivity
import org.timestamp.mobile.getUser
import org.timestamp.mobile.ui.elements.*
import org.timestamp.mobile.ui.theme.Colors
import org.timestamp.mobile.ui.theme.tsTypography
import org.timestamp.mobile.utility.EventFilterKey
import org.timestamp.mobile.utility.Screen
import org.timestamp.shared.viewmodel.EventViewModel
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

private data class EventHomeFields(
    val events: List<EventDTO>,
    val navigateCreateEvent: (EventDTO?) -> Unit,
    val isSearching: Boolean = false,
    val setIsSearching: (Boolean) -> Unit,
    val addFilter: (EventFilterKey, List<EventFilterKey>, (List<EventDTO>) -> List<EventDTO>) -> Unit,
    val removeFilter: (EventFilterKey) -> Unit
)

private val LocalFields = compositionLocalOf<EventHomeFields> { error("No Context provided") }

@Composable
fun EventHomeScreen(
    navigateCreateEvent: (EventDTO?) -> Unit,
    navigate: (Screen) -> Unit
) {
    val eventVm: EventViewModel = viewModel(LocalContext.current as TimestampActivity)
    val events by eventVm.events.collectAsState()
    val pendingEvent by eventVm.pendingEvent.collectAsState()

    // Filters we have, and the corresponding functions
    val filters = remember { mutableStateOf(emptySet<EventFilterKey>()) }
    val filterMap = remember { mutableMapOf<EventFilterKey, (List<EventDTO>) -> List<EventDTO>>() }

    fun addFilter(
        key: EventFilterKey,
        removeKey: List<EventFilterKey>,
        filter: (List<EventDTO>) -> List<EventDTO>
    ) {
        filters.value = filters.value - removeKey.toSet() + key // Add key AFTER removeKey
        for (k in removeKey) filterMap.remove(k)
        filterMap[key] = filter
    }

    fun removeFilter(key: EventFilterKey) {
        filters.value -= key
        filterMap.remove(key)
    }

    // The fields to pass down to children
    var isSearching by remember { mutableStateOf(false) }
    val eventsFiltered = remember(events, filters.value) {
        var filtered = events
        for (key in filters.value) filtered = filterMap[key]!!(filtered)

        filtered
    }
    val fields = EventHomeFields(
        eventsFiltered,
        navigateCreateEvent,
        isSearching,
        { isSearching = it},
        ::addFilter,
        ::removeFilter
    )

    // Make create event button show on top of scroll, and on scroll up
    var fabVisible by remember { mutableStateOf(true) }

    CompositionLocalProvider(LocalFields provides fields) {
        Scaffold(
            topBar = {
                EventHomeScreenTopBar()
            },
            bottomBar = {
                NavBarV2(navigate, Screen.Events)
            },
            floatingActionButton = {
                if (fabVisible) {
                    FloatingActionButton(
                        onClick = { navigateCreateEvent(null) },
                        containerColor = Colors.TeaRose,
                        contentColor = Color.Unspecified,
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "Add Event Button")
                    }
                }
            }
        ) { inset ->
            EventHomeScreenContent(inset)
        }
    }

    pendingEvent?.let {
        AcceptEvent(it)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventHomeScreenContent(
    inset: PaddingValues = PaddingValues(0.dp)
) {
    val eventVm: EventViewModel = viewModel(LocalContext.current as TimestampActivity)
    val (events, _, _, setIsSearching) = LocalFields.current
    val scope = rememberCoroutineScope()
    val state = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }
    val onRefresh: () -> Unit = {
        // Refresh the content
        isRefreshing = true
        scope.launch {
            // Simulate a refresh delay
            eventVm.getEvents()
            isRefreshing = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Colors.Platinum)
            .pullToRefresh(isRefreshing = isRefreshing, onRefresh = onRefresh, state = state)
            .pointerInput(Unit) {
                detectTapGestures { setIsSearching(false) }
            }
    ) {
        PullToRefreshDefaults.Indicator(state = state, isRefreshing = isRefreshing)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inset)
        ) {
            item { Overview() }
            itemsIndexed(events) { i, e ->
                ColumnDivider()
                EventCard(e, i == 0)
            }
        }
    }
}

@Composable
private fun Overview() = Row(
    modifier = Modifier
        .fillMaxWidth()
        .animateContentSize()
        .height(110.dp)
        .background(Colors.White),
    horizontalArrangement = Arrangement.SpaceAround,
    verticalAlignment = Alignment.CenterVertically
) {
    val eventVm = viewModel<EventViewModel>(LocalContext.current.getActivity())
    val events by eventVm.events.collectAsState()
    val (_, _, _, _, addFilter) = LocalFields.current
    var selected by remember { mutableStateOf(EventFilterKey.ALL) }

    val allFilter = { e: List<EventDTO> -> e }
    val todayFilter = { e: List<EventDTO> -> e.filter { it.withinNextDay() } }
    val laterFilter = { e: List<EventDTO> -> e.filter { !it.withinNextDay() } }
    val removeKeys = EventFilterKey.entries.toList()

    listOf(
        Triple(EventFilterKey.ALL, allFilter, "Events"),
        Triple(EventFilterKey.TODAY, todayFilter, "Today"),
        Triple(EventFilterKey.LATER, laterFilter, "Later")
    ).forEach { (key, filter, name) ->
        val count = remember(events) { filter(events).size }
        val isSelected = selected == key
        val modifier = Modifier
            .size(50.dp)
            .background(if (isSelected) Colors.TeaRose else Color.Transparent, CircleShape)
        ProgressButton(
            progress = count.toFloat() / events.size,
            supportingText = {
                Text(name, style = tsTypography.titleMedium)
            },
            innerText = {
                Text(count.toString(), style = tsTypography.titleLarge)
            },
            onClick = {
                addFilter(key, removeKeys - key, filter)
                selected = key
            },
            modifier = modifier
        )
    }
}

@Composable
private fun EventCard(event: EventDTO, initialExpanded: Boolean = false) {

    val contentPadding = PaddingValues(start = 12.dp, end = 12.dp)
    val textPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 2.dp)
    var expanded by remember { mutableStateOf(initialExpanded) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Colors.White)
            .animateContentSize()
            .height(if (expanded) 400.dp else 180.dp)
    ) {
        // The padding has to be uneven because the icon is centered
        EventCardHeader(event, PaddingValues(top = 12.dp, start = 12.dp, end = 4.dp, bottom = 8.dp))
        // Location Name
        Text(
            text = event.description,
            style = tsTypography.bodyLarge,
            modifier = Modifier.padding(contentPadding),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )

        // Address
        Text(
            text = event.address,
            style = tsTypography.bodySmall,
            modifier = Modifier.padding(textPadding),
            color = Colors.Gray,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )

        Spacer(Modifier.height(20.dp))
        EventCardInfo(event, PaddingValues(start = 24.dp, end = 24.dp))

        Spacer(Modifier.height(12.dp))
        if (expanded) EventCardFooterExpanded(event, contentPadding) { expanded = false }
        else EventCardFooter(contentPadding) { expanded = true }
    }
}

@Composable
private fun EventCardHeader(
    event: EventDTO,
    contentPadding: PaddingValues
) {
    val activity = LocalContext.current.getActivity()
    val eventVm: EventViewModel = viewModel(activity)
    val scope = rememberCoroutineScope()
    val isOwner = event.creator == getUser()?.uid
    val (_, navigateCreateEvent, _, _) = LocalFields.current
    var showUsers by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.padding(contentPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            event.name,
            style = tsTypography.headlineMedium,
            modifier = Modifier
                .weight(2f)
                .padding(end = 12.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Row {
            Box {
                // Number above icon
                val numUsers = event.users.size
                Text(
                    numUsers.toString(),
                    style = tsTypography.labelSmall,
                    modifier = Modifier.align(Alignment.TopStart)
                )
                IconButton(onClick = { showUsers = true }, modifier = Modifier.size(28.dp)) {
                    Icon(
                        if (numUsers <= 1) Icons.Outlined.Person else Icons.Outlined.PeopleAlt,
                        contentDescription = "Person Icon",
                        modifier = Modifier.size(28.dp),
                        tint = Colors.Gray
                    )
                }
            }

            DropdownIcon(
                Icons.Default.MoreVert,
                iconSize = 28.dp
            ) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = { navigateCreateEvent(event) }
                )
                DropdownMenuItem(
                    text = { Text("Group") },
                    onClick = { showUsers = true }
                )
                DropdownMenuItem(
                    trailingIcon = { Icon(Icons.Default.Share, contentDescription = "Share", Modifier.size(20.dp)) },
                    text = { Text("Share") },
                    onClick = { scope.shareLink(event, activity) }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            "Delete",
                            color = Colors.BittersweetDark,
                            style = tsTypography.titleSmall
                        )
                    },
                    onClick = { showDelete = true }
                )
            }
        }
    }

    if (showUsers) UserBottomSheet(event, event.arrival, isOwner) { showUsers = false }
    if (showDelete) ConfirmDeleteModal(
        onDismiss = { showDelete = false },
        onConfirm = {
            eventVm.deleteEvent(event.id!!)
            showDelete = false
        }
    )
}

@Composable
private fun EventCardInfo(
    event: EventDTO,
    contentPadding: PaddingValues
) {
    val uid = getUser()!!.uid
    val userEvent = event.users.first { it.id == uid }
    val eventVm: EventViewModel = viewModel(LocalContext.current.getActivity())

    val isUpcoming = event.withinNextDay()
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d")

    val statusTextStyle = tsTypography.labelMedium
    val textStyle = tsTypography.bodySmall
    val iconSize = 20.dp

    // This allows the user to update their travel method for smart alarms
    var showTravelDialog by remember { mutableStateOf(false) }

    @Composable
    fun BasicInfo(icon: ImageVector, text: String) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = Colors.PowderBlue,
                modifier = Modifier.size(iconSize)
            )
            Text(
                text,
                style = textStyle,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }

    @Composable
    fun TimeInfo(upcoming: Boolean = true) = Button(
        onClick = { showTravelDialog = true },
        border = BorderStroke(1.dp, Colors.Gray),
        shape = ShapeDefaults.Small,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Colors.Black
        ),
        contentPadding = PaddingValues(8.dp)
    ) {
        BasicInfo(
            when(userEvent.travelMode) {
                TravelMode.Foot -> Icons.AutoMirrored.Filled.DirectionsWalk
                TravelMode.Bike -> Icons.AutoMirrored.Filled.DirectionsBike
                TravelMode.Car -> Icons.Default.DirectionsCar
                else -> Icons.Default.Commute
            },
            when {
                upcoming -> userEvent.timeString()
                else -> userEvent.travelMode?.name ?: "Auto"
            }
        )
    }

    @Composable
    fun ArrivalTimeInfo() = BasicInfo(Icons.Default.AccessTime, event.arrival.format(timeFormatter))

    @Composable
    fun DistanceInfo() = BasicInfo(Icons.Default.LocationOn, userEvent.distanceString())

    @Composable
    fun InfoArrived() {
        Text("Arrived", style = statusTextStyle, color = Colors.Green)
        ArrivalTimeInfo()
    }

    @Composable
    fun InfoLater() {
        BasicInfo(Icons.Default.CalendarMonth, event.arrival.format(dateFormatter))
        TimeInfo(upcoming = false)
        Spacer(Modifier.fillMaxWidth(0.25f)) // Take up a quarter of the space
        ArrivalTimeInfo()
    }

    @Composable
    fun InfoUpcoming() {
        // Check if the user traveling now will make it
        val timeEst = userEvent.timeEst
        val arrivalTime = OffsetDateTime.now().plusSeconds((timeEst ?: 0L).floorDiv(1000))
        val isOnTime = arrivalTime.isBefore(event.arrival)

        val (text, color) = when {
            (timeEst == null) -> "Calculating..." to Colors.Gray
            isOnTime -> "On Time" to Colors.Green
            else -> "Late" to Colors.BittersweetDark
        }

        Text(text, style = statusTextStyle, color = color)
        TimeInfo()
        DistanceInfo()
        ArrivalTimeInfo()
    }

    Row(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxWidth()
            .height(36.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        when {
            userEvent.arrived -> InfoArrived()
            isUpcoming -> InfoUpcoming()
            else -> InfoLater()
        }
    }

    if (showTravelDialog) TravelModeModal(onDismiss = { showTravelDialog = false }) {
        if (it != userEvent.travelMode) eventVm.updateEventTravelMode(event.id!!, it)
        showTravelDialog = false
    }
}

@Composable
private fun EventCardFooter(
    contentPadding: PaddingValues,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxWidth()
            .pointerInput(onClick) {
                detectTapGestures { onClick() }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider(thickness = 3.dp, color = Colors.Platinum)
        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
    }
}

@Composable
private fun EventCardFooterExpanded(
    event: EventDTO,
    contentPadding: PaddingValues,
    onClick: () -> Unit
) {
    val markerState = rememberMarkerState()
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(event.id) {
        val loc = LatLng(event.latitude, event.longitude)
        cameraPositionState.position = CameraPosition.fromLatLngZoom(loc, 15f)
        markerState.position = loc
    }

    GoogleMap(
        modifier = Modifier
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .fillMaxWidth()
            .height(200.dp)
            .clip(ShapeDefaults.Large),
        cameraPositionState = cameraPositionState,
        googleMapOptionsFactory = {
            GoogleMapOptions().apply {
                liteMode(true) // Use lite mode for better performance
            }
        }
    ) {
        Marker(state = markerState)
    }

    Spacer(Modifier.height(18.dp))

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxWidth()
            .pointerInput(onClick) {
                detectTapGestures { onClick() }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider(thickness = 3.dp, color = Colors.Platinum)
        Icon(Icons.Default.ArrowDropUp, contentDescription = null)
    }
}

@Composable
private fun ColumnDivider() = HorizontalDivider(thickness = 10.dp, color = Color.Transparent)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventHomeScreenTopBar() {
    val (_, _, isSearching, setIsSearching, addFilter) = LocalFields.current
    var query by remember(isSearching) { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearching) {
        // Focus the search field when searching
        if (isSearching) focusRequester.requestFocus()
    }

    @Composable
    fun TopSearchBar() = TextField(
        value = query,
        onValueChange = { query = it },
        placeholder = { Text("Search an event...", style = tsTypography.bodyMedium) },
        trailingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Clear Search")
        },
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(0.9f)
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                // Filter the events
                addFilter(EventFilterKey.SEARCH, emptyList()) {
                    it.filter { e -> e.name.contains(query, ignoreCase = true) }
                }
                setIsSearching(false)
            }
        ),
        singleLine = true,
        textStyle = tsTypography.bodyMedium,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Colors.TeaRose,
            unfocusedContainerColor = Colors.TeaRose,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(50)
    )

    TopAppBar(
        title = {
            if (isSearching) TopSearchBar() else Text("Upcoming Events...", style = tsTypography.headlineSmall)
        },
        actions = {
            Row {
                if (!isSearching) {
                    IconButton(onClick = { setIsSearching(true) }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }

                IconButton(onClick = {}) {
                    Icon(
                        Icons.Default.Public,
                        contentDescription = "Map",
                        modifier = Modifier.size(36.dp),
                        tint = Colors.TeaRose
                    )
                }
            }
        },
        modifier = Modifier
            .bottomBorder(Colors.Platinum, 5.dp)
            .shadow(3.dp),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Colors.White
        )
    )
}

fun Modifier.bottomBorder(color: Color, width: Dp) = drawBehind {
    drawLine(
        color = color,
        start = Offset(0f, size.height),
        end = Offset(size.width, size.height),
        strokeWidth = width.toPx()
    )
}

