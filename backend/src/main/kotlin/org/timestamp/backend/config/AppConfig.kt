package org.timestamp.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.json.KotlinSerializationJsonDecoder
import org.springframework.http.codec.json.KotlinSerializationJsonEncoder
import org.springframework.web.reactive.function.client.WebClient
import kotlinx.serialization.json.Json
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.KotlinSerializationJsonHttpMessageConverter

@Configuration
class AppConfig {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
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

    /**
     * Switch to KotlinSerializationJsonHttpMessageConverter
     * over Jackson2JsonHttpMessageConverter as default HTTP message converter
     */

    @Bean
    fun kotlinxSerializationConverter(): HttpMessageConverter<*> {
        return KotlinSerializationJsonHttpMessageConverter(json)
    }
}
