package org.timestamp.shared.repository

import io.ktor.client.request.post
import org.timestamp.shared.dto.UserDTO

class UserRepository: BaseRepository<UserDTO?>(
    null,
    UserRepository::class
) {

    /**
     * Post the current user to the backend, verifies the user and sets the user state
     * to the current user. Initialize the user in the backend if it doesn't exist.
     */
    suspend fun postUser()  {
        val tag = "Get User"
        handler(tag) {
            val endpoint = "users/me"
            val body: UserDTO? = ktorClient.post(endpoint).bodyOrNull()
            body?.let { state = it }
        }
    }

    companion object {
        operator fun invoke() = StateRepository { UserRepository() }
    }
}