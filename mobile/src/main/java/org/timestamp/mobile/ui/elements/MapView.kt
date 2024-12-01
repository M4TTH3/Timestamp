package org.timestamp.mobile.ui.elements

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.request.ImageRequest
import coil.size.Size
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseUser
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import org.timestamp.lib.dto.EventDTO
import org.timestamp.mobile.R
import org.timestamp.mobile.TimestampActivity
import org.timestamp.mobile.models.EventViewModel
import org.timestamp.mobile.models.LocationViewModel
import org.timestamp.mobile.ui.theme.Colors

fun Bitmap.toCircularBitmap(): Bitmap {
    // First, ensure we have a software bitmap
    val softwareBitmap = if (this.isMutable) {
        this
    } else {
        this.copy(Bitmap.Config.ARGB_8888, true)
    }

    val size = minOf(softwareBitmap.width, softwareBitmap.height)
    val output = Bitmap.createBitmap(size, size + 20, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    val paint = Paint().apply {
        isAntiAlias = true
        shader = BitmapShader(softwareBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }

    val centerX = size / 2f
    val centerRadius = size / 2f

    val borderPaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    canvas.drawCircle(centerX, centerX, centerRadius - 5, borderPaint)
    canvas.drawCircle(centerX, centerX, centerRadius - 7, paint)

    val trianglePaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.BLACK
    }

    val path = Path().apply {
        moveTo(centerX - 15f, centerX + centerRadius - 2) // Left point of the triangle
        lineTo(centerX + 15f, centerX + centerRadius - 2) // Right point of the triangle
        lineTo(centerX, centerX + centerRadius + 25f) // Bottom point of the triangle
        close()
    }

    canvas.drawPath(path, trianglePaint)

    return output
}

@Composable
fun MapView(
    eventViewModel: EventViewModel = viewModel(LocalContext.current as TimestampActivity),
    locationViewModel: LocationViewModel = viewModel(LocalContext.current as TimestampActivity),
    currentUser: FirebaseUser?
) {
    var googleMapInstance: GoogleMap? by remember { mutableStateOf(null) }
    var userMarker: Marker? by remember { mutableStateOf(null) }

    val context = LocalContext.current
    val density = LocalDensity.current
    val eventListState = eventViewModel.events.collectAsState()
    val eventList: MutableList<EventDTO> = eventListState.value.toMutableList()

    val locationState by locationViewModel.location.collectAsState()
    val userLocation = locationState?.let { location ->
        LatLng(location.latitude, location.longitude).also {
            Log.d("MAP LOCATION STATE", "New user location: ${it.latitude}, ${it.longitude}")
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = userLocation?.let { CameraPosition.fromLatLngZoom(it, 12f) } ?:
        CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 12f)
    }

    var isDropdownExpanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("Event Select") }
    var dropTextFieldSize by remember { mutableStateOf(DpSize.Zero)}

    var pfpMarker by remember { mutableStateOf<BitmapDescriptor?>(null) }
    val pfp = currentUser?.photoUrl
    val loader = ImageLoader(context = LocalContext.current)
    val request = ImageRequest.Builder(LocalContext.current)
        .data(pfp)
        .size(Size.ORIGINAL)
        .target{ result ->
            val bitmap = result.toBitmap().toCircularBitmap()
            pfpMarker = BitmapDescriptorFactory.fromBitmap(bitmap)
        }
        .build()
    loader.enqueue(request)

    LaunchedEffect(cameraPositionState.position) {
        googleMapInstance?.animateCamera(
            CameraUpdateFactory.newCameraPosition(cameraPositionState.position)
        )
    }

    LaunchedEffect(userLocation) {
        userLocation?.let { location ->
            Log.d("MAP USER LOCATION", "${location.latitude}, ${location.longitude}")
            googleMapInstance?.let { googleMap ->
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 12f)
                )

                if (userMarker == null) {
                    userMarker = googleMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(location.latitude, location.longitude))
                            .title("My Position")
                            .icon(pfpMarker)
                    )
                } else {
                    userMarker?.position = LatLng(location.latitude, location.longitude)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(860.dp)
            .background(Colors.White)
            .pointerInput(Unit) {
                detectTapGestures {  }
            }
    ) {
        AndroidView(
            modifier = Modifier
                .matchParentSize(),
            factory = { context ->
                MapView(context).apply {
                    onCreate(Bundle())
                    onResume()
                    getMapAsync(OnMapReadyCallback { googleMap ->
                        googleMapInstance = googleMap

                        googleMap.uiSettings.isZoomControlsEnabled = true
                        googleMap.uiSettings.isMyLocationButtonEnabled = true


                        userLocation?.let {
                            MarkerOptions()
                                .position(it)
                                .title("My location")
                        }?.let {
                            googleMap.addMarker(
                                it
                            )
                        }

                        googleMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(0.0, 0.0))
                                .title("test")
                                .icon(pfpMarker)
                        )

                        for (event in eventList) {
                            googleMap.addMarker(
                                MarkerOptions()
                                    .position(LatLng(event.latitude, event.longitude))
                                    .title(event.name)
                            )
                        }

                    })
                }
            },
            update = { mapView ->
                mapView.onResume()
            }
        )
        Column(
            modifier = Modifier
                .padding(vertical = 24.dp)
                .offset(x = 24.dp)
                .align(Alignment.BottomStart)
        ) {
            OutlinedTextField(
                value = selectedText,
                onValueChange = { selectedText = it },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(50.dp)
                    .background(Colors.White)
                    .clickable {
                        isDropdownExpanded = !isDropdownExpanded
                    }
                    .onGloballyPositioned { coordinates ->
                        with(density) {
                            dropTextFieldSize = DpSize(
                                width = coordinates.size.width.toDp(),
                                height = coordinates.size.height.toDp()
                            )
                        }
                    },
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_drop_up),
                        contentDescription = "dropdown arrow",
                        modifier = Modifier
                            .size(32.dp)
                            .clickable {
                                isDropdownExpanded = !isDropdownExpanded
                            }
                    )
                }
            )
            DropdownMenu(
                modifier = Modifier
                    .width(Dp(dropTextFieldSize.width.value))
                    .offset(x = 24.dp),
                expanded = isDropdownExpanded,
                onDismissRequest = {
                    isDropdownExpanded = false
                }
            ) {
                eventList.forEach { event ->
                    DropdownMenuItem(
                        modifier = Modifier
                            .height(Dp(dropTextFieldSize.height.value)),
                        onClick = {
                            selectedText = event.name
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                LatLng(event.latitude, event.longitude),
                                15f
                            )
                            Log.d("CAMERA UPDATE", "${event.latitude}, ${event.longitude}")
                            isDropdownExpanded = false
                        },
                    ) {
                        Text(
                            text = event.name,
                            color = Colors.Black
                        )
                    }
                }
            }
        }
    }
}