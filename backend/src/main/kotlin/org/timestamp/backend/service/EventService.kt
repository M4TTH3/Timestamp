package org.timestamp.backend.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.timestamp.backend.config.FirebaseUser
import org.timestamp.backend.model.Arrival
import org.timestamp.backend.model.Event
import org.timestamp.backend.model.User
import org.timestamp.backend.repository.TimestampEventRepository
import org.timestamp.backend.repository.TimestampUserRepository


@Service
class EventService(private val db: TimestampEventRepository, private val userDb: TimestampUserRepository) {
    fun getAllEvents(): List<Event> = db.findAll()

    /**
     * Get events by user ID. We only return events that the user is a part of.
     */
    fun getEventById(firebaseUser: FirebaseUser, id: Long): Event? {
        val user = userDb.findById(firebaseUser.uid).orElseThrow()
        val event = db.findByIdOrNull(id)
        return if (event?.users?.map { it.id }?.contains(user.id) == true) event else null
    }

    fun getEventById(id: Long): Event? = db.findByIdOrNull(id)

    fun getEvents(firebaseUser: FirebaseUser): List<Event> = db.findAllEventsByUser(firebaseUser.uid)

    /**
     * Create an event with the given user as the creator.
     */
    fun createEvent(userId: String, event: Event): Event? {
        val user = userDb.findByIdOrNull(userId) ?: return null
        return createEvent(user, event)
    }

    fun createEvent(user: User, event: Event): Event? {
        event.creator = user.id
        event.users.add(user)
        return db.save(event)
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
    fun joinEvent(firebaseUser: FirebaseUser, id: Long): Event? {
        val user = userDb.findByIdOrNull(firebaseUser.uid)?: return null
        val event = db.findByIdOrNull(id) ?: return null

        val added = event.users.add(user)
        if (added) db.save(event) // Save the event if the user wasn't inside already

        return event
    }

    /**
     * Delete a user from an event. The user must exist in the database.
     */
    fun deleteEvent(id: Long, firebaseUser: FirebaseUser): Boolean {
        val item: Event? = db.findByIdOrNull(id)

        if (item?.creator != firebaseUser.uid) return false

        db.deleteById(id)
        return true
    }

    /**
     * Remove oneself from an event if they are NOT the creator
     */
    fun removeUserFromEvent(id: Long, firebaseUser: FirebaseUser): Boolean {
        val item: Event? = db.findByIdOrNull(id)

        if (item == null || item.creator == firebaseUser.uid) return false

        val user = userDb.findById(firebaseUser.uid).orElseThrow()
        item.users.remove(user)
        db.save(item)
        return true
    }
}