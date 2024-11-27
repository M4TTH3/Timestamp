package org.timestamp.backend.service

import com.graphhopper.GraphHopper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.timestamp.backend.config.FirebaseUser
import org.timestamp.backend.model.Event
import org.timestamp.backend.model.EventLink
import org.timestamp.backend.model.User
import org.timestamp.backend.model.UserEvent
import org.timestamp.backend.repository.TimestampEventLinkRepository
import org.timestamp.backend.repository.TimestampEventRepository
import org.timestamp.backend.repository.TimestampUserRepository
import org.timestamp.lib.dto.utcNow
import java.util.*


@Service
class EventService(
    private val db: TimestampEventRepository,
    private val userDb: TimestampUserRepository,
    private val eventLinkDb: TimestampEventLinkRepository,
    private val graphHopperService: GraphHopperService
) {
    fun getAllEvents(): List<Event> = db.findAll()

    /**
     * Get events by user ID. We only return events the user isn't part of yet.
     */
    fun getEventByLinkId(firebaseUser: FirebaseUser, id: UUID): Event? {
        val user = userDb.findById(firebaseUser.uid).orElseThrow()

        val link = eventLinkDb.findByIdOrNull(id) ?: return null
        val threshold = utcNow().minusMinutes(30)
        if (link.createdAt!!.isBefore(threshold)) return null

        val event = link.event!!
        return if (event.userEvents.firstOrNull { it.id.userId == user.id } != null) null else event
    }

    fun getEventById(id: Long): Event? = db.findByIdOrNull(id)

    fun getEvents(firebaseUser: FirebaseUser): List<Event> = db.findAllEventsByUser(firebaseUser.uid)

    /**
     * Create an event with the given user as the creator.
     */
    fun createEvent(userId: String, event: Event): Event? {
        val user = userDb.findByIdOrNull(userId) ?: return null
        event.creator = user.id

        val userEvent = graphHopperService.createUserEvent(user, event)
        event.userEvents.add(userEvent)
        return db.save(event)
    }

    fun createEvent(user: User, event: Event): Event? {
        return createEvent(user.id, event)
    }

    /**
     * Update an event with the given user. The user must be the creator of the event.
     * We will verify the event info by getting the DB version and modifying that.
     * Returns true if the event was updated, false otherwise.
     */
     fun updateEvent(firebaseUser: FirebaseUser, event: Event): Event? {
        val item: Event = db.findByIdOrNull(event.id) ?: return null

        if (item.creator != firebaseUser.uid) return null

        item.latitude = event.latitude
        item.longitude = event.longitude
        item.address = event.address
        item.arrival = event.arrival
        item.description = event.description
        item.name = event.name

        return db.save(item)
    }

    /**
     * Join an event with the given user. The user must exist in the database.
     */
    fun joinEvent(firebaseUser: FirebaseUser, eventLinkId: UUID): Event? {
        val user = userDb.findByIdOrNull(firebaseUser.uid)?: return null
        val link = eventLinkDb.findByIdOrNull(eventLinkId) ?: return null
        val threshold = utcNow().minusMinutes(30)

        if (link.createdAt!!.isBefore(threshold)) return null

        val event = link.event!!
        if (user.id in event.userEvents.map { it.id.userId }) return null

        val userEvent = graphHopperService.createUserEvent(user, event)
        event.userEvents.add(userEvent)

        return db.save(event)
    }

    fun getEventLink(firebaseUser: FirebaseUser, id: Long): EventLink? {
        val event = db.findByIdOrNull(id) ?: return null
        if (event.creator != firebaseUser.uid) return null // Can only get the link if you are the creator

        return eventLinkDb.save(EventLink(event = event))
    }

    /**
     * Delete an event. The user must exist in the database.
     * If the user is the creator of the event, the event will be deleted.
     * If the user is NOT the creator, the user will be removed from the event.
     * Returns true if the event was deleted, false otherwise.
     */
    fun deleteEvent(id: Long, firebaseUser: FirebaseUser): Boolean {
        val item: Event = db.findByIdOrNull(id) ?: return false

        if (item.creator == firebaseUser.uid) {
            db.deleteById(id)
            return true
        }

        val joinRow = item.userEvents.firstOrNull { it.id.userId == firebaseUser.uid } ?: return false
        item.userEvents.remove(joinRow)
        db.save(item)
        return true
    }
}