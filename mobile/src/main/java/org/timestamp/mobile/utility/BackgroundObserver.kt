package org.timestamp.mobile.utility

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Observes the lifecycle of the application to determine whether the app
 * is in the background
 */
class BackgroundObserver: DefaultLifecycleObserver {
    var isInBackground = false
        private set

    override fun onStart(owner: LifecycleOwner) {
        isInBackground = false
    }

    override fun onStop(owner: LifecycleOwner) {
        isInBackground = true
    }
}