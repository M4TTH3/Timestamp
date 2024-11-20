package org.timestamp.backend.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.timestamp.backend.model.Event

interface TimestampEventRepository : JpaRepository<Event, Long> {

    /**
     * Filter events by current user, that is >= to Today
     */
    @Query(nativeQuery = true,
        value = """
        SELECT e.* FROM events e
        JOIN user_events ue ON e.id = ue.event_id
        WHERE ue.user_id = :userId AND 
        (e.arrival AT TIME ZONE 'UTC')::DATE >= (CURRENT_DATE AT TIME ZONE 'UTC')
        """
    )
    fun findAllEventsByUser(@Param("userId") userId: String): List<Event>
}