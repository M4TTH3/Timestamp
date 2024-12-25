package org.timestamp.backend.config

import com.graphhopper.GraphHopper
import com.graphhopper.GraphHopperConfig
import com.graphhopper.config.CHProfile
import com.graphhopper.config.Profile
import com.graphhopper.routing.ev.EncodedValue
import com.graphhopper.storage.GHDirectory
import com.graphhopper.storage.MMapDataAccess
import com.graphhopper.util.GHUtility
import com.graphhopper.util.Parameters
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.exists
import kotlin.system.exitProcess

data class RouteResponse(
    val time: Long,
    val distance: Double
)

@Configuration
class GraphHopperConfig(
    @Value("\${osm.file}")
    private val osmFile: String
) {

    private val path = Path("osm/$osmFile")
    private val cachePath = Path("graph-cache")

    /**
     * CommandLineRunner bean that exits the application
     * after the GraphHopper import is done
     */
    @Bean
    @org.springframework.context.annotation.Profile("graphhopper-import")
    fun graphHopperPreprocessor(): CommandLineRunner {
        return CommandLineRunner {
            println("Import complete, exiting...")
            exitProcess(0)
        }
    }

    /**
     * Dummy variable to prevent dependency injection error
     * when running the import profile
     * @see graphHopperPreprocessor
     */
    @Bean
    @org.springframework.context.annotation.Profile("graphhopper-import")
    fun graphHopperPreprocessorDummy(): GraphHopper {
        println("Running GraphHopper import...")
        return createGraphHopperInstance(path, cachePath, shouldImport = true)
    }

    @Bean
    @org.springframework.context.annotation.Profile("!graphhopper-import")
    fun graphHopperInstance(): GraphHopper {
        val hopper = createGraphHopperInstance(path, cachePath)
        if (!hopper.fullyLoaded) throw IllegalStateException("GraphHopper instance not fully loaded")
        return hopper
    }

    companion object {
        private val profilesCustom = listOf(
            Profile("car").apply {
                customModel = GHUtility.loadCustomModelFromJar("car.json")
            },
            Profile("bike").apply {
                // Factor elevation into the bike profile
                customModel = GHUtility.loadCustomModelFromJar("bike.json")
            },
            Profile("foot").apply {
                // Factor elevation into the foot profile
                customModel = GHUtility.loadCustomModelFromJar("foot.json")
            }
        )

        private val chProfilesCustom = listOf(CHProfile("car"), CHProfile("bike"), CHProfile("foot"))

        // Encoded values for the custom models
        private val encodedValues = listOf(
            "car_access", "car_average_speed", "bike_priority", "mtb_rating", "hike_rating",
            "bike_access", "roundabout", "bike_average_speed", "foot_access", "foot_priority",
            "foot_average_speed"
        )

        private fun createGraphHopperInstance(
            path: Path,
            cachePath: Path,
            /**
             * If true, the OSM file will be imported and the graphhopper instance will be closed
             * after the import is done. If false, the graphhopper instance will be loaded from the cache.
             */
            shouldImport: Boolean = false
        ): GraphHopper {
            if (path.exists().not()) throw IllegalArgumentException("OSM file does not exist")

            // This is the pre-setup configurations for the GraphHopper instance.
            // Will use the fields to that of the config YML file.
            val graphHopperConfig = GraphHopperConfig().apply {
                // Set the data reader to the OSM file
                putObject("datareader.file", path.absolute().toString())
                putObject("graph.location", cachePath.absolute().toString())

                // Set the data access to MMAP
                putObject("graph.dataaccess", "MMAP")

                setProfiles(profilesCustom)
                setCHProfiles(chProfilesCustom) // Enable CH "speed mode" - Alternatively use LM

                putObject("graph.encoded_values", encodedValues.joinToString(","))
                putObject("import.osm.ignored_highways", "")
            }

            val hopper = GraphHopper().init(graphHopperConfig)

            // Load graphhopper cache if it exists, otherwise create it
            if (shouldImport) hopper.importOrLoad() else hopper.load()
            return hopper
        }
    }
}

