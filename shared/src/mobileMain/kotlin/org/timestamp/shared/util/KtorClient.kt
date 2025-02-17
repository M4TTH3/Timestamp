package org.timestamp.shared.util

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json

/**
 * Inject the Authorization header into the ktorClient.
 * Required for backend authorization & access.
 */
object KtorClient {

    val json = Json {
        prettyPrint = true
        isLenient = true // Optimize payload
        ignoreUnknownKeys = true
    }

    lateinit var backendBase: String
        private set

    lateinit var backend: HttpClient
        private set

    /**
     * Initialize the ktor client with the backend url and the
     * required headers for authorization.
     */
    fun init(
        backendBase: String,
        getToken: suspend () -> String?
    ) {
        this.backendBase = backendBase
        backend = HttpClient(CIO) {
            install(ContentNegotiation.Plugin) {
                json(json)
            }

            defaultRequest {
                url {
                    takeFrom(backendBase)
                    path("api/")
                }
            }
        }.apply {
            plugin(HttpSend.Plugin).intercept { req ->
                val token = getToken() ?: throw Exception("No token found")
                req.headers.append("Authorization", "Bearer $token")
                execute(req)
            }
        }
    }

    private val lastActiveJob = mutableMapOf<String, Job>()

    /**
     * Handler for a request, performs try catch and updates
     * states if required. Run the action in the IO context.
     */
    suspend fun <T> handler(
        tag: String = "Backend Request",
        cancelOnNewRequest: Boolean = false,
        onError: suspend (e: Throwable?) -> Unit = {},
        action: suspend () -> T?
    ): T? {

        if (cancelOnNewRequest) lastActiveJob[tag]?.cancel()

        val job = CoroutineScope(Dispatchers.IO).async {
            val tmp = runCatching {
                withContext(Dispatchers.IO) {
                    action()
                }
            }.onFailure {
                if (it is CancellationException) {
                    AppLogger.d(tag, "Request Cancelled for $tag")
                } else {
                    onError(it)
                    AppLogger.e(tag, it.toString())
                }
            }

            tmp.getOrNull()
        }

        lastActiveJob[tag] = job
        return job.await()
    }

    /**
     * Check if we get a successful response. If not, then return false and
     * log the response values.
     */
    fun HttpResponse.success(tag: String = "Timestamp Request"): Boolean {
        if (this.status.isSuccess()) return true

        AppLogger.e(tag, "${this.status.value} - ${this.status.description}: $this")
        return false
    }

    /**
     * Get the body of the response if the request was successful.
     * If not, then return null. Log the error as well.
     */
    suspend inline fun <reified T> HttpResponse.bodyOrNull(
        tag: String = "Timestamp Request"
    ): T? = if (success(tag)) this.body<T>().also { AppLogger.d(tag, it.toString()) } else null
}