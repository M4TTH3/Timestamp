package org.timestamp.backend.viewModels

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
