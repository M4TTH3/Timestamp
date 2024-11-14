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
    var name: String,
    var description: String,
    var address: String = "",

    // The location of the event
    var latitude: Double,
    var longitude: Double,

    // When the event starts
    var arrival: LocalDateTime,

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
{
    constructor(): this(
        name = "",
        description = "",
        latitude = 0.0,
        longitude = 0.0,
        arrival = LocalDateTime.now(),
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Event

        if (id != other.id) return false
        if (creator != other.creator) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false
        if (arrival != other.arrival) return false
        if (address != other.address) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + creator.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        result = 31 * result + arrival.hashCode()
        result = 31 * result + address.hashCode()
        return result
    }
}