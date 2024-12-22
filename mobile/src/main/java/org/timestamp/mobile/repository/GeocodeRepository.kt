package org.timestamp.mobile.repository

import io.ktor.client.request.get
import io.ktor.http.URLBuilder
import io.ktor.http.path
import org.timestamp.lib.dto.LocationDTO
import org.timestamp.lib.dto.GeocodeDTO


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
            val endpoint = "/geocode/reverse?lat=$lat&lon=$lon"
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

        return handler(tag) {
            val data: GeocodeDTO? = ktorClient
                .get {
                    url {
                        path("/geocode")
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