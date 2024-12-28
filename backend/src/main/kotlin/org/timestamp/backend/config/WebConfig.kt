package org.timestamp.backend.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.method.HandlerTypePredicate
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
//@EnableWebMvc
class WebConfig: WebMvcConfigurer {


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
}