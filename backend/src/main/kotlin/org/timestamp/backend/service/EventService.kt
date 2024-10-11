package org.timestamp.backend.service

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import org.timestamp.backend.model.Event

interface TimestampEventRepository : JpaRepository<Event, Long>

@Service
class EventService(private val db: TimestampEventRepository) {}