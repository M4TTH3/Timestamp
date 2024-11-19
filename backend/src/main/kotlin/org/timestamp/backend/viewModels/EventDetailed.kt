package org.timestamp.backend.viewModels

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.timestamp.backend.model.Arrival
import org.timestamp.backend.model.Event
import org.timestamp.backend.service.ArrivalService
import java.time.LocalDateTime
import javax.annotation.PostConstruct

@Serializable
data class EventDetailedUser(
    val id: String,
    val name: String,
    val email: String,
    val pfp: String,
    val timeEst: Long?,
    val distance: Double?
)

@Serializable
data class RouteResponse(
    val paths: List<Path>
)

@Serializable
data class Path(
    val time: Long,
    val distance: Double
)

@Serializable
data class ArrivalVm(
    val userId: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val time: LocalDateTime
) {
    companion object {
        fun from(arrival: Arrival): ArrivalVm {
            return ArrivalVm(
                userId = arrival.userId,
                time = arrival.createdAt!!
            )
        }
    }
}

/**
 * This will extract the detailed information of an event.
 * Includes:
 *  - Time est. for each user to an event
 */
@Serializable
data class EventDetailed(
    val id: Long? = null,
    val creator: String = "",
    val name: String = "",
    val description: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    @Serializable(with = LocalDateTimeSerializer::class)
    val arrival: LocalDateTime = LocalDateTime.now(),
    val users: List<EventDetailedUser> = emptyList(),
    val arrivals: List<ArrivalVm> = emptyList()
) {
    companion object {
        private lateinit var arrivalService: ArrivalService

        fun initialize(arrivalService: ArrivalService) {
            this.arrivalService = arrivalService
        }

        suspend fun from(event: Event): EventDetailed {
            val webClient = WebClient.builder().baseUrl("https://maps.mattheway.com").build()
            val users = mutableListOf<EventDetailedUser>()
            val arrivals = event.arrivals
            val arrivalSet = arrivals.map { it.userId }.toSet() // Used to lookup fast if an arrival exists

            for (user in event.users) {
                val endpoint = "/route?point=${user.latitude},${user.longitude}&point=${event.latitude},${event.longitude}&profile=${user.travelMode.value}"
                var timeEst: Long? = null
                var distance: Double? = null

                // Only get the time est. if the event is today and the user has not arrived
                if (user.id !in arrivalSet && event.arrival.toLocalDate() == LocalDateTime.now().toLocalDate()) {
                    try {
                        val travelData = webClient.get()
                            .uri(endpoint)
                            .retrieve()
                            .bodyToMono(String::class.java)
                            .awaitSingle()
                        val json = Json { ignoreUnknownKeys = true }
                        val routeResponse = json.decodeFromString<RouteResponse>(travelData)
                        timeEst = routeResponse.paths[0].time

                        // If the distance is close (150m) AND the arrives after 1 hour before the event,
                        // then the user has arrived.
                        distance = routeResponse.paths[0].distance
                        val twoHundredMeters = 200.0
                        val hourBefore = event.arrival.minusHours(1)
                        val inArrivalPeriod = LocalDateTime.now().isAfter(hourBefore)

                        if (distance <= twoHundredMeters && inArrivalPeriod) {
                            val arrival = arrivalService.addArrival(event.id!!, user.id)
                            arrivals.add(arrival)
                        }
                    } catch (e: Exception) {
                        println("Error: $e")
                    }
                }

                users.add(EventDetailedUser(
                    id = user.id,
                    name = user.name,
                    email = user.email,
                    pfp = user.pfp,
                    timeEst = timeEst,
                    distance = distance
                ))
            }

            return EventDetailed(
                id = event.id!!,
                creator = event.creator,
                name = event.name,
                description = event.description,
                address = event.address,
                latitude = event.latitude,
                longitude = event.longitude,
                arrival = event.arrival,
                users = users,
                arrivals = arrivals.map { ArrivalVm.from(it) }
            )
        }
    }
}

/**
 * Inject the event service into the EventDetailed class.
 * This is done to support companion object.
 */
@Component
class EventDetailedInitializer(
    private val arrivalService: ArrivalService
) {
    @PostConstruct
    fun init() {
        EventDetailed.initialize(arrivalService)
    }
}