package org.timestamp.backend.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.util.UriComponentsBuilder
import org.timestamp.shared.dto.LocationDTO
import org.timestamp.shared.dto.GeocodeDTO

@Service
class GeocoderService(
    @Value("\${geocoder.url}")
    private val base: String,
    private val webClient: WebClient
) {

    private val uriBuilder
        get() = UriComponentsBuilder.fromUriString(base)

    private fun UriComponentsBuilder.buildUri() = this.build().toUri()

    /**
     * Geocode a query string to a list of possible locations.
     * Use the user's location to bias the search.
     */
     suspend fun geocode(query: String, lat: Double, lon: Double, limit: Int = 5): GeocodeDTO {
        val uri = uriBuilder
            .path("/api")
            .queryParam("q", query)
            .queryParam("limit", limit)
            .queryParam("lat", lat)
            .queryParam("lon", lon)
            .buildUri()

        val res = runCatching {
            webClient.get()
                .uri(uri)
                .retrieve()
                .awaitBody<GeocodeDTO>()
        }

        return res.getOrNull() ?: GeocodeDTO()
    }

    /**
     * Reverse geocode a location to an address.
     */
    suspend fun reverseGeocode(location: LocationDTO): GeocodeDTO {
        val uri = uriBuilder
            .path("/reverse")
            .queryParam("lat", location.latitude)
            .queryParam("lon", location.longitude)
            .buildUri()

        val res = runCatching {
            webClient.get()
                .uri(uri)
                .retrieve()
                .awaitBody<GeocodeDTO>()
        }

        return res.getOrNull() ?: GeocodeDTO()
    }

}