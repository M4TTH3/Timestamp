package org.timestamp.shared.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.timestamp.shared.dto.EventDTO
import org.timestamp.shared.dto.TravelMode
import org.timestamp.shared.repository.ErrorRepository
import org.timestamp.shared.repository.EventRepository
import org.timestamp.shared.repository.PendingEventRepository
import org.timestamp.shared.repository.UserRepository

private const val THIRTY_SECONDS_MS = 30000L

/**
 * Global view model that holds Auth & Events states
 */
class EventViewModel () : ViewModel() {

    private val userRepo = UserRepository()
    private val eventRepo = EventRepository.Companion()
    private val pendEventRepo = PendingEventRepository.Companion()
    private val errorRepo = ErrorRepository.Companion()

    // This is the event if selected will be used to create or edit the event
    private val _viewEvent: MutableStateFlow<EventDTO?> = MutableStateFlow(null)

    // Whether we are polling events
    private var isPollingEvents = false

    // Events model
    val events: StateFlow<List<EventDTO>> = eventRepo.get()
    val viewEvent = _viewEvent.asStateFlow()

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
     * Start polling the backend for events. This needs to be
     * concurrent to the main thread.
     */
     fun startGetEventsPolling() = viewModelScope.launch {
        if (isPollingEvents) return@launch

        isPollingEvents = true

        while(isPollingEvents) {
            getEvents()
            delay(THIRTY_SECONDS_MS)
        }

        isPollingEvents = false
    }

    fun stopGetEventsPolling() { isPollingEvents = false }

    /**
     * Fetch ALL events to update UI
     */
    suspend fun getEvents() = eventRepo.getEvents()

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

    fun kickUser(eventId: Long, userId: String) = viewModelScope.launch {
        eventRepo.kickUser(eventId, userId)
    }

    /**
     * This will update an event, and update the local state for that
     * event only. Improves latency.
     */
    fun updateEvent(event: EventDTO) = viewModelScope.launch {
        eventRepo.patchEvent(event)
    }

    fun updateEventTravelMode(eventId: Long, travelMode: TravelMode?) = viewModelScope.launch {
        eventRepo.patchEventTravelMode(eventId, travelMode)
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

    fun setViewEvent(event: EventDTO?) {
        _viewEvent.value = event
    }
}