package org.timestamp.mobile

import android.net.Uri
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.google.firebase.auth.FirebaseUser
import org.junit.Before
import org.junit.Rule
import org.timestamp.lib.dto.EventDTO
import org.timestamp.lib.dto.LocationDTO
import org.timestamp.lib.dto.TravelMode
import org.timestamp.mobile.models.EventViewModel
import org.timestamp.mobile.models.LocationViewModel
import java.time.OffsetDateTime

class MapViewTest {
    @JvmField @Rule
    val composeTestRule = createAndroidComposeRule<TimestampActivity>()

    private lateinit var mockEventViewModel: EventViewModel
    private lateinit var mockLocationViewModel: LocationViewModel
    private lateinit var mockFirebaseUser: FirebaseUser

    @Before
    fun setup() {
        mockEventViewModel = mock(EventViewModel::class::java)
        mockLocationViewModel = mockk(relaxed = true) // Mock LocationViewModel
        mockFirebaseUser = mockk(relaxed = true) // Mock FirebaseUser

        // Mock the data emitted by ViewModels
        val events = listOf(
            EventDTO(name = "Event1", latitude = 37.7749, longitude = -122.4194, address = "San Francisco", arrival = OffsetDateTime.now().plusDays(1), users = listOf()),
            EventDTO(name = "Event2", latitude = 34.0522, longitude = -118.2437, address = "Los Angeles", arrival = OffsetDateTime.now().plusDays(2), users = listOf())
        )

        val mockLocation = LocationDTO(latitude = 37.7749, longitude = -122.4194, travelMode = TravelMode.Car)

        every { mockEventViewModel.events } returns flowOf(events)
        every { mockLocationViewModel.location } returns flowOf(mockLocation)

        // Mock Firebase user details
        every { mockFirebaseUser.photoUrl } returns Uri.parse("https://example.com/pfp.png")
    }
}