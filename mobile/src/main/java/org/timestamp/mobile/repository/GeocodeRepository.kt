package org.timestamp.mobile.repository

import androidx.compose.runtime.MutableState
import io.ktor.client.request.get
import io.ktor.http.URLBuilder
import io.ktor.http.path
import org.timestamp.lib.dto.*


class GeocodeRepository private constructor(): BaseRepository<GeocodeDTO?>(
    null,
    "GeocodeRepository"
) {

    private val locRepo = LocationRepository()

    /**
     * Reverse geocode the given latitude and longitude
     * @param lat The latitude
     * @param lon The longitude
     */
    suspend fun reverseGeocode(lat: Double, lon: Double): GeocodeDTO? {
        val tag = "ReverseGeocode"
        return handler(tag) {
            val endpoint = "geocode/reverse?lat=$lat&lon=$lon"
            val data: GeocodeDTO? = ktorClient.get(endpoint).bodyOrNull()

            data
        }
    }

    /**
     * Geocode the given search query, latitude, and longitude - Updates the state
     * @param query The search query
     * @param lat The latitude
     * @param lon The longitude
     */
    suspend fun geocode(query: String, lat: Double, lon: Double): GeocodeDTO? {
        val tag = "Geocode"

        return handler(tag, true) {
            val data: GeocodeDTO? = ktorClient
                .get {
                    url {
                        path("geocode")
                        parameters.append("query", query)
                        parameters.append("lat", "$lat")
                        parameters.append("lon", "$lon")
                    }
                }
                .bodyOrNull()

            state = data
            data
        }
    }

    // Reset the geocode values (i.e. on close)
    fun reset() { state = null }

    companion object {
        operator fun invoke() = StateRepository { GeocodeRepository() }
    }
}

/**
 * This will extract the information of the selected geocode (location info)
 * and load it into the EventDTO. If there is no name, it will use the format:
 *
 *      description = housenumber street
 *      address = city, state, country
 *
 * Otherwise, it will use the name as the description.
 * @param feature The selected GeoJsonFeature
 */
fun EventDTO.copy(feature: GeoJsonFeature?, setLoc: Boolean = false): EventDTO? {
    feature ?: return null // If the feature is null, return

    val properties = feature.properties
    val geometry = feature.geometry
    val name = properties.name

    val address = properties.address()
    val description = name ?: address

    return when {
        setLoc -> this.copy(
            latitude = geometry.coordinates[1],
            longitude = geometry.coordinates[0],
            description = description,
            address = address
        )
        else -> this.copy(
            description = description,
            address = address
        )
    }
}

fun GeoJsonProperties.address(): String {
    val localAddress = listOfNotNull(houseNumber, street).joinToString(" ").ifBlank { null }
    val cityAddressArray = listOfNotNull(localAddress, city, state)

    return cityAddressArray.joinToString(", ")
}

fun GeoJsonProperties.headline(): String = this.name ?: this.address()