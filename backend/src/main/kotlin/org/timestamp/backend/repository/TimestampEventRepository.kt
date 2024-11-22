package org.timestamp.backend.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.timestamp.backend.model.Event

interface TimestampEventRepository : JpaRepository<Event, Long> {

    /**
     * Filter events by current user, that is >= to Today
     */
    @Query("SELECT e FROM Event e JOIN e.users u WHERE u.id = :userId AND e.arrival >= CURRENT_TIMESTAMP - 2 HOUR")
    fun findAllEventsByUser(@Param("userId") userId: String): List<Event>
}