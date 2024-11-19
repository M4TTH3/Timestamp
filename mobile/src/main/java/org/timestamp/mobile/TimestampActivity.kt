package org.timestamp.mobile

/**
 * https://stackoverflow.com/questions/72563673/google-authentication-with-firebase-and-jetpack-compose
 */

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import androidx.activity.compose.setContent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import androidx.navigation.NavController
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.android.libraries.places.api.Places
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.timestamp.mobile.models.AppViewModel
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TimestampActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    private val appViewModel: AppViewModel by viewModels()

    // Scopes we want access to (Google API endpoints we want permission for)
    private val googleScopes: List<Scope> = listOf(
        Scope("https://www.googleapis.com/auth/calendar")
    )

    // Used to pause state of google API authorization if we rely on user input
    private var googleContinuation: (Continuation<AuthorizationResult>)? = null

    // Acquire a new access token, and run any function necessary after
    private val reqAuthIntentLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        Log.i("GoogleAPI Authorization", "Result Launcher")
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val authorizationResult = Identity
                .getAuthorizationClient(this)
                .getAuthorizationResultFromIntent(data)

            Log.i("GoogleAPI Authorization", authorizationResult.accessToken!!)
            googleContinuation?.resume(authorizationResult)
        } else {
            googleContinuation?.resumeWithException(Exception("Authorization canceled"))
        }

        googleContinuation = null // Safety precaution
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val apiKey = packageManager
            .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            .metaData
            .getString("com.google.android.geo.API_KEY")
        Places.initialize(this, apiKey!!)
        enableEdgeToEdge()

        auth = Firebase.auth

        setContent {
            MainNavController(
                appViewModel = appViewModel,
                handleSignIn = ::handleSignIn,
            )
        }
    }

    /**
     * Return an AuthorizationResult after waiting for the reqAuthIntentLauncher to finish.
     * That is, the user flow to admit permissions.
     */
    private suspend fun waitForAuthIntent(
        intentSenderRequest: IntentSenderRequest
    ): AuthorizationResult = suspendCancellableCoroutine { continuation ->
        // Wait for the user flow to complete and then exec
        googleContinuation = continuation
        try {
            reqAuthIntentLauncher.launch(intentSenderRequest) // pause here
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }

        // Cleanup in case
        continuation.invokeOnCancellation {
            googleContinuation = null
        }
    }

    /**
     * Authorize Google API scopes, and sets up a user-flow for permissions if required.
     * Returns an AuthorizationResult which contains the access token.
     */
    private suspend fun authorizeGoogleAPI(
        scopes: List<Scope>
    ): AuthorizationResult? {
        try {
            // Attempt to obtain authorization
            val authorizationReq = AuthorizationRequest.Builder().setRequestedScopes(scopes).build()
            var result = Identity.getAuthorizationClient(this).authorize(authorizationReq).await()
            val pendingIntent = result.pendingIntent // Verify intent

            if (result.hasResolution() && pendingIntent != null) { // Need user-flow permission
                try {
                    val intent = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                    result = waitForAuthIntent(intent)
                } catch (e: IntentSender.SendIntentException) {
                    Log.e("GoogleAPI Authorization",
                        "Couldn't start Authorization UI: ${e.localizedMessage}"
                    )
                }
            } else {
                Log.i("GoogleAPI Authorization", "Already Granted")
            }

            return result
        } catch (e: Exception) {
            Log.e("GoogleAPI Authorization", "Failed", e)
        }

        return null
    }

    /**
     * Function to get the access token from google for google calendar etc.
     */
    suspend fun getGoogleAccessToken() = authorizeGoogleAPI(googleScopes)

    /**
     * Test function to check access tokens
     */
    private suspend fun fetchCalendarEvents(accessToken: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://www.googleapis.com/calendar/v3/calendars/primary/events")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            val responseCode = connection.responseCode
            return@withContext if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { block ->
                    val text = block.readText()
                    Log.i("Calendar Text", text)
                    text
                }
            } else {
                Log.e("GoogleCalendar", "Error fetching events: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e("GoogleCalendar", "Exception: ${e.message}")
            null
        }
    }

    /**
     * Initial user sign in flow. Will also allow for
     */
    private fun handleSignIn(result: GetCredentialResponse, navController : NavController) {
        val credential = result.credential

        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            Log.e("Sign In", "Wrong token type!")
            return
        }

        try {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider
                .getCredential(googleIdTokenCredential.idToken, null)

            auth.signInWithCredential(firebaseCredential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i("Sign In", "Successfully logged in")
                    appViewModel.pingBackend()
                    navController.navigate(Screen.Home.name)
                }
            }
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("Sign In", "Received an invalid google id token response", e)
        }
    }
}
