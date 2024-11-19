package org.timestamp.backend.service

import org.springframework.stereotype.Service
import org.timestamp.backend.model.Arrival
import org.timestamp.backend.model.Event
import org.timestamp.backend.repository.TimestampArrivalRepository

@Service
class ArrivalService(val db: TimestampArrivalRepository) {
    fun addArrival(eventId: Long, userId: String): Arrival {
        val event = Event(id = eventId)
        val arrivalTest = db.findFirstByEventIdAndUserId(eventId, userId)

        return if (arrivalTest.isEmpty())
            db.save(Arrival(event = event, userId = userId))
        else arrivalTest.first()
    }
}