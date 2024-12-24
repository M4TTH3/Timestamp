package org.timestamp.backend

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.timestamp.backend.config.FirebaseUser
import org.timestamp.backend.service.GeocoderService
import org.timestamp.backend.service.UserService
import org.timestamp.lib.dto.LocationDTO
import org.timestamp.lib.dto.GeocodeDTO
import org.timestamp.lib.dto.TravelMode

@RestController
@RequestMapping("/geocode")
class GeocodeController(
    private val gs: GeocoderService
) {
    @GetMapping
    suspend fun geocode(
        @AuthenticationPrincipal user: FirebaseUser,
        @RequestParam query: String,
        @RequestParam lat: Double,
        @RequestParam lon: Double
    ): ResponseEntity<GeocodeDTO> {
        val photonDTO = gs.geocode(query, lat, lon)
        return ResponseEntity.ok(photonDTO)
    }

    @GetMapping("/reverse")
    suspend fun reverseGeocode(
        @AuthenticationPrincipal user: FirebaseUser,
        @RequestParam lat: Double,
        @RequestParam lon: Double
    ): ResponseEntity<GeocodeDTO> {
        val locationDTO = LocationDTO(lat, lon, TravelMode.Car)
        val photonDTO = gs.reverseGeocode(locationDTO)
        return ResponseEntity.ok(photonDTO)
    }
}