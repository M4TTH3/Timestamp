package org.timestamp.shared.repository

import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.timestamp.shared.dto.LocationDTO
import org.timestamp.shared.util.KtorClient.success

class LocationRepository private constructor(): BaseRepository<LocationDTO?>(
    null, LocationRepository::class
) {

    suspend fun updateLocation(location: LocationDTO) {
        val tag = "UpdateLocation"
        handler(tag) {
            val endpoint = "users/me/location"
            val success = ktorClient.patch(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(location)
            }.success(tag)

            if (success) state = location
        }
    }


    companion object {
        operator fun invoke() = StateRepository { LocationRepository() }
    }
}