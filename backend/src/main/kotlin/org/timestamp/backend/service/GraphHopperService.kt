package org.timestamp.backend.service

import com.graphhopper.GHRequest
import com.graphhopper.GraphHopper
import com.graphhopper.ResponsePath
import org.springframework.stereotype.Component
import org.timestamp.backend.model.Event
import org.timestamp.backend.model.User
import org.timestamp.backend.model.UserEvent
import org.timestamp.backend.model.toDTO
import org.timestamp.lib.dto.*
import java.util.*

@Component
class GraphHopperService(
    val graphHopper: GraphHopper
) {

    /**
     * Edit an already created userEvent with the updated time est, distance, and arrival status.
     * Arrived if within 200m of the event and arrives after 1 hour before the event.
     */
     fun updateUserEvent(
        userEvent: UserEvent
     ): UserEvent {

        if (userEvent.arrived) return userEvent

        val user = userEvent.user!!
        val event = userEvent.event!!
        val res = graphHopper.route(
            event.latitude,
            event.longitude,
            user.latitude,
            user.longitude,
            user.travelMode
        )

        userEvent.timeEst = res?.time
        userEvent.distance = res?.distance

        val twoHundredMeters = 200.0
        val hourBefore = event.arrival.toUtc().minusHours(1)
        val inArrivalPeriod = utcNow().isAfter(hourBefore)
        val distance = userEvent.distance
        if (distance != null && distance <= twoHundredMeters && inArrivalPeriod) {
            userEvent.arrived = true
            userEvent.arrivedTime = utcNow()
        }

        return userEvent
    }

    /**
     * Creates a new user event with both the event & user. It will update
     * it's time est, distance, and arrival status.
     */
    fun createUserEvent(user: User, event: Event): UserEvent {
        return updateUserEvent(
            UserEvent(
                event = event,
                user = user
            )
        )
    }

}

fun GraphHopper.route(
    fromLat: Double,
    fromLon: Double,
    toLat: Double,
    toLon: Double,
    type: TravelMode
): ResponsePath? {
    // Reference: https://github.com/graphhopper/graphhopper/blob/master/example/src/main/java/com/graphhopper/example/RoutingExample.java
    val req = GHRequest(
        fromLat,
        fromLon,
        toLat,
        toLon
    ).apply {
        profile = type.value
        locale = Locale.US
    }
    val res = this.route(req)
    return if (res.hasErrors()) return null else res.best
}