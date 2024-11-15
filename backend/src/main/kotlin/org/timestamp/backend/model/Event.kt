package org.timestamp.backend.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "events", schema = "public")
data class Event(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Auto incremented id
    val id: Long? = null,
    var creator: String = "",
    var name: String = "",
    var description: String = "",
    var address: String = "",

    // The location of the event
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,

    // When the event starts
    var arrival: LocalDateTime = LocalDateTime.now(),

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_events",
        schema = "public",
        joinColumns = [JoinColumn(name = "event_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")]
    )
    @JsonIgnoreProperties("events")
    val users: MutableSet<User> = mutableSetOf(),
): Base()