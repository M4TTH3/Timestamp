package org.timestamp.mobile

import android.app.Application
import android.content.Intent
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.manipulation.Ordering.Context
import org.timestamp.lib.dto.LocationDTO
import org.timestamp.lib.dto.TravelMode
import org.timestamp.mobile.models.LocationViewModel

class LocationModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val mockContext: Context = mockk(relaxed = true)
    private val mockApplication: Application = mockk(relaxed = true)

    private val testLocation = LocationDTO(
        latitude = 51.5074, longitude = -0.1278,
        travelMode = TravelMode.Car
    ) // Example location

    @Test
    fun `LocationViewModel updates _location when LocationReceiver receives valid data`(): Unit {
        // Arrange
        val viewModel = LocationViewModel(mockApplication)
        val intent = mockk<Intent>(relaxed = true)

        val locationJson = Json.encodeToString(LocationDTO.serializer(), testLocation)
        every { intent.getStringExtra(INTENT_EXTRA_LOCATION) } returns locationJson

        // Act
        viewModel.receiver.onReceive(mockContext, intent)

        // Assert
        val currentLocation = viewModel.location.value
        assertEquals(testLocation, currentLocation)
    }

    @Test
    fun `LocationViewModel does not update _location when LocationReceiver receives invalid data`(): Unit {
        // Arrange
        val viewModel = LocationViewModel(mockApplication)
        val intent = mockk<Intent>(relaxed = true)

        every { intent.getStringExtra(INTENT_EXTRA_LOCATION) } returns "{invalid_json}"

        // Act
        viewModel.receiver.onReceive(mockContext, intent)

        // Assert
        val currentLocation = viewModel.location.value
        assertEquals(null, currentLocation) // Should remain null on invalid data
    }

    @Test
    fun `LocationViewModel does not update _location when intent has no extra data`(): Unit {
        // Arrange
        val viewModel = LocationViewModel(mockApplication)
        val intent = mockk<Intent>(relaxed = true)

        every { intent.getStringExtra(INTENT_EXTRA_LOCATION) } returns null

        // Act
        viewModel.receiver.onReceive(mockContext, intent)

        // Assert
        val currentLocation = viewModel.location.value
        assertEquals(null, currentLocation) // Should remain null on missing data
    }
}