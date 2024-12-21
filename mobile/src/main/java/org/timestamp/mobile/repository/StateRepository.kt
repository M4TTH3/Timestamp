package org.timestamp.mobile.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.timestamp.mobile.utility.KtorClient

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

open class ViewModelRepository<T>(
    itemInit: T,
    protected val repositoryTag: String = "View Model Repository"
) : StateRepository<T> {
    private val errorRepository = ErrorRepository() // Log errors here
    protected val item: MutableStateFlow<T> = MutableStateFlow(itemInit)
    protected val ktorClient = KtorClient.backend // Singleton instance of ktorClient

    protected fun setError(e: Throwable?) = errorRepository.setError(e)
    override fun get(): StateFlow<T> = item.asStateFlow()

    /**
     * Set the current item state. Update the state in the Main thread
     * to update the UI components.
     */
    protected open suspend fun set(newItem: T) {
        item.value = newItem
    }

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
}




