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
import org.timestamp.mobile.R

/**
 * Global view model that holds Auth & Events states
 */
class AppViewModel(private val application: Application) : AndroidViewModel(application) {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _events: MutableStateFlow<List<EventDetailed>> = MutableStateFlow(emptyList())
    val events: StateFlow<List<EventDetailed>> = _events

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

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
     * Fetch ALL events to update UI
     */
    fun getEvents() {
        _loading.value = true
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val token = auth.currentUser?.getIdToken(false)?.await()?.token
                val endpoint = "${application.getString(R.string.backend_url)}/events"
                val res = ktorClient.get(endpoint) {
                    headers {
                        append("Authorization", "Bearer $token")
                    }
                }

                if (res.status.isSuccess()) {
                    val eventList: List<EventDetailed> = res.body()
                    Log.d("Backend Pull", "Updated Contents: $eventList")

                    withContext(Dispatchers.Main) {
                        _events.value = eventList.sortedBy { it.arrival }
                        _loading.value = false
                    }
                } else {
                    Log.println(Log.ERROR, "Backend Pull Error", res.status.toString())
                }
            }
        } catch (e: Exception) {
            Log.e("Backend Pull Error", e.toString())
            _error.value = e.toString()
        }

        _loading.value = false
    }

    /**
     * This will ping a request to the backend to verify the token.
     * It will also create the user if required.
     */
     fun pingBackend() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = auth.currentUser?.getIdToken(false)?.await()?.token
                val endpoint = "${application.getString(R.string.backend_url)}/users/me"
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

    fun postEvent(event: EventDetailed) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val endpoint = "${application.getString(R.string.backend_url)}/events"
                val tokenResult = auth.currentUser?.getIdToken(false)?.await()
                val res = ktorClient.post(endpoint) {
                    contentType(ContentType.Application.Json)
                    setBody(event)
                    headers {
                        append("Authorization", "Bearer ${tokenResult?.token}")
                    }
                }

                // Check response
                if (res.status.isSuccess()) {
                    Log.d("Events Post", "Successfully created: ${res.bodyAsText()}")
                    getEvents()
                } else {
                    Log.e("Events Post", "res status: ${res.status}, $event")
                }
            } catch (e: Exception) {
                Log.e("Events Post", e.toString())
            }
        }
    }

    fun deleteEvent(eventId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val endpoint = "${application.getString(R.string.backend_url)}/events/$eventId"
                val tokenResult = auth.currentUser?.getIdToken(false)?.await()

                val res = ktorClient.delete(endpoint) {
                    headers {
                        append("Authorization", "Bearer ${tokenResult?.token}")
                    }
                }

                // Check response
                if (res.status.isSuccess()) {
                    Log.d("Events Delete", "Successfully deleted $eventId")
                    getEvents()
                } else {
                    Log.e("Events Delete", "res status: $res")
                }
            } catch (e: Exception) {
                Log.e("Events Post", e.toString())
            }
        }
    }
}