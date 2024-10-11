package org.timestamp.backend.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.timestamp.backend.model.Event
import org.timestamp.backend.repository.TimestampEventRepository
import org.timestamp.backend.repository.TimestampUserRepository


@Service
class EventService(private val db: TimestampEventRepository, private val userDb: TimestampUserRepository) {
    fun getAllEvents(): List<Event> = db.findAll()
    fun getEventById(id: Long): Event? = db.findByIdOrNull(id)
    fun createEvent(userId: String, event: Event): Event {
        val user = userDb.findById(userId).orElseThrow()
        event.creator = userId
        event.users.add(user)
        return db.save(event)
    }

    fun deleteEvent(id: Long, userId: String): Boolean {
        val item: Event? = db.findByIdOrNull(id)

        if (item?.creator != userId) return false

        db.deleteById(id)
        return true
    }
}