package org.timestamp.backend.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import java.beans.Encoder
import java.time.LocalDateTime
import java.util.Base64.Decoder

@Serializable
data class EventDTO(
    val id: Long? = null,
    val creator: String = "",
    val name: String,
    val description: String,
    val address: String = "",
    val latitude: Double,
    val longitude: Double,
    @Contextual
    val arrival: LocalDateTime,
    val users: MutableSet<@Contextual User> = mutableSetOf()
)

fun Event.toDTO() = EventDTO(
    id = this.id,
    creator = this.creator,
    name = this.name,
    description = this.description,
    address = this.address,
    latitude = this.latitude,
    longitude = this.longitude,
    arrival = this.arrival,
    users = this.users
)

fun EventDTO.toEvent() = Event(
    id = this.id,
    creator = this.creator,
    name = this.name,
    description = this.description,
    address = this.address,
    latitude = this.latitude,
    longitude = this.longitude,
    arrival = this.arrival,
    users = this.users
)

