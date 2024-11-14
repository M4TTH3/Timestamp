package org.timestamp.backend.viewModels

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.springframework.web.reactive.function.client.WebClient
import org.timestamp.backend.model.Event
import java.time.LocalDateTime

data class EventDetailedUser(
    val name: String,
    val email: String,
    val pfp: String,
    val timeEst: Long,
)

@Serializable
data class RouteResponse(
    val paths: List<Path>
)

@Serializable
data class Path(
    val time: Long
)

/**
 * This will extract the detailed information of an event.
 * Includes:
 *  - Time est. for each user to an event
 */
data class EventDetailed(
    val id: Long,
    val creator: String,
    val name: String,
    val description: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val arrival: LocalDateTime,

    val users: List<EventDetailedUser>,
) {
    companion object {
        suspend fun fromEvent(event: Event, profile: String = "car"): EventDetailed {
            val webClient = WebClient.builder().baseUrl("https://maps.mattheway.com").build()
            val users = mutableListOf<EventDetailedUser>()
            for (user in event.users) {
                val travelData = webClient.get()
                    .uri("/route?point=${event.latitude},${event.longitude}&point=${user.latitude},${user.longitude}&profile=$profile")
                    .retrieve()
                    .bodyToMono(String::class.java)
                    .awaitSingle()

                var timeEst = 0L
                try {
                    val routeResponse = Json.decodeFromString<RouteResponse>(travelData)
                    timeEst = routeResponse.paths[0].time
                } catch (e: Exception) {
                    println("Error: $e")
                }

                users.add(EventDetailedUser(
                    name = user.name,
                    email = user.email,
                    pfp = user.pfp,
                    timeEst = timeEst
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
                users = users
            )
        }
    }
}