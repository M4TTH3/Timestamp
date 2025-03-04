package org.timestamp.shared.dto

import kotlinx.serialization.Serializable

@Serializable
enum class TravelMode(val value: String) {
    Car("car"),
    Foot("foot"),
    Bike("bike")
}
