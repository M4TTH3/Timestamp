package org.timestamp.mobile.repository

import android.net.Uri
import android.util.Log
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.timestamp.lib.dto.EventDTO
import org.timestamp.lib.dto.EventLinkDTO
import org.timestamp.mobile.utility.KtorClient
import org.timestamp.mobile.utility.KtorClient.success
import java.util.*

class PendingEventRepository private constructor(): BaseRepository<EventDTO?>(
    null,
    "Pending Event Repository"
) {
    private val eventViewRepository = EventRepository()
    private val base = KtorClient.backendBase
    private val deepLinkPathBase = "events/join"
    private val deepLinkPrefix = "$base/$deepLinkPathBase"
    private var pendingEventLink: UUID? = null

    /**
     * Get Event Link base_url/events/join/
     */
    suspend fun getEventLink(eventId: Long): String? {
        val tag = "Event Link"
        return handler(tag) {
            val endpoint = "events/link/$eventId"
            val body: EventLinkDTO? = ktorClient.get(endpoint).bodyOrNull(tag)

            body?.let {
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
            ktorClient.post(endpoint).bodyOrNull<EventDTO>(tag)?.let {
                eventViewRepository.add(it)
                cancelPendingEvent()
            }
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
            ktorClient.get(endpoint).bodyOrNull<EventDTO>()?.let {
                state = it
            }
        }
    }

    /**
     * Set the pending event link to allow the user to load
     * the pending event when logged in.
     */
    fun setPendingEventLink(uri: Uri?) {
        val tag = "Set Pending Event Link"
        if (uri == null || !uri.toString().startsWith(deepLinkPrefix)) return

        runCatching {
            val uuid = UUID.fromString(uri.lastPathSegment)
            pendingEventLink = uuid
            Log.d(tag, "Pending Event Link: $uuid")
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