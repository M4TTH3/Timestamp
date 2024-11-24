package org.timestamp.backend.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.timestamp.backend.config.FirebaseUser
import org.timestamp.lib.dto.TravelMode
import org.timestamp.lib.dto.UserDTO

@Entity
@Table(name = "users", schema = "public")
class User(
    @Id
    val id: String = "",
    var name: String = "",
    var email: String = "",
    var pfp: String = "",

    // Current location of the user
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,

    @Enumerated(EnumType.STRING)
    @Column(name = "travel_mode", nullable = false)
    var travelMode: TravelMode = TravelMode.Car,

    @ManyToMany(mappedBy = "users")
    @JsonIgnore
    val events: MutableSet<Event> = mutableSetOf(),
): Base()
{
    constructor(firebaseUser: FirebaseUser): this(
        id = firebaseUser.uid,
        name = firebaseUser.name,
        email = firebaseUser.email,
        pfp = firebaseUser.picture,
        latitude = 0.0,
        longitude = 0.0,
    )
}

fun User.toDTO(): UserDTO {
    return UserDTO(
        id = id,
        name = name,
        email = email,
        pfp = pfp,
        latitude = latitude,
        longitude = longitude
    )
}