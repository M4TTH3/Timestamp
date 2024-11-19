package org.timestamp.backend.viewModels

import kotlinx.serialization.Serializable
import org.timestamp.backend.model.TravelMode
import org.timestamp.backend.model.User

@Serializable
data class LocationVm(
    val latitude: Double,
    val longitude: Double,
    val travelMode: TravelMode
)

@Serializable
data class UserVm(
    val id: String,
    val name: String,
    val email: String,
    val pfp: String,

    // Current location of the user
    val latitude: Double,
    val longitude: Double,
) {

    companion object {
        fun fromUser(user: User): UserVm {
            return UserVm(
                user.id,
                user.name,
                user.email,
                user.pfp,
                user.latitude,
                user.longitude
            )
        }
    }

}