package org.timestamp.backend.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.timestamp.backend.model.Arrival

interface TimestampArrivalRepository: JpaRepository<Arrival, Long> {
    fun findFirstByEventIdAndUserId(eventId: Long, userId: String): MutableList<Arrival>
}