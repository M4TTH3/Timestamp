package org.timestamp.mobile.models

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.timestamp.lib.dto.EventDTO
import org.timestamp.mobile.repository.ErrorRepository
import org.timestamp.mobile.repository.EventRepository
import org.timestamp.mobile.repository.PendingEventRepository
import org.timestamp.mobile.repository.UserRepository

/**
 * Global view model that holds Auth & Events states
 */
class EventViewModel (
    application: Application
) : AndroidViewModel(application) {

    private val userRepo = UserRepository()
    private val eventRepo = EventRepository()
    private val pendEventRepo = PendingEventRepository()
    private val errorRepo = ErrorRepository()

    // Whether we are polling events
    private var isPollingEvents = false

    // Events model
    val events: StateFlow<List<EventDTO>> = eventRepo.get()

    // Error progress
    val error: StateFlow<Throwable?> = errorRepo.get()

    // Used if there is a pending event join
    val pendingEvent: StateFlow<EventDTO?> = pendEventRepo.get()

    override fun onCleared() {
        super.onCleared()
        stopGetEventsPolling()
    }

    /**
     * This will ping a request to the backend to verify the token.
     * It will also create the user if required.
     */
    fun pingBackend() = viewModelScope.launch {
        userRepo.postUser()
    }

    /**
     * Start polling the backend for events.
     */
    suspend fun startGetEventsPolling() {
        if (isPollingEvents) return

        isPollingEvents = true

        while(isPollingEvents) {
            getEvents()
            delay(10000)
        }

        isPollingEvents = false
    }

    fun stopGetEventsPolling() { isPollingEvents = false }

    /**
     * Fetch ALL events to update UI
     */
    private fun getEvents() = viewModelScope.launch {
        eventRepo.getEvents()
    }

    /**
     * Post an event, and modify the current list with the new event.
     */
    fun postEvent(event: EventDTO) = viewModelScope.launch {
        eventRepo.postEvent(event)
    }

    /**
     * Delete an event, and modify the current list without the current event
     */
    fun deleteEvent(eventId: Long) = viewModelScope.launch {
        eventRepo.deleteEvent(eventId)
    }

    /**
     * This will update an event, and update the local state for that
     * event only. Improves latency.
     */
    fun updateEvent(event: EventDTO) = viewModelScope.launch {
        eventRepo.patchEvent(event)
    }

    /**
     * Get Event Link base_url/events/join/
     */
    suspend fun getEventLink(eventId: Long): String? = withContext(
        viewModelScope.coroutineContext
    ) {
        pendEventRepo.getEventLink(eventId)
    }

    /**
     * Join the current pending event and add it to the list on accept.
     * Reset all information on pending events
     */
    fun joinPendingEvent() = viewModelScope.launch {
        pendEventRepo.joinPendingEvent()
    }

    /**
     * Reset the pending events, so the user won't be re-prompted.
     */
    fun cancelPendingEvents() = viewModelScope.launch {
        pendEventRepo.cancelPendingEvent()
    }

    fun setPendingEventLink(uri: Uri?) = viewModelScope.launch {
        pendEventRepo.setPendingEventLink(uri)
    }

    /**
     * Get a single event and return it. Used for showing info when joining one.
     */
    fun setPendingEvent() = viewModelScope.launch {
        pendEventRepo.setPendingEvent()
    }

}