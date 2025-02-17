package org.timestamp.shared.util

import org.slf4j.LoggerFactory

actual object AppLogger: TimestampLogger {
    private val logger = LoggerFactory.getLogger(TimestampLogger::class.java)

    override fun d(tag: String, message: String) = logger.debug("$tag: $message")

    override fun e(tag: String, message: String) = logger.error("$tag: $message")

    override fun e(tag: String, message: String, exception: Throwable) = logger.error("$tag: $message", exception)

    override fun i(tag: String, message: String) = logger.debug("Info - $tag: $message")

    override fun w(tag: String, message: String) = logger.warn("$tag: $message")

    override fun v(tag: String, message: String) = logger.trace("$tag: $message")
}