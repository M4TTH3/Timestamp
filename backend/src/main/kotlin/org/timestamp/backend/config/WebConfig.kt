package org.timestamp.backend.config

import kotlinx.serialization.json.Json
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.codec.json.KotlinSerializationJsonDecoder
import org.springframework.http.codec.json.KotlinSerializationJsonEncoder
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.KotlinSerializationJsonHttpMessageConverter
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.method.HandlerTypePredicate
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Use @enableWebMvc to enable MVC pages
 */
@Configuration
class WebConfig: WebMvcConfigurer {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
    }

    /**
     * Configure all Rest Endpoints to be prefixed with /api
     * This is to allow for future expansion of the API
     */
    override fun configurePathMatch(configurer: PathMatchConfigurer) {
        super.configurePathMatch(configurer)
        configurer.addPathPrefix(
            "/api",
            HandlerTypePredicate.forAnnotation(RestController::class.java)
        )
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