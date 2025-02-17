package org.timestamp.shared.dto

import kotlinx.serialization.Serializable
import org.timestamp.shared.util.UUIDSerializer
import java.util.UUID

@Serializable
data class EventLinkDTO (
    @Serializable(with = UUIDSerializer::class)
    val id: UUID
)