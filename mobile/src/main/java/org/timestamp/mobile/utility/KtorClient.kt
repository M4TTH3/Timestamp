package org.timestamp.mobile.utility

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.plugin
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.timestamp.mobile.R

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
    fun init(context: Context) {
        backendBase = context.getString(R.string.backend_url)
        backend = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(json)
            }

            defaultRequest {
                url {
                    // Set the default backend url protocol and host
                    takeFrom(backendBase)
                }
            }
        }.apply {
            plugin(HttpSend).intercept { req ->
                val result = getIdTokenResult()
                val token = result?.token ?: throw Exception("No token found")
                req.headers.append("Authorization", "Bearer $token")
                execute(req)
            }
        }
    }

    /**
     * Handler for a request, performs try catch and updates
     * states if required. Run the action in the IO context.
     */
    suspend fun <T> handler(
        tag: String = "Backend Request",
        onError: suspend (e: Throwable?) -> Unit = {},
        action: suspend () -> T?
    ): T? {
        val t = runCatching {
            withContext(Dispatchers.IO) {
                action()
            }
        }.onFailure {
            onError(it)
            Log.e(tag, it.toString())
        }

        return t.getOrNull()
    }

    /**
     * Check if we get a successful response. If not, then return false and
     * log the response values.
     */
    fun HttpResponse.success(tag: String = "Timestamp Request"): Boolean {
        if (this.status.isSuccess()) return true

        Log.e(tag, "${this.status.value} - ${this.status.description}: $this")
        return false
    }

    /**
     * Get the body of the response if the request was successful.
     * If not, then return null. Log the error as well.
     */
    suspend inline fun <reified T> HttpResponse.bodyOrNull(
        tag: String = "Timestamp Request"
    ): T? = if (success(tag)) this.body<T>().also { Log.d(tag, it.toString()) } else null
}

suspend fun getIdTokenResult(forceRefresh: Boolean = false): GetTokenResult? =
    FirebaseAuth.getInstance().currentUser?.getIdToken(forceRefresh)?.await()