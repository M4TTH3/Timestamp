package org.timestamp.mobile.repository

import io.ktor.client.request.post
import kotlinx.coroutines.launch
import org.timestamp.lib.dto.UserDTO
import org.timestamp.mobile.utility.KtorClient.bodyOrNull
import org.timestamp.mobile.utility.KtorClient.success

class UserRepository: ViewModelRepository<UserDTO?>(
    null,
    "User Repository"
) {

    /**
     * Post the current user to the backend, verifies the user and sets the user state
     * to the current user. Initialize the user in the backend if it doesn't exist.
     */
    suspend fun postUser()  {
        val tag = "Get User"
        handler(tag) {
            val endpoint = "/users/me"
            val body: UserDTO? = ktorClient.post(endpoint).bodyOrNull()
            body?.let { set(it) }
        }
    }

    companion object {
        operator fun invoke() = StateRepository { UserRepository() }
    }
}