package org.timestamp.backend.util

import com.graphhopper.GHRequest
import com.graphhopper.GraphHopper
import com.graphhopper.ResponsePath
import org.timestamp.backend.model.UserEvent
import org.timestamp.backend.model.toDTO
import org.timestamp.shared.dto.*
import org.timestamp.shared.util.utcNow
import java.util.*
import kotlin.math.*

private const val EARTH_RADIUS = 6371.0
private const val ROUTE_LENIENCY = 1.2
private const val ARRIVAL_DISTANCE = 200.0

/**
 * Edit an already created userEvent with the updated time est, distance, and arrival status.
 * Arrived if within 200m of the event and arrives after 1 hour before the event.
 */
fun GraphHopper.updateUserEvent(
    userEvent: UserEvent
): UserEvent {

    if (userEvent.arrived) return userEvent

    val user = userEvent.user!!
    val event = userEvent.event!!
    val res = this.route(
        user.latitude,
        user.longitude,
        event.latitude,
        event.longitude,
        userEvent.travelMode ?: user.travelMode
    )

    userEvent.timeEst = res?.time
    userEvent.distance = res?.distance

    val inArrivalPeriod = inArrivalPeriod(event.arrival)
    val inArrivalDistance = userEvent.distance.let { it != null && it <= ARRIVAL_DISTANCE }
    if (inArrivalDistance && inArrivalPeriod) {
        userEvent.arrived = true
        userEvent.arrivedTime = utcNow()
    }

    return userEvent
}

/**
 * Get the route info between the user and the event.
 */
fun GraphHopper.getNotification(userEvent: UserEvent): NotificationDTO {
    val user = userEvent.user!!
    val event = userEvent.event!!
    val travelMode = user.travelMode

    // There should already be a pre-calculated route info.
    // for the current travel mode.
    val res = this.route(user.latitude, user.longitude, event.latitude, event.longitude, travelMode)
    val routeInfo = RouteInfoDTO(
        distance = res?.distance,
        timeEst = res?.time,
        travelMode = travelMode
    )

    return NotificationDTO(
        event = event.toDTO(),
        routeInfo = routeInfo
    )
}

fun GraphHopper.route(
    fromLat: Double,
    fromLon: Double,
    toLat: Double,
    toLon: Double,
    type: TravelMode
): ResponsePath? {
    // https://github.com/graphhopper/graphhopper/blob/master/example/src/main/java/com/graphhopper/example/RoutingExample.java
    if (!shouldCalculateRoute(fromLat, fromLon, toLat, toLon)) return outOfBoundsGHRequest()

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

/**
 * Calculate a route only if it's less than distance away.
 * We will add +20% for routing leniency
 * @param distance the distance in km
 */
private fun shouldCalculateRoute(
    fromLat: Double,
    fromLon: Double,
    toLat: Double,
    toLon: Double,
    distance: Double = 60.0
): Boolean {
    val limit = distance * ROUTE_LENIENCY
    val dLat = Math.toRadians(toLat - fromLat)
    val dLon = Math.toRadians(toLon - fromLon)
    val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(fromLat)) * cos(Math.toRadians(toLat)) * sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    val dist = EARTH_RADIUS * c

    return dist <= limit
}

private fun outOfBoundsGHRequest() = ResponsePath().apply {
    distance = OutOfRangeRouteInfoDTO.distance!!
    time = OutOfRangeRouteInfoDTO.timeEst!!
}
