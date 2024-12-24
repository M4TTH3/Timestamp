package org.timestamp.lib.dto

import kotlinx.serialization.Serializable
import org.timestamp.lib.util.OffsetDateTimeSerializer
import java.time.OffsetDateTime

@Serializable
data class ArrivalDTO(
    val userId: String,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val time: OffsetDateTime
)
