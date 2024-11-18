package org.timestamp.backend

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.timestamp.backend.config.FirebaseUser
import org.timestamp.backend.model.User
import org.timestamp.backend.service.UserService
import org.timestamp.backend.viewModels.LocationVm

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {

    /**
     * This endpoint is used to verify the ID token from the client and
     * create the user if they do not exist.
     */
    @RequestMapping("/me", method = [RequestMethod.GET, RequestMethod.POST])
    fun getUserAndCreateIfNotExist(
        @AuthenticationPrincipal firebaseUser: FirebaseUser
    ): ResponseEntity<User> {
        val user = userService.createUser(firebaseUser)
        return ResponseEntity.ok(user)
    }

    @PatchMapping("/me/location")
    fun updateLocation(
        @AuthenticationPrincipal firebaseUser: FirebaseUser,
        @RequestBody location: LocationVm
    ): ResponseEntity<User> {
        val user = userService.updateLocation(firebaseUser, location.latitude, location.longitude)
        return ResponseEntity.ok(user)
    }
}