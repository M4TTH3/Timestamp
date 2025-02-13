package org.timestamp.mobile.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import org.timestamp.lib.dto.EventDTO
import org.timestamp.lib.dto.timeString
import org.timestamp.mobile.ui.previewdata.eventsTest
import org.timestamp.mobile.ui.previewdata.testEvent
import org.timestamp.mobile.ui.theme.Colors
import org.timestamp.mobile.ui.theme.tsTypography
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
@Preview
fun MapScreen(

) {
    FullScreenMap()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
private fun FullScreenMap(
    eventSelected: EventDTO? = testEvent,
    events: List<EventDTO> = eventsTest
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    var userId: String? by remember { mutableStateOf(null) }
    val moveToUser: (String) -> Unit = { userId = it }
    val resetUser: () -> Unit = { userId = null }

    var showSearch by remember { mutableStateOf(false) }
    var e by remember(eventSelected?.id) { mutableStateOf(eventSelected) }
    val setEvent: (EventDTO?) -> Unit = {
        it?.let { e = it }
        showSearch = false
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            e?.let { ScaffoldList(it, moveToUser) }
        },
        sheetPeekHeight = if (e != null) 60.dp else 0.dp,
        sheetContainerColor = Colors.White
    ) {
        Box {
            GoogleMapView(
                e = e,
                userId = userId,
                resetUser = resetUser
            ) {
                e?.let { e ->
                    e.users.forEach {
                        // Only visible if they've arrived too
                        val lat = it.latitude
                        val lon = it.longitude
                        if (lat != null && lon != null && it.arrived) {
                            val painter = rememberAsyncImagePainter(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(it.pfp)
                                    .allowHardware(false)
                                    .build()
                            )

                            MarkerComposable(
                                keys = arrayOf(it.id, painter.state),
                                state = rememberMarkerState(position = LatLng(lat, lon)),
                                title = it.name.ifBlank { "Unknown" },
                                snippet = it.email
                            ) {
                                Image(
                                    painter = painter,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                )
                            }
                        }
                    }

                    Marker(
                        state = rememberMarkerState(position = LatLng(e.latitude, e.longitude)),
                        title = e.name,
                        snippet = e.description,
                        draggable = false
                    )
                }

                if (e == null) {
                    events.forEach {
                        Marker(
                            state = rememberMarkerState(position = LatLng(it.latitude, it.longitude)),
                            title = it.name,
                            snippet = it.description,
                            draggable = false
                        )
                    }
                }
            }

            TopBar(
                e = e,
                navigateBack = { },
                showSearch = { showSearch = it },
                resetEvent = { setEvent(null) }
            )
        }
    }

    if (!showSearch) return

    SelectEventDialog(
        events = events,
        callback = setEvent
    )
}

@Composable
@Preview
private fun GoogleMapView(
    e: EventDTO? = null,
    userId: String? = null,
    modifier: Modifier = Modifier,
    resetUser: () -> Unit = {},
    content: @Composable @GoogleMapComposable (() -> Unit) = { }
) {
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(UW_LATITUDE, UW_LONGITUDE), 15f)
    }

    LaunchedEffect(userId) {
        if (userId != null) {
            val user = e?.users?.find { it.id == userId }
            user?.let {
                cameraState.position = CameraPosition.fromLatLngZoom(
                    LatLng(
                        it.latitude ?: UW_LATITUDE,
                        it.longitude ?: UW_LONGITUDE
                    ),
                    20f
                )
            }
            resetUser()
        }
    }

    LaunchedEffect(e?.id) {
        if (e != null) {
            cameraState.position = CameraPosition.fromLatLngZoom(
                LatLng(e.latitude, e.longitude),
                15f
            )
        }
    }

    GoogleMap(
        cameraPositionState = cameraState,
        modifier = Modifier.fillMaxSize(),
        googleMapOptionsFactory = {
            GoogleMapOptions().apply {

            }
        },
        properties = MapProperties(),
        uiSettings = MapUiSettings(
            mapToolbarEnabled = false,
            zoomControlsEnabled = false
        ),
        content = content
    )
}

@Composable
private fun TopBar(
    e: EventDTO? = null,
    navigateBack: () -> Unit = {},
    showSearch: (Boolean) -> Unit = {},
    resetEvent: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(top = 16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        IconButton(onClick = navigateBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back", tint = Colors.Black,
                modifier = Modifier.size(24.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(end = 32.dp)
                .fillMaxWidth(0.9f)
                .border(BorderStroke(1.dp, Colors.TeaRose), ShapeDefaults.ExtraLarge)
                .background(Colors.White, ShapeDefaults.ExtraLarge)
                .clickable { showSearch(true) }
        ) {
            val defaultText = "Select Event"

            Text(
                e?.name?.ifBlank { defaultText } ?: defaultText,
                style = tsTypography.bodyMedium,
                color = Colors.Gray,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Icon(
                Icons.Default.Close,
                contentDescription = "Reset Event",
                tint = Colors.Black,
                modifier = Modifier.size(24.dp).clickable {  }
            )
        }
    }
}

@Composable
@Preview
private fun ScaffoldList(
    e: EventDTO = testEvent,
    moveToUser: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier.height(300.dp).verticalScroll(rememberScrollState())
    ) {

        e.users.sortedBy {
            val arrivalTime = OffsetDateTime.now().plusSeconds((it.timeEst ?: 0L).floorDiv(1000))
            val isOnTime = arrivalTime.isBefore(e.arrival)
            when {
                it.arrived -> 0
                isOnTime -> 1
                else -> 2
            }
        }.forEach {
            val timeEst = it.timeEst
            val arrivalTime = OffsetDateTime.now().plusSeconds((timeEst ?: 0L).floorDiv(1000))
            val isOnTime = arrivalTime.isBefore(e.arrival)

            ListItem(
                overlineContent = { Text(it.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                headlineContent = { Text(it.email, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                trailingContent = {
                    if (!it.arrived && timeEst != null) {
                        Text("ETA: ${it.timeString()}")
                    }
                },
                supportingContent = {
                    when {
                        it.arrived -> Text("Arrived", color = Colors.Green)
                        isOnTime -> Text("On time", color = Colors.Green)
                        else -> Text("Late", color = Colors.BittersweetDark)
                    }
                },
                leadingContent = {
                    val painter = rememberAsyncImagePainter(it.pfp)
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Colors.White),
                modifier = Modifier.clickable { if (it.arrived) moveToUser(it.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectEventDialog(
    events: List<EventDTO>,
    callback: (EventDTO?) -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd")
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mma")
    BasicAlertDialog(
        onDismissRequest = { callback(null) },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .height(400.dp)
                .background(Color.White, ShapeDefaults.ExtraLarge)
                .clip(ShapeDefaults.ExtraLarge)
                .verticalScroll(rememberScrollState())
        ) {
            events.forEach {
                ListItem(
                    overlineContent = {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                        ) {
                            Text(
                                it.arrival.format(dateFormatter),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                it.arrival.format(timeFormatter),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    headlineContent = { Text(it.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    supportingContent = {
                        Text("${it.description}, ${it.address}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    modifier = Modifier.clickable { callback(it) },
                    colors = ListItemDefaults.colors(containerColor = Colors.White)
                )

                HorizontalDivider(
                    thickness = 2.dp,
                    color = Colors.Platinum,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}