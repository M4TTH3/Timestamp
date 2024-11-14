package org.timestamp.backend

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.timestamp.backend.config.FirebaseUser
import org.timestamp.backend.model.Event
import org.timestamp.backend.model.User
import org.timestamp.backend.service.EventService
import org.timestamp.backend.service.UserService
import org.timestamp.backend.viewModels.EventDetailed
import java.net.URI

@RestController
@RequestMapping("/events")
class EventController(
    private val eventService: EventService
) {

    @GetMapping
    suspend fun getEvents(@AuthenticationPrincipal firebaseUser: FirebaseUser): ResponseEntity<List<EventDetailed>> {
        val events = eventService.getEvents(firebaseUser)
        return ResponseEntity.ok(events.map { EventDetailed.fromEvent(it) })
    }

    @PostMapping
    fun createEvent(
        @AuthenticationPrincipal firebaseUser: FirebaseUser,
        @RequestBody event: Event
    ): ResponseEntity<Event> {
        val e = eventService.createEvent(User(firebaseUser), event)
        return ResponseEntity.created(URI("/events/${e.id}")).body(e)
    }

    @GetMapping("/{id}")
    suspend fun getEvent(
        @AuthenticationPrincipal firebaseUser: FirebaseUser,
        @PathVariable id: Long
    ): ResponseEntity<EventDetailed> {
        val e = eventService.getEventById(firebaseUser, id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(EventDetailed.fromEvent(e))
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
        var success = eventService.deleteEvent(id, firebaseUser)
        if (!success) success = eventService.removeUserFromEvent(id, firebaseUser)
        return if (success) ResponseEntity.noContent().build() else ResponseEntity.notFound().build()
    }

}