package org.timestamp.mobile.ui.previewdata

import org.timestamp.lib.dto.EventDTO
import org.timestamp.lib.dto.EventUserDTO
import org.timestamp.mobile.ui.screens.UW_LATITUDE
import org.timestamp.mobile.ui.screens.UW_LONGITUDE
import java.time.OffsetDateTime

val testEvent = EventDTO(
    1,
    "Test Event",
    "New Event Upcoming Tomorrow",
    address = "200 University Ave W, Waterloo, ON N2L 3G1",
    description = "University of Waterloo",
    latitude = UW_LATITUDE,
    longitude = UW_LONGITUDE,
    arrival = OffsetDateTime.now().plusHours(3),
    users = listOf(
        EventUserDTO(
            "1",
            "Test User",
            "tamp@gmail.com",
            latitude = UW_LATITUDE,
            longitude = UW_LONGITUDE - 0.0001,
            arrived = true,
            pfp = "https://lh3.googleusercontent.com/a/ACg8ocKa52qVuuqk3tKfiEqfI5Sbk12VSyKpc8XGAB5rNoOGGPBeaQ=s96-c"
        ),
        EventUserDTO(
            "2",
            "Test User 2",
            "matthewa04@gmail.com",
            latitude = UW_LATITUDE + 0.0001,
            longitude = UW_LONGITUDE,
            arrived = false,
            pfp = "https://lh3.googleusercontent.com/a/ACg8ocLJEXurWDwRTHAoHuxuAABsPT3Wne9QVSzQUxugBjkt_55RIU0=s96-c"
        ),
        EventUserDTO(
            "3",
            "Test User 3",
            "mattheung043@gmail.com",
            latitude = UW_LATITUDE + 0.0001,
            longitude = UW_LONGITUDE,
            arrived = false,
            pfp = "https://lh3.googleusercontent.com/a/ACg8ocLJEXurWDwRTHAoHuxuAABsPT3Wne9QVSzQUxugBjkt_55RIU0=s96-c"
        ),
        EventUserDTO(
            "4",
            "Test User 4",
            "matthewang044@gmail.com",
            latitude = UW_LATITUDE + 0.0001,
            longitude = UW_LONGITUDE,
            arrived = false,
            pfp = "https://lh3.googleusercontent.com/a/ACg8ocLJEXurWDwRTHAoHuxuAABsPT3Wne9QVSzQUxugBjkt_55RIU0=s96-c"
        )
    )
)

val eventsTest = listOf(
    testEvent,
    testEvent.copy(
        id = 2,
        name = "Test Event 2",
        description = "Mathematics & Computer Science",
        address = "200 University Ave W, Waterloo, ON N2L 3G1",
        latitude = UW_LATITUDE + 0.0002,
        longitude = UW_LONGITUDE + 0.0002,
        arrival = OffsetDateTime.now().plusHours(3),
        users = listOf(
            testEvent.users.first().copy(
                latitude = UW_LATITUDE + 0.0002,
                longitude = UW_LONGITUDE + 0.0002
            )
        )
    ),
    testEvent.copy(
        id = 3,
        name = "Test Event 3",
        description = "Engineering 5",
        address = "200 University Ave W, Waterloo, ON N2L 3G1",
        latitude = UW_LATITUDE - 0.0002,
        longitude = UW_LONGITUDE - 0.0002,
        arrival = OffsetDateTime.now().plusHours(3)
    ),
    testEvent.copy(
        id = 4,
        name = "Test Event 4",
        description = "Engineering 6",
        address = "200 University Ave W, Waterloo, ON N2L 3G1",
        latitude = UW_LATITUDE - 0.0002,
        longitude = UW_LONGITUDE + 0.0002,
        arrival = OffsetDateTime.now().plusHours(3)
    ),
    testEvent.copy(
        id = 5,
        name = "Test Event 5",
        description = "Engineering 7",
        address = "200 University Ave W, Waterloo, ON N2L 3G1",
        latitude = UW_LATITUDE + 0.0002,
        longitude = UW_LONGITUDE - 0.0002,
        arrival = OffsetDateTime.now().plusHours(3)
    )
)