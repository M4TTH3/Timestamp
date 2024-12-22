package org.timestamp.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.StateFlow
import org.timestamp.lib.dto.LocationDTO
import org.timestamp.mobile.repository.LocationRepository

class LocationViewModel (
    application: Application
) : AndroidViewModel(application) {
    private val repo = LocationRepository()
    val location: StateFlow<LocationDTO?> = repo.get()
}