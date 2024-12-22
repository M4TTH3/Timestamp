package org.timestamp.mobile

/**
 * https://stackoverflow.com/questions/72563673/google-authentication-with-firebase-and-jetpack-compose
 */

import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.google.android.libraries.places.api.Places
import org.timestamp.mobile.repository.LocationRepository
import org.timestamp.mobile.repository.NotificationRepository
import org.timestamp.mobile.viewmodels.EventViewModel
import org.timestamp.mobile.viewmodels.LocationViewModel
import org.timestamp.mobile.utility.GoogleAPI
import org.timestamp.mobile.utility.KtorClient

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

        // Setup Places API
        val apiKey = packageManager
            .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            .metaData
            .getString("com.google.android.geo.API_KEY")
        Places.initialize(this, apiKey!!)

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
