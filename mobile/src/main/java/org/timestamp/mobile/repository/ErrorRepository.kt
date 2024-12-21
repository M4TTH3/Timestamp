package org.timestamp.mobile.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ErrorRepository private constructor() : StateRepository<Throwable?> {

    private val error = MutableStateFlow<Throwable?>(null)

    override fun get(): StateFlow<Throwable?> = error.asStateFlow()

    fun setError(e: Throwable?) {
        error.value = e
    }

    companion object {
        operator fun invoke() = StateRepository<ErrorRepository> { ErrorRepository() }
    }
}