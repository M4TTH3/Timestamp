package org.timestamp.backend.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import org.timestamp.backend.viewModels.OffsetDateTimeSerializer
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class EventDTO(
    val id: Long? = null,
    val creator: String = "",
    val name: String,
    val description: String,
    val address: String = "",
    val latitude: Double,
    val longitude: Double,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val arrival: OffsetDateTime,
    val users: MutableSet<EventUser> = mutableSetOf()
)

@Serializable
data class EventUser(
    val name: String,
    val email: String,
    val pfp: String,
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
    users = this.users.mapTo(mutableSetOf()) { user ->
        EventUser(
            name = user.name,
            email = user.email,
            pfp = user.pfp,
        )
    }
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
    users = this.users.mapTo(mutableSetOf()) { user ->
        User(
            id = "", // You'll need a way to resolve the User's ID. This is just an example.
            name = user.name,
            email = user.email,
            pfp = user.pfp,
            latitude = 0.0, // Set a default or resolve this if necessary.
            longitude = 0.0, // Set a default or resolve this if necessary.
            events = mutableSetOf() // Assuming this is managed elsewhere or can be ignored here.
        )
    }
)

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override val descriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString(), formatter)
    }

    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: LocalDateTime) {
        encoder.encodeString(value.format(formatter))
    }
}

