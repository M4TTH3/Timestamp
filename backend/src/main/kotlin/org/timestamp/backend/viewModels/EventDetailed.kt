package org.timestamp.backend.viewModels

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.springframework.web.reactive.function.client.WebClient
import org.timestamp.backend.model.Event
import java.time.LocalDateTime

@Serializable
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
) {
    companion object {
        suspend fun from(event: Event, profile: String = "car"): EventDetailed {
            val webClient = WebClient.builder().baseUrl("https://maps.mattheway.com").build()
            val users = mutableListOf<EventDetailedUser>()
            for (user in event.users) {
                val endpoint = "/route?point=${event.latitude},${event.longitude}&point=${user.latitude},${user.longitude}&profile=$profile"
                var timeEst = 0L
                try {
                    val travelData = webClient.get()
                        .uri(endpoint)
                        .retrieve()
                        .bodyToMono(String::class.java)
                        .awaitSingle()
                    val json = Json { ignoreUnknownKeys = true }
                    val routeResponse = json.decodeFromString<RouteResponse>(travelData)
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