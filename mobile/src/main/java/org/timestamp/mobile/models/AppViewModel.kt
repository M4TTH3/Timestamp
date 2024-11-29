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
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
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
    private val base = application.getString(R.string.backend_url) // Base Url of backend
    private val eventJoinBase = "$base/events/join" // Base Url for Events Join

    private var isPollingEvents = false
    private var isTrackingLocation = false

    // User Location
    private val locationDTO: LocationDTO = LocationDTO(0.0, 0.0, TravelMode.Foot)
    var location: LatLng = LatLng(0.0, 0.0)
    private var trackingInterval : Long = 30000L

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
    val pendingEvent: StateFlow<EventDTO?> = _pendingEvent

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
        ktorClient.close()
    }

    /**
     * Check if we get a successful response. If not, then return false and
     * log the response values.
     */
    private fun HttpResponse.success(tag: String = "Timestamp Request"): Boolean {
        if (this.status.isSuccess()) return true

        Log.e(tag, "${this.status.value} - ${this.status.description}: $this")
        return false
    }

    /**
     * Handler for a request, performs try catch and updates
     * states if required.
     */
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
    fun pingBackend() = ioCoroutineScope.launch {
        val tag = "Ping Backend"
        handler(tag) {
            val token = getToken()
            val endpoint = "$base/users/me"
            val res = ktorClient.post(endpoint)

            if (!res.success(tag)) return@handler
            Log.d(tag, "ID TOKEN: $token")
        }
    }

    fun updateLocation(location: LocationDTO) = ioCoroutineScope.launch {
        val tag = "Update Location"
        handler(tag) {
            val endpoint = "$base/users/me/location"
            val res = ktorClient.patch(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(location)
            }

            if (!res.success(tag)) return@handler
            Log.d(tag, "Updated location with $location")
        }
    }

    /**
     * Start polling the backend for events.
     */
    fun startGetEventsPolling() {
        isPollingEvents = true
        // need events to show relevant information immediately
        fetchCurrentLocation(
            context = getApplication<Application>().applicationContext,
            onLocationRetrieved = { latlng ->
                location = latlng
            }
        )
        updateLocation( LocationDTO(
            latitude = location.latitude,
            longitude = location.longitude,
            travelMode = TravelMode.Foot, // for now
        ))
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
                    Log.d("LOCATION TRACKER", "User Location successfully retrieved: ${location.latitude}, ${location.longitude}")
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

    fun startTrackingLocation() = viewModelScope.launch {
        isTrackingLocation = true
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
                travelMode = TravelMode.Foot, // for now
            ))
            delay(trackingInterval)
        }
    }

    fun stopTrackingLocation() = viewModelScope.launch { isTrackingLocation = false }

    /**
     * Fetch ALL events to update UI
     */
    private fun getEvents() = ioCoroutineScope.launch {
        val tag = "Events Get"
        handler(tag) {
            val endpoint = "$base/events"
            val res = ktorClient.get(endpoint)
            if (!res.success(tag)) return@handler

            val eventList: List<EventDTO> = res.body()
            withContext(Dispatchers.Main) {
                _events.value = eventList.sortedBy { it.arrival }
            }

            Log.d(tag, "Updated Contents: $eventList")
        }
    }

    /**
     * Post an event, and modify the current list with the new event.
     */
    fun postEvent(event: EventDTO) = ioCoroutineScope.launch {
        val tag = "Events Post"
        handler(tag) {
            val endpoint = "$base/events"
            val res = ktorClient.post(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(event)
            }

            // Insert event into the list state
            if (!res.success(tag)) return@handler
            val e: EventDTO = res.body()
            val newList = events.value + e
            withContext(Dispatchers.Main) {
                _events.value = newList
            }

            Log.d(tag, newList.toString())
        }
    }

    /**
     * Delete an event, and modify the current list without the current event
     */
    fun deleteEvent(eventId: Long) = ioCoroutineScope.launch {
        val tag = "Events Delete"
        handler(tag) {
            val endpoint = "$base/events/$eventId"
            val res = ktorClient.delete(endpoint)

            if (!res.success(tag)) return@handler

            // Delete the element from the current list
            val index = events.value.indexOfFirst { it.id == eventId }
            val newList = events.value.toMutableList()
            newList.removeAt(index)
            withContext(Dispatchers.Main) {
                _events.value = newList
            }

            Log.d(tag, "Successfully deleted $eventId")
        }
    }

    /**
     * This will update an event, and update the local state for that
     * event only. Improves latency.
     */
    fun updateEvent(event: EventDTO) = ioCoroutineScope.launch {
        val tag = "Events Update"
        handler(tag) {
            val endpoint = "$base/events"
            val res = ktorClient.patch(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(event)
            }

            if (!res.success(tag)) return@handler
            val newEvent: EventDTO = res.body()
            val newEventList = events.value.toMutableList().map {
                if (it.id == newEvent.id) newEvent else it
            }

            withContext(Dispatchers.Main) {
                _events.value = newEventList
            }
        }
    }

    /**
     * Get Event Link base_url/events/join/
     */
    suspend fun getEventLink(eventId: Long): String? = withContext(Dispatchers.IO) {
        val tag = "Event Link"
        return@withContext handler(tag) {
            val endpoint = "$base/events/link/$eventId"
            val res = ktorClient.get(endpoint)

            if (!res.success(tag)) return@handler null
            val eventLinkDTO: EventLinkDTO = res.body()
            val eventLink = "$eventJoinBase/${eventLinkDTO.id}"
            return@handler eventLink
        }
    }

    /**
     * Join the current pending event and add it to the list on accept.
     * Reset all information on pending events
     */
    fun joinPendingEvent() = ioCoroutineScope.launch {
        val tag = "Events Join"
        handler(tag) {
            val endpoint = "$eventJoinBase/${_pendingEventLink.value}"
            val res = ktorClient.post(endpoint)

            if (!res.success(tag)) return@handler

            val newEvent: EventDTO = res.body()
            val newList = _events.value + newEvent
            withContext(Dispatchers.Main) {
                _events.value = newList

                // Remove the current pending events
                _pendingEvent.value = null
                _pendingEventLink.value = null
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
     fun setPendingEvent() = ioCoroutineScope.launch {
        val tag = "Update Pending Event"
        val uuid = _pendingEventLink.value ?: return@launch

        handler(tag) {
            val endpoint = "$base/events/$uuid"
            val res = ktorClient.get(endpoint)

            if (!res.success(tag)) return@handler

            val event = res.body<EventDTO>()
            withContext(Dispatchers.Main) {
                _pendingEvent.value = event
            }

            Log.d(tag, "Link UUID: $uuid, Event: $event")
        }
    }

}