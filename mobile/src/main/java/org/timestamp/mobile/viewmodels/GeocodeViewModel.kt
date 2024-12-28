package org.timestamp.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.timestamp.lib.dto.GeocodeDTO
import org.timestamp.mobile.repository.GeocodeRepository
import org.timestamp.mobile.repository.LocationRepository

class GeocodeViewModel (
    application: Application
) : AndroidViewModel(application) {
    private val repo = GeocodeRepository()
    private val locRepo = LocationRepository()
    private val loc = locRepo.get()
    val searchResults = repo.get()

    /**
     * Get the current location of the user
     * @return The current location
     */
    suspend fun currentLocPhotonDTO(): GeocodeDTO? = withContext(
        viewModelScope.coroutineContext
    ) {
        val cur = loc.value
        cur?.let {
            repo.reverseGeocode(it.latitude, it.longitude) // Return this
        }
    }

    suspend fun reverseGeocode(lat: Double, lon: Double): GeocodeDTO? = withContext(
        viewModelScope.coroutineContext
    ) {
        repo.reverseGeocode(lat, lon)
    }


    /**
     * Search for possible locations based on the given query. Use a pinpointed location
     * as bias.
     * @param query The search query
     * @param lat The latitude
     * @param lon The longitude
     * @return The search results
     */
    fun search(query: String, lat: Double, lon: Double) = viewModelScope.launch {
        repo.geocode(query, lat, lon)
    }

    /**
     * Reset the search results
     */
    fun clear() = repo.reset()
}