package org.timestamp.backend

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.timestamp.backend.config.FirebaseUser
import org.timestamp.backend.service.EventService
import org.timestamp.backend.service.UserService
import org.timestamp.backend.viewModels.EventDetailed

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

    
}