package org.timestamp.backend.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import org.timestamp.backend.config.FirebaseUser

@Entity
@Table(name = "users", schema = "public")
data class User(
    @Id
    val id: String,
    var name: String,
    var email: String,
    var pfp: String,

    // Current location of the user
    var latitude: Double,
    var longitude: Double,

    @ManyToMany(mappedBy = "users")
    @JsonIgnoreProperties("users")
    val events: MutableSet<Event> = mutableSetOf(),
): Base()
{
    constructor(): this(
        id = "",
        name = "",
        email = "",
        pfp = "",
        latitude = 0.0,
        longitude = 0.0,
    )

    constructor(firebaseUser: FirebaseUser): this(
        id = firebaseUser.uid,
        name = firebaseUser.name,
        email = firebaseUser.email,
        pfp = firebaseUser.picture,
        latitude = 0.0,
        longitude = 0.0,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (id != other.id) return false
        if (name != other.name) return false
        if (email != other.email) return false
        if (pfp != other.pfp) return false
        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        result = 31 * result + pfp.hashCode()
        return result
    }


}
