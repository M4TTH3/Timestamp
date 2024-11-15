package org.timestamp.mobile

/**
 * https://stackoverflow.com/questions/72563673/google-authentication-with-firebase-and-jetpack-compose
 */

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import androidx.activity.compose.setContent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.timestamp.backend.model.Event
import org.timestamp.backend.model.EventDTO
import org.timestamp.backend.model.toDTO
import androidx.credentials.CredentialManager as CredentialManager
import org.timestamp.mobile.ui.theme.TimestampTheme
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

enum class Screen {
    Login,
    Home,
    Events,
    Calendar,
    Settings
}

val ktorClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

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
        enableEdgeToEdge()

        val activityContext = this as Context
        val credentialManager = CredentialManager.create(this)
        auth = Firebase.auth

        // Creating a google sign in request
        val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption
            .Builder(getString(R.string.web_client_id))
            .build()

        // Create a request to get credentials
        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        setContent {
            TimestampTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()
                val startDestination = if (auth.currentUser == null) Screen.Login.name else Screen.Home.name

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable(Screen.Login.name) {
                        LoginScreen(
                            onSignInClick = {
                                scope.launch {
                                    try {
                                        val result = credentialManager.getCredential(
                                            request = request,
                                            context = activityContext
                                        )
                                        handleSignIn(result, navController)
                                    } catch (e: GetCredentialException) {
                                        Log.e("Sign In", e.toString())
                                    }
                                }
                            }
                        )
                    }
                    composable(Screen.Home.name) {
                        HomeScreen(
                            currentUser = auth.currentUser,
                            onSignOutClick = {
                                auth.signOut()
                                scope.launch {
                                    credentialManager.clearCredentialState(
                                        ClearCredentialStateRequest()
                                    )
                                }
                                navController.popBackStack()
                                navController.navigate(Screen.Login.name)
                            },
                            onContinueClick = {
                                navController.navigate(Screen.Events.name)
                            }
                        )
                    }
                    composable(Screen.Events.name) {
                        EventsScreen()
                        NavBar(navController = navController, currentScreen = "Events")
                    }
                    composable(Screen.Calendar.name) {
                        CalendarScreen()
                        NavBar(navController = navController, currentScreen = "Calendar")
                    }
                    composable(Screen.Settings.name) {
                        SettingsScreen(
                            currentUser = auth.currentUser,
                            onSignOutClick = {
                                auth.signOut()
                                scope.launch {
                                    credentialManager.clearCredentialState(
                                        ClearCredentialStateRequest()
                                    )
                                }
                                navController.popBackStack()
                                navController.navigate(Screen.Login.name)
                            }
                        )
                        NavBar(navController = navController, currentScreen = "Settings")
                    }
                }
            }
        }
    }

    @Composable
    fun NavBar(navController: NavController, currentScreen : String) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFF6F61))
                    .padding(8.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = {
                        navController.navigate(Screen.Events.name)
                    },
                        modifier = Modifier
                            .size(48.dp)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.home), contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (currentScreen == "Events" ) Color.Black else Color.Unspecified)
                    }
                    IconButton(onClick = {
                        navController.navigate(Screen.Calendar.name)
                    },
                        modifier = Modifier
                            .size(48.dp)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.calendar), contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (currentScreen == "Calendar" ) Color.Black else Color.Unspecified)
                    }
                    IconButton(onClick = {
                        navController.navigate(Screen.Settings.name)
                    },
                        modifier = Modifier
                            .size(48.dp)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.settings), contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (currentScreen == "Settings" ) Color.Black else Color.Unspecified)
                    }
                }
            }
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
                    pingBackend()
                    pullBackendEvents()
                    navController.navigate(Screen.Home.name)
                }
            }
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("Sign In", "Received an invalid google id token response", e)
        }
    }

    /**
     * This will ping a request to the backend to verify the token.
     * It will also create the user if required.
     */
    private fun pingBackend() {
        auth.currentUser?.getIdToken(false)?.addOnSuccessListener { result ->
            Log.i("ID TOKEN", "ID TOKEN: ${result.token}")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val endpoint = "${getString(R.string.backend_url)}/users/me"
                    Log.d("Verifying ID", endpoint)
                    val res = ktorClient.post(endpoint) {
                        headers {
                            append("Authorization", "Bearer ${result.token}")
                        }
                    }
                    if (res.status == HttpStatusCode.OK) Log.i("Verifying ID", res.bodyAsText())
                    else Log.e("Verifying ID", res.bodyAsText())
                } catch(e: Exception) {
                    Log.e("Ping Backend Error", e.toString())
                }
            }
        }
    }

    // Assuming that pingBackend was a success and the user has successfully logged in
    private fun pullBackendEvents() {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val endpoint = "${getString(R.string.backend_url)}events GET"
                val res = ktorClient.post(endpoint)

                if (res.status == HttpStatusCode.OK) {
                    val eventsJson = res.bodyAsText()
                    val events = Json.decodeFromString<MutableList<Event>>(eventsJson)

                    withContext(Dispatchers.Main) {
                        eventList.clear()
                        for (event in events) {
                            eventList.add(event.toDTO())
                        }
                        eventList.sortBy { it.arrival }
                    }
                } else {
                    Log.println(Log.ERROR, "Backend Pull Error", res.status.toString())
                }
            }
        } catch (e: Exception) {
            Log.e("Backend Pull Error", e.toString())
        }
    }

}
