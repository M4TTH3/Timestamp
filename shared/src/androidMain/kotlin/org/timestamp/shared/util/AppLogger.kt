package org.timestamp.shared.util

import android.util.Log

actual object AppLogger: TimestampLogger {
    override fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    override fun e(tag: String, message: String) {
        Log.e(tag, message)
    }

    override fun e(tag: String, message: String, exception: Throwable) {
        Log.e(tag, message, exception)
    }

    override fun i(tag: String, message: String) {
        Log.i(tag, message)
    }

    override fun w(tag: String, message: String) {
        Log.w(tag, message)
    }

    override fun v(tag: String, message: String) {
        Log.v(tag, message)
    }
}