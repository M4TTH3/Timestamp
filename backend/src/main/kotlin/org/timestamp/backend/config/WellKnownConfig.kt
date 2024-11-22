package org.timestamp.backend.config

import kotlinx.serialization.json.Json
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.timestamp.backend.viewModels.AssetLink

@Configuration
class WellKnownConfig {

    @Bean
    fun getAssetLinks(): List<AssetLink> {
        val resource = ClassPathResource("assetlinks.json")
        val file = resource.inputStream.bufferedReader().use { it.readText() }
        val json = Json { prettyPrint = true }
        val assetLinks = json.decodeFromString<List<AssetLink>>(file)

        return assetLinks
    }

}