package org.timestamp.backend.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.timestamp.backend.config.FirebaseUser
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
        return if (user.id in (event?.users?.map { it.id } ?: emptyList())) event else null
    }

    fun getEventById(id: Long): Event? = db.findByIdOrNull(id)

    fun getEvents(firebaseUser: FirebaseUser): List<Event> = db.findAllEventsByUser(firebaseUser.uid)

    /**
     * Create an event with the given user as the creator.
     */
    fun createEvent(userId: String, event: Event): Event {
        val user = userDb.findById(userId).orElseThrow()
        return createEvent(user, event)
    }

    fun createEvent(user: User, event: Event): Event {
        event.creator = user.id
        event.users.add(user)
        return db.save(event)
    }

    /**
     * Add a user to an event. The user must exist in the database.
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
        val item: Event = db.findById(id).orElseThrow()

        if (item.creator == firebaseUser.uid) return false

        val user = userDb.findById(firebaseUser.uid).orElseThrow()
        item.users.remove(user)
        db.save(item)
        return true
    }
}