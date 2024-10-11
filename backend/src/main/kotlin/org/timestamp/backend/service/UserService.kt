package org.timestamp.backend.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.timestamp.backend.model.User
import org.timestamp.backend.repository.TimestampUserRepository

@Service
class UserService(private val db: TimestampUserRepository) {
    fun getUserById(id: String): User? = db.findByIdOrNull(id)

    fun createUser(user: User): User {
        val existingUser: User? = db.findByIdOrNull(user.id)
        return existingUser ?: db.save(user)
    }

    fun updateLocation(id: String, latitude: Double, longitude: Double): User {
        val user: User = db.findByIdOrNull(id) ?: throw IllegalArgumentException("User not found")
        user.latitude = latitude
        user.longitude = longitude
        return db.save(user)
    }
}