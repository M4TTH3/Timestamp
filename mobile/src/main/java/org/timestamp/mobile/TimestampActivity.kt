package org.timestamp.mobile

/**
 * https://stackoverflow.com/questions/72563673/google-authentication-with-firebase-and-jetpack-compose
 */

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.google.android.libraries.places.api.Places
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import org.timestamp.mobile.models.AppViewModel
import org.timestamp.mobile.utility.GoogleAPI

class TimestampActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleAPI: GoogleAPI
    private lateinit var mainNavController: MainNavController
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Setup variables
        auth = Firebase.auth
        googleAPI = GoogleAPI(this)
        mainNavController = MainNavController(this.applicationContext, appViewModel)

        // Setup Places API
        val apiKey = packageManager
            .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            .metaData
            .getString("com.google.android.geo.API_KEY")
        Places.initialize(this, apiKey!!)

        // Main content
        setContent {
            mainNavController.TimestampNavController()
        }

        // ATTENTION: This was auto-generated to handle app links.
        val appLinkIntent: Intent = intent
        val appLinkData: Uri? = appLinkIntent.data
        appViewModel.updatePendingEvent(appLinkData)
    }
}
