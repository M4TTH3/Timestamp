package org.timestamp.backend.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "users", schema = "public")
data class User(
    @Id
    val id: String,
    val name: String,
    val email: String,

    // Current location of the user
    val latitude: Double,
    val longitude: Double,

    @ManyToMany(mappedBy = "users")
    val events: MutableSet<Event> = mutableSetOf(),

    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    constructor(): this(
        id = "",
        name = "",
        email = "",
        latitude = 0.0,
        longitude = 0.0,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )
}
