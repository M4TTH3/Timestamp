package org.timestamp.mobile

/**
 * https://stackoverflow.com/questions/72563673/google-authentication-with-firebase-and-jetpack-compose
 */

import android.content.Context
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
import androidx.navigation.compose.currentBackStackEntryAsState

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import androidx.credentials.CredentialManager as CredentialManager
import org.timestamp.mobile.ui.theme.TimestampTheme

enum class Screen {
    Login,
    Home,
    Events,
    Calendar,
    Settings
}


class MainActivity : ComponentActivity() {

    private var auth: FirebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val activityContext = this as Context
        val credentialManager = CredentialManager.create(this)

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
                    //startDestination = Screen.Events.name
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
                            }
                        )
                    }
                    composable(Screen.Events.name) {
                        EventsScreen(false)
                        NavBar(navController = navController)
                    }
                    composable(Screen.Calendar.name) {
                        CalendarScreen()
                        NavBar(navController = navController)
                    }
                    composable(Screen.Settings.name) {
                        SettingsScreen()
                        NavBar(navController = navController)
                    }
                }
            }
        }
    }

    @Composable
    fun NavBar(navController: NavController) {
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
                            tint = Color.Unspecified)
                    }
                    IconButton(onClick = {
                        navController.navigate(Screen.Calendar.name)
                    },
                        modifier = Modifier
                            .size(48.dp)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.calendar), contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color.Unspecified)
                    }
                    IconButton(onClick = {
                        navController.navigate(Screen.Settings.name)
                    },
                        modifier = Modifier
                            .size(48.dp)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.settings), contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color.Unspecified)
                    }
                }
            }
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
            auth = Firebase.auth
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider
                .getCredential(googleIdTokenCredential.idToken, null)

            auth.signInWithCredential(firebaseCredential).addOnCompleteListener(
                { task ->
                    if (task.isSuccessful) {
                        Log.i("Sign In", "Successfully logged in")
                        navController.navigate(Screen.Home.name)
                    }
                }
            )
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("Sign In", "Received an invalid google id token response", e)
        }
    }
}