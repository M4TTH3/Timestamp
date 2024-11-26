package org.timestamp.mobile.models

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.plugin
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.timestamp.lib.dto.EventDTO
import org.timestamp.lib.dto.EventLinkDTO
import org.timestamp.lib.dto.LocationDTO
import org.timestamp.lib.dto.TravelMode
import org.timestamp.mobile.R
import java.util.UUID

/**
 * Global view model that holds Auth & Events states
 */
class AppViewModel (
    application: Application
) : AndroidViewModel(application) {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private var isPollingEvents = false
    private var isTrackingLocation = false

    // User Location
    var location: LatLng = LatLng(0.0, 0.0)
    private var trackingInterval : Long = 1000L

    // Events model
    private val _events: MutableStateFlow<List<EventDTO>> = MutableStateFlow(emptyList())
    val events: StateFlow<List<EventDTO>> = _events

    // Loading progress
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    // Error progress
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Used if there is a pending event join
    // We will prefetch _pendingEvent to make requests look faster
    private val _pendingEventLink: MutableStateFlow<UUID?> = MutableStateFlow(null)
    private val _pendingEvent: MutableStateFlow<EventDTO?> = MutableStateFlow(null)
    val pendingEventLink: StateFlow<UUID?> = _pendingEventLink
    val pendingEvent: StateFlow<EventDTO?> = _pendingEvent

    private val base = application.getString(R.string.backend_url) // Base Url of backend
    private val eventJoinBase = "$base/events/join" // Base Url for Events Join

    private val ioCoroutineScope = CoroutineScope(Dispatchers.IO) // Used for coroutine

    private suspend fun getToken(): String? = auth.currentUser?.getIdToken(false)?.await()?.token

    private val ktorClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    /**
     * Inject the Authorization header into the ktorClient.
     * Required for backend authorization & access.
     */
    init {
        ktorClient.plugin(HttpSend).intercept { req ->
            val token = getToken()
            req.headers.append("Authorization", "Bearer $token")
            execute(req)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Safely close it
        ktorClient.close()
    }

    private suspend fun <T> handler(
        tag: String = "Backend Request",
        onError: suspend () -> Unit = {},
        action: suspend () -> T?
    ): T? {
        try {
            return action()
        } catch (e: Exception) {
            Log.e(tag, e.toString())
            _error.value = e.toString()
            onError()
        }
        return null
    }

    /**
     * This will ping a request to the backend to verify the token.
     * It will also create the user if required.
     */
    fun pingBackend() {
        val tag = "Ping Backend"
        ioCoroutineScope.launch {
            handler(tag) {
                val token = getToken()
                val endpoint = "$base/users/me"
                val res = ktorClient.post(endpoint)

                Log.i(tag, "ID TOKEN: $token")
                if (res.status == HttpStatusCode.OK) Log.i("Verifying ID", res.bodyAsText())
                else Log.e(tag, res.bodyAsText())
            }
        }
    }

    fun updateLocation(location: LocationDTO) {
        val tag = "Update Location"
        ioCoroutineScope.launch {
            handler(tag) {
                val endpoint = "$base/users/me/location"
                val res = ktorClient.patch(endpoint) {
                    contentType(ContentType.Application.Json)
                    setBody(location)
                }

                if (res.status.isSuccess()) Log.i(tag, "Success")
                else Log.e(tag, res.toString())
            }
        }
    }

    /**
     * Start polling the backend for events.
     */
    fun startGetEventsPolling() {
        isPollingEvents = true
        viewModelScope.launch {
            while(isPollingEvents) {
                getEvents()
                delay(10000)
            }
        }
    }

    fun stopGetEventsPolling() { isPollingEvents = false }

    private fun fetchCurrentLocation(
        context: Context,
        onLocationRetrieved: (LatLng) -> Unit
    ) {
        val permissionCheck = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
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

    fun updateTrackingInterval(newInterval: Long) {
        trackingInterval = newInterval
    }

    fun startTrackingLocation() {
        isTrackingLocation = true
        viewModelScope.launch {
            while (isTrackingLocation) {
                fetchCurrentLocation(
                    context = getApplication<Application>().applicationContext,
                    onLocationRetrieved = { latlng ->
                        location = latlng
                    }
                )
                updateLocation( LocationDTO(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    travelMode = TravelMode.Car, // for now
                ))
                delay(trackingInterval)
            }
        }
    }

    fun stopTrackingLocation() { isTrackingLocation = false }

    // meters
    fun calculateDistance(
        pos1: LatLng,
        pos2: LatLng,
        onDistanceCalculated: (Long) -> Unit
    ) {
        val location1 = Location("").apply {
            latitude = pos1.latitude
            longitude = pos1.longitude
        }

        val location2 = Location("").apply {
            latitude = pos2.latitude
            longitude = pos2.longitude
        }

        val distanceInMeters = location1.distanceTo(location2).toLong()
        onDistanceCalculated(distanceInMeters)
    }

    /**
     * Fetch ALL events to update UI
     */
    fun getEvents() {
        _loading.value = true

        val tag = "Events Get"
        ioCoroutineScope.launch {
            handler(tag, {_loading.value = false}) {
                val endpoint = "$base/events"
                val res = ktorClient.get(endpoint)

                if (res.status.isSuccess()) {
                    val eventList: List<EventDTO> = res.body()
                    Log.d(tag, "Updated Contents: $eventList")

                    withContext(Dispatchers.Main) {
                        _events.value = eventList.sortedBy { it.arrival }
                        _loading.value = false
                    }
                } else {
                    Log.e(tag, res.status.toString())
                }
            }
        }
    }

    /**
     * Post an event, and modify the current list with the new event.
     */
    fun postEvent(event: EventDTO) {
        val tag = "Events Post"
        ioCoroutineScope.launch {
            handler(tag) {
                val endpoint = "$base/events"
                val res = ktorClient.post(endpoint) {
                    contentType(ContentType.Application.Json)
                    setBody(event)
                }

                // Check response
                if (res.status.isSuccess()) {
                    val e: EventDTO = res.body()
                    val newList = events.value + e // Insert it into the list and create an update
                    withContext(Dispatchers.Main) {
                        _events.value = newList
                    }
                    Log.d(tag, newList.toString())
                } else {
                    Log.e(tag, "res status: ${res.status}, $event")
                }
            }
        }
    }

    /**
     * Delete an event, and modify the current list without the current event
     */
    fun deleteEvent(eventId: Long) {
        val tag = "Events Delete"
        ioCoroutineScope.launch {
            handler(tag) {
                val endpoint = "$base/events/$eventId"
                val res = ktorClient.delete(endpoint)

                if (res.status.isSuccess()) {
                    // Delete the element from the current list
                    val index = events.value.indexOfFirst { it.id == eventId }
                    val newList = events.value.toMutableList()
                    newList.removeAt(index)
                    withContext(Dispatchers.Main) {
                        _events.value = newList
                    }

                    Log.d(tag, "Successfully deleted $eventId")
                } else {
                    Log.e(tag, "res status: $res")
                }
            }
        }
    }

    /**
     * This will update an event, and update the local state for that
     * event only. Improves latency.
     */
    fun updateEvent(event: EventDTO) {
        val tag = "Events Update"
        ioCoroutineScope.launch {
            handler(tag) {
                val endpoint = "$base/events"
                val res = ktorClient.patch(endpoint) {
                    contentType(ContentType.Application.Json)
                    setBody(event)
                }

                if (res.status.isSuccess()) {
                    val newEvent: EventDTO = res.body()
                    val newEventList = events.value.toMutableList().map {
                        if (it.id == newEvent.id) newEvent else it
                    }

                    withContext(Dispatchers.Main) {
                        _events.value = newEventList
                    }
                } else {
                    Log.e(tag, res.toString())
                }
            }
        }
    }

    /**
     * Get Event Link base_url/events/join/
     */
    suspend fun getEventLink(eventId: Long): String? {
        val tag = "Event Link"
        return withContext(Dispatchers.IO) {
            handler(tag) {
                val endpoint = "$base/events/link/$eventId"
                val res = ktorClient.get(endpoint)
                if (!res.status.isSuccess()) throw Exception(res.status.description)
                val eventLinkDTO: EventLinkDTO = res.body()
                val eventLink = "$eventJoinBase/${eventLinkDTO.id}"
                Log.d(tag, "Link: $eventLink")
                return@handler eventLink
            }
        }
    }

    /**
     * Join the current pending event and add it to the list on accept.
     * Reset all information on pending events
     */
    fun joinPendingEvent() {
        val tag = "Events Join"
        ioCoroutineScope.launch {
            handler(tag) {
                val endpoint = "$eventJoinBase/${pendingEventLink.value}"
                val res = ktorClient.post(endpoint)
                val success = res.status.isSuccess()

                if (success) {
                    val newEvent: EventDTO = res.body()
                    val newList = _events.value + newEvent

                    withContext(Dispatchers.Main) {
                        _events.value = newList

                        // Remove the current pending events
                        _pendingEvent.value = null
                        _pendingEventLink.value = null
                    }
                } else {
                    Log.e(tag, res.toString())
                }
            }
        }
    }

    /**
     * Reset the pending events, so the user won't be re-prompted.
     */
    fun cancelPendingEvents() {
        _pendingEvent.value = null
        _pendingEventLink.value = null
    }

    fun setPendingEventLink(uri: Uri?) {
        val tag = "Update Pending Event"
        if (uri == null || uri.toString().startsWith("$eventJoinBase/").not()) return

        try {
            val uuid = UUID.fromString(uri.lastPathSegment)
            _pendingEventLink.value = uuid
        } catch(e: Exception) {
            Log.e(tag, e.toString())
        }
    }

    /**
     * Get a single event and return it. Used for showing info when joining one.
     */
     fun setPendingEvent() {
        val tag = "Update Pending Event"
        val uuid = pendingEventLink.value ?: return

        ioCoroutineScope.launch {
            handler(tag) {
                val endpoint = "$base/events/$uuid"
                val res = ktorClient.get(endpoint)

                val success = res.status.isSuccess()
                if (success) {
                    val event = res.body<EventDTO>()
                    withContext(Dispatchers.Main) {
                        _pendingEvent.value = event
                    }

                    Log.d(tag, "Link UUID: $uuid, Event: $event")
                } else {
                    Log.e(tag, res.toString())
                }
            }
        }
    }

}