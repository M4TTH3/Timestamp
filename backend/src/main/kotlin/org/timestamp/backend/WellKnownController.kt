package org.timestamp.backend

import org.apache.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.timestamp.lib.dto.AssetLink

@RestController
@RequestMapping("/.well-known")
class WellKnownController(
    val assetLinks: List<AssetLink>
) {

    @GetMapping("/assetlinks.json")
    fun getAssetLinks(): ResponseEntity<List<AssetLink>> {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(assetLinks)
    }

}