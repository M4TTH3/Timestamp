package org.timestamp.backend.controller

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for public endpoints
 */
@RestController
@RequestMapping(PublicController.ENDPOINT)
class PublicController {

    companion object {
        const val ENDPOINT = "/public"
    }
}