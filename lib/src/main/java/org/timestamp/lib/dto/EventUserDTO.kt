package org.timestamp.lib.dto

import kotlinx.serialization.Serializable
import org.timestamp.lib.util.OffsetDateTimeSerializer
import java.time.OffsetDateTime
import kotlin.math.roundToInt

@Serializable
data class EventUserDTO(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val pfp: String = "",
    val timeEst: Long? = null,
    val distance: Double? = null,
    val arrived: Boolean = false,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val arrivedTime: OffsetDateTime? = null,
    val travelMode: TravelMode? = null,
)

fun EventDTO.withinNextDay(): Boolean = this.before(OffsetDateTime.now().plusDays(1))
fun EventDTO.before(time: OffsetDateTime): Boolean = this.arrival.isBefore(time)
fun EventUserDTO.distanceString(): String {
    // Convert meters to a string, depending on best format
    // < 1000m -> meters
    val dist = this.distance ?: return "???"

    return when {
        dist < 1000 -> "${dist.roundToInt()} m"
        else -> "${(dist / 1000).roundToInt()} km"
    }
}
fun EventUserDTO.timeString(): String {
    // Convert milliseconds to a string, depending on best format
    // < 60s -> seconds
    // < 60m -> minutes
    // < 24h -> hours
    // < 7d -> days
    val time = this.timeEst?.floorDiv(1000) ?: return "???"

    return when {
        time < 60 -> "$time s"
        time < 60 * 60 -> "${time / 60} min"
        time < 60 * 60 * 24 -> "${time / 60 / 60} hr"
        time < 60 * 60 * 24 * 7 -> "${time / 60 / 60 / 24} d"
        else -> "${time / 60 / 60 / 24 / 7} w"
    }
}