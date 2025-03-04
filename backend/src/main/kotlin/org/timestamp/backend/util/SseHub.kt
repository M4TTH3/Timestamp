package org.timestamp.backend.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.springframework.http.codec.ServerSentEvent

/**
 * A wrapper class to maintain a list of SSE flows and send them to the client.
 * Should be used in a singleton setting
 *
 * T: The type of the data to be sent
 * E: The type of the event to be sent
 */
class SseHub <T, E>(
    val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val managerMap = mutableMapOf<String, SseManager<T, E>>()
    private val mutex = Mutex()

    fun registerFlow(
        id: String,
        vararg initial: Pair<T, E>
    ): Flow<ServerSentEvent<T>> = callbackFlow {
        for ((t, e) in initial) {
            send(t.toSse(e))
        }

        register(id)
    }

    suspend fun send(userId: String, data: T, event: E) = withContext(scope.coroutineContext) {
        val manager = managerMap[userId] ?: return@withContext
        val success = manager.sendEvent(data, event)
        if (success) return@withContext

        mutex.withLock {
            manager.mutex.withLock {
                if (manager.size == 0) {
                    managerMap.remove(userId)
                }
            }
        }
    }

    private suspend fun ProducerScope<ServerSentEvent<T>>.register(id: String) {
        val callback = object : SseManagerCallback<T, E> {
            override suspend fun sendEvent(data: T, event: E): Boolean {
                val result = trySend(data.toSse(event))
                return result.isSuccess
            }
        }

        val sseManager = mutex.withLock {
            managerMap.getOrPut(id) { SseManager() } .also {
                it.mutex.lock() // Hand over hand lock
            }
        }

        sseManager.register(callback)
        sseManager.mutex.unlock() // Hand over hand lock
        awaitClose { sseManager.unregister(callback) }
    }

    private fun T.toSse(e: E): ServerSentEvent<T> {
        return ServerSentEvent
            .builder<T>()
            .data(this)
            .event(e.toString())
            .build()
    }

    private interface SseManagerCallback <T, E> {
        suspend fun sendEvent(data: T, event: E): Boolean
    }

    private class SseManager <T, E> {
        private val mCallbacks = mutableSetOf<SseManagerCallback<T, E>>()
        val size get() = mCallbacks.size
        val mutex = Mutex()

        fun register(callback: SseManagerCallback<T, E>) {
            mCallbacks.add(callback)
        }

        fun unregister(callback: SseManagerCallback<T, E>) {
            mCallbacks.remove(callback)
        }

        suspend fun sendEvent(data: T, event: E): Boolean {
            mutex.withLock {
                if (size == 0) return false

                mCallbacks.forEach {
                    if (!it.sendEvent(data, event)) return false
                }
            }

            return true
        }
    }
}