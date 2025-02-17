package org.timestamp.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class LocationDTO(
    val latitude: Double,
    val longitude: Double,
    val travelMode: TravelMode = TravelMode.Car
)
