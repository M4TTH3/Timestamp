package org.timestamp.mobile

import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import kotlinx.coroutines.launch
import org.timestamp.mobile.models.AppViewModel
import org.timestamp.mobile.ui.theme.TimestampTheme

enum class Screen {
    Login,
    Home,
    Events,
    Calendar,
    Settings
}

/**
 * Main composable for navigation features. Includes credential management.
 * Sets up Credential Manager.
 */
@Composable
fun MainNavController(
    appViewModel: AppViewModel = viewModel(),
    handleSignIn: (GetCredentialResponse, NavHostController) -> Unit
) {
    val activityContext = LocalContext.current
    val auth = appViewModel.auth

    val credentialManager = CredentialManager.create(activityContext)
    // Creating a google sign in request
    val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption
        .Builder(activityContext.getString(R.string.web_client_id))
        .build()

    // Create a request to get credentials
    val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(signInWithGoogleOption)
        .build()

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
                    viewModel = appViewModel,
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
                EventsScreen(viewModel = appViewModel)
                NavBar(navController = navController, currentScreen = "Events")
            }
            composable(Screen.Calendar.name) {
                CalendarScreen(viewModel = appViewModel)
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
                .background(Color(0xFFFF6F61))
                .padding(8.dp)
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = {
                        navController.navigate(Screen.Events.name)
                    },
                    modifier = Modifier
                        .size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.home), contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = if (currentScreen == "Events") Color.Black else Color.Unspecified
                    )
                }
                IconButton(
                    onClick = {
                        navController.navigate(Screen.Calendar.name)
                    },
                    modifier = Modifier
                        .size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.calendar),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = if (currentScreen == "Calendar") Color.Black else Color.Unspecified
                    )
                }
                IconButton(
                    onClick = {
                        navController.navigate(Screen.Settings.name)
                    },
                    modifier = Modifier
                        .size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.settings),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = if (currentScreen == "Settings") Color.Black else Color.Unspecified
                    )
                }
            }
        }
    }
}