package org.timestamp.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [JacksonAutoConfiguration::class])
class BackendApplication

fun main(args: Array<String>) {
	runApplication<BackendApplication>(*args)
}
