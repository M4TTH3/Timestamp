package org.timestamp.backend.model

import jakarta.persistence.*
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("events")
data class Event(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Auto incremented id
    val id: Long? = null,
    val name: String,
    val timestamp: Long,

    @ManyToMany
    @JoinTable(
        name = "user_events",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "event_id")]
    )
    val users: MutableSet<User> = mutableSetOf()
)