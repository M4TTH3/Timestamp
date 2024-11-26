package org.timestamp.mobile

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import org.timestamp.mobile.models.AppViewModel
import org.timestamp.mobile.ui.theme.Colors
import org.timestamp.mobile.ui.theme.TimestampTheme

enum class Screen {
    Login,
    Home,
    Events,
    Calendar,
    Settings
}

class MainNavController(
    private val context: Context,
    private val appViewModel: AppViewModel
) {
    private val auth = appViewModel.auth
    private val credentialManager: CredentialManager = CredentialManager.create(context)
    private val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption
        .Builder(context.getString(R.string.web_client_id))
        .build()
    private val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(signInWithGoogleOption)
        .build()

    /**
     * Initialize auth login with firebase, prompting a user flow if required
     */
    private suspend fun handleSignIn(navController : NavController) {
        try {
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )
            val credential = result.credential

            if (credential !is CustomCredential ||
                credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                Log.e("Sign In", "Wrong token type!")
                return
            }

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
        } catch (e: GetCredentialException) {
            Log.e("Sign In", e.toString())
        }
    }

    /**
     * Main composable for navigation features. Includes credential management.
     * Sets up Credential Manager.
     */
    @Composable
    fun TimestampNavController() {
        val navController = rememberNavController()
        val scope = rememberCoroutineScope()
        val startDestination = if (auth.currentUser == null) Screen.Login.name else Screen.Home.name

        fun signIn() { scope.launch{ handleSignIn(navController) } }
        fun signOut() {
            auth.signOut()
            appViewModel.stopGetEventsPolling()
            appViewModel.stopTrackingLocation()
            scope.launch {
                credentialManager.clearCredentialState(
                    ClearCredentialStateRequest()
                )
                navController.popBackStack()
                navController.navigate(Screen.Login.name)
            }
        }

        TimestampTheme {
            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                composable(
                    Screen.Login.name,
                ) {
                    LoginScreen(
                        onSignInClick = ::signIn
                    )
                }
                composable(Screen.Home.name) {
                    HomeScreen(
                        viewModel = appViewModel,
                        currentUser = auth.currentUser,
                        onSignOutClick = ::signOut,
                        onContinueClick = {
                            navController.navigate(Screen.Events.name)
                        }
                    )
                }
                composable(Screen.Events.name) {
                    EventsScreen(viewModel = appViewModel,
                        currentUser = auth.currentUser)
                    NavBar(navController = navController, currentScreen = "Events")
                }
                composable(Screen.Calendar.name) {
                    CalendarScreen(viewModel = appViewModel)
                    NavBar(navController = navController, currentScreen = "Calendar")
                }
                composable(Screen.Settings.name) {
                    SettingsScreen(
                        currentUser = auth.currentUser,
                        onSignOutClick = ::signOut
                    )
                    NavBar(navController = navController, currentScreen = "Settings")
                }
            }
        }
    }

    /**
     * Composable for navigation bar at the bottom
     */
    @Composable
    fun NavBar(navController: NavController, currentScreen : String) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(Color(0xFFFF6F61))
                    .align(Alignment.BottomCenter)
                    .offset(y = (-2).dp)
            ) {
                Box (
                    modifier = Modifier
                        .height(3.dp)
                        .fillMaxWidth()
                        .background(Colors.Black)
                        .align(Alignment.TopCenter)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box() {}
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.Events.name)
                        },
                        modifier = Modifier
                            .padding(8.dp)
                            .size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.home),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (currentScreen == "Events") Color.Black else Color.Unspecified
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight(0.95f)
                            .background(Color.LightGray)
                            .align(Alignment.Bottom)
                    )
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.Calendar.name)
                        },
                        modifier = Modifier
                            .padding(vertical = 8.dp, horizontal = 20.dp)
                            .size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.calendar),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (currentScreen == "Calendar") Color.Black else Color.Unspecified
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight(0.95f)
                            .background(Color.LightGray)
                            .align(Alignment.Bottom)
                    )
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.Settings.name)
                        },
                        modifier = Modifier
                            .padding(8.dp)
                            .size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.settings),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (currentScreen == "Settings") Color.Black else Color.Unspecified
                        )
                    }
                    Box() {}
                }
            }
        }
    }
}