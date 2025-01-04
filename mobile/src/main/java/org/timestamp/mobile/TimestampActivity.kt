package org.timestamp.mobile

/**
 * https://stackoverflow.com/questions/72563673/google-authentication-with-firebase-and-jetpack-compose
 */

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import org.timestamp.mobile.utility.GoogleAPI
import org.timestamp.mobile.utility.KtorClient
import org.timestamp.mobile.viewmodels.EventViewModel
import org.timestamp.mobile.viewmodels.LocationViewModel

class TimestampActivity : ComponentActivity() {

    private lateinit var googleAPI: GoogleAPI
    private lateinit var mainNavController: MainNavController
    private val eventViewModel: EventViewModel by viewModels()
    private val locationViewModel: LocationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        KtorClient.init(this) // Initialize the Ktor client

        // Setup variables
        googleAPI = GoogleAPI(this)
        mainNavController = MainNavController(this)

        // ATTENTION: This was auto-generated to handle app links.
        val appLinkIntent: Intent = intent
        val appLinkData: Uri? = appLinkIntent.data
        eventViewModel.setPendingEventLink(appLinkData)

        // Main content
        setContent {
            mainNavController.TimestampNavController()
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

/**
 * In any activity nested in this, i.e. a view, use this function
 * to get the main activity instance.
 */

internal fun Context.getActivity(): ComponentActivity {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) {
            return context
        }
        context = context.baseContext
    }

    throw IllegalStateException("Activity not found")
}
