package org.timestamp.backend.service

import com.graphhopper.GraphHopper
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.timestamp.shared.dto.RouteInfoDTO
import org.timestamp.shared.dto.TravelMode

/**
 * Currently restrict access to this endpoint to only authenticated users.
 * This is a placeholder for future functionality.
 */

@RestController
@RequestMapping("/route")
class RouteController(private val graphHopper: GraphHopper) {

    @GetMapping
    fun getRoute(
        @RequestParam("from-lat") fromLat: Double,
        @RequestParam("from-lon") fromLon: Double,
        @RequestParam("to-lat") toLat: Double,
        @RequestParam("to-lon") toLon: Double,
        @RequestParam("profile") profile: TravelMode
    ): ResponseEntity<RouteInfoDTO> {
        val res = graphHopper.route(fromLat, fromLon, toLat, toLon, profile)
        val routeInfo = RouteInfoDTO(
            timeEst = res?.time,
            distance = res?.distance,
            travelMode = profile
        )

        return ResponseEntity.ok(routeInfo)
    }
}