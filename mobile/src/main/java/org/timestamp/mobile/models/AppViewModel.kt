package org.timestamp.mobile.models

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.timestamp.backend.viewModels.EventDetailed
import org.timestamp.backend.viewModels.LocationVm
import org.timestamp.mobile.R
import java.util.UUID

val ktorClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

/**
 * Global view model that holds Auth & Events states
 */
class AppViewModel (
    private val application: Application
) : AndroidViewModel(application) {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _events: MutableStateFlow<List<EventDetailed>> = MutableStateFlow(emptyList())
    val events: StateFlow<List<EventDetailed>> = _events

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val base = application.getString(R.string.backend_url)

    private suspend fun getToken(): String? = auth.currentUser?.getIdToken(false)?.await()?.token

    /**
     * This will ping a request to the backend to verify the token.
     * It will also create the user if required.
     */
    fun pingBackend() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = getToken()
                val endpoint = "$base/users/me"
                val res = ktorClient.post(endpoint) {
                    headers {
                        append("Authorization", "Bearer $token")
                    }
                }

                Log.i("ID TOKEN", "ID TOKEN: $token")
                if (res.status == HttpStatusCode.OK) Log.i("Verifying ID", res.bodyAsText())
                else Log.e("Verifying ID", res.bodyAsText())
            } catch(e: Exception) {
                Log.e("Ping Backend Error", e.toString())
            }
        }
    }

    fun updateLocation(location: LocationVm) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = getToken()
                val endpoint = "$base/users/me/location"
                val res = ktorClient.patch(endpoint) {
                    headers {
                        append("Authorization", "Bearer $token")
                    }
                    contentType(ContentType.Application.Json)
                    setBody(location)
                }

                if (res.status.isSuccess()) Log.i("Update Location", "Success")
                else Log.e("Update Location", res.toString())
            } catch(e: Exception) {
                Log.e("Ping Backend Error", e.toString())
            }
        }
    }

    /**
     * Get Event Link
     */
    suspend fun getEventLink(eventId: Long): String? {
        return withContext(Dispatchers.IO) {
            val token = getToken()
            val endpoint = "$base/events/link/$eventId"

            try {
                val res = ktorClient.get(endpoint) {
                    headers {
                        append("Authorization", "Bearer $token")
                    }
                }
                Log.d("Event Link Get", "Link: ${res.bodyAsText()}")
                if (!res.status.isSuccess()) throw Exception(res.status.description)
                val eventLink = "$base/${res.body<String>()}"
                Log.d("Event Link Get", "Link: $eventLink")
                return@withContext eventLink
            } catch (e: Exception) {
                Log.e("Event Link Get", e.toString())
                _error.value = e.toString()
            }

            null
        }
    }

    /**
     * Fetch ALL events to update UI
     */
    fun getEvents() {
        _loading.value = true
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val token = getToken()
                val endpoint = "${application.getString(R.string.backend_url)}/events"
                val res = ktorClient.get(endpoint) {
                    headers {
                        append("Authorization", "Bearer $token")
                    }
                }

                if (res.status.isSuccess()) {
                    val eventList: List<EventDetailed> = res.body()
                    Log.d("Events Get", "Updated Contents: $eventList")

                    withContext(Dispatchers.Main) {
                        _events.value = eventList.sortedBy { it.arrival }
                        _loading.value = false
                    }
                } else {
                    Log.println(Log.ERROR, "Events Get", res.status.toString())
                }
            }
        } catch (e: Exception) {
            Log.e("Events Get", e.toString())
            _error.value = e.toString()
        }

        _loading.value = false
    }

    /**
     * Post an event, and modify the current list with the new event.
     */
    fun postEvent(event: EventDetailed) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val endpoint = "${application.getString(R.string.backend_url)}/events"
                val tokenResult = getToken()
                val res = ktorClient.post(endpoint) {
                    contentType(ContentType.Application.Json)
                    setBody(event)
                    headers {
                        append("Authorization", "Bearer $tokenResult")
                    }
                }

                // Check response
                if (res.status.isSuccess()) {
                    Log.d("Events Post", "Successfully created: ${res.bodyAsText()}")
                    val e: EventDetailed = res.body()
                    val newList = events.value + e // Insert it into the list and create an update
                    Log.d("Events Post", newList.toString())
                    withContext(Dispatchers.Main) {
                        _events.value = newList
                    }
                } else {
                    Log.e("Events Post", "res status: ${res.status}, $event")
                }
            } catch (e: Exception) {
                Log.e("Events Post", e.toString())
            }
        }
    }

    /**
     * Delete an event, and modify the current list without the current event
     */
    fun deleteEvent(eventId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val endpoint = "${application.getString(R.string.backend_url)}/events/$eventId"
                val tokenResult = getToken()

                val res = ktorClient.delete(endpoint) {
                    headers {
                        append("Authorization", "Bearer $tokenResult")
                    }
                }

                if (res.status.isSuccess()) {
                    // Delete the element from the current list
                    val index = events.value.indexOfFirst { it.id == eventId }
                    val newList = events.value.toMutableList()
                    newList.removeAt(index)
                    withContext(Dispatchers.Main) {
                        _events.value = newList
                    }

                    Log.d("Events Delete", "Successfully deleted $eventId")
                } else {
                    Log.e("Events Delete", "res status: $res")
                }
            } catch (e: Exception) {
                Log.e("Events Delete", e.toString())
            }
        }
    }

    /**
     * This will update an event, and update the local state for that
     * event only. Improves latency.
     */
    fun updateEvent(event: EventDetailed) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val endpoint = "$base/events"
                val token = getToken()

                val res = ktorClient.patch(endpoint) {
                    contentType(ContentType.Application.Json)
                    setBody(event)
                    headers {
                        append("Authorization", "Bearer $token")
                    }
                }

                if (res.status.isSuccess()) {
                    val newEvent: EventDetailed = res.body()
                    val newEventList = events.value.toMutableList().map {
                        if (it.id == newEvent.id) newEvent else it
                    }

                    withContext(Dispatchers.Main) {
                        _events.value = newEventList
                    }
                } else {
                    Log.e("Events Update", res.toString())
                }
            } catch (e: Exception) {
                Log.e("Events Update", e.toString())
            }
        }
    }

    /**
     * Join an event given a specific ID, update the current state on success
     */
    fun joinEvent(eventLinkId: UUID) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val endpoint = "$base/events/join/$eventLinkId"
                val token = getToken()

                val res = ktorClient.post(endpoint) {
                    headers {
                        append("Authorization", "Bearer $token")
                    }
                }

                // Check response
                if (res.status.isSuccess()) {
                    val newEvent: EventDetailed = res.body()
                    val newList = _events.value + newEvent

                    withContext(Dispatchers.Main) {
                        _events.value = newList
                    }
                } else {
                    Log.e("Events Join", res.toString())
                }
            } catch (e: Exception) {
                Log.e("Events Join", e.toString())
            }
        }
    }

    /**
     * Get a single event and return it. Used for showing info when joining one.
     */
    suspend fun getEvent(eventId: Long): EventDetailed? {
        return withContext(Dispatchers.IO) {
            try {
                val endpoint = "$base/events/$eventId"
                val token = getToken()

                val res = ktorClient.get(endpoint) {
                    headers {
                        append("Authorization", "Bearer $token")
                    }
                }

                if (res.status.isSuccess()) {
                    res.body<EventDetailed>() // return the event
                }
            } catch (e: Exception) {
                Log.e("Event Get", e.toString())
            }

            null // Return null default case
        }
    }
}