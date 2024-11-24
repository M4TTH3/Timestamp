package org.timestamp.lib.dto

import kotlinx.serialization.Serializable

@Serializable
data class EventUserDTO(
    val id: String,
    val name: String,
    val email: String,
    val pfp: String,
    val timeEst: Long?,
    val distance: Double?
)