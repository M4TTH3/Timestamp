package org.timestamp.mobile.repository

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.timestamp.lib.dto.EventDTO
import org.timestamp.mobile.utility.KtorClient.success

class EventRepository private constructor(): BaseRepository<List<EventDTO>>(
    emptyList(),
    "Events Repository"
) {

    /* --- Backend Request Operations --- */

    suspend fun getEvents() {
        val tag = "Events Get"
        handler(tag) {
            val endpoint = "/events"
            val body: List<EventDTO>? = ktorClient.get(endpoint).bodyOrNull(tag)
            body?.let { set(it) }
        }
    }

    suspend fun postEvent(event: EventDTO) {
        val tag = "Events Post"
        handler(tag) {
            val endpoint = "/events"
            val body: EventDTO? = ktorClient.post(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(event)
            }.bodyOrNull(tag)

            body?.let { add(it) }
        }
    }

    suspend fun deleteEvent(eventId: Long) {
        val tag = "Events Delete"
        handler(tag) {
            val endpoint = "/events/$eventId"
            val res = ktorClient.delete(endpoint)
            if (res.success(tag)) delete(eventId)
        }
    }

    suspend fun patchEvent(event: EventDTO) {
        val tag = "Events Patch"
        handler(tag) {
            val endpoint = "/events"
            val body: EventDTO? = ktorClient.patch(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(event)
            }.bodyOrNull(tag)

            body?.let { update(it) }
        }
    }

    /* --- Operations on the event list -- Update on Main Thread for UI components --- */

    fun update(event: EventDTO) {
        val newList = state.map { if (it.id == event.id) event else it }
        set(newList, false)
    }

    fun delete(eventId: Long) {
        val newList = state.filter { it.id != eventId }
        set(newList, false)
    }

    fun add(event: EventDTO) {
        val newList = state + event
        set(newList)
    }

    /**
     * Set the events list and update the UI state.
     * @param events List of events to set
     * @param sort Sort the list by arrival time (default: true)
     */
    fun set(events: List<EventDTO>, sort: Boolean = true) {
        val newList = if (sort) events.sortedBy { it.arrival } else events
        state = newList
    }

    /* --- Companion Object - Singleton invocation --- */
    companion object {
        operator fun invoke() = StateRepository { EventRepository() }
    }
}