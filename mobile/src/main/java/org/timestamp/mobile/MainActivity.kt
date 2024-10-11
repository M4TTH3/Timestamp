package org.timestamp.mobile

/**
 * https://stackoverflow.com/questions/72563673/google-authentication-with-firebase-and-jetpack-compose
 */

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

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
    Home
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
            .Builder(getString(R.string.default_web_client_id))
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
                                        handleSignIn(result)
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
                }
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        val credential = result.credential

        if (credential !is CustomCredential ||
            credential.type !== GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
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
                    }
                }
            )
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("Sign In", "Received an invalid google id token response", e)
        }
    }
}