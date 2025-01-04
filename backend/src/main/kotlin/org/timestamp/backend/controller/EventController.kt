package org.timestamp.backend.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.timestamp.backend.config.FirebaseUser
import org.timestamp.backend.model.User
import org.timestamp.backend.service.EventService
import org.timestamp.lib.dto.EventDTO
import org.timestamp.lib.dto.EventLinkDTO
import org.timestamp.lib.dto.TravelMode
import java.net.URI
import java.util.*

@RestController
@RequestMapping("/events")
class EventController(
    private val eventService: EventService
) {

    @GetMapping
    suspend fun getEvents(@AuthenticationPrincipal firebaseUser: FirebaseUser): ResponseEntity<List<EventDTO>> {
        val events = eventService.getEvents(firebaseUser)
        return ResponseEntity.ok(events)
    }

    @PostMapping
    suspend fun createEvent(
        @AuthenticationPrincipal firebaseUser: FirebaseUser,
        @RequestBody event: EventDTO
    ): ResponseEntity<EventDTO> {
        val e = eventService.createEvent(User(firebaseUser), event)
        return ResponseEntity.created(URI("/events/${e.id}")).body(e)
    }

    @GetMapping("/{id}")
    suspend fun getEventFromLink(
        @AuthenticationPrincipal firebaseUser: FirebaseUser,
        @PathVariable id: UUID
    ): ResponseEntity<EventDTO> {
        val e = eventService.getEventByLinkId(firebaseUser, id)
        return ResponseEntity.ok(e)
    }

    @PostMapping("/join/{eventLinkId}")
    suspend fun joinEvent(
        @AuthenticationPrincipal firebaseUser: FirebaseUser,
        @PathVariable eventLinkId: UUID,
        @RequestParam travelMode: TravelMode? = null
    ): ResponseEntity<EventDTO> {
        val e = eventService.joinEvent(firebaseUser, eventLinkId, travelMode)
        return ResponseEntity.ok(e)
    }

    @GetMapping("/link/{eventId}")
    suspend fun getEventLink(
        @AuthenticationPrincipal firebaseUser: FirebaseUser,
        @PathVariable eventId: Long
    ): ResponseEntity<EventLinkDTO> {
        val eventLinkDTO = eventService.getEventLink(firebaseUser, eventId)
        return ResponseEntity.ok(eventLinkDTO)
    }

    @PatchMapping
    suspend fun updateEvent(
        @AuthenticationPrincipal firebaseUser: FirebaseUser,
        @RequestBody event: EventDTO
    ): ResponseEntity<EventDTO> {
        val e = eventService.updateEvent(firebaseUser, event)
        return ResponseEntity.ok(e)
    }

    @PatchMapping("/{eventId}/travel-mode")
    suspend fun updateEventTravelMode(
        @AuthenticationPrincipal firebaseUser: FirebaseUser,
        @PathVariable eventId: Long,
        @RequestBody travelMode: TravelMode?
    ): ResponseEntity<EventDTO> {
        val e = eventService.updateEventTravelMode(firebaseUser, eventId, travelMode)
        return ResponseEntity.ok(e)
    }

    /**
     * Attempt to delete an event as the creator. Default to try and leave the event
     * if the user is not the creator.
     */
    @DeleteMapping("/{id}")
    fun deleteEvent(
        @AuthenticationPrincipal firebaseUser: FirebaseUser,
        @PathVariable id: Long
    ): ResponseEntity<Unit> {
        val success = eventService.deleteEvent(id, firebaseUser)
        return if (success) ResponseEntity.noContent().build() else ResponseEntity.notFound().build()
    }

    @DeleteMapping("/{id}/kick/{userId}")
    fun kickUser(
        @AuthenticationPrincipal firebaseUser: FirebaseUser,
        @PathVariable id: Long,
        @PathVariable userId: String
    ): ResponseEntity<Unit> {
        val success = eventService.kickUser(id, userId, firebaseUser)
        return if (success) ResponseEntity.noContent().build() else ResponseEntity.badRequest().build()
    }
}