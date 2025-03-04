package org.timestamp.shared.dto

import kotlinx.serialization.Serializable
import org.timestamp.shared.util.OffsetDateTimeSerializer
import java.time.OffsetDateTime

/**
 * Hours before an event arrival time to check if a user has arrived.
 * Also, how long it will stay valid.
 */
const val THRESHOLD_BEFORE = 1

/**
 * This will extract the detailed information of an event.
 * Includes:
 *  - Time est. for each user to an event
 */
@Serializable
data class EventDTO(
    val id: Long? = null,
    val creator: String = "",
    val name: String = "",
    val description: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val arrival: OffsetDateTime = OffsetDateTime.now(),
    val users: List<EventUserDTO> = emptyList()
)

@Serializable
enum class EventStreamType {
    ADD,
    DELETE,
    UPDATE
}

fun EventDTO.inArrivalPeriod(): Boolean = inArrivalPeriod(arrival)

fun inArrivalPeriod(arrival: OffsetDateTime): Boolean {
    val hourBefore = arrival.minusHours(THRESHOLD_BEFORE.toLong())
    return OffsetDateTime.now().isAfter(hourBefore)
}