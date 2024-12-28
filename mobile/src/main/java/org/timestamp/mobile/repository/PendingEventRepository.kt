package org.timestamp.mobile.repository

import android.net.Uri
import android.util.Log
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import org.timestamp.lib.dto.EventDTO
import org.timestamp.lib.dto.EventLinkDTO
import org.timestamp.mobile.utility.KtorClient
import org.timestamp.mobile.utility.KtorClient.success
import java.util.UUID

class PendingEventRepository private constructor(): BaseRepository<EventDTO?>(
    null,
    "Pending Event Repository"
) {
    private val eventViewRepository = EventRepository()
    private val base = KtorClient.backendBase
    private val deepLinkPathBase = "events/join"
    private val deepLinkPath = "/$deepLinkPathBase"
    private val deepLinkPrefix = "$base$deepLinkPath"
    private var pendingEventLink: UUID? = null

    /**
     * Get Event Link base_url/events/join/
     */
    suspend fun getEventLink(eventId: Long): String? {
        val tag = "Event Link"
        return handler(tag) {
            val endpoint = "events/link/$eventId"
            val body: EventLinkDTO? = ktorClient.get(endpoint).bodyOrNull(tag)

            return@handler body?.let {
                "$deepLinkPrefix/${it.id}"
            }
        }
    }

    /**
     * Join the current pending event and add it to the list on accept.
     * Reset all information on pending events
     */
    suspend fun joinPendingEvent() {
        val tag = "Events Join"
        handler(tag) {
            val endpoint = "$deepLinkPathBase/$pendingEventLink"
            val res = ktorClient.post(endpoint)

            if (!res.success(tag)) return@handler

            val newEvent: EventDTO = res.body()
            eventViewRepository.add(newEvent)
            cancelPendingEvent()
        }
    }

    /**
     * Set the pending event to the current event. Successful if the event is
     * found, not expired, and the user hasn't joined it yet.
     */
    suspend fun setPendingEvent() {
        val tag = "Set Pending Event"
        val uuid = pendingEventLink ?: return

        handler(tag) {
            val endpoint = "events/$uuid"
            val res = ktorClient.get(endpoint)

            if (!res.success(tag)) return@handler

            val event = res.body<EventDTO>()
            state = event
            Log.d(tag, "Link UUID: $uuid, Event: $event")
        }
    }

    /**
     * Set the pending event link to allow the user to load
     * the pending event when logged in.
     */
    fun setPendingEventLink(uri: Uri?) {
        val tag = "Set Pending Event Link"
        if (uri?.toString()?.startsWith(deepLinkPrefix)?.not() != null) return

        runCatching {
            val uuid = UUID.fromString(uri?.lastPathSegment)
            pendingEventLink = uuid
        }
    }

    fun cancelPendingEvent() {
        // Reset the pending events, so the user won't be re-prompted.
        pendingEventLink = null
        state = null
    }

    companion object {
        operator fun invoke() = StateRepository { PendingEventRepository() }
    }
}