package org.timestamp.backend.service

import com.graphhopper.GHRequest
import com.graphhopper.GraphHopper
import com.graphhopper.ResponsePath
import org.springframework.stereotype.Component
import org.timestamp.backend.model.Event
import org.timestamp.backend.model.toDTO
import org.timestamp.lib.dto.*
import java.util.*

@Component
class GraphHopperService(
    val arrivalService: ArrivalService,
    val graphHopper: GraphHopper
) {
    fun getEventDTO(event: Event): EventDTO {
        return event.toDTO(graphHopper, arrivalService)
    }
}

fun Event.toDTO(graphHopper: GraphHopper, arrivalService: ArrivalService): EventDTO {
    val users = mutableListOf<EventUserDTO>()
    val arrivals = this.arrivals
    val arrivalSet = arrivals.map { it.userId }.toSet() // Used to lookup fast if an arrival exists
    for (user in this.users) {
        val arrivalTime = this.arrival.toUtc()
        var timeEst: Long? = null
        var distance: Double? = null

        // Only get the time est. if the event is today and the user has not arrived
        if (user.id !in arrivalSet && arrivalTime.toLocalDate() == utcNow().toLocalDate()) {
            val res = graphHopper.route(
                this.latitude,
                this.longitude,
                user.latitude,
                user.longitude,
                user.travelMode
            )
            timeEst = res?.time
            distance = res?.distance

            // If the distance is close (150m) AND the arrives after 1 hour before the event,
            // then the user has arrived.
            val twoHundredMeters = 200.0
            val hourBefore = arrivalTime.minusHours(1)
            val inArrivalPeriod = utcNow().isAfter(hourBefore)
            if (distance != null && distance <= twoHundredMeters && inArrivalPeriod) {
                val arrival = arrivalService.addArrival(this.id!!, user.id)
                arrivals.add(arrival)
            }
        }

        users.add(
            EventUserDTO(
                id = user.id,
                name = user.name,
                email = user.email,
                pfp = user.pfp,
                timeEst = timeEst,
                distance = distance
            )
        )
    }

    return EventDTO(
        id = this.id!!,
        creator = this.creator,
        name = this.name,
        description = this.description,
        address = this.address,
        latitude = this.latitude,
        longitude = this.longitude,
        arrival = this.arrival,
        users = users,
        arrivals = arrivals.map { it.toDTO() }
    )
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