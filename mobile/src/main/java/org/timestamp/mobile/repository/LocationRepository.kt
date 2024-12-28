package org.timestamp.mobile.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.timestamp.lib.dto.LocationDTO
import org.timestamp.mobile.INTENT_EXTRA_LOCATION
import org.timestamp.mobile.utility.KtorClient
import org.timestamp.mobile.utility.KtorClient.success

class LocationRepository private constructor(): BaseRepository<LocationDTO?>
    (null, "LocationRepository")
{

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