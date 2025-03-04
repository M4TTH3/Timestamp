package org.timestamp.backend.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.timestamp.backend.util.SseHub
import org.timestamp.shared.dto.EventDTO
import org.timestamp.shared.dto.EventStreamType

@Configuration
class SseEventHubConfig {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Bean
    fun sseEventHub() = SseHub<EventDTO, EventStreamType>(scope)
}