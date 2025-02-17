package org.timestamp.shared.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import org.timestamp.shared.dto.LocationDTO
import org.timestamp.shared.repository.LocationRepository

class LocationViewModel () : ViewModel() {
    private val repo = LocationRepository.Companion()
    val location: StateFlow<LocationDTO?> = repo.get()
}