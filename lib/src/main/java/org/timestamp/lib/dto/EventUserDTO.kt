package org.timestamp.lib.dto

import kotlinx.serialization.Serializable
import org.timestamp.lib.util.OffsetDateTimeSerializer
import java.time.OffsetDateTime

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