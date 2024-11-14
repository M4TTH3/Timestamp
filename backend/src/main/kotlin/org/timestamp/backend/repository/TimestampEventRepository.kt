package org.timestamp.backend.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.timestamp.backend.model.Event

interface TimestampEventRepository : JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e JOIN e.users u WHERE u.id = :userId")
    fun findAllEventsByUser(@Param("userId") userId: String): List<Event>
}