package org.timestamp.mobile

/**
 * https://stackoverflow.com/questions/72563673/google-authentication-with-firebase-and-jetpack-compose
 */

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import android.os.Bundle
import android.util.Log
import android.widget.Space
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.credentials.CredentialManager as CredentialManager
import org.timestamp.mobile.ui.theme.TimestampTheme
import java.io.IOException

import java.net.HttpURLConnection
import java.net.URL

enum class Screen {
    Login,
    Home,
    Events,
    Calendar,
    Settings
}

private const val RC_SIGN_IN = 9001
private const val RC_AUTHORIZATION = 9002

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val activityContext = this as Context
        val credentialManager = CredentialManager.create(this)
        auth = Firebase.auth

        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(activityContext)
        if (resultCode != ConnectionResult.SUCCESS) {
            Log.e("Sign In", "Google Play Services is not available")
        }

        // Creating a google sign in request
        val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption
            .Builder(getString(R.string.web_client_id))
            .build()

        // Google sign-in options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.web_client_id))
            .requestScopes(
                Scope(Scopes.PROFILE),
                Scope("https://www.googleapis.com/auth/calendar")
                // add more as needed
            )
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Create a request to get credentials
        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        // check if user is already signed in and retrieve access token
        if (auth.currentUser != null) {
            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInClient.silentSignIn().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val account = task.result
                    CoroutineScope(Dispatchers.Main).launch {
                        val accessToken = getAccessToken(account)
                        if (accessToken != null) {
                            Log.d("AccessToken", "Access Token (Already logged-in): $accessToken")
                            val calendarData = fetchCalendarEvents(accessToken)
                            if (calendarData != null) {
                                Log.d("CalendarData", "Fetched events: $calendarData")
                            } else {
                                Log.e("CalendarData", "Failed to fetch calendar events")
                            }
                        } else {
                            Log.e("AccessToken", "Failed to retrieve access token on start")
                        }
                    }
                } else {
                    Log.e("SilentSignIn", "Silent sign-in failed", task.exception)
                }
            }
        }

        setContent {
            TimestampTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()
                val startDestination = if (auth.currentUser == null) Screen.Login.name else Screen.Home.name

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                    //startDestination = Screen.Events.name
                ) {
                    composable(Screen.Login.name) {
                        LoginScreen(
                            onSignInClick = {
                                val signInIntent = googleSignInClient.signInIntent
                                startActivityForResult(signInIntent, RC_SIGN_IN)
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
                        EventsScreen(true)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithIDToken(account.idToken!!, account)
            } catch (e: ApiException) {
                Log.w("SignIn", "Sign-in failed: " + e.statusCode)
            }
        }
    }

    private fun firebaseAuthWithIDToken(idToken: String, account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    // fetch access token
                    CoroutineScope(Dispatchers.Main).launch {
                        val accessToken = getAccessToken(account)
                        if (accessToken != null) {
                            Log.d("AccessToken", "Access Token: $accessToken")

                            val calendarData = fetchCalendarEvents(accessToken)
                            if (calendarData != null) {
                                Log.d("CalendarData", "Fetched events: $calendarData")
                            } else {
                                Log.e("CalendarData", "Failed to fetch calendar events")
                            }

                        } else {
                            Log.e("AccessToken", "Failed to get access token")
                        }
                    }
                } else {
                    Log.w("SignIn", "signInWithCredential:failure", task.exception)
                }
            }
    }

    suspend fun getAccessToken(account: GoogleSignInAccount): String? = withContext(Dispatchers.IO) {
        try {
            val scope = "oauth2:https://www.googleapis.com/auth/calendar"
            val token = GoogleAuthUtil.getToken(applicationContext, account.account!!, scope)
            token
        } catch (e: UserRecoverableAuthException) {
            e.intent?.let { startActivityForResult(it, RC_AUTHORIZATION) }
            null
        } catch (e: GoogleAuthException) {
            Log.e("AccessToken", "GoogleAuthException: ${e.message}")
            null
        } catch (e: IOException) {
            Log.e("AccessToken", "IOException: ${e.message}")
            null
        }
    }

    suspend fun fetchCalendarEvents(accessToken: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://www.googleapis.com/calendar/v3/calendars/primary/events")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            val responseCode = connection.responseCode
            return@withContext if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                Log.e("GoogleCalendar", "Error fetching events: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e("GoogleCalendar", "Exception: ${e.message}")
            null
        }
    }

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

            auth.signInWithCredential(firebaseCredential).addOnCompleteListener(
                { task ->
                    if (task.isSuccessful) {
                        Log.i("Sign In", "Successfully logged in")
                        Log.i("ID Token", "ID Token: " + googleIdTokenCredential.idToken)
                        navController.navigate(Screen.Home.name)
                    }
                }
            )
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("Sign In", "Received an invalid google id token response", e)
        }
    }
}
