package org.timestamp.shared.repository

import io.ktor.client.request.get
import org.timestamp.shared.dto.NotificationDTO

class NotificationRepository: BaseRepository<NotificationDTO?>(
    null,
    NotificationRepository::class
) {

    suspend fun getNotifications(): NotificationDTO? {
        val tag = "Get Notifications"
        return handler(tag) {
            val endpoint = "users/me/notifications"
            val body: NotificationDTO? = ktorClient.get(endpoint).bodyOrNull()
            body?.also {
                if (somewhatEqual(it)) state = it
            }
        }
    }

    /**
     * Update this function for a relaxed comparison between new and old notifications
     * @param n The new notification
     */
    private fun somewhatEqual(n: NotificationDTO): Boolean {
        if (state == null) return true
        return state == n
    }

    companion object {
        operator fun invoke() = StateRepository { NotificationRepository() }
    }
}