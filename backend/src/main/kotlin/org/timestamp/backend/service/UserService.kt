package org.timestamp.backend.service

import com.graphhopper.GraphHopper
import kotlinx.coroutines.launch
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.timestamp.backend.config.FirebaseUser
import org.timestamp.backend.config.UserNotFoundException
import org.timestamp.backend.model.User
import org.timestamp.backend.model.toDTO
import org.timestamp.backend.repository.TimestampUserRepository
import org.timestamp.backend.util.SseHub
import org.timestamp.backend.util.updateUserEvent
import org.timestamp.shared.dto.EventDTO
import org.timestamp.shared.dto.EventStreamType
import org.timestamp.shared.dto.TravelMode
import org.timestamp.shared.dto.UserDTO
import org.timestamp.shared.util.utcNow

@Service
class UserService(
    private val db: TimestampUserRepository,
    private val graphHopper: GraphHopper,
    private val sseEventHub: SseHub<EventDTO, EventStreamType>
) {

    /**
     * Create a user from a FirebaseUser object if it does not exist, otherwise return
     * the existing user.
     */
    fun createUser(principal: FirebaseUser): UserDTO {
        val user = User(principal)
        return createUser(user)
    }

    fun createUser(user: User): UserDTO {
        val existingUser: User? = db.findByIdOrNull(user.id)
        if (existingUser != null) {
            // Update fields in case google changes them.
            existingUser.name = user.name
            existingUser.pfp = user.pfp
            db.save(existingUser)
        }

        return (existingUser ?: db.save(user)).toDTO()
    }

    fun updateLocation(
        firebaseUser: FirebaseUser,
        latitude: Double,
        longitude: Double,
        travelMode: TravelMode
    ): UserDTO {
        val user: User = db.findByIdOrNull(firebaseUser.uid) ?: throw UserNotFoundException()
        user.latitude = latitude
        user.longitude = longitude
        user.travelMode = travelMode

        for (userEvent in user.userEvents) {
            // Only update if the event is within the next -2 hours -> 24 hours
            val twoHoursBefore = utcNow().minusHours(2)
            val nextDay = utcNow().plusDays(1)
            val arrival = userEvent.event!!.arrival
            val withinPeriod = arrival.isAfter(twoHoursBefore) && arrival.isBefore(nextDay)

            if (withinPeriod) graphHopper.updateUserEvent(userEvent)
        }

        // Update each event associated with the user

        user.userEvents.forEach {
            val event = it.event!!
            val users = event.userEvents.map { it.user!!.id }
            val e = event.toDTO()
            users.forEach {
                if (it != user.id) sseEventHub.scope.launch { sseEventHub.send(it, e, EventStreamType.UPDATE) }
            }
        }

        return db.save(user).toDTO()
    }
}
