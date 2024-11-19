package org.timestamp.backend.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.timestamp.backend.config.FirebaseUser
import org.timestamp.backend.model.TravelMode
import org.timestamp.backend.model.User
import org.timestamp.backend.repository.TimestampUserRepository

@Service
class UserService(private val db: TimestampUserRepository) {
    fun getUserById(id: String): User? = db.findByIdOrNull(id)

    /**
     * Create a user from a FirebaseUser object if it does not exist, otherwise return
     * the existing user.
     */
    fun createUser(principal: FirebaseUser): User {
        val user = User(principal)
        return createUser(user)
    }

    fun createUser(user: User): User {
        val existingUser: User? = db.findByIdOrNull(user.id)
        return existingUser ?: db.save(user)
    }

    fun updateLocation(
        firebaseUser: FirebaseUser,
        latitude: Double,
        longitude: Double,
        travelMode: TravelMode
    ): User {
        val user: User = db.findById(firebaseUser.uid).orElseThrow()
        user.latitude = latitude
        user.longitude = longitude
        user.travelMode = travelMode
        return db.save(user)
    }
}