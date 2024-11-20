package org.timestamp.backend.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "events", schema = "public")
class Event(
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
    var arrival: OffsetDateTime = OffsetDateTime.now(),

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_events",
        schema = "public",
        joinColumns = [JoinColumn(name = "event_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")]
    )
    @JsonIgnoreProperties("events")
    val users: MutableSet<User> = mutableSetOf(),

    @OneToMany(mappedBy = "event", cascade = [CascadeType.ALL], orphanRemoval = true)
    val arrivals: MutableSet<Arrival> = mutableSetOf()
): Base()