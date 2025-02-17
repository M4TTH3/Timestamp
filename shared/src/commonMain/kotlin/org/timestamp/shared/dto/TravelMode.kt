package org.timestamp.shared.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TravelMode(val value: String) {
    @SerialName("car") Car("car"),
    @SerialName("foot") Foot("foot"),
    @SerialName("bike") Bike("bike")
}
