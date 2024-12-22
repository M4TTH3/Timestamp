package org.timestamp.mobile.repository

import android.util.Log
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.contentLength
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.timestamp.mobile.utility.KtorClient
import org.timestamp.mobile.utility.KtorClient.success

/**
 * Interface for repositories that hold state.
 * T is a data class that holds the states
 */
interface StateRepository<T> {
    fun get(): StateFlow<T>

    companion object {
        /**
         * Singleton instances of repositories. Globally map one instance per class.
         * This is to ensure that the same instance is used across the app.
         */
        private val instances = mutableMapOf<Class<*>, Any>()

        private inline fun <reified R> get(initialize: () -> R): R {
            return instances.getOrPut(R::class.java) { initialize() as Any } as R
        }

        internal inline operator fun <reified T : StateRepository<*>> invoke(
            initialize: () -> T
        ): T = get(initialize)
    }
}

open class BaseRepository<T>(
    itemInit: T,
    protected val repositoryTag: String = "View Model Repository"
) : StateRepository<T> {
    private val errorRepository = ErrorRepository() // Log errors here
    private val stateFlow = MutableStateFlow(itemInit) // State flow for the item

    /**
     * An accessor variable for stateFlow value
     */
    protected var state: T
        get() = stateFlow.value
        set(value) {
            stateFlow.value = value
        }

    protected val ktorClient = KtorClient.backend // Singleton instance of ktorClient

    /**
     * A local error state corresponding to the repository.
     */
    private val _localError = MutableStateFlow<Throwable?>(null)
    val localError: StateFlow<Throwable?> = _localError.asStateFlow()

    protected fun setError(e: Throwable?) {
        errorRepository.setError(e)
        _localError.value = e
    }

    override fun get(): StateFlow<T> = stateFlow.asStateFlow()

    /**
     * Handler for a request, performs try catch and updates
     * states if required.
     */
    protected suspend fun <T> handler(
        tag: String = "View Model Request",
        onError: suspend () -> Unit = {},
        action: suspend () -> T?
    ): T? {
        suspend fun onErrorHandler(e: Throwable?) {
            setError(e)
            onError()
        }

        return KtorClient.handler(tag, ::onErrorHandler, action)
    }

    protected suspend inline fun <reified T> HttpResponse.bodyOrNull(
        tag: String = repositoryTag
    ): T? = if (success(tag) && this.contentLength() != 0L)
        this.body<T>().also { Log.d(tag, it.toString()) } else null
}




