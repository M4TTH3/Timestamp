package org.timestamp.backend.model

import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Id
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.ManyToMany
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import java.time.LocalDateTime

@Entity
@Table(name = "events", schema = "public")
data class Event(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Auto incremented id
    val id: Long? = null,
    val name: String,
    val description: String,

    // The location of the event
    val latitude: Double,
    val longitude: Double,

    // When the event starts
    val arrival: LocalDateTime,

    @ManyToMany
    @JoinTable(
        name = "user_events",
        schema = "public",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "event_id")]
    )
    val users: MutableSet<User> = mutableSetOf(),

    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    constructor(): this(
        name = "",
        description = "",
        latitude = 0.0,
        longitude = 0.0,
        arrival = LocalDateTime.now(),
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )
}