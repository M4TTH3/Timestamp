package org.timestamp.mobile.repository

import android.util.Log
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.timestamp.lib.dto.EventDTO
import org.timestamp.lib.dto.TravelMode
import org.timestamp.mobile.utility.KtorClient.success

class EventRepository private constructor(): BaseRepository<List<EventDTO>>(
    emptyList(),
    "Events Repository"
) {

    /* --- Backend Request Operations --- */

    suspend fun getEvents() {
        val tag = "Events Get"
        handler(tag) {
            val endpoint = "events"
            val body: List<EventDTO>? = ktorClient.get(endpoint).bodyOrNull(tag)
            body?.let { set(it) }
        }
    }

    suspend fun postEvent(event: EventDTO) {
        val tag = "Events Post"
        handler(tag) {
            val endpoint = "events"
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
            val endpoint = "events/$eventId"
            val res = ktorClient.delete(endpoint)
            if (res.success(tag)) delete(eventId)
        }
    }

    suspend fun kickUser(eventId: Long, userId: String) {
        val tag = "Events Kick User"
        handler(tag) {
            val endpoint = "events/$eventId/kick/$userId"
            val res = ktorClient.delete(endpoint)
            if (res.success(tag)) deleteUser(eventId, userId)
        }
    }

    suspend fun patchEvent(event: EventDTO) {
        val tag = "Events Patch"
        handler(tag) {
            val endpoint = "events"
            val body: EventDTO? = ktorClient.patch(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(event)
            }.bodyOrNull(tag)

            body?.let { update(it) }
        }
    }

    suspend fun patchEventTravelMode(eventId: Long, travelMode: TravelMode?) {
        val tag = "Events Patch Travel Mode"
        handler(tag) {
            val endpoint = "events/$eventId/travel-mode"
            val body: EventDTO? = ktorClient.patch(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(travelMode)
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

    fun deleteUser(eventId: Long, userId: String) {
        val newList = state.map {
            if (it.id == eventId) {
                val newUsers = it.users.filter { eu -> eu.id != userId }
                it.copy(users = newUsers)
            } else it
        }
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