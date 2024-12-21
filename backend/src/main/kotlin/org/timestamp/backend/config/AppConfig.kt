package org.timestamp.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.json.KotlinSerializationJsonDecoder
import org.springframework.http.codec.json.KotlinSerializationJsonEncoder
import org.springframework.web.reactive.function.client.WebClient
import kotlinx.serialization.json.Json

@Configuration
class AppConfig {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Bean
    fun webClient(): WebClient {
        return WebClient.builder()
            .codecs {
                it.defaultCodecs().kotlinSerializationJsonDecoder(KotlinSerializationJsonDecoder(json))
                it.defaultCodecs().kotlinSerializationJsonEncoder(KotlinSerializationJsonEncoder(json))
            }
            .build()
    }
}