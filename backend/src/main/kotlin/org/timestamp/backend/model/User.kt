package org.timestamp.backend.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import org.timestamp.backend.config.FirebaseUser

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
