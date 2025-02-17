package org.timestamp.shared.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.timestamp.shared.dto.GeocodeDTO
import org.timestamp.shared.repository.GeocodeRepository
import org.timestamp.shared.repository.LocationRepository

class GeocodeViewModel () : ViewModel() {
    private val repo = GeocodeRepository.Companion()
    private val locRepo = LocationRepository.Companion()
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