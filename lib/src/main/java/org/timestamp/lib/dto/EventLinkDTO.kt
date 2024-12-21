package org.timestamp.lib.dto

import kotlinx.serialization.Serializable
import org.timestamp.lib.util.UUIDSerializer
import java.util.UUID

@Serializable
data class EventLinkDTO (
    @Serializable(with = UUIDSerializer::class)
    val id: UUID
)