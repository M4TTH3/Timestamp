package org.timestamp.shared.util

interface TimestampLogger {
    fun d(tag: String, message: String)
    fun e(tag: String, message: String)
    fun e(tag: String, message: String, exception: Throwable)
    fun i(tag: String, message: String)
    fun w(tag: String, message: String)
    fun v(tag: String, message: String)
}

/**
 * We want it to be a singleton logger implemented by
 * each front-end
 */
expect object AppLogger: TimestampLogger