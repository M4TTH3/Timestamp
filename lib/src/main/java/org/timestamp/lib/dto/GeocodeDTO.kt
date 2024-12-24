package org.timestamp.lib.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This file contains ALL the DTOs used in Photon Geocoder API
 */
@Serializable
data class GeocodeDTO (
    val features: List<GeoJsonFeature> = emptyList(),
    val type: String? = null
)

@Serializable
data class GeoJsonFeature(
    val type: String,
    val geometry: GeoJsonGeometry,
    val properties: GeoJsonProperties
)

@Serializable
data class GeoJsonGeometry(
    val type: String,
    val coordinates: List<Double>
)

@Serializable
data class GeoJsonProperties(
    @SerialName("osm_id") val osmId: Long,
    @SerialName("osm_type") val osmType: String,
    @SerialName("osm_key") val osmKey: String,
    @SerialName("osm_value") val osmValue: String,
    @SerialName("housenumber") val houseNumber: String? = null,
    @SerialName("postcode") val postCode: String? = null,
    @SerialName("countrycode") val countryCode: String? = null,
    val extent: List<Double>? = null,
    val country: String? = null,
    val city: String? = null,
    val state: String? = null,
    val street: String? = null,
    val district: String? = null,
    val name: String? = null,
    val locality: String? = null,
    val type: String? = null
)
